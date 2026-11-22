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
    val isCompleted: Boolean
) {
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
}

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
    val passiveWaterBonus: Int = 0
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
    val totalHousingCapacity: Int
        get() = housingUnits.filter { it.isCompleted }.sumOf { it.type.capacity }

    val pendingConstructionCapacity: Int
        get() = housingUnits.filter { !it.isCompleted }.sumOf { it.type.capacity }

    val availableHousingSlots: Int
        get() = (totalHousingCapacity - survivors.size).coerceAtLeast(0)

    val foodCapacity: Int
        get() = 50 + (totalHousingCapacity * 2) + bonusFoodCap

    val waterCapacity: Int
        get() = 50 + (totalHousingCapacity * 2) + bonusWaterCap

    val suppliesCapacity: Int
        get() = 40 + totalHousingCapacity

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
        val clampedGlobal = globalMarketMult.coerceIn(0.85, 1.30)
        // Strictly capped around 0.65x..1.25x baseValue to prevent infinite money loops
        return (good.baseValue * localDemand * repFactor * (0.92 + (clampedGlobal - 1.0) * 0.35))
            .roundToLong()
            .coerceAtLeast(1L)
    }

    fun survivorAskPrice(good: TradeGoodInfo, globalMarketMult: Double): Long {
        val bid = survivorBidPrice(good, globalMarketMult)
        // Ask is always strictly higher than bid (minimum +38% spread + 2 coins) so buy->sell arbitrage cannot profit
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

        val farmers = survivors.filter { it.assignedJob == SurvivorProfession.FARMER }
        val foodProd = farmers.sumOf { (2.6 * it.effectiveEfficiencyMultiplier()).roundToInt() } + bonusDailyFoodProd
        val waterProd = farmers.sumOf { (2.4 * it.effectiveEfficiencyMultiplier()).roundToInt() } + bonusDailyWaterProd
        val dailyConsumption = pop

        val salaries = survivors.sumOf { it.dailySalary() }
        val housingMaint = housingUnits.filter { it.isCompleted }.sumOf { it.type.dailyMaintenanceCost }
        val totalMaint = salaries + housingMaint

        val tradersAndTransport = survivors.filter {
            it.assignedJob == SurvivorProfession.TRADER || it.assignedJob == SurvivorProfession.TRANSPORT_WORKER
        }
        val estRevenue = tradersAndTransport.sumOf { worker ->
            val base = if (worker.assignedJob == SurvivorProfession.TRADER) 14.0 else 11.0
            (base * worker.effectiveEfficiencyMultiplier()).roundToLong()
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
