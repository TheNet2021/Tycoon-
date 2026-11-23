package com.example.data

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

sealed interface SettlementActionOutcome {
    data class Success(
        val updatedState: TycoonSaveState,
        val bannerMessage: String
    ) : SettlementActionOutcome

    data class Failure(
        val reason: String
    ) : SettlementActionOutcome
}

enum class SupplyRestockKind(
    val id: String,
    val title: String,
    val unitsPerBatch: Int,
    val coinCostPerBatch: Long
) {
    FOOD("food", "Food Rations", 10, 24L),
    WATER("water", "Clean Water", 10, 22L),
    SUPPLIES("supplies", "Basic Supplies", 10, 30L)
}

object SettlementEngine {

    const val DAY_DURATION_MS: Long = 60_000L
    const val ARRIVAL_INTERVAL_MS: Long = 32_000L
    private const val MAX_PENDING_ARRIVALS = 5
    private const val MAX_TRANSACTIONS = 60

    private val SURVIVOR_FIRST_NAMES = listOf(
        "Elena", "Marcus", "Nadia", "Tariq", "Clara", "Samir",
        "Lina", "Owen", "Maya", "Kieran", "Zara", "Theo",
        "Hana", "Darius", "Sora", "Rafael", "Iris", "Noah"
    )

    private val SURVIVOR_LAST_NAMES = listOf(
        "Vance", "Okafor", "Lind", "Haddad", "Mercer", "Kovac",
        "Sato", "Brennan", "Solis", "Takahashi", "Kerr", "Navarro"
    )

    fun ensureInitialized(state: TycoonSaveState, nowMs: Long): TycoonSaveState {
        var s = state.settlement
        var seq = s.nextSequenceId
        var changed = false

        if (s.lastDailyTickEpochMs <= 0L) {
            s = s.copy(lastDailyTickEpochMs = nowMs)
            changed = true
        }
        if (s.lastArrivalEpochMs <= 0L) {
            s = s.copy(lastArrivalEpochMs = nowMs)
            changed = true
        }
        if (s.survivors.isEmpty() && s.pendingArrivals.isEmpty()) {
            val a1 = generateArrival(seq, nowMs, preferredProfession = SurvivorProfession.FARMER)
            seq++
            val a2 = generateArrival(seq, nowMs, preferredProfession = SurvivorProfession.MINER)
            seq++
            s = s.copy(pendingArrivals = listOf(a1, a2), nextSequenceId = seq)
            changed = true
        }
        if (s.survivorOffersForSale.isEmpty()) {
            s = s.copy(
                survivorOffersForSale = generateTradeOffers(state.copy(settlement = s), seq, nowMs)
            )
            changed = true
        }
        if (s.openContracts.isEmpty()) {
            val (contracts, nextSeq) = generateConstructionContracts(
                state.copy(settlement = s),
                seq,
                count = 4
            )
            s = s.copy(openContracts = contracts, nextSequenceId = nextSeq)
            changed = true
        }

        return if (changed) state.copy(settlement = s) else state
    }

    fun startHousingConstruction(
        state: TycoonSaveState,
        housingType: HousingType,
        nowMs: Long
    ): SettlementActionOutcome {
        if (state.coins < housingType.coinCost) {
            return SettlementActionOutcome.Failure("Need ${housingType.coinCost} coins to build ${housingType.title}.")
        }
        for ((matId, reqQty) in housingType.materialCosts) {
            val have = state.availableForTrade(matId)
            if (have < reqQty) {
                val name = TradeCatalog.find(matId)?.displayName ?: matId
                return SettlementActionOutcome.Failure("Need $reqQty $name (have $have).")
            }
        }

        var nextStock = state.warehouseStock
        var nextBag = state.inventory
        for ((matId, reqQty) in housingType.materialCosts) {
            val deducted = deductFromStockAndBag(nextStock, nextBag, matId, reqQty)
            nextStock = deducted.first
            nextBag = deducted.second
        }

        val seq = state.settlement.nextSequenceId
        val newBuilding = HousingBuilding(
            id = "house_${housingType.id}_$seq",
            type = housingType,
            startedEpochMs = nowMs,
            completesEpochMs = nowMs + housingType.buildTimeSeconds * 1000L,
            isCompleted = housingType.buildTimeSeconds <= 0L
        )
        val updatedSettlement = state.settlement.copy(
            housingUnits = state.settlement.housingUnits + newBuilding,
            nextSequenceId = seq + 1
        )
        val updated = state.copy(
            coins = state.coins - housingType.coinCost,
            warehouseStock = nextStock,
            inventory = nextBag,
            settlement = updatedSettlement
        )
        return SettlementActionOutcome.Success(
            updatedState = updated,
            bannerMessage = "Started ${housingType.title} (${housingType.buildTimeSeconds}s)"
        )
    }

    fun upgradeHousing(
        state: TycoonSaveState,
        housingId: String,
        nowMs: Long
    ): SettlementActionOutcome {
        val settlement = state.settlement
        val existing = settlement.housingUnits.firstOrNull { it.id == housingId }
            ?: return SettlementActionOutcome.Failure("Housing building not found.")
        if (!existing.isCompleted) {
            return SettlementActionOutcome.Failure("Finish construction before upgrading.")
        }
        if (!existing.canUpgrade()) {
            return SettlementActionOutcome.Failure("${existing.type.title} is already at max Tier ${HousingBuilding.MAX_HOUSING_LEVEL}.")
        }
        val coinCost = existing.nextUpgradeCoinCost()
        if (state.coins < coinCost) {
            return SettlementActionOutcome.Failure("Need $coinCost coins to upgrade ${existing.type.title}.")
        }
        val matCosts = existing.nextUpgradeMaterialCosts()
        for ((matId, reqQty) in matCosts) {
            val have = state.availableForTrade(matId)
            if (have < reqQty) {
                val name = TradeCatalog.find(matId)?.displayName ?: matId
                return SettlementActionOutcome.Failure("Need $reqQty $name (have $have).")
            }
        }

        var nextStock = state.warehouseStock
        var nextBag = state.inventory
        for ((matId, reqQty) in matCosts) {
            val res = deductFromStockAndBag(nextStock, nextBag, matId, reqQty)
            nextStock = res.first
            nextBag = res.second
        }

        val nextLevel = existing.upgradeLevel + 1
        val nextUnits = settlement.housingUnits.map { h ->
            if (h.id == housingId) h.copy(upgradeLevel = nextLevel) else h
        }
        val seq = settlement.nextSequenceId
        val tx = SettlementTransaction(
            id = "tx_$seq",
            type = SettlementTransactionType.CONTRACT_PAYOUT,
            description = "Upgraded ${existing.type.title} to Tier $nextLevel",
            coinDelta = -coinCost,
            timestampEpochMs = nowMs
        )
        val preNeeds = settlement.copy(
            housingUnits = nextUnits,
            nextSequenceId = seq + 1,
            transactionHistory = prependTx(settlement.transactionHistory, tx)
        )
        val finalSettlement = preNeeds.copy(
            survivors = refreshNeedsAndHappiness(preNeeds, paidFull = true)
        )
        val upgradedBuilding = nextUnits.first { it.id == housingId }
        return SettlementActionOutcome.Success(
            updatedState = state.copy(
                coins = state.coins - coinCost,
                warehouseStock = nextStock,
                inventory = nextBag,
                settlement = finalSettlement
            ),
            bannerMessage = "${existing.type.title} upgraded to Tier $nextLevel (${upgradedBuilding.effectiveCapacity()} beds)!"
        )
    }

    fun buildOrUpgradeFacility(
        state: TycoonSaveState,
        facilityType: SettlementFacilityType,
        nowMs: Long
    ): SettlementActionOutcome {
        val settlement = state.settlement
        val currentLevel = settlement.facilityLevel(facilityType)
        if (currentLevel >= facilityType.maxLevel) {
            return SettlementActionOutcome.Failure("${facilityType.title} is already at max Tier ${facilityType.maxLevel}.")
        }
        val targetLevel = currentLevel + 1
        val coinCost = facilityType.coinCostForLevel(targetLevel)
        if (state.coins < coinCost) {
            return SettlementActionOutcome.Failure("Need $coinCost coins to build ${facilityType.title} Tier $targetLevel.")
        }
        val matCosts = facilityType.materialCostsForLevel(targetLevel)
        for ((matId, reqQty) in matCosts) {
            val have = state.availableForTrade(matId)
            if (have < reqQty) {
                val name = TradeCatalog.find(matId)?.displayName ?: matId
                return SettlementActionOutcome.Failure("Need $reqQty $name (have $have).")
            }
        }

        var nextStock = state.warehouseStock
        var nextBag = state.inventory
        for ((matId, reqQty) in matCosts) {
            val res = deductFromStockAndBag(nextStock, nextBag, matId, reqQty)
            nextStock = res.first
            nextBag = res.second
        }

        val updatedFacilities = if (currentLevel == 0) {
            settlement.facilities + SettlementFacilityBuilding(facilityType, 1)
        } else {
            settlement.facilities.map { f ->
                if (f.type == facilityType) f.copy(level = targetLevel) else f
            }
        }
        val seq = settlement.nextSequenceId
        val actionVerb = if (currentLevel == 0) "Built" else "Upgraded"
        val tx = SettlementTransaction(
            id = "tx_$seq",
            type = SettlementTransactionType.CONTRACT_PAYOUT,
            description = "$actionVerb ${facilityType.title} Tier $targetLevel",
            coinDelta = -coinCost,
            timestampEpochMs = nowMs
        )
        val preNeeds = settlement.copy(
            facilities = updatedFacilities,
            reputation = settlement.reputation + 8 * targetLevel,
            nextSequenceId = seq + 1,
            transactionHistory = prependTx(settlement.transactionHistory, tx)
        )
        val finalSettlement = preNeeds.copy(
            survivors = refreshNeedsAndHappiness(preNeeds, paidFull = true)
        )
        return SettlementActionOutcome.Success(
            updatedState = state.copy(
                coins = state.coins - coinCost,
                xp = state.xp + 30L * targetLevel,
                warehouseStock = nextStock,
                inventory = nextBag,
                settlement = finalSettlement
            ),
            bannerMessage = "$actionVerb ${facilityType.title} Tier $targetLevel!"
        )
    }

    fun demolishHousing(
        state: TycoonSaveState,
        housingId: String
    ): SettlementActionOutcome {
        val settlement = state.settlement
        val target = settlement.housingUnits.firstOrNull { it.id == housingId }
            ?: return SettlementActionOutcome.Failure("Housing building not found.")
        val remainingUnits = settlement.housingUnits.filterNot { it.id == housingId }
        val remainingCap = remainingUnits.filter { it.isCompleted }.sumOf { it.effectiveCapacity() }
        if (remainingCap < settlement.survivors.size) {
            return SettlementActionOutcome.Failure(
                "Cannot demolish: ${settlement.survivors.size} residents need at least $remainingCap beds."
            )
        }

        val reassignedSurvivors = reassignHousingBeds(settlement.survivors, remainingUnits)
        val updatedSettlement = settlement.copy(
            housingUnits = remainingUnits,
            survivors = reassignedSurvivors
        )
        return SettlementActionOutcome.Success(
            updatedState = state.copy(settlement = updatedSettlement),
            bannerMessage = "Removed ${target.type.title}"
        )
    }

    fun admitRefugee(
        state: TycoonSaveState,
        arrivalId: String,
        nowMs: Long
    ): SettlementActionOutcome {
        val settlement = state.settlement
        val arrival = settlement.pendingArrivals.firstOrNull { it.id == arrivalId }
            ?: return SettlementActionOutcome.Failure("Refugee arrival no longer available.")

        if (settlement.survivors.size >= settlement.totalHousingCapacity) {
            return SettlementActionOutcome.Failure("No available housing space! Build or finish housing first.")
        }

        val openHouse = settlement.housingUnits.firstOrNull { h ->
            h.isCompleted && settlement.occupantsIn(h.id) < h.effectiveCapacity()
        } ?: return SettlementActionOutcome.Failure("No completed housing beds available.")

        val needs = computeNeedsForNewResident(settlement)
        val newSurvivor = SurvivorProfile(
            id = "surv_${settlement.nextSequenceId}",
            name = arrival.name,
            profession = arrival.profession,
            skillLevel = arrival.skillLevel,
            assignedJob = null,
            housingUnitId = openHouse.id,
            needs = needs,
            happiness = computeSurvivorHappiness(needs, isEmployed = false, isMatched = false, settlement.communityHappinessBonus, paidFull = true),
            arrivedEpochMs = nowMs
        )

        val updatedSettlement = settlement.copy(
            survivors = settlement.survivors + newSurvivor,
            pendingArrivals = settlement.pendingArrivals.filterNot { it.id == arrivalId },
            nextSequenceId = settlement.nextSequenceId + 1
        )
        return SettlementActionOutcome.Success(
            updatedState = state.copy(settlement = updatedSettlement),
            bannerMessage = "${newSurvivor.name} (${newSurvivor.profession.title}) joined settlement!"
        )
    }

    fun dismissRefugee(
        state: TycoonSaveState,
        arrivalId: String
    ): TycoonSaveState {
        val nextArrivals = state.settlement.pendingArrivals.filterNot { it.id == arrivalId }
        return state.copy(settlement = state.settlement.copy(pendingArrivals = nextArrivals))
    }

    fun scoutRefugeeArrival(
        state: TycoonSaveState,
        nowMs: Long
    ): SettlementActionOutcome {
        val settlement = state.settlement
        if (settlement.pendingArrivals.size >= MAX_PENDING_ARRIVALS) {
            return SettlementActionOutcome.Failure("Gate queue is full (${MAX_PENDING_ARRIVALS}/$MAX_PENDING_ARRIVALS).")
        }
        val seq = settlement.nextSequenceId
        val arrival = generateArrival(seq, nowMs)
        val updated = state.copy(
            settlement = settlement.copy(
                pendingArrivals = settlement.pendingArrivals + arrival,
                lastArrivalEpochMs = nowMs,
                nextSequenceId = seq + 1
            )
        )
        return SettlementActionOutcome.Success(
            updatedState = updated,
            bannerMessage = "${arrival.name} (${arrival.profession.title}) arrived at the gate!"
        )
    }

    fun assignSurvivorJob(
        state: TycoonSaveState,
        survivorId: String,
        role: SurvivorProfession?
    ): SettlementActionOutcome {
        val settlement = state.settlement
        val existing = settlement.survivors.firstOrNull { it.id == survivorId }
            ?: return SettlementActionOutcome.Failure("Survivor not found.")

        val updatedList = settlement.survivors.map { s ->
            if (s.id != survivorId) s
            else {
                val happy = computeSurvivorHappiness(
                    needs = s.needs,
                    isEmployed = role != null,
                    isMatched = role == s.profession,
                    communityBonus = settlement.communityHappinessBonus,
                    paidFull = true
                )
                s.copy(assignedJob = role, happiness = happy)
            }
        }
        val roleLabel = role?.title ?: "Unemployed"
        return SettlementActionOutcome.Success(
            updatedState = state.copy(settlement = settlement.copy(survivors = updatedList)),
            bannerMessage = "${existing.name} assigned as $roleLabel"
        )
    }

    fun buyCommunitySupplies(
        state: TycoonSaveState,
        kind: SupplyRestockKind,
        batches: Int,
        nowMs: Long
    ): SettlementActionOutcome {
        if (batches <= 0) return SettlementActionOutcome.Failure("Invalid quantity.")
        val settlement = state.settlement
        val totalCost = kind.coinCostPerBatch * batches
        if (state.coins < totalCost) {
            return SettlementActionOutcome.Failure("Need $totalCost coins for ${kind.title}.")
        }
        val addedUnits = kind.unitsPerBatch * batches
        val nextSettlement = when (kind) {
            SupplyRestockKind.FOOD -> {
                val next = (settlement.foodStock + addedUnits).coerceAtMost(settlement.foodCapacity)
                if (next == settlement.foodStock) return SettlementActionOutcome.Failure("Food storage is already full.")
                settlement.copy(foodStock = next)
            }
            SupplyRestockKind.WATER -> {
                val next = (settlement.waterStock + addedUnits).coerceAtMost(settlement.waterCapacity)
                if (next == settlement.waterStock) return SettlementActionOutcome.Failure("Water tanks are already full.")
                settlement.copy(waterStock = next)
            }
            SupplyRestockKind.SUPPLIES -> {
                val next = (settlement.suppliesStock + addedUnits).coerceAtMost(settlement.suppliesCapacity)
                if (next == settlement.suppliesStock) return SettlementActionOutcome.Failure("Supply depot is already full.")
                settlement.copy(suppliesStock = next)
            }
        }

        val refreshedSurvivors = refreshNeedsAndHappiness(nextSettlement, paidFull = true)
        val seq = nextSettlement.nextSequenceId
        val tx = SettlementTransaction(
            id = "tx_$seq",
            type = SettlementTransactionType.SUPPLIES_PURCHASE,
            description = "Purchased +$addedUnits ${kind.title}",
            coinDelta = -totalCost,
            timestampEpochMs = nowMs
        )
        val finalSettlement = nextSettlement.copy(
            survivors = refreshedSurvivors,
            nextSequenceId = seq + 1,
            transactionHistory = prependTx(nextSettlement.transactionHistory, tx)
        )
        return SettlementActionOutcome.Success(
            updatedState = state.copy(
                coins = state.coins - totalCost,
                settlement = finalSettlement
            ),
            bannerMessage = "Restocked +$addedUnits ${kind.title} (-$$totalCost)"
        )
    }

    fun sellGoodToSurvivors(
        state: TycoonSaveState,
        goodId: String,
        quantity: Int,
        nowMs: Long
    ): SettlementActionOutcome {
        val settlement = state.settlement
        if (settlement.survivors.isEmpty()) {
            return SettlementActionOutcome.Failure("Admit at least 1 survivor before trading with settlers.")
        }
        if (quantity <= 0) return SettlementActionOutcome.Failure("Quantity must be positive.")
        val available = state.availableForTrade(goodId)
        val qty = minOf(quantity, available)
        if (qty <= 0) return SettlementActionOutcome.Failure("No stock available to sell.")
        val good = TradeCatalog.find(goodId)
            ?: return SettlementActionOutcome.Failure("Unknown trade item.")

        val unitPrice = settlement.survivorBidPrice(good, state.marketMultiplier(goodId))
        val payout = unitPrice * qty
        val (nextStock, nextBag) = deductFromStockAndBag(state.warehouseStock, state.inventory, goodId, qty)

        // Demand saturation rises with volume sold and is buffered by larger populations
        val popBuffer = (1.0 + settlement.survivors.size * 0.12)
        val saturationGain = (qty * 0.014) / popBuffer
        val nextSaturation = settlement.demandSaturation.toMutableMap()
        nextSaturation[goodId] = ((nextSaturation[goodId] ?: 0.0) + saturationGain).coerceIn(0.0, 0.45)

        val seq = settlement.nextSequenceId
        val tx = SettlementTransaction(
            id = "tx_$seq",
            type = SettlementTransactionType.SOLD_TO_SURVIVORS,
            description = "Sold ${qty}x ${good.displayName} @ $$unitPrice",
            coinDelta = payout,
            timestampEpochMs = nowMs
        )
        val updatedSettlement = settlement.copy(
            demandSaturation = nextSaturation,
            nextSequenceId = seq + 1,
            transactionHistory = prependTx(settlement.transactionHistory, tx)
        )
        val xpGain = (qty * 2L).coerceIn(2L, 60L)
        return SettlementActionOutcome.Success(
            updatedState = state.copy(
                coins = state.coins + payout,
                lifetimeEarned = state.lifetimeEarned + payout,
                xp = state.xp + xpGain,
                warehouseStock = nextStock,
                inventory = nextBag,
                settlement = updatedSettlement
            ),
            bannerMessage = "Sold ${qty}x ${good.displayName} to settlers (+$$payout)"
        )
    }

    fun buyGoodFromSurvivors(
        state: TycoonSaveState,
        goodId: String,
        quantity: Int,
        nowMs: Long
    ): SettlementActionOutcome {
        val settlement = state.settlement
        val offer = settlement.survivorOffersForSale.firstOrNull { it.goodId == goodId }
            ?: return SettlementActionOutcome.Failure("Settlers are not offering this item right now.")
        if (quantity <= 0) return SettlementActionOutcome.Failure("Quantity must be positive.")
        val qty = minOf(quantity, offer.availableQuantity)
        if (qty <= 0) return SettlementActionOutcome.Failure("Offer sold out.")

        val good = TradeCatalog.find(goodId)
            ?: return SettlementActionOutcome.Failure("Unknown trade good.")

        val totalCost = offer.unitAskPrice * qty
        if (state.coins < totalCost) {
            return SettlementActionOutcome.Failure("Need $totalCost coins to buy ${qty}x ${good.displayName}.")
        }

        val warehouseFree = (state.warehouseStockCapacity - state.warehouseStockUsed).coerceAtLeast(0)
        val bagFree = (state.backpackMaxCapacity - state.backpackUsed).coerceAtLeast(0)
        if (warehouseFree + bagFree < qty) {
            return SettlementActionOutcome.Failure("Not enough Warehouse or Backpack space for $qty units.")
        }

        val toWarehouse = minOf(qty, warehouseFree)
        val toBag = qty - toWarehouse
        val nextStock = state.warehouseStock.toMutableMap()
        val nextBag = state.inventory.toMutableMap()
        if (toWarehouse > 0) nextStock[goodId] = (nextStock[goodId] ?: 0) + toWarehouse
        if (toBag > 0) nextBag[goodId] = (nextBag[goodId] ?: 0) + toBag

        val nextOffers = settlement.survivorOffersForSale.mapNotNull { item ->
            if (item.goodId != goodId) item
            else {
                val left = item.availableQuantity - qty
                if (left <= 0) null else item.copy(availableQuantity = left)
            }
        }

        val seq = settlement.nextSequenceId
        val tx = SettlementTransaction(
            id = "tx_$seq",
            type = SettlementTransactionType.BOUGHT_FROM_SURVIVORS,
            description = "Bought ${qty}x ${good.displayName} from ${offer.sellerName}",
            coinDelta = -totalCost,
            timestampEpochMs = nowMs
        )
        val updatedSettlement = settlement.copy(
            survivorOffersForSale = nextOffers,
            nextSequenceId = seq + 1,
            transactionHistory = prependTx(settlement.transactionHistory, tx)
        )
        return SettlementActionOutcome.Success(
            updatedState = state.copy(
                coins = state.coins - totalCost,
                warehouseStock = nextStock,
                inventory = nextBag,
                settlement = updatedSettlement
            ),
            bannerMessage = "Bought ${qty}x ${good.displayName} (-$$totalCost)"
        )
    }

    fun completeConstructionContract(
        state: TycoonSaveState,
        contractId: String,
        nowMs: Long
    ): SettlementActionOutcome {
        val settlement = state.settlement
        val contract = settlement.openContracts.firstOrNull { it.id == contractId }
            ?: return SettlementActionOutcome.Failure("Construction contract not found.")

        for ((matId, reqQty) in contract.materialRequirements) {
            val have = state.availableForTrade(matId)
            if (have < reqQty) {
                val name = TradeCatalog.find(matId)?.displayName ?: matId
                return SettlementActionOutcome.Failure("Need $reqQty $name (have $have).")
            }
        }

        var nextStock = state.warehouseStock
        var nextBag = state.inventory
        for ((matId, reqQty) in contract.materialRequirements) {
            val res = deductFromStockAndBag(nextStock, nextBag, matId, reqQty)
            nextStock = res.first
            nextBag = res.second
        }

        val seq = settlement.nextSequenceId
        val tx = SettlementTransaction(
            id = "tx_$seq",
            type = SettlementTransactionType.CONTRACT_PAYOUT,
            description = "${contract.title} (+${contract.rewardReputation} Rep)",
            coinDelta = contract.rewardCoins,
            timestampEpochMs = nowMs
        )

        val targetFacility = contract.targetFacilityId?.let { SettlementFacilityType.fromId(it) }
        var nextFacilities = settlement.facilities
        var contractSuppliesBonus = 0
        if (targetFacility != null) {
            val curLvl = settlement.facilityLevel(targetFacility)
            if (curLvl == 0) {
                nextFacilities = nextFacilities + SettlementFacilityBuilding(targetFacility, 1)
            } else if (curLvl < targetFacility.maxLevel) {
                nextFacilities = nextFacilities.map { f ->
                    if (f.type == targetFacility) f.copy(level = curLvl + 1) else f
                }
            } else {
                contractSuppliesBonus = 8
            }
        }

        val remainingContracts = settlement.openContracts.filterNot { it.id == contractId }
        val nextCompletedCount = settlement.completedContractsCount + 1
        var updatedSettlement = settlement.copy(
            facilities = nextFacilities,
            suppliesStock = (settlement.suppliesStock + contractSuppliesBonus).coerceAtMost(settlement.suppliesCapacity),
            reputation = settlement.reputation + contract.rewardReputation,
            completedContractsCount = nextCompletedCount,
            communityHappinessBonus = (settlement.communityHappinessBonus + contract.happinessBonus).coerceAtMost(30),
            bonusFoodCap = settlement.bonusFoodCap + contract.foodCapBonus,
            bonusWaterCap = settlement.bonusWaterCap + contract.waterCapBonus,
            bonusDailyFoodProd = settlement.bonusDailyFoodProd + contract.passiveFoodBonus,
            bonusDailyWaterProd = settlement.bonusDailyWaterProd + contract.passiveWaterBonus,
            openContracts = remainingContracts,
            nextSequenceId = seq + 1,
            transactionHistory = prependTx(settlement.transactionHistory, tx)
        )
        updatedSettlement = updatedSettlement.copy(
            survivors = refreshNeedsAndHappiness(updatedSettlement, paidFull = true)
        )

        var nextState = state.copy(
            coins = state.coins + contract.rewardCoins,
            lifetimeEarned = state.lifetimeEarned + contract.rewardCoins,
            xp = state.xp + contract.rewardXp,
            warehouseStock = nextStock,
            inventory = nextBag,
            settlement = updatedSettlement
        )

        if (updatedSettlement.openContracts.size < 4) {
            val (replacements, nextSeqAfter) = generateConstructionContracts(
                nextState,
                updatedSettlement.nextSequenceId,
                count = 4 - updatedSettlement.openContracts.size
            )
            nextState = nextState.copy(
                settlement = nextState.settlement.copy(
                    openContracts = updatedSettlement.openContracts + replacements,
                    nextSequenceId = nextSeqAfter
                )
            )
        }

        return SettlementActionOutcome.Success(
            updatedState = nextState,
            bannerMessage = "Built ${contract.title}! +$${contract.rewardCoins} & +${contract.rewardReputation} Rep"
        )
    }

    fun tick(
        state: TycoonSaveState,
        nowMs: Long,
        allowArrivalSpawn: Boolean = true
    ): Pair<TycoonSaveState, String?> {
        var currentState = ensureInitialized(state, nowMs)
        var s = currentState.settlement
        var changed = (currentState != state)
        var banner: String? = null

        // 1. Complete any finished housing construction
        val completedNames = mutableListOf<String>()
        val updatedHousing = s.housingUnits.map { building ->
            if (!building.isCompleted && nowMs >= building.completesEpochMs) {
                completedNames += building.type.title
                changed = true
                building.copy(isCompleted = true)
            } else {
                building
            }
        }
        if (completedNames.isNotEmpty()) {
            s = s.copy(
                housingUnits = updatedHousing,
                survivors = refreshNeedsAndHappiness(s.copy(housingUnits = updatedHousing), paidFull = true)
            )
            banner = "${completedNames.first()} construction complete!"
        }

        // 2. Spawn periodic refugee arrivals at settlement gate
        if (allowArrivalSpawn &&
            s.pendingArrivals.size < MAX_PENDING_ARRIVALS &&
            nowMs - s.lastArrivalEpochMs >= ARRIVAL_INTERVAL_MS
        ) {
            val seq = s.nextSequenceId
            val arrival = generateArrival(seq, nowMs)
            s = s.copy(
                pendingArrivals = s.pendingArrivals + arrival,
                lastArrivalEpochMs = nowMs,
                nextSequenceId = seq + 1
            )
            changed = true
            if (banner == null) {
                banner = "Refugee ${arrival.name} arrived at settlement gate"
            }
        }

        // 3. Process Settlement Day Cycle (consumption, worker production, salaries, upkeep, trading reset)
        currentState = currentState.copy(settlement = s)
        if (nowMs - s.lastDailyTickEpochMs >= DAY_DURATION_MS) {
            currentState = processDailyCycle(currentState, nowMs)
            changed = true
        }

        return if (changed) currentState to banner else state to null
    }

    fun processDailyCycle(state: TycoonSaveState, nowMs: Long): TycoonSaveState {
        var s = state.settlement
        val pop = s.survivors.size
        val nextStock = state.warehouseStock.toMutableMap()
        var stockUsed = nextStock.values.sum()
        val stockCap = state.warehouseStockCapacity
        var nextCoins = state.coins
        var nextLifetime = state.lifetimeEarned
        var seq = s.nextSequenceId

        val farmLvl = s.facilityLevel(SettlementFacilityType.FARM)
        val millLvl = s.facilityLevel(SettlementFacilityType.MILL)
        val watermillLvl = s.facilityLevel(SettlementFacilityType.WATERMILL)
        val marketLvl = s.facilityLevel(SettlementFacilityType.MARKET)
        val mineLvl = s.facilityLevel(SettlementFacilityType.SETTLEMENT_MINE)
        val smelterLvl = s.facilityLevel(SettlementFacilityType.SETTLEMENT_SMELTER)

        // Farmers + Farm/Mill/Watermill produce Food, Water & small Timber harvest
        val farmers = s.survivors.filter { it.assignedJob == SurvivorProfession.FARMER }
        val farmMult = 1.0 + farmLvl * 0.15
        val foodGained = farmers.sumOf { (2.6 * it.effectiveEfficiencyMultiplier() * farmMult).roundToInt() } +
            s.bonusDailyFoodProd + farmLvl * 5 + millLvl * 3
        val waterGained = farmers.sumOf { (2.4 * it.effectiveEfficiencyMultiplier()).roundToInt() } +
            s.bonusDailyWaterProd + watermillLvl * 5
        val timberBonus = farmers.sumOf { (0.6 * it.effectiveEfficiencyMultiplier()).toInt() }
        if (timberBonus > 0 && stockUsed < stockCap) {
            val add = minOf(timberBonus, stockCap - stockUsed)
            nextStock["wood"] = (nextStock["wood"] ?: 0) + add
            stockUsed += add
        }

        // Miners + Quarry Mine extract ores based on unlocked biomes
        val minerPool = unlockedMinerResourcePool(state.unlockedBiomes)
        val miners = s.survivors.filter { it.assignedJob == SurvivorProfession.MINER }
        miners.forEachIndexed { idx, miner ->
            val yieldUnits = (1.5 * miner.effectiveEfficiencyMultiplier()).roundToInt().coerceAtLeast(1) + mineLvl * 2
            val resId = minerPool[(s.dayNumber + idx) % minerPool.size]
            if (stockUsed < stockCap) {
                val add = minOf(yieldUnits, stockCap - stockUsed)
                nextStock[resId] = (nextStock[resId] ?: 0) + add
                stockUsed += add
            }
        }
        if (mineLvl > 0 && stockUsed < stockCap) {
            val quarryYield = minOf(mineLvl * 2, stockCap - stockUsed)
            val quarryRes = minerPool[s.dayNumber % minerPool.size]
            nextStock[quarryRes] = (nextStock[quarryRes] ?: 0) + quarryYield
            stockUsed += quarryYield
        }

        // Factory workers + Settlement Smeltery convert raw stock into manufactured products (only when raw input exists!)
        val factoryWorkers = s.survivors.filter { it.assignedJob == SurvivorProfession.FACTORY_WORKER }
        factoryWorkers.forEach { worker ->
            val maxBatches = (1.2 * worker.effectiveEfficiencyMultiplier()).roundToInt().coerceAtLeast(1) + smelterLvl * 2
            var crafted = 0
            val recipes = listOf(
                "wood" to "planks",
                "stone" to "stone_blocks",
                "iron" to "metal_bars",
                "gold" to "refined_gold"
            )
            for ((inputId, outputId) in recipes) {
                val have = nextStock[inputId] ?: 0
                if (have >= 2 && crafted < maxBatches) {
                    val make = minOf(have / 2, maxBatches - crafted)
                    if (make > 0) {
                        val consume = make * 2
                        if (have == consume) nextStock.remove(inputId) else nextStock[inputId] = have - consume
                        nextStock[outputId] = (nextStock[outputId] ?: 0) + make
                        // Net stock decreases by `make` (2 inputs -> 1 output), always safe for capacity
                        stockUsed -= make
                        crafted += make
                    }
                }
            }
        }

        // Traders & Transport workers + Frontier Market generate capped service revenue & recover demand saturation
        val traders = s.survivors.filter { it.assignedJob == SurvivorProfession.TRADER }
        val transporters = s.survivors.filter { it.assignedJob == SurvivorProfession.TRANSPORT_WORKER }
        val marketRevMult = 1.0 + marketLvl * 0.18
        val traderEarnings = traders.sumOf { (14.0 * it.effectiveEfficiencyMultiplier() * marketRevMult).roundToLong() }
        val transportEarnings = transporters.sumOf { (11.0 * it.effectiveEfficiencyMultiplier() * marketRevMult).roundToLong() }
        val totalJobRevenue = traderEarnings + transportEarnings

        // Operating costs: Worker salaries + Housing maintenance
        val salaries = s.survivors.sumOf { it.dailySalary() }
        val housingMaint = s.housingUnits.filter { it.isCompleted }.sumOf { it.effectiveMaintenanceCost() }
        val totalOperatingCost = salaries + housingMaint

        nextCoins += totalJobRevenue
        if (totalJobRevenue > 0L) nextLifetime += totalJobRevenue

        val paidFull = nextCoins >= totalOperatingCost
        nextCoins = (nextCoins - totalOperatingCost).coerceAtLeast(0L)
        val netDayDelta = totalJobRevenue - totalOperatingCost

        // Food, water & supplies update
        val foodNeed = pop
        val waterNeed = pop
        val supplyNeed = if (pop == 0) 0 else ((pop + 1) / 2)

        val nextFood = ((s.foodStock + foodGained) - foodNeed).coerceIn(0, s.foodCapacity)
        val nextWater = ((s.waterStock + waterGained) - waterNeed).coerceIn(0, s.waterCapacity)
        val nextSupplies = (s.suppliesStock - supplyNeed).coerceIn(0, s.suppliesCapacity)

        // Demand saturation mean-reverts slowly each day (accelerated by Traders)
        val decayRate = 0.06 + traders.size * 0.025
        val nextSaturation = s.demandSaturation.mapValues { (_, v) ->
            (v - decayRate).coerceAtLeast(0.0)
        }.filterValues { it > 0.005 }

        val updatedPreNeeds = s.copy(
            foodStock = nextFood,
            waterStock = nextWater,
            suppliesStock = nextSupplies,
            dayNumber = s.dayNumber + 1,
            lastDailyTickEpochMs = nowMs,
            demandSaturation = nextSaturation,
            survivorOffersForSale = generateTradeOffers(state.copy(settlement = s), seq, nowMs)
        )
        val updatedSurvivors = refreshNeedsAndHappiness(updatedPreNeeds, paidFull = paidFull)

        val nextHistory = if (pop > 0 || housingMaint > 0L) {
            val tx = SettlementTransaction(
                id = "tx_$seq",
                type = SettlementTransactionType.DAILY_OPERATIONS,
                description = "Day ${s.dayNumber}: Rev +$$totalJobRevenue / Cost -$$totalOperatingCost",
                coinDelta = netDayDelta,
                timestampEpochMs = nowMs
            )
            seq++
            prependTx(s.transactionHistory, tx)
        } else {
            s.transactionHistory
        }

        val finalSettlement = updatedPreNeeds.copy(
            survivors = updatedSurvivors,
            nextSequenceId = seq,
            transactionHistory = nextHistory
        )
        return state.copy(
            coins = nextCoins,
            lifetimeEarned = nextLifetime,
            warehouseStock = nextStock,
            settlement = finalSettlement
        )
    }

    private fun generateArrival(
        seq: Long,
        nowMs: Long,
        preferredProfession: SurvivorProfession? = null
    ): RefugeeArrival {
        val fName = SURVIVOR_FIRST_NAMES[(abs(seq.toInt()) * 7 + 3) % SURVIVOR_FIRST_NAMES.size]
        val lName = SURVIVOR_LAST_NAMES[(abs(seq.toInt()) * 5 + 1) % SURVIVOR_LAST_NAMES.size]
        val prof = preferredProfession
            ?: SurvivorProfession.entries[abs(seq.toInt()) % SurvivorProfession.entries.size]
        val skill = 1 + (abs(seq.toInt() + 2) % 4)
        return RefugeeArrival(
            id = "arr_$seq",
            name = "$fName $lName",
            profession = prof,
            skillLevel = skill,
            arrivedEpochMs = nowMs
        )
    }

    private fun generateTradeOffers(
        state: TycoonSaveState,
        seed: Long,
        nowMs: Long
    ): List<SurvivorTradeOffer> {
        val settlement = state.settlement
        val pool = TradeCatalog.allGoods.filter { good ->
            when (good.id) {
                "wood", "stone", "planks", "stone_blocks", "wood_craft" -> true
                "coal", "iron", "metal_bars", "ore_concentrate" -> state.unlockedBiomes.contains("mountains") || state.playerLevel >= 2
                "copper", "gold", "refined_gold" -> state.unlockedBiomes.contains("desert") || state.playerLevel >= 3
                "crystal", "cut_crystal" -> state.unlockedBiomes.contains("frozen") || state.playerLevel >= 4
                else -> false
            }
        }.ifEmpty { TradeCatalog.allGoods.take(4) }

        val offers = mutableListOf<SurvivorTradeOffer>()
        val marketLvl = settlement.facilityLevel(SettlementFacilityType.MARKET)
        val offerCount = (4 + marketLvl).coerceAtMost(pool.size)
        val dayPhase = (nowMs / DAY_DURATION_MS).toInt() + seed.toInt()
        for (i in 0 until offerCount) {
            val good = pool[abs(dayPhase + i * 3) % pool.size]
            if (offers.any { it.goodId == good.id }) continue
            val qty = 4 + (abs(dayPhase + i) % 9) + (settlement.survivors.size / 3)
            val askPrice = settlement.survivorAskPrice(good, state.marketMultiplier(good.id))
            val seller = settlement.survivors.getOrNull(i)?.name
                ?: SURVIVOR_FIRST_NAMES[abs(dayPhase + i) % SURVIVOR_FIRST_NAMES.size]
            offers += SurvivorTradeOffer(
                goodId = good.id,
                availableQuantity = qty,
                unitAskPrice = askPrice,
                sellerName = seller
            )
        }
        return offers
    }

    private fun generateConstructionContracts(
        state: TycoonSaveState,
        startSeq: Long,
        count: Int
    ): Pair<List<SettlementConstructionContract>, Long> {
        val settlement = state.settlement
        val existingCats = settlement.openContracts.map { it.category }.toMutableSet()
        val categories = SettlementContractCategory.entries
        val out = mutableListOf<SettlementConstructionContract>()
        var seq = startSeq

        repeat(count) {
            val cat = categories.firstOrNull { !existingCats.contains(it) }
                ?: categories[abs(seq.toInt()) % categories.size]
            existingCats.add(cat)
            val requester = settlement.survivors.getOrNull(abs(seq.toInt()) % settlement.survivors.size.coerceAtLeast(1))?.name
                ?: "Settlement Council"
            val scale = 1 + (settlement.completedContractsCount / 3) + (settlement.survivors.size / 6)
            val variant = abs(seq.toInt())

            val contract = when (cat) {
                SettlementContractCategory.HOUSES -> if (variant % 2 == 0) {
                    SettlementConstructionContract(
                        id = "sc_$seq",
                        title = "Build Frontier Cottages",
                        description = "Settlers request timber-framed houses to welcome arriving families.",
                        category = cat,
                        requesterName = requester,
                        materialRequirements = mapOf("wood" to 8 * scale, "planks" to 4 * scale),
                        rewardCoins = (140L + 75L * scale),
                        rewardXp = 55L * scale,
                        rewardReputation = 18 + scale * 4,
                        happinessBonus = 2
                    )
                } else {
                    SettlementConstructionContract(
                        id = "sc_$seq",
                        title = "Village Commons Quarter",
                        description = "Residents ask for stone-chimney cottages & courtyard lanterns.",
                        category = cat,
                        requesterName = requester,
                        materialRequirements = mapOf("planks" to 5 * scale, "stone" to 6 * scale, "stone_blocks" to 2 * scale),
                        rewardCoins = (175L + 85L * scale),
                        rewardXp = 65L * scale,
                        rewardReputation = 20 + scale * 4,
                        happinessBonus = 3
                    )
                }
                SettlementContractCategory.SHELTERS -> SettlementConstructionContract(
                    id = "sc_$seq",
                    title = "Timber Frontier Cabins",
                    description = "Reinforce insulated bunkhouses and stormproof timber cabins.",
                    category = cat,
                    requesterName = requester,
                    materialRequirements = mapOf("wood" to 6 * scale, "stone" to 6 * scale, "stone_blocks" to 3 * scale),
                    rewardCoins = (165L + 85L * scale),
                    rewardXp = 65L * scale,
                    rewardReputation = 22 + scale * 4,
                    happinessBonus = 3
                )
                SettlementContractCategory.STORAGE_BUILDINGS -> SettlementConstructionContract(
                    id = "sc_$seq",
                    title = "Construct Grain Windmill",
                    description = "Residents request a Windmill to expand Food & Supply reserves.",
                    category = cat,
                    requesterName = requester,
                    materialRequirements = mapOf("wood" to 6 * scale, "planks" to 5 * scale, "stone_blocks" to 4 * scale),
                    rewardCoins = (190L + 95L * scale),
                    rewardXp = 75L * scale,
                    rewardReputation = 24 + scale * 5,
                    foodCapBonus = 25,
                    waterCapBonus = 20,
                    targetFacilityId = SettlementFacilityType.MILL.id
                )
                SettlementContractCategory.COMMUNITY_FACILITIES -> if (variant % 2 == 0) {
                    SettlementConstructionContract(
                        id = "sc_$seq",
                        title = "Frontier Market Stalls",
                        description = "Merchants & settlers request a covered Market plaza for trading.",
                        category = cat,
                        requesterName = requester,
                        materialRequirements = buildMap {
                            put("planks", 5 * scale)
                            put("wood_craft", 3 * scale)
                            put("stone_blocks", 4 * scale)
                        },
                        rewardCoins = (230L + 115L * scale),
                        rewardXp = 95L * scale,
                        rewardReputation = 30 + scale * 6,
                        happinessBonus = 4,
                        targetFacilityId = SettlementFacilityType.MARKET.id
                    )
                } else {
                    SettlementConstructionContract(
                        id = "sc_$seq",
                        title = "River Watermill Station",
                        description = "Settlers request a riverside Watermill for steady fresh water.",
                        category = cat,
                        requesterName = requester,
                        materialRequirements = buildMap {
                            put("stone_blocks", 5 * scale)
                            put("planks", 4 * scale)
                            if (state.unlockedBiomes.contains("mountains")) put("metal_bars", 2 * scale)
                        },
                        rewardCoins = (240L + 115L * scale),
                        rewardXp = 95L * scale,
                        rewardReputation = 30 + scale * 6,
                        happinessBonus = 3,
                        passiveWaterBonus = 4,
                        targetFacilityId = SettlementFacilityType.WATERMILL.id
                    )
                }
                SettlementContractCategory.PRODUCTION_BUILDINGS -> when (variant % 3) {
                    0 -> SettlementConstructionContract(
                        id = "sc_$seq",
                        title = "Hexagon Farmstead",
                        description = "Farmers request irrigated plots & barns for daily food.",
                        category = cat,
                        requesterName = requester,
                        materialRequirements = mapOf("wood" to 8 * scale, "planks" to 5 * scale, "stone" to 5 * scale),
                        rewardCoins = (210L + 100L * scale),
                        rewardXp = 85L * scale,
                        rewardReputation = 26 + scale * 5,
                        passiveFoodBonus = 4,
                        targetFacilityId = SettlementFacilityType.FARM.id
                    )
                    1 -> SettlementConstructionContract(
                        id = "sc_$seq",
                        title = "Settlement Quarry Mine",
                        description = "Miners request timber-shored quarry shafts for stone & ore.",
                        category = cat,
                        requesterName = requester,
                        materialRequirements = mapOf("wood" to 8 * scale, "stone" to 8 * scale, "stone_blocks" to 4 * scale),
                        rewardCoins = (230L + 110L * scale),
                        rewardXp = 90L * scale,
                        rewardReputation = 28 + scale * 5,
                        happinessBonus = 2,
                        targetFacilityId = SettlementFacilityType.SETTLEMENT_MINE.id
                    )
                    else -> SettlementConstructionContract(
                        id = "sc_$seq",
                        title = "Settlement Smeltery Forge",
                        description = "Artisans request a brick smeltery to refine metal bars & planks.",
                        category = cat,
                        requesterName = requester,
                        materialRequirements = mapOf("stone_blocks" to 6 * scale, "stone" to 6 * scale, "planks" to 4 * scale),
                        rewardCoins = (250L + 120L * scale),
                        rewardXp = 100L * scale,
                        rewardReputation = 32 + scale * 5,
                        happinessBonus = 2,
                        targetFacilityId = SettlementFacilityType.SETTLEMENT_SMELTER.id
                    )
                }
            }
            out += contract
            seq++
        }
        return out to seq
    }

    private fun computeNeedsForNewResident(settlement: SettlementState): SurvivorNeeds {
        val foodScore = if (settlement.foodStock >= 5) 85 else 45
        val waterScore = if (settlement.waterStock >= 5) 85 else 45
        val supScore = if (settlement.suppliesStock >= 4) 80 else 45
        return SurvivorNeeds(
            food = foodScore,
            water = waterScore,
            supplies = supScore,
            housing = 100
        )
    }

    private fun refreshNeedsAndHappiness(
        settlement: SettlementState,
        paidFull: Boolean
    ): List<SurvivorProfile> {
        val pop = settlement.survivors.size.coerceAtLeast(1)
        val foodRatio = (settlement.foodStock.toDouble() / (pop * 2.0)).coerceIn(0.0, 1.0)
        val waterRatio = (settlement.waterStock.toDouble() / (pop * 2.0)).coerceIn(0.0, 1.0)
        val supRatio = (settlement.suppliesStock.toDouble() / (pop * 1.5)).coerceIn(0.0, 1.0)

        val foodScore = (25 + foodRatio * 75).roundToInt().coerceIn(0, 100)
        val waterScore = (25 + waterRatio * 75).roundToInt().coerceIn(0, 100)
        val supScore = (30 + supRatio * 70).roundToInt().coerceIn(0, 100)

        return settlement.survivors.map { s ->
            val hasHouse = s.housingUnitId != null && settlement.housingUnits.any { it.id == s.housingUnitId && it.isCompleted }
            val nextNeeds = SurvivorNeeds(
                food = foodScore,
                water = waterScore,
                supplies = supScore,
                housing = if (hasHouse) 100 else 20
            )
            val nextHappy = computeSurvivorHappiness(
                needs = nextNeeds,
                isEmployed = s.isEmployed,
                isMatched = s.isMatchedRole,
                communityBonus = settlement.communityHappinessBonus,
                paidFull = paidFull
            )
            s.copy(needs = nextNeeds, happiness = nextHappy)
        }
    }

    private fun computeSurvivorHappiness(
        needs: SurvivorNeeds,
        isEmployed: Boolean,
        isMatched: Boolean,
        communityBonus: Int,
        paidFull: Boolean
    ): Int {
        var score = needs.averageScore()
        if (!isEmployed) score -= 6 else if (isMatched) score += 6
        if (!paidFull) score -= 12
        score += communityBonus
        return score.coerceIn(5, 100)
    }

    private fun reassignHousingBeds(
        survivors: List<SurvivorProfile>,
        housingUnits: List<HousingBuilding>
    ): List<SurvivorProfile> {
        val completed = housingUnits.filter { it.isCompleted }
        val occupancy = mutableMapOf<String, Int>()
        return survivors.map { s ->
            val currentHouse = completed.firstOrNull { it.id == s.housingUnitId }
            val assignedId = if (currentHouse != null && (occupancy[currentHouse.id] ?: 0) < currentHouse.effectiveCapacity()) {
                occupancy[currentHouse.id] = (occupancy[currentHouse.id] ?: 0) + 1
                currentHouse.id
            } else {
                val fallback = completed.firstOrNull { (occupancy[it.id] ?: 0) < it.effectiveCapacity() }
                if (fallback != null) {
                    occupancy[fallback.id] = (occupancy[fallback.id] ?: 0) + 1
                    fallback.id
                } else null
            }
            s.copy(housingUnitId = assignedId)
        }
    }

    private fun unlockedMinerResourcePool(unlockedBiomes: Set<String>): List<String> = buildList {
        add("stone")
        if (unlockedBiomes.contains("mountains")) {
            add("coal")
            add("iron")
        }
        if (unlockedBiomes.contains("desert")) {
            add("copper")
            add("gold")
        }
        if (unlockedBiomes.contains("frozen")) {
            add("crystal")
        }
    }

    private fun deductFromStockAndBag(
        currentStock: Map<String, Int>,
        currentBag: Map<String, Int>,
        goodId: String,
        amount: Int
    ): Pair<Map<String, Int>, Map<String, Int>> {
        var remaining = amount
        val stock = currentStock.toMutableMap()
        val bag = currentBag.toMutableMap()

        val ws = stock[goodId] ?: 0
        if (ws > 0 && remaining > 0) {
            val take = minOf(ws, remaining)
            if (ws == take) stock.remove(goodId) else stock[goodId] = ws - take
            remaining -= take
        }
        val bg = bag[goodId] ?: 0
        if (bg > 0 && remaining > 0) {
            val take = minOf(bg, remaining)
            if (bg == take) bag.remove(goodId) else bag[goodId] = bg - take
        }
        return stock to bag
    }

    private fun prependTx(
        history: List<SettlementTransaction>,
        tx: SettlementTransaction
    ): List<SettlementTransaction> = (listOf(tx) + history).take(MAX_TRANSACTIONS)
}
