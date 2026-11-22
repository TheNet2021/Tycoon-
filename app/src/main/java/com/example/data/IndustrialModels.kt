package com.example.data

import kotlin.math.pow
import kotlin.math.roundToLong

enum class ProductType(
    val id: String,
    val displayName: String,
    val baseValue: Int,
    val colorHex: Long,
    val isManufactured: Boolean = true
) {
    PLANKS("planks", "Timber Planks", 18, 0xFFD97706),
    WOOD_CRAFT("wood_craft", "Wooden Millwork", 42, 0xFFF59E0B),
    STONE_BLOCKS("stone_blocks", "Stone Blocks", 28, 0xFFCBD5E1),
    ORE_CONCENTRATE("ore_concentrate", "Refined Ore", 68, 0xFFFB923C),
    METAL_BARS("metal_bars", "Metal Bars", 115, 0xFF94A3B8),
    REFINED_GOLD("refined_gold", "Refined Gold", 210, 0xFFFDE047),
    CUT_CRYSTAL("cut_crystal", "Cut Crystal", 340, 0xFF22D3EE),
    INDUSTRIAL_GEAR("industrial_gear", "Alloy Assembly", 580, 0xFFE879F9),
    AETHER_CORE("aether_core", "Aether Core", 920, 0xFF7DD3FC),
    TITAN_PLATE("titan_plate", "Titan Armor Plate", 1650, 0xFF38BDF8);

    companion object {
        fun fromId(id: String): ProductType? = entries.firstOrNull { it.id == id }
    }
}

data class TradeGoodInfo(
    val id: String,
    val displayName: String,
    val baseValue: Int,
    val colorHex: Long,
    val isManufactured: Boolean
)

object TradeCatalog {
    val allGoods: List<TradeGoodInfo> = ResourceType.entries.map {
        TradeGoodInfo(it.id, it.displayName, it.baseValue, it.colorHex, isManufactured = false)
    } + ProductType.entries.map {
        TradeGoodInfo(it.id, it.displayName, it.baseValue, it.colorHex, isManufactured = true)
    }

    fun find(id: String): TradeGoodInfo? = allGoods.firstOrNull { it.id == id }
}

enum class MachineType(
    val id: String,
    val title: String,
    val subtitle: String,
    val inputId: String?,
    val outputId: String,
    val baseCost: Long,
    val costGrowth: Double,
    val baseCycleSeconds: Double,
    val powerCostPerMinute: Int,
    val requiredPlayerLevel: Int,
    val requiredBiomeId: String,
    val accentHex: Long,
    val maxLevel: Int = 6
) {
    WOOD_CUTTER(
        id = "wood_cutter",
        title = "Wood Cutting Machine",
        subtitle = "Cuts timber logs into smooth wooden planks",
        inputId = "wood",
        outputId = "planks",
        baseCost = 140L,
        costGrowth = 2.1,
        baseCycleSeconds = 6.0,
        powerCostPerMinute = 4,
        requiredPlayerLevel = 1,
        requiredBiomeId = "forest",
        accentHex = 0xFFD97706
    ),
    STONE_CRUSHER(
        id = "stone_crusher",
        title = "Stone Crusher",
        subtitle = "Crushes raw stone boulders into masonry blocks",
        inputId = "stone",
        outputId = "stone_blocks",
        baseCost = 220L,
        costGrowth = 2.15,
        baseCycleSeconds = 7.0,
        powerCostPerMinute = 6,
        requiredPlayerLevel = 1,
        requiredBiomeId = "forest",
        accentHex = 0xFF94A3B8
    ),
    CONVEYOR_BELT(
        id = "conveyor_belt",
        title = "Conveyor Belt System",
        subtitle = "+20% speed & +15 warehouse slots per tier across all machines",
        inputId = null,
        outputId = "wood",
        baseCost = 180L,
        costGrowth = 2.2,
        baseCycleSeconds = 12.0,
        powerCostPerMinute = 3,
        requiredPlayerLevel = 1,
        requiredBiomeId = "forest",
        accentHex = 0xFF38BDF8
    ),
    SAWMILL_MACHINE(
        id = "sawmill_machine",
        title = "Precision Sawmill",
        subtitle = "Crafts wooden planks into finished millwork products",
        inputId = "planks",
        outputId = "wood_craft",
        baseCost = 420L,
        costGrowth = 2.2,
        baseCycleSeconds = 8.5,
        powerCostPerMinute = 8,
        requiredPlayerLevel = 2,
        requiredBiomeId = "forest",
        accentHex = 0xFFF59E0B
    ),
    AUTO_MINER_MACHINE(
        id = "auto_miner_machine",
        title = "Automatic Mining Machine",
        subtitle = "Drills raw stone, coal & iron ore directly into warehouse stock",
        inputId = null,
        outputId = "iron",
        baseCost = 520L,
        costGrowth = 2.2,
        baseCycleSeconds = 7.5,
        powerCostPerMinute = 10,
        requiredPlayerLevel = 2,
        requiredBiomeId = "mountains",
        accentHex = 0xFF10B981
    ),
    ORE_PROCESSOR(
        id = "ore_processor",
        title = "Ore Processing Machine",
        subtitle = "Sifts & concentrates raw copper, iron, and gold ores",
        inputId = "copper",
        outputId = "ore_concentrate",
        baseCost = 780L,
        costGrowth = 2.2,
        baseCycleSeconds = 8.0,
        powerCostPerMinute = 12,
        requiredPlayerLevel = 2,
        requiredBiomeId = "mountains",
        accentHex = 0xFFFB923C
    ),
    SMELTING_FURNACE(
        id = "smelting_furnace",
        title = "Smelting Furnace",
        subtitle = "Smelts iron & refined ore into high-strength metal bars",
        inputId = "iron",
        outputId = "metal_bars",
        baseCost = 1250L,
        costGrowth = 2.25,
        baseCycleSeconds = 9.0,
        powerCostPerMinute = 18,
        requiredPlayerLevel = 3,
        requiredBiomeId = "mountains",
        accentHex = 0xFFEF4444
    ),
    GOLD_REFINERY(
        id = "gold_refinery",
        title = "Gold Refinery",
        subtitle = "Purifies raw gold nuggets into 24K gold bullion",
        inputId = "gold",
        outputId = "refined_gold",
        baseCost = 2400L,
        costGrowth = 2.25,
        baseCycleSeconds = 10.0,
        powerCostPerMinute = 24,
        requiredPlayerLevel = 4,
        requiredBiomeId = "desert",
        accentHex = 0xFFFDE047
    ),
    CRYSTAL_PROCESSOR(
        id = "crystal_processor",
        title = "Crystal Processor",
        subtitle = "Precision-cuts raw crystals into high-value faceted prisms",
        inputId = "crystal",
        outputId = "cut_crystal",
        baseCost = 5400L,
        costGrowth = 2.3,
        baseCycleSeconds = 11.0,
        powerCostPerMinute = 32,
        requiredPlayerLevel = 5,
        requiredBiomeId = "frozen",
        accentHex = 0xFF22D3EE
    ),
    INDUSTRIAL_FACTORY(
        id = "industrial_factory",
        title = "Industrial Factory",
        subtitle = "Assembles metal bars & prisms into advanced alloy kits",
        inputId = "metal_bars",
        outputId = "industrial_gear",
        baseCost = 11500L,
        costGrowth = 2.35,
        baseCycleSeconds = 13.0,
        powerCostPerMinute = 45,
        requiredPlayerLevel = 6,
        requiredBiomeId = "volcanic",
        accentHex = 0xFFE879F9
    ),
    CRYSTAL_SYNTHESIZER(
        id = "crystal_synthesizer",
        title = "Prism Core Synthesizer",
        subtitle = "Fuses desert quartz & aether into luminous Aether Cores",
        inputId = "quartz",
        outputId = "aether_core",
        baseCost = 48000L,
        costGrowth = 2.35,
        baseCycleSeconds = 12.0,
        powerCostPerMinute = 65,
        requiredPlayerLevel = 7,
        requiredBiomeId = "crystal_desert",
        accentHex = 0xFFF472B6
    ),
    TITAN_FORGE(
        id = "titan_forge",
        title = "Titan Clockwork Forge",
        subtitle = "Presses obsidian & titan cores into ultra-dense armor plates",
        inputId = "titan_alloy",
        outputId = "titan_plate",
        baseCost = 280000L,
        costGrowth = 2.4,
        baseCycleSeconds = 14.0,
        powerCostPerMinute = 95,
        requiredPlayerLevel = 8,
        requiredBiomeId = "ancient_machine_world",
        accentHex = 0xFF38BDF8
    );

    fun costForNextLevel(currentLevel: Int): Long {
        if (currentLevel >= maxLevel) return 0L
        return (baseCost * costGrowth.pow(currentLevel)).roundToLong()
    }

    fun unitsPerMinute(level: Int, conveyorLevel: Int): Double {
        if (level <= 0) return 0.0
        val lvlBoost = 1.0 + (level - 1) * 0.42
        val beltBoost = 1.0 + conveyorLevel * 0.20
        return (60.0 / baseCycleSeconds) * lvlBoost * beltBoost
    }
}

enum class FleetEquipType(
    val id: String,
    val title: String,
    val subtitle: String,
    val baseCost: Long,
    val costGrowth: Double,
    val accentHex: Long,
    val maxLevel: Int = 5
) {
    CHAINSAW_DRILL(
        id = "chainsaw_drill",
        title = "Chainsaw & Power Drill",
        subtitle = "+25% chop & mine damage + faster swing bursts per tier",
        baseCost = 260L,
        costGrowth = 2.4,
        accentHex = 0xFFF97316
    ),
    MINING_CART(
        id = "mining_cart",
        title = "Automated Mining Cart",
        subtitle = "+25 stockpile capacity & +12% ore yield per tier",
        baseCost = 340L,
        costGrowth = 2.35,
        accentHex = 0xFF94A3B8
    ),
    FORKLIFT(
        id = "forklift",
        title = "Industrial Forklift",
        subtitle = "+18% machine throughput & +20 stockpile slots per tier",
        baseCost = 580L,
        costGrowth = 2.35,
        accentHex = 0xFFFBBF24
    ),
    DELIVERY_TRUCK(
        id = "delivery_truck",
        title = "Heavy Delivery Truck",
        subtitle = "+18% contract payouts & +1 active contract slot (Lv.2+)",
        baseCost = 900L,
        costGrowth = 2.4,
        accentHex = 0xFF38BDF8
    ),
    EXCAVATOR(
        id = "excavator",
        title = "Heavy Excavator Rig",
        subtitle = "+22% mining output & +15% automated extraction per tier",
        baseCost = 1800L,
        costGrowth = 2.45,
        accentHex = 0xFF10B981
    ),
    STORAGE_CONTAINER(
        id = "storage_container",
        title = "Cargo Containers",
        subtitle = "+45 industrial warehouse storage capacity per tier",
        baseCost = 210L,
        costGrowth = 2.2,
        accentHex = 0xFFE879F9
    );

    fun costForNextLevel(currentLevel: Int): Long {
        if (currentLevel >= maxLevel) return 0L
        return (baseCost * costGrowth.pow(currentLevel)).roundToLong()
    }
}

enum class DrivableVehicleType(
    val id: String,
    val title: String,
    val subtitle: String,
    val cost: Long,
    val accentHex: Long,
    val speedMultiplier: Double,
    val damageReduction: Double,
    val requiredBiomeId: String
) {
    MINING_ROVER(
        id = "mining_rover",
        title = "Small Mining Rover",
        subtitle = "Nimble 4x4 rover with nose drill (+35% speed, +20% mining cadence)",
        cost = 850L,
        accentHex = 0xFF38BDF8,
        speedMultiplier = 1.35,
        damageReduction = 0.25,
        requiredBiomeId = "forest"
    ),
    HEAVY_HAULER(
        id = "heavy_hauler",
        title = "Heavy Mining Transport",
        subtitle = "Armored 6-wheel dump hauler (+45 bag capacity, 50% insect armor)",
        cost = 3200L,
        accentHex = 0xFFF59E0B,
        speedMultiplier = 1.20,
        damageReduction = 0.50,
        requiredBiomeId = "mountains"
    ),
    EXPLORER_BUGGY(
        id = "explorer_buggy",
        title = "Exploration Vehicle",
        subtitle = "All-terrain radar buggy (+55% speed, wide cave & crystal detector)",
        cost = 7500L,
        accentHex = 0xFF10B981,
        speedMultiplier = 1.55,
        damageReduction = 0.35,
        requiredBiomeId = "desert"
    );

    companion object {
        fun fromId(id: String?): DrivableVehicleType? = entries.firstOrNull { it.id == id }
    }
}

enum class BuyerCompany(
    val id: String,
    val companyName: String,
    val categoryLabel: String,
    val tagline: String,
    val accentHex: Long,
    val badgeCode: String,
    val preferredGoods: Set<String>,
    val specialtyMultiplier: Double,
    val unlockLevel: Int,
    val unlockBiomeId: String
) {
    OAKHEART_TIMBER(
        id = "oakheart",
        companyName = "Oakheart Timber Co.",
        categoryLabel = "Wood Merchant",
        tagline = "Steady buyer of logs, planks & crafted millwork",
        accentHex = 0xFFD97706,
        badgeCode = "OT",
        preferredGoods = setOf("wood", "planks", "wood_craft"),
        specialtyMultiplier = 1.25,
        unlockLevel = 1,
        unlockBiomeId = "forest"
    ),
    GRANITE_BUILDERS(
        id = "granite",
        companyName = "Granite & Co. Builders",
        categoryLabel = "Construction Company",
        tagline = "High-volume stone blocks, iron & structural timber",
        accentHex = 0xFF94A3B8,
        badgeCode = "GB",
        preferredGoods = setOf("stone", "stone_blocks", "planks", "iron"),
        specialtyMultiplier = 1.30,
        unlockLevel = 2,
        unlockBiomeId = "forest"
    ),
    VULCAN_FOUNDRY(
        id = "vulcan",
        companyName = "Vulcan Foundry Corp.",
        categoryLabel = "Metal Factory",
        tagline = "Smelts copper, coal, iron & refined ore concentrates",
        accentHex = 0xFFFB923C,
        badgeCode = "VF",
        preferredGoods = setOf("coal", "copper", "iron", "ore_concentrate", "metal_bars"),
        specialtyMultiplier = 1.38,
        unlockLevel = 3,
        unlockBiomeId = "mountains"
    ),
    AURELIA_JEWELS(
        id = "aurelia",
        companyName = "Aurelia Fine Jewels",
        categoryLabel = "Jewelry Company",
        tagline = "Pays top rates for 24K gold bullion & cut crystals",
        accentHex = 0xFFFDE047,
        badgeCode = "AJ",
        preferredGoods = setOf("gold", "refined_gold", "crystal", "cut_crystal"),
        specialtyMultiplier = 1.48,
        unlockLevel = 4,
        unlockBiomeId = "desert"
    ),
    APEX_INDUSTRIES(
        id = "apex",
        companyName = "Apex Heavy Industries",
        categoryLabel = "Industrial Buyer",
        tagline = "Sources alloy kits, metal bars & precision crystal cores",
        accentHex = 0xFF38BDF8,
        badgeCode = "AI",
        preferredGoods = setOf("metal_bars", "cut_crystal", "industrial_gear"),
        specialtyMultiplier = 1.55,
        unlockLevel = 5,
        unlockBiomeId = "frozen"
    ),
    GLOBAL_HORIZON(
        id = "global_horizon",
        companyName = "Global Horizon Exports",
        categoryLabel = "International Buyer",
        tagline = "Massive overseas bulk contracts with premium payouts",
        accentHex = 0xFFE879F9,
        badgeCode = "GH",
        preferredGoods = setOf("refined_gold", "cut_crystal", "industrial_gear", "rare"),
        specialtyMultiplier = 1.70,
        unlockLevel = 6,
        unlockBiomeId = "volcanic"
    );

    companion object {
        fun fromId(id: String): BuyerCompany? = entries.firstOrNull { it.id == id }
    }
}

enum class ContractTier(val label: String, val bonusMult: Double, val durationSeconds: Long) {
    SMALL("Small Order", 1.15, 0L),
    MEDIUM("Medium Order", 1.35, 0L),
    RUSH("Rush Timed", 1.65, 120L),
    LARGE("Bulk Contract", 1.85, 0L),
    LONG_TERM("Long-Term Deal", 2.15, 300L)
}

data class ContractOrder(
    val id: String,
    val buyerId: String,
    val goodId: String,
    val quantity: Int,
    val tier: ContractTier,
    val rewardCoins: Long,
    val rewardXp: Long,
    val rewardRep: Int,
    val acceptedEpochMs: Long = 0L,
    val deadlineEpochMs: Long = 0L
) {
    fun isExpired(nowMs: Long): Boolean = deadlineEpochMs > 0L && nowMs > deadlineEpochMs

    fun remainingSeconds(nowMs: Long): Long =
        if (deadlineEpochMs <= 0L) -1L else ((deadlineEpochMs - nowMs) / 1000L).coerceAtLeast(0L)
}

object ContractGenerator {
    fun generateOffers(state: TycoonSaveState, seedOffset: Long = 0L): List<ContractOrder> {
        val unlockedBuyers = BuyerCompany.entries.filter { state.isBuyerUnlocked(it) }
        if (unlockedBuyers.isEmpty()) return emptyList()
        val orders = mutableListOf<ContractOrder>()
        val salt = ((System.currentTimeMillis() / 30_000L) + seedOffset).toInt()
        unlockedBuyers.forEachIndexed { idx, buyer ->
            val goodsList = buyer.preferredGoods.toList()
            val goodId = goodsList[(idx + kotlin.math.abs(salt)) % goodsList.size]
            val good = TradeCatalog.find(goodId) ?: return@forEachIndexed
            val tier = when {
                buyer == BuyerCompany.GLOBAL_HORIZON -> ContractTier.LONG_TERM
                idx % 4 == 0 -> ContractTier.SMALL
                idx % 4 == 1 -> ContractTier.RUSH
                idx % 4 == 2 -> ContractTier.MEDIUM
                else -> ContractTier.LARGE
            }
            val qtyBase = when (tier) {
                ContractTier.SMALL -> 8
                ContractTier.MEDIUM -> 16
                ContractTier.RUSH -> 12
                ContractTier.LARGE -> 28
                ContractTier.LONG_TERM -> 40
            }
            val qty = qtyBase + (state.playerLevel * 2)
            val truckMult = 1.0 + state.fleetLevel(FleetEquipType.DELIVERY_TRUCK) * 0.18
            val repMult = 1.0 + state.buyerReputationLevel(buyer) * 0.08
            val rewardCoins = (qty * good.baseValue * buyer.specialtyMultiplier * tier.bonusMult * truckMult * repMult).roundToLong()
            val rewardXp = (qty * 4L) + when (tier) {
                ContractTier.SMALL -> 35L
                ContractTier.MEDIUM -> 70L
                ContractTier.RUSH -> 110L
                ContractTier.LARGE -> 160L
                ContractTier.LONG_TERM -> 240L
            }
            val rewardRep = when (tier) {
                ContractTier.SMALL -> 15
                ContractTier.MEDIUM -> 25
                ContractTier.RUSH -> 40
                ContractTier.LARGE -> 55
                ContractTier.LONG_TERM -> 80
            }
            orders += ContractOrder(
                id = "c_${buyer.id}_${goodId}_${tier.name.lowercase()}_${kotlin.math.abs(salt % 997)}",
                buyerId = buyer.id,
                goodId = goodId,
                quantity = qty,
                tier = tier,
                rewardCoins = rewardCoins,
                rewardXp = rewardXp,
                rewardRep = rewardRep
            )
        }
        return orders
    }
}
