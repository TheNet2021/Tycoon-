package com.example.data

import kotlin.math.roundToInt
import kotlin.math.roundToLong

enum class HousingType(
    val id: String,
    val title: String,
    val description: String,
    val capacity: Int,
    val coinCost: Long,
    val materialCosts: Map<String, Int>,
    val buildTimeSeconds: Long,
    val dailyMaintenanceCost: Long,
    val accentHex: Long
) {
    SMALL_TENT(
        id = "small_tent",
        title = "Small Tent",
        description = "Quick canvas shelter for first frontier arrivals",
        capacity = 2,
        coinCost = 80L,
        materialCosts = mapOf("wood" to 6),
        buildTimeSeconds = 8L,
        dailyMaintenanceCost = 4L,
        accentHex = 0xFF34D399
    ),
    WOODEN_CABIN(
        id = "wooden_cabin",
        title = "Wooden Cabin",
        description = "Warm timber lodge with bunks and hearth",
        capacity = 5,
        coinCost = 260L,
        materialCosts = mapOf("wood" to 12, "planks" to 6, "stone" to 4),
        buildTimeSeconds = 15L,
        dailyMaintenanceCost = 12L,
        accentHex = 0xFFD97706
    ),
    STONE_HOUSE(
        id = "stone_house",
        title = "Stone House",
        description = "Sturdy masonry home resilient to frontier storms",
        capacity = 10,
        coinCost = 720L,
        materialCosts = mapOf("stone" to 16, "stone_blocks" to 10, "planks" to 8),
        buildTimeSeconds = 25L,
        dailyMaintenanceCost = 28L,
        accentHex = 0xFF94A3B8
    ),
    APARTMENT_BUILDING(
        id = "apartment_building",
        title = "Apartment Building",
        description = "Multi-story timber & steel quarters for growing crews",
        capacity = 25,
        coinCost = 2400L,
        materialCosts = mapOf("stone_blocks" to 24, "metal_bars" to 10, "wood_craft" to 8),
        buildTimeSeconds = 40L,
        dailyMaintenanceCost = 75L,
        accentHex = 0xFF38BDF8
    ),
    RESIDENTIAL_COMPLEX(
        id = "residential_complex",
        title = "Residential Complex",
        description = "High-density self-contained settlement district",
        capacity = 50,
        coinCost = 6800L,
        materialCosts = mapOf(
            "stone_blocks" to 40,
            "metal_bars" to 22,
            "industrial_gear" to 6,
            "cut_crystal" to 4
        ),
        buildTimeSeconds = 60L,
        dailyMaintenanceCost = 180L,
        accentHex = 0xFFE879F9
    );

    companion object {
        fun fromId(id: String): HousingType? = entries.firstOrNull { it.id == id }
    }
}

enum class SurvivorProfession(
    val id: String,
    val title: String,
    val baseDailyWage: Long,
    val accentHex: Long
) {
    MINER("miner", "Miner", 16L, 0xFF94A3B8),
    FARMER("farmer", "Farmer", 12L, 0xFF10B981),
    FACTORY_WORKER("factory_worker", "Factory Worker", 18L, 0xFFFB923C),
    TRANSPORT_WORKER("transport_worker", "Transport Worker", 15L, 0xFF38BDF8),
    TRADER("trader", "Trader", 20L, 0xFFFBBF24);

    companion object {
        fun fromId(id: String): SurvivorProfession? = entries.firstOrNull { it.id == id }
    }
}

data class SurvivorNeeds(
    val food: Int = 80,
    val water: Int = 80,
    val supplies: Int = 75,
    val housing: Int = 100
) {
    fun averageScore(): Int = ((food + water + supplies + housing) / 4.0).roundToInt().coerceIn(0, 100)
}

data class SurvivorProfile(
    val id: String,
    val name: String,
    val profession: SurvivorProfession,
    val skillLevel: Int,
    val assignedJob: SurvivorProfession? = null,
    val housingUnitId: String? = null,
    val needs: SurvivorNeeds = SurvivorNeeds(),
    val happiness: Int = 80,
    val arrivedEpochMs: Long = 0L
) {
    val isEmployed: Boolean get() = assignedJob != null
    val isMatchedRole: Boolean get() = assignedJob == profession

    fun dailySalary(): Long {
        val job = assignedJob ?: return 0L
        return job.baseDailyWage + (skillLevel - 1) * 3L
    }

    fun effectiveEfficiencyMultiplier(): Double {
        if (assignedJob == null) return 0.0
        val skillFactor = 1.0 + (skillLevel - 1) * 0.14
        val matchBonus = if (isMatchedRole) 1.35 else 1.0
        val moodFactor = 0.55 + (happiness.coerceIn(0, 100) / 100.0) * 0.65
        return skillFactor * matchBonus * moodFactor
    }
}

data class RefugeeArrival(
    val id: String,
    val name: String,
    val profession: SurvivorProfession,
    val skillLevel: Int,
    val arrivedEpochMs: Long
)

data class HousingBuilding(
    val id: String,
    val type: HousingType,
    val startedEpochMs: Long,
    val completesEpochMs: Long,
    val isCompleted: Boolean,
    val upgradeLevel: Int = 1
) {
    fun effectiveCapacity(): Int {
        if (upgradeLevel <= 1) return type.capacity
        val perLevelExtra = maxOf(1, (type.capacity * 0.42).roundToInt())
        return type.capacity + (upgradeLevel - 1) * perLevelExtra
    }

    fun effectiveMaintenanceCost(): Long {
        if (upgradeLevel <= 1) return type.dailyMaintenanceCost
        return (type.dailyMaintenanceCost * (1.0 + (upgradeLevel - 1) * 0.35)).roundToLong()
    }

    fun canUpgrade(): Boolean = isCompleted && upgradeLevel < MAX_HOUSING_LEVEL

    fun nextUpgradeCoinCost(): Long =
        ((type.coinCost * 0.72) * upgradeLevel).roundToLong().coerceAtLeast(50L)

    fun nextUpgradeMaterialCosts(): Map<String, Int> = buildMap {
        type.materialCosts.forEach { (mat, qty) ->
            put(mat, maxOf(2, (qty * 0.55 * upgradeLevel).roundToInt()))
        }
        if (upgradeLevel == 1) {
            put("planks", (this["planks"] ?: 0) + maxOf(3, type.capacity / 2))
        } else {
            put("stone_blocks", (this["stone_blocks"] ?: 0) + maxOf(4, type.capacity / 2))
            if (type.ordinal >= HousingType.STONE_HOUSE.ordinal) {
                put("metal_bars", (this["metal_bars"] ?: 0) + 3)
            }
        }
    }

    val hexModelName: String
        get() = when {
            type == HousingType.RESIDENTIAL_COMPLEX || type == HousingType.APARTMENT_BUILDING -> "building-village"
            type == HousingType.STONE_HOUSE && upgradeLevel >= 2 -> "building-village"
            type == HousingType.STONE_HOUSE -> "building-house"
            type == HousingType.WOODEN_CABIN && upgradeLevel >= 2 -> "building-house"
            type == HousingType.SMALL_TENT && upgradeLevel >= 3 -> "building-house"
            else -> "building-cabin"
        }

    fun remainingSeconds(nowMs: Long): Long {
        if (isCompleted) return 0L
        return ((completesEpochMs - nowMs) / 1000L).coerceAtLeast(0L)
    }

    fun progressFraction(nowMs: Long): Float {
        if (isCompleted) return 1f
        val total = (completesEpochMs - startedEpochMs).coerceAtLeast(1L)
        val elapsed = (nowMs - startedEpochMs).coerceIn(0L, total)
        return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }

    companion object {
        const val MAX_HOUSING_LEVEL = 3
    }
}

enum class SettlementFacilityType(
    val id: String,
    val title: String,
    val hexModelName: String,
    val description: String,
    val baseCoinCost: Long,
    val baseMaterialCosts: Map<String, Int>,
    val accentHex: Long,
    val maxLevel: Int = 3
) {
    FARM(
        id = "farm",
        title = "Hexagon Farmstead",
        hexModelName = "building-farm",
        description = "Cultivates daily food (+5/lv) & boosts Farmer yield (+15%/lv).",
        baseCoinCost = 180L,
        baseMaterialCosts = mapOf("wood" to 8, "planks" to 4, "stone" to 4),
        accentHex = 0xFF34D399
    ),
    MILL(
        id = "mill",
        title = "Grain Windmill",
        hexModelName = "building-mill",
        description = "Expands Food (+35/lv) & Supplies (+20/lv) storage and harvests grain.",
        baseCoinCost = 260L,
        baseMaterialCosts = mapOf("wood" to 10, "planks" to 6, "stone_blocks" to 4),
        accentHex = 0xFFFBBF24
    ),
    WATERMILL(
        id = "watermill",
        title = "River Watermill",
        hexModelName = "building-watermill",
        description = "Pumps clean river water (+5/d & +35 Water Cap per lv).",
        baseCoinCost = 280L,
        baseMaterialCosts = mapOf("planks" to 6, "stone" to 8, "stone_blocks" to 4),
        accentHex = 0xFF38BDF8
    ),
    MARKET(
        id = "market",
        title = "Frontier Market",
        hexModelName = "building-market",
        description = "Lifts trade revenue (+18%/lv), settler bids (+4%/lv) & offer variety.",
        baseCoinCost = 320L,
        baseMaterialCosts = mapOf("planks" to 8, "wood_craft" to 4, "stone_blocks" to 4),
        accentHex = 0xFFF59E0B
    ),
    SETTLEMENT_MINE(
        id = "mine",
        title = "Settlement Quarry",
        hexModelName = "building-mine",
        description = "Extracts stone/ores daily (+2 per lv) and boosts Miner output.",
        baseCoinCost = 360L,
        baseMaterialCosts = mapOf("wood" to 8, "stone" to 10, "stone_blocks" to 5),
        accentHex = 0xFF94A3B8
    ),
    SETTLEMENT_SMELTER(
        id = "smelter",
        title = "Settlement Smeltery",
        hexModelName = "building-smelter",
        description = "Increases factory refining throughput (+2 batches/d per lv).",
        baseCoinCost = 440L,
        baseMaterialCosts = mapOf("stone_blocks" to 8, "stone" to 6, "metal_bars" to 4),
        accentHex = 0xFFFB923C
    );

    fun coinCostForLevel(targetLevel: Int): Long {
        val lvl = targetLevel.coerceIn(1, maxLevel)
        return (baseCoinCost * when (lvl) {
            1 -> 1.0
            2 -> 1.65
            else -> 2.5
        }).roundToLong()
    }

    fun materialCostsForLevel(targetLevel: Int): Map<String, Int> = buildMap {
        val lvl = targetLevel.coerceIn(1, maxLevel)
        val mult = when (lvl) {
            1 -> 1.0
            2 -> 1.5
            else -> 2.15
        }
        baseMaterialCosts.forEach { (k, v) -> put(k, maxOf(1, (v * mult).roundToInt())) }
        if (lvl >= 2) {
            put("stone_blocks", (this["stone_blocks"] ?: 0) + lvl * 2)
        }
        if (lvl >= 3) {
            put("metal_bars", (this["metal_bars"] ?: 0) + 4)
        }
    }

    companion object {
        fun fromId(id: String): SettlementFacilityType? = entries.firstOrNull { it.id == id }
    }
}

data class SettlementFacilityBuilding(
    val type: SettlementFacilityType,
    val level: Int = 1
)

enum class SettlementContractCategory(val label: String, val accentHex: Long) {
    HOUSES("Houses", 0xFF34D399),
    SHELTERS("Shelters", 0xFF38BDF8),
    STORAGE_BUILDINGS("Storage", 0xFFD97706),
    COMMUNITY_FACILITIES("Community", 0xFFE879F9),
    PRODUCTION_BUILDINGS("Production", 0xFFFB923C);

    companion object {
        fun fromId(id: String): SettlementContractCategory? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }
}

data class SettlementConstructionContract(
    val id: String,
    val title: String,
    val description: String,
    val category: SettlementContractCategory,
    val requesterName: String,
    val materialRequirements: Map<String, Int>,
    val rewardCoins: Long,
    val rewardXp: Long,
    val rewardReputation: Int,
    val happinessBonus: Int = 0,
    val foodCapBonus: Int = 0,
    val waterCapBonus: Int = 0,
    val passiveFoodBonus: Int = 0,
    val passiveWaterBonus: Int = 0,
    val targetFacilityId: String? = null
)

data class SurvivorTradeOffer(
    val goodId: String,
    val availableQuantity: Int,
    val unitAskPrice: Long,
    val sellerName: String
)

enum class SettlementTransactionType(val label: String) {
    SOLD_TO_SURVIVORS("Sold to Settlers"),
    BOUGHT_FROM_SURVIVORS("Bought from Settlers"),
    SUPPLIES_PURCHASE("Rations & Supplies"),
    CONTRACT_PAYOUT("Build Contract"),
    DAILY_OPERATIONS("Wages & Upkeep")
}

data class SettlementTransaction(
    val id: String,
    val type: SettlementTransactionType,
    val description: String,
    val coinDelta: Long,
    val timestampEpochMs: Long
)

data class SettlementStats(
    val population: Int,
    val totalHousingCapacity: Int,
    val pendingConstructionCapacity: Int,
    val underConstructionCount: Int,
    val housingOccupancyRate: Float,
    val employedCount: Int,
    val unemployedCount: Int,
    val unemploymentRatePercent: Int,
    val averageHappiness: Int,
    val foodStock: Int,
    val foodCapacity: Int,
    val waterStock: Int,
    val waterCapacity: Int,
    val suppliesStock: Int,
    val suppliesCapacity: Int,
    val dailyFoodNet: Int,
    val dailyWaterNet: Int,
    val dailyWorkerSalaries: Long,
    val dailyHousingMaintenance: Long,
    val totalDailyMaintenanceCost: Long,
    val estimatedDailyTradeAndLogistics: Long,
    val reputation: Int,
    val settlementTierName: String
)

data class SettlementState(
    val survivors: List<SurvivorProfile> = emptyList(),
    val pendingArrivals: List<RefugeeArrival> = emptyList(),
    val housingUnits: List<HousingBuilding> = emptyList(),
    val facilities: List<SettlementFacilityBuilding> = emptyList(),
    val foodStock: Int = 20,
    val waterStock: Int = 20,
    val suppliesStock: Int = 15,
    val reputation: Int = 0,
    val dayNumber: Int = 1,
    val lastDailyTickEpochMs: Long = 0L,
    val lastArrivalEpochMs: Long = 0L,
    val nextSequenceId: Long = 1L,
    val demandSaturation: Map<String, Double> = emptyMap(),
    val survivorOffersForSale: List<SurvivorTradeOffer> = emptyList(),
    val openContracts: List<SettlementConstructionContract> = emptyList(),
    val completedContractsCount: Int = 0,
    val communityHappinessBonus: Int = 0,
    val bonusFoodCap: Int = 0,
    val bonusWaterCap: Int = 0,
    val bonusDailyFoodProd: Int = 0,
    val bonusDailyWaterProd: Int = 0,
    val transactionHistory: List<SettlementTransaction> = emptyList()
) {
    fun facilityLevel(type: SettlementFacilityType): Int =
        facilities.firstOrNull { it.type == type }?.level?.coerceIn(0, type.maxLevel) ?: 0

    val totalHousingCapacity: Int
        get() = housingUnits.filter { it.isCompleted }.sumOf { it.effectiveCapacity() }

    val pendingConstructionCapacity: Int
        get() = housingUnits.filter { !it.isCompleted }.sumOf { it.effectiveCapacity() }

    val availableHousingSlots: Int
        get() = (totalHousingCapacity - survivors.size).coerceAtLeast(0)

    val foodCapacity: Int
        get() = 50 + (totalHousingCapacity * 2) + bonusFoodCap +
            facilityLevel(SettlementFacilityType.FARM) * 15 +
            facilityLevel(SettlementFacilityType.MILL) * 35

    val waterCapacity: Int
        get() = 50 + (totalHousingCapacity * 2) + bonusWaterCap +
            facilityLevel(SettlementFacilityType.WATERMILL) * 35

    val suppliesCapacity: Int
        get() = 40 + totalHousingCapacity + facilityLevel(SettlementFacilityType.MILL) * 20

    fun occupantsIn(housingId: String): Int =
        survivors.count { it.housingUnitId == housingId }

    fun demandMultiplierFor(goodId: String): Double {
        val popScale = (0.95 + (survivors.size * 0.015)).coerceAtMost(1.30)
        val saturation = (demandSaturation[goodId] ?: 0.0).coerceIn(0.0, 0.45)
        return (popScale - saturation).coerceIn(0.65, 1.30)
    }

    fun survivorBidPrice(good: TradeGoodInfo, globalMarketMult: Double): Long {
        val localDemand = demandMultiplierFor(good.id)
        val repFactor = (1.0 + (reputation / 400.0)).coerceAtMost(1.22)
        val marketLvl = facilityLevel(SettlementFacilityType.MARKET)
        val marketFacilityMult = 1.0 + marketLvl * 0.04
        val clampedGlobal = globalMarketMult.coerceIn(0.85, 1.30)
        // Strictly capped around 0.65x..1.35x baseValue to prevent infinite money loops
        return (good.baseValue * localDemand * repFactor * marketFacilityMult * (0.92 + (clampedGlobal - 1.0) * 0.35))
            .roundToLong()
            .coerceAtLeast(1L)
    }

    fun survivorAskPrice(good: TradeGoodInfo, globalMarketMult: Double): Long {
        val bid = survivorBidPrice(good, globalMarketMult)
        // Ask is always strictly higher than bid (minimum +42% spread + 2 coins) so buy->sell arbitrage cannot profit
        val minNoArbitrageAsk = (bid * 1.42).roundToLong() + 2L
        val marketAsk = (good.baseValue * 1.38 * globalMarketMult.coerceIn(0.95, 1.35)).roundToLong()
        return maxOf(minNoArbitrageAsk, marketAsk)
    }

    fun computeStats(): SettlementStats {
        val pop = survivors.size
        val cap = totalHousingCapacity
        val employed = survivors.count { it.isEmployed }
        val unemployed = (pop - employed).coerceAtLeast(0)
        val unemploymentPct = if (pop == 0) 0 else ((unemployed * 100.0) / pop).roundToInt()
        val occupancy = if (cap <= 0) 0f else (pop.toFloat() / cap.toFloat()).coerceIn(0f, 1f)
        val avgHappy = if (pop == 0) {
            (75 + communityHappinessBonus).coerceIn(0, 100)
        } else {
            survivors.map { it.happiness }.average().roundToInt().coerceIn(0, 100)
        }

        val farmLvl = facilityLevel(SettlementFacilityType.FARM)
        val millLvl = facilityLevel(SettlementFacilityType.MILL)
        val watermillLvl = facilityLevel(SettlementFacilityType.WATERMILL)
        val marketLvl = facilityLevel(SettlementFacilityType.MARKET)
        val farmMult = 1.0 + farmLvl * 0.15

        val farmers = survivors.filter { it.assignedJob == SurvivorProfession.FARMER }
        val foodProd = farmers.sumOf { (2.6 * it.effectiveEfficiencyMultiplier() * farmMult).roundToInt() } +
            bonusDailyFoodProd + farmLvl * 5 + millLvl * 3
        val waterProd = farmers.sumOf { (2.4 * it.effectiveEfficiencyMultiplier()).roundToInt() } +
            bonusDailyWaterProd + watermillLvl * 5
        val dailyConsumption = pop

        val salaries = survivors.sumOf { it.dailySalary() }
        val housingMaint = housingUnits.filter { it.isCompleted }.sumOf { it.effectiveMaintenanceCost() }
        val totalMaint = salaries + housingMaint

        val marketRevMult = 1.0 + marketLvl * 0.18
        val tradersAndTransport = survivors.filter {
            it.assignedJob == SurvivorProfession.TRADER || it.assignedJob == SurvivorProfession.TRANSPORT_WORKER
        }
        val estRevenue = tradersAndTransport.sumOf { worker ->
            val base = if (worker.assignedJob == SurvivorProfession.TRADER) 14.0 else 11.0
            (base * worker.effectiveEfficiencyMultiplier() * marketRevMult).roundToLong()
        }

        val tierName = when {
            pop >= 40 || reputation >= 350 -> "Frontier City"
            pop >= 20 || reputation >= 180 -> "Thriving Town"
            pop >= 10 || reputation >= 80 -> "Timber Village"
            pop >= 3 || cap >= 4 -> "Refugee Camp"
            else -> "Frontier Outpost"
        }

        return SettlementStats(
            population = pop,
            totalHousingCapacity = cap,
            pendingConstructionCapacity = pendingConstructionCapacity,
            underConstructionCount = housingUnits.count { !it.isCompleted },
            housingOccupancyRate = occupancy,
            employedCount = employed,
            unemployedCount = unemployed,
            unemploymentRatePercent = unemploymentPct,
            averageHappiness = avgHappy,
            foodStock = foodStock,
            foodCapacity = foodCapacity,
            waterStock = waterStock,
            waterCapacity = waterCapacity,
            suppliesStock = suppliesStock,
            suppliesCapacity = suppliesCapacity,
            dailyFoodNet = foodProd - dailyConsumption,
            dailyWaterNet = waterProd - dailyConsumption,
            dailyWorkerSalaries = salaries,
            dailyHousingMaintenance = housingMaint,
            totalDailyMaintenanceCost = totalMaint,
            estimatedDailyTradeAndLogistics = estRevenue,
            reputation = reputation,
            settlementTierName = tierName
        )
    }
}
