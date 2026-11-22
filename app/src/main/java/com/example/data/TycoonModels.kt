package com.example.data

import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToLong

enum class ResourceType(
    val id: String,
    val displayName: String,
    val baseValue: Int,
    val colorHex: Long
) {
    WOOD("wood", "Wood", 6, 0xFFB45309),
    STONE("stone", "Stone", 10, 0xFF94A3B8),
    COAL("coal", "Coal", 16, 0xFF475569),
    COPPER("copper", "Copper", 26, 0xFFEA580C),
    IRON("iron", "Iron", 38, 0xFFCBD5E1),
    GOLD("gold", "Gold", 65, 0xFFFBBF24),
    CRYSTAL("crystal", "Crystal", 110, 0xFF22D3EE),
    RARE("rare", "Rare Relic", 220, 0xFFE879F9),
    QUARTZ("quartz", "Prism Quartz", 145, 0xFFF472B6),
    AETHER("aether", "Aetherium", 185, 0xFF7DD3FC),
    VENOM("venom", "Bio-Resin", 240, 0xFF84CC16),
    FROSTITE("frostite", "Frostite", 310, 0xFF67E8F9),
    OBSIDIAN("obsidian", "Obsidian", 390, 0xFFA855F7),
    TITAN_ALLOY("titan_alloy", "Titan Core", 520, 0xFF38BDF8);

    companion object {
        fun fromId(id: String): ResourceType? = entries.firstOrNull { it.id == id }
    }
}

data class UpgradeStatMetric(
    val label: String,
    val currentText: String,
    val nextText: String?,
    val improvementPct: Int
)

data class ResourceRequirement(
    val resource: ResourceType,
    val requiredTotal: Int
)

enum class UpgradeType(
    val id: String,
    val title: String,
    val subtitle: String,
    val maxLevel: Int = 8
) {
    AXE("axe", "Felling Axe", "Chopping damage, swing speed & timber yield"),
    PICKAXE("pickaxe", "Mining Pickaxe", "Mining power, strike speed & rare ore extraction"),
    BACKPACK("backpack", "Cargo Backpack", "Carrying capacity & resource haul efficiency"),
    MOVE_SPEED("moveSpeed", "Explorer Stride", "Walking speed & trail XP bonus"),
    GATHER_SPEED("gatherSpeed", "Harvester Gloves", "Automatic gathering cadence & efficiency");

    fun materialTierLabel(level: Int): String = when (level.coerceIn(1, 8)) {
        1 -> "Wooden"
        2 -> "Stone"
        3 -> "Copper"
        4 -> "Iron"
        5 -> "Steel"
        6 -> "Gold"
        7 -> "Diamond"
        else -> "Legendary"
    }

    fun tierName(level: Int): String = when (this) {
        AXE -> when (level.coerceIn(1, 8)) {
            1 -> "Wooden Hatchet"
            2 -> "Stone Cleaver"
            3 -> "Copper Broadaxe"
            4 -> "Iron Felling Axe"
            5 -> "Tempered Steel Axe"
            6 -> "Golden Timber Splitter"
            7 -> "Diamond Twin-Bit"
            else -> "Legendary Astral Cleaver"
        }
        PICKAXE -> when (level.coerceIn(1, 8)) {
            1 -> "Wooden Mallet-Pick"
            2 -> "Stone Quarry Pick"
            3 -> "Copper Prospector"
            4 -> "Iron Striker Pick"
            5 -> "Tempered Steel Pick"
            6 -> "Golden Drill-Pick"
            7 -> "Diamond Prism Pick"
            else -> "Legendary Void Bore"
        }
        BACKPACK -> when (level.coerceIn(1, 8)) {
            1 -> "Wooden Frame Sack"
            2 -> "Stone-Trimmed Pack"
            3 -> "Copper Carrier Rig"
            4 -> "Iron Frame Backpack"
            5 -> "Steel Expedition Pack"
            6 -> "Golden Twin-Hopper"
            7 -> "Diamond Vault Pack"
            else -> "Legendary Quantum Core"
        }
        MOVE_SPEED -> when (level.coerceIn(1, 8)) {
            1 -> "Wooden Clogs"
            2 -> "Stone-Soled Boots"
            3 -> "Copper Trail Runners"
            4 -> "Iron Greaves"
            5 -> "Steel Striders"
            6 -> "Golden Wingboots"
            7 -> "Diamond Thrusters"
            else -> "Legendary Hyper Stride"
        }
        GATHER_SPEED -> when (level.coerceIn(1, 8)) {
            1 -> "Wooden Grip Wraps"
            2 -> "Stone Knuckle Grips"
            3 -> "Copper Reflex Bracers"
            4 -> "Iron Power Gauntlets"
            5 -> "Steel Servo Gloves"
            6 -> "Golden Haste Cuffs"
            7 -> "Diamond Pulse Arms"
            else -> "Legendary Overdrive"
        }
    }

    fun categoryLabel(): String = when (this) {
        AXE -> "Axe"
        PICKAXE -> "Pickaxe"
        BACKPACK -> "Backpack"
        MOVE_SPEED, GATHER_SPEED -> "Character"
    }

    fun costForNextLevel(currentLevel: Int): Long {
        if (currentLevel >= maxLevel) return 0L
        return when (this) {
            AXE, PICKAXE -> (50.0 * 2.35.pow(currentLevel - 1)).roundToLong()
            BACKPACK -> (40.0 * 2.4.pow(currentLevel - 1)).roundToLong()
            MOVE_SPEED -> (45.0 * 2.35.pow(currentLevel - 1)).roundToLong()
            GATHER_SPEED -> (60.0 * 2.35.pow(currentLevel - 1)).roundToLong()
        }
    }

    fun resourceRequirementForNext(currentLevel: Int): ResourceRequirement? {
        val next = currentLevel + 1
        if (next > maxLevel) return null
        return when (this) {
            AXE -> when (next) {
                2 -> ResourceRequirement(ResourceType.WOOD, 8)
                3 -> ResourceRequirement(ResourceType.STONE, 10)
                4 -> ResourceRequirement(ResourceType.COPPER, 12)
                5 -> ResourceRequirement(ResourceType.IRON, 18)
                6 -> ResourceRequirement(ResourceType.GOLD, 18)
                7 -> ResourceRequirement(ResourceType.CRYSTAL, 20)
                8 -> ResourceRequirement(ResourceType.RARE, 14)
                else -> null
            }
            PICKAXE -> when (next) {
                2 -> ResourceRequirement(ResourceType.STONE, 8)
                3 -> ResourceRequirement(ResourceType.COAL, 10)
                4 -> ResourceRequirement(ResourceType.COPPER, 14)
                5 -> ResourceRequirement(ResourceType.IRON, 20)
                6 -> ResourceRequirement(ResourceType.GOLD, 20)
                7 -> ResourceRequirement(ResourceType.CRYSTAL, 22)
                8 -> ResourceRequirement(ResourceType.RARE, 16)
                else -> null
            }
            BACKPACK -> when (next) {
                2 -> ResourceRequirement(ResourceType.WOOD, 10)
                3 -> ResourceRequirement(ResourceType.STONE, 14)
                4 -> ResourceRequirement(ResourceType.COPPER, 12)
                5 -> ResourceRequirement(ResourceType.IRON, 16)
                6 -> ResourceRequirement(ResourceType.GOLD, 15)
                7 -> ResourceRequirement(ResourceType.CRYSTAL, 18)
                8 -> ResourceRequirement(ResourceType.RARE, 12)
                else -> null
            }
            MOVE_SPEED, GATHER_SPEED -> when (next) {
                2 -> ResourceRequirement(ResourceType.WOOD, 8)
                3 -> ResourceRequirement(ResourceType.STONE, 12)
                4 -> ResourceRequirement(ResourceType.COAL, 12)
                5 -> ResourceRequirement(ResourceType.IRON, 15)
                6 -> ResourceRequirement(ResourceType.GOLD, 16)
                7 -> ResourceRequirement(ResourceType.CRYSTAL, 18)
                8 -> ResourceRequirement(ResourceType.RARE, 12)
                else -> null
            }
        }
    }

    fun statComparisonLines(level: Int): List<UpgradeStatMetric> {
        val cur = level.coerceIn(1, maxLevel)
        val nxt = (cur + 1).coerceAtMost(maxLevel)
        val isMax = cur >= maxLevel
        return when (this) {
            AXE -> {
                val damages = listOf(1, 2, 3, 5, 7, 9, 12, 16)
                val curDmg = damages[cur - 1]
                val nxtDmg = damages[nxt - 1]
                val curSpd = 1.0 + (cur - 1) * 0.24
                val nxtSpd = 1.0 + (nxt - 1) * 0.24
                val curWood = 1 + (cur - 1) / 2
                val nxtWood = 1 + (nxt - 1) / 2
                listOf(
                    UpgradeStatMetric("Chop Damage", "$curDmg", if (isMax) null else "$nxtDmg", pctDelta(curDmg.toDouble(), nxtDmg.toDouble())),
                    UpgradeStatMetric("Attack Speed", "${String.format(Locale.US, "%.2f", curSpd)}x", if (isMax) null else "${String.format(Locale.US, "%.2f", nxtSpd)}x", pctDelta(curSpd, nxtSpd)),
                    UpgradeStatMetric("Wood Yield", "$curWood/tree", if (isMax) null else "$nxtWood/tree", pctDelta(curWood.toDouble(), nxtWood.toDouble()))
                )
            }
            PICKAXE -> {
                val damages = listOf(1, 2, 3, 5, 7, 9, 12, 16)
                val curDmg = damages[cur - 1]
                val nxtDmg = damages[nxt - 1]
                val curSpd = 1.0 + (cur - 1) * 0.24
                val nxtSpd = 1.0 + (nxt - 1) * 0.24
                val curRare = (cur - 1) * 15
                val nxtRare = (nxt - 1) * 15
                listOf(
                    UpgradeStatMetric("Mining Power", "$curDmg", if (isMax) null else "$nxtDmg", pctDelta(curDmg.toDouble(), nxtDmg.toDouble())),
                    UpgradeStatMetric("Mining Speed", "${String.format(Locale.US, "%.2f", curSpd)}x", if (isMax) null else "${String.format(Locale.US, "%.2f", nxtSpd)}x", pctDelta(curSpd, nxtSpd)),
                    UpgradeStatMetric("Rare Ore Yield", "+$curRare%", if (isMax) null else "+$nxtRare%", if (isMax) 0 else 15)
                )
            }
            BACKPACK -> {
                val caps = listOf(12, 22, 36, 56, 84, 126, 184, 260)
                val curCap = caps[cur - 1]
                val nxtCap = caps[nxt - 1]
                val curEff = 100 + (cur - 1) * 10
                val nxtEff = 100 + (nxt - 1) * 10
                listOf(
                    UpgradeStatMetric("Carry Capacity", "$curCap", if (isMax) null else "$nxtCap", pctDelta(curCap.toDouble(), nxtCap.toDouble())),
                    UpgradeStatMetric("Haul Efficiency", "$curEff%", if (isMax) null else "$nxtEff%", pctDelta(curEff.toDouble(), nxtEff.toDouble()))
                )
            }
            MOVE_SPEED -> {
                val speeds = listOf(6.4, 7.4, 8.5, 9.7, 11.0, 12.4, 13.9, 15.6)
                val curSpd = speeds[cur - 1]
                val nxtSpd = speeds[nxt - 1]
                val curXp = 100 + (cur - 1) * 10
                val nxtXp = 100 + (nxt - 1) * 10
                listOf(
                    UpgradeStatMetric("Walk Speed", "${String.format(Locale.US, "%.1f", curSpd)} m/s", if (isMax) null else "${String.format(Locale.US, "%.1f", nxtSpd)} m/s", pctDelta(curSpd, nxtSpd)),
                    UpgradeStatMetric("XP Gain Bonus", "$curXp%", if (isMax) null else "$nxtXp%", pctDelta(curXp.toDouble(), nxtXp.toDouble()))
                )
            }
            GATHER_SPEED -> {
                val rates = listOf(1.0, 1.25, 1.55, 1.9, 2.3, 2.75, 3.25, 3.85)
                val curR = rates[cur - 1]
                val nxtR = rates[nxt - 1]
                val curEff = 100 + (cur - 1) * 15
                val nxtEff = 100 + (nxt - 1) * 15
                listOf(
                    UpgradeStatMetric("Gather Speed", "${String.format(Locale.US, "%.2f", curR)}x", if (isMax) null else "${String.format(Locale.US, "%.2f", nxtR)}x", pctDelta(curR, nxtR)),
                    UpgradeStatMetric("Gather Efficiency", "$curEff%", if (isMax) null else "$nxtEff%", pctDelta(curEff.toDouble(), nxtEff.toDouble()))
                )
            }
        }
    }
}

enum class ResourceUpgradeType(
    val id: String,
    val title: String,
    val subtitle: String,
    val accentHex: Long,
    val maxLevel: Int = 8
) {
    TRADE_VALUE(
        id = "tradeValue",
        title = "Market Valuation",
        subtitle = "Boosts selling price for all gathered resources",
        accentHex = 0xFFFBBF24
    ),
    GATHER_EFFICIENCY(
        id = "gatherEfficiency",
        title = "Harvest Mastery",
        subtitle = "Increases bonus yield chance on every felled node",
        accentHex = 0xFF34D399
    ),
    RARE_DISCOVERY(
        id = "rareDiscovery",
        title = "Prospecting Luck",
        subtitle = "Increases chance to extract bonus rare ores & relics",
        accentHex = 0xFFE879F9
    ),
    STORAGE_CAPACITY(
        id = "storageCapacity",
        title = "Logistics Storage",
        subtitle = "Expands carry capacity & warehouse buffer slots",
        accentHex = 0xFF38BDF8
    ),
    AUTO_PRODUCTION(
        id = "autoProduction",
        title = "Automation Overclock",
        subtitle = "Accelerates factory & auto-miner production speed",
        accentHex = 0xFFFB923C
    );

    fun costForNextLevel(currentLevel: Int): Long {
        if (currentLevel >= maxLevel) return 0L
        val base = when (this) {
            TRADE_VALUE -> 85.0
            GATHER_EFFICIENCY -> 75.0
            RARE_DISCOVERY -> 110.0
            STORAGE_CAPACITY -> 70.0
            AUTO_PRODUCTION -> 140.0
        }
        return (base * 2.25.pow(currentLevel - 1)).roundToLong()
    }

    fun resourceRequirementForNext(currentLevel: Int): ResourceRequirement? {
        val next = currentLevel + 1
        if (next > maxLevel) return null
        return when (next) {
            2 -> ResourceRequirement(ResourceType.WOOD, 12)
            3 -> ResourceRequirement(ResourceType.STONE, 16)
            4 -> ResourceRequirement(ResourceType.COAL, 14)
            5 -> ResourceRequirement(ResourceType.IRON, 18)
            6 -> ResourceRequirement(ResourceType.GOLD, 18)
            7 -> ResourceRequirement(ResourceType.CRYSTAL, 20)
            8 -> ResourceRequirement(ResourceType.RARE, 14)
            else -> null
        }
    }

    fun statComparisonLines(level: Int): List<UpgradeStatMetric> {
        val cur = level.coerceIn(1, maxLevel)
        val nxt = (cur + 1).coerceAtMost(maxLevel)
        val isMax = cur >= maxLevel
        return when (this) {
            TRADE_VALUE -> {
                val curPct = 100 + (cur - 1) * 15
                val nxtPct = 100 + (nxt - 1) * 15
                listOf(
                    UpgradeStatMetric("Sell Multiplier", "$curPct%", if (isMax) null else "$nxtPct%", pctDelta(curPct.toDouble(), nxtPct.toDouble()))
                )
            }
            GATHER_EFFICIENCY -> {
                val curBonus = (cur - 1) * 12
                val nxtBonus = (nxt - 1) * 12
                listOf(
                    UpgradeStatMetric("Bonus Drop Chance", "+$curBonus%", if (isMax) null else "+$nxtBonus%", if (isMax) 0 else 12)
                )
            }
            RARE_DISCOVERY -> {
                val curLuck = (cur - 1) * 8
                val nxtLuck = (nxt - 1) * 8
                listOf(
                    UpgradeStatMetric("Rare Find Chance", "+$curLuck%", if (isMax) null else "+$nxtLuck%", if (isMax) 0 else 8)
                )
            }
            STORAGE_CAPACITY -> {
                val curSlots = (cur - 1) * 6
                val nxtSlots = (nxt - 1) * 6
                val curPct = 100 + (cur - 1) * 10
                val nxtPct = 100 + (nxt - 1) * 10
                listOf(
                    UpgradeStatMetric("Bonus Bag Slots", "+$curSlots", if (isMax) null else "+$nxtSlots", if (isMax) 0 else 15),
                    UpgradeStatMetric("Depot Multiplier", "$curPct%", if (isMax) null else "$nxtPct%", pctDelta(curPct.toDouble(), nxtPct.toDouble()))
                )
            }
            AUTO_PRODUCTION -> {
                val curSpd = 100 + (cur - 1) * 20
                val nxtSpd = 100 + (nxt - 1) * 20
                listOf(
                    UpgradeStatMetric("Factory Output", "$curSpd%", if (isMax) null else "$nxtSpd%", pctDelta(curSpd.toDouble(), nxtSpd.toDouble()))
                )
            }
        }
    }
}

private fun pctDelta(cur: Double, nxt: Double): Int {
    if (cur <= 0.0 || nxt <= cur) return 0
    return (((nxt - cur) / cur) * 100.0).roundToLong().toInt()
}

enum class BuildingType(
    val id: String,
    val title: String,
    val description: String,
    val biomeId: String,
    val baseCost: Double,
    val costGrowth: Double,
    val passivePerLevel: Double,
    val maxLevel: Int = 8,
    val maxWorkers: Int = 4,
    val maxLines: Int = 3
) {
    SAWMILL(
        id = "sawmill",
        title = "Timber Sawmill",
        description = "+45% Wood value, faster plank lines & +$4/s per tier",
        biomeId = "forest",
        baseCost = 120.0,
        costGrowth = 2.25,
        passivePerLevel = 4.0
    ),
    WAREHOUSE(
        id = "warehouse",
        title = "Grand Warehouse",
        description = "+20% all sell prices & +8 storage capacity per tier",
        biomeId = "forest",
        baseCost = 180.0,
        costGrowth = 2.3,
        passivePerLevel = 2.0
    ),
    SMELTER(
        id = "smelter",
        title = "Ore Smelter",
        description = "+45% mineral/ore value & +$10/s per tier",
        biomeId = "mountains",
        baseCost = 320.0,
        costGrowth = 2.3,
        passivePerLevel = 10.0
    ),
    MINER_FOREST(
        id = "miner_forest",
        title = "Forest Lumber Bot",
        description = "Automated timber collector (+$3/s base per tier)",
        biomeId = "forest",
        baseCost = 90.0,
        costGrowth = 2.15,
        passivePerLevel = 3.0
    ),
    MINER_MOUNTAINS(
        id = "miner_mountains",
        title = "Mountain Drill Rig",
        description = "Automated iron & coal drill (+$12/s base per tier)",
        biomeId = "mountains",
        baseCost = 350.0,
        costGrowth = 2.15,
        passivePerLevel = 12.0
    ),
    MINER_DESERT(
        id = "miner_desert",
        title = "Solar Ore Extractor",
        description = "Automated copper & gold rig (+$30/s base per tier)",
        biomeId = "desert",
        baseCost = 950.0,
        costGrowth = 2.15,
        passivePerLevel = 30.0
    ),
    MINER_FROZEN(
        id = "miner_frozen",
        title = "Cryo Crystal Harvester",
        description = "Automated frost crystal rig (+$75/s base per tier)",
        biomeId = "frozen",
        baseCost = 2800.0,
        costGrowth = 2.15,
        passivePerLevel = 75.0
    ),
    MINER_VOLCANIC(
        id = "miner_volcanic",
        title = "Magma Core Extractor",
        description = "Automated obsidian & core rig (+$190/s base per tier)",
        biomeId = "volcanic",
        baseCost = 8000.0,
        costGrowth = 2.15,
        passivePerLevel = 190.0
    ),
    MINER_JUNGLE(
        id = "miner_jungle",
        title = "Relic Synthesizer",
        description = "Automated ancient relic rig (+$450/s base per tier)",
        biomeId = "jungle",
        baseCost = 22000.0,
        costGrowth = 2.15,
        passivePerLevel = 450.0
    ),
    MINER_CRYSTAL_DESERT(
        id = "miner_crystal_desert",
        title = "Prism Siphon",
        description = "Automated quartz & desert prism extractor (+$900/s base)",
        biomeId = "crystal_desert",
        baseCost = 48000.0,
        costGrowth = 2.15,
        passivePerLevel = 900.0
    ),
    MINER_SKY_ISLANDS(
        id = "miner_sky_islands",
        title = "Cloud Turbine",
        description = "Automated aetherium wind condenser (+$1.8K/s base)",
        biomeId = "sky_islands",
        baseCost = 110000.0,
        costGrowth = 2.15,
        passivePerLevel = 1800.0
    ),
    MINER_TOXIC_SWAMP(
        id = "miner_toxic_swamp",
        title = "Bio-Resin Purifier",
        description = "Automated swamp venom refinery (+$3.4K/s base)",
        biomeId = "toxic_swamp",
        baseCost = 220000.0,
        costGrowth = 2.15,
        passivePerLevel = 3400.0
    ),
    MINER_FROZEN_MOUNTAINS(
        id = "miner_frozen_mountains",
        title = "Glacier Bore Rig",
        description = "Automated alpine frostite harvester (+$6.2K/s base)",
        biomeId = "frozen_mountains",
        baseCost = 420000.0,
        costGrowth = 2.15,
        passivePerLevel = 6200.0
    ),
    MINER_VOLCANIC_LANDS(
        id = "miner_volcanic_lands",
        title = "Caldera Forge",
        description = "Automated obsidian magma tap (+$11.5K/s base)",
        biomeId = "volcanic_lands",
        baseCost = 780000.0,
        costGrowth = 2.15,
        passivePerLevel = 11500.0
    ),
    MINER_ANCIENT_MACHINE(
        id = "miner_ancient_machine",
        title = "Titan Dynamo",
        description = "Automated clockwork core reactor (+$22K/s base)",
        biomeId = "ancient_machine_world",
        baseCost = 1450000.0,
        costGrowth = 2.15,
        passivePerLevel = 22000.0
    );

    fun costForNextLevel(currentLevel: Int): Long {
        if (currentLevel >= maxLevel) return 0L
        return (baseCost * costGrowth.pow(currentLevel)).roundToLong()
    }

    fun costForNextWorker(currentWorkers: Int): Long {
        if (currentWorkers >= maxWorkers) return 0L
        return (baseCost * 0.75 * 1.9.pow(currentWorkers)).roundToLong()
    }

    fun costForNextLine(currentLines: Int): Long {
        if (currentLines >= maxLines) return 0L
        return (baseCost * 1.35 * 2.3.pow(currentLines)).roundToLong()
    }
}

enum class BiomeType(
    val id: String,
    val title: String,
    val subtitle: String,
    val unlockCost: Long,
    val accentHex: Long,
    val resourcesSummary: String,
    val spawnX: Double,
    val spawnZ: Double,
    val requiredAxeLevel: Int = 1,
    val requiredPickaxeLevel: Int = 1,
    val requiredBackpackLevel: Int = 1
) {
    FOREST(
        id = "forest",
        title = "Green Forest",
        subtitle = "Lush pine groves, Trading Post & Timber Sawmill",
        unlockCost = 0L,
        accentHex = 0xFF10B981,
        resourcesSummary = "Wood, Stone",
        spawnX = 0.0,
        spawnZ = 4.0
    ),
    MOUNTAINS(
        id = "mountains",
        title = "Rocky Mountains",
        subtitle = "Slate ridges rich in iron, coal & the Ore Smelter",
        unlockCost = 150L,
        accentHex = 0xFF64748B,
        resourcesSummary = "Stone, Coal, Iron",
        spawnX = 0.0,
        spawnZ = -26.0,
        requiredAxeLevel = 2,
        requiredPickaxeLevel = 2
    ),
    DESERT(
        id = "desert",
        title = "Golden Desert",
        subtitle = "Sunlit sandstone mesas with copper & gold seams",
        unlockCost = 650L,
        accentHex = 0xFFF59E0B,
        resourcesSummary = "Copper, Gold, Stone",
        spawnX = 26.0,
        spawnZ = 0.0,
        requiredPickaxeLevel = 3
    ),
    FROZEN(
        id = "frozen",
        title = "Frozen Lands",
        subtitle = "Aurora ice spires sparkling with pure crystals",
        unlockCost = 2000L,
        accentHex = 0xFF38BDF8,
        resourcesSummary = "Crystals, Iron, Wood",
        spawnX = -26.0,
        spawnZ = 0.0,
        requiredPickaxeLevel = 4,
        requiredBackpackLevel = 3
    ),
    VOLCANIC(
        id = "volcanic",
        title = "Volcanic Region",
        subtitle = "Basalt caldera hiding magma cores & gold veins",
        unlockCost = 6500L,
        accentHex = 0xFFEF4444,
        resourcesSummary = "Coal, Gold, Magma Cores",
        spawnX = 0.0,
        spawnZ = 26.0,
        requiredPickaxeLevel = 5,
        requiredBackpackLevel = 4
    ),
    JUNGLE(
        id = "jungle",
        title = "Ancient Jungle",
        subtitle = "Mossy temple ruins brimming with rare relics",
        unlockCost = 18000L,
        accentHex = 0xFF14B8A6,
        resourcesSummary = "Wood, Crystals, Ancient Relics",
        spawnX = 26.0,
        spawnZ = -26.0,
        requiredAxeLevel = 6,
        requiredPickaxeLevel = 6
    ),
    CRYSTAL_DESERT(
        id = "crystal_desert",
        title = "Crystal Desert",
        subtitle = "Rose-quartz dunes pierced by singing prism spires",
        unlockCost = 42000L,
        accentHex = 0xFFF472B6,
        resourcesSummary = "Prism Quartz, Gold, Crystal",
        spawnX = 62.0,
        spawnZ = 0.0,
        requiredAxeLevel = 6,
        requiredPickaxeLevel = 6,
        requiredBackpackLevel = 4
    ),
    SKY_ISLANDS(
        id = "sky_islands",
        title = "Sky Islands",
        subtitle = "Floating celestial turf ringed by ancient wind monoliths",
        unlockCost = 95000L,
        accentHex = 0xFF7DD3FC,
        resourcesSummary = "Aetherium, Crystal, Relics",
        spawnX = 0.0,
        spawnZ = -62.0,
        requiredAxeLevel = 6,
        requiredPickaxeLevel = 7,
        requiredBackpackLevel = 5
    ),
    TOXIC_SWAMP(
        id = "toxic_swamp",
        title = "Toxic Swamp",
        subtitle = "Bioluminescent bog hiding bio-resin vents & spiders",
        unlockCost = 185000L,
        accentHex = 0xFF84CC16,
        resourcesSummary = "Bio-Resin, Coal, Copper",
        spawnX = -34.0,
        spawnZ = 28.0,
        requiredAxeLevel = 7,
        requiredPickaxeLevel = 7,
        requiredBackpackLevel = 5
    ),
    FROZEN_MOUNTAINS(
        id = "frozen_mountains",
        title = "Frozen Mountains",
        subtitle = "High-altitude glacier peaks guarded by crystal insects",
        unlockCost = 340000L,
        accentHex = 0xFF67E8F9,
        resourcesSummary = "Frostite, Crystal, Iron",
        spawnX = -62.0,
        spawnZ = 0.0,
        requiredAxeLevel = 7,
        requiredPickaxeLevel = 8,
        requiredBackpackLevel = 6
    ),
    VOLCANIC_LANDS(
        id = "volcanic_lands",
        title = "Volcanic Lands",
        subtitle = "Deep obsidian badlands & molten cinder-cone geysers",
        unlockCost = 620000L,
        accentHex = 0xFFFB7185,
        resourcesSummary = "Obsidian, Gold, Rare Relics",
        spawnX = 0.0,
        spawnZ = 62.0,
        requiredAxeLevel = 8,
        requiredPickaxeLevel = 8,
        requiredBackpackLevel = 7
    ),
    ANCIENT_MACHINE_WORLD(
        id = "ancient_machine_world",
        title = "Ancient Machine World",
        subtitle = "Colossal clockwork plating & titan energy cores",
        unlockCost = 1200000L,
        accentHex = 0xFF38BDF8,
        resourcesSummary = "Titan Core, Crystal, Relics",
        spawnX = 34.0,
        spawnZ = 28.0,
        requiredAxeLevel = 8,
        requiredPickaxeLevel = 8,
        requiredBackpackLevel = 8
    );

    fun meetsEquipmentRequirements(state: TycoonSaveState): Boolean {
        return state.axeLevel >= requiredAxeLevel &&
            state.pickaxeLevel >= requiredPickaxeLevel &&
            state.backpackLevel >= requiredBackpackLevel
    }

    fun requirementSummary(): String? {
        val parts = buildList {
            if (requiredAxeLevel > 1) add("Axe Lv.$requiredAxeLevel")
            if (requiredPickaxeLevel > 1) add("Pickaxe Lv.$requiredPickaxeLevel")
            if (requiredBackpackLevel > 1) add("Bag Lv.$requiredBackpackLevel")
        }
        return if (parts.isEmpty()) null else "Req: ${parts.joinToString(" • ")}"
    }

    companion object {
        fun fromId(id: String): BiomeType? = entries.firstOrNull { it.id == id }
    }
}

enum class UniqueQuestItem(
    val id: String,
    val title: String,
    val perkDescription: String
) {
    GOLDEN_COMPASS("golden_compass", "Golden Compass", "+15% sell prices & faster trail speed"),
    LUMBER_TOTEM("timber_charm", "Lumberjack Totem", "+25% Wood & Sawmill output"),
    MINER_CREST("pickaxe_crest", "Master Miner's Crest", "+25% ore & mineral value"),
    PRISM_LANTERN("prism_lantern", "Prism Lantern", "+10 bag capacity & +25% Crystal value"),
    TYCOON_CROWN("tycoon_crown", "Crown of the Wilds", "+35% passive income & global sell value");

    companion object {
        fun fromId(id: String): UniqueQuestItem? = entries.firstOrNull { it.id == id }
    }
}

enum class QuestCategory(val label: String) {
    GATHER("Gather"),
    UPGRADE("Upgrade"),
    EXPLORE("Explore"),
    FACTORY("Factory")
}

sealed interface QuestRequirement {
    data class GatherResource(val resourceId: String, val targetAmount: Int) : QuestRequirement
    data class UpgradeTool(val upgradeType: UpgradeType, val targetLevel: Int) : QuestRequirement
    data class UnlockBiome(val biomeType: BiomeType) : QuestRequirement
    data class BuildFacility(val buildingType: BuildingType, val targetLevel: Int) : QuestRequirement
    data class OwnAutoMiners(val targetCount: Int) : QuestRequirement
    data class DiscoverSecrets(val targetCount: Int) : QuestRequirement
    data class DefeatInsects(val targetCount: Int) : QuestRequirement
    data class DefeatBosses(val targetCount: Int) : QuestRequirement
}

data class QuestDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: QuestCategory,
    val requirement: QuestRequirement,
    val rewardCoins: Long,
    val rewardXp: Long,
    val rewardItem: UniqueQuestItem? = null
)

object QuestCatalog {
    val all: List<QuestDefinition> = listOf(
        QuestDefinition(
            id = "q_gather_wood_1",
            title = "First Timber Haul",
            description = "Chop trees to gather 10 Wood",
            category = QuestCategory.GATHER,
            requirement = QuestRequirement.GatherResource("wood", 10),
            rewardCoins = 60L,
            rewardXp = 50L
        ),
        QuestDefinition(
            id = "q_upgrade_axe_2",
            title = "Stone Edge",
            description = "Evolve Felling Axe to Tier 2 (Stone)",
            category = QuestCategory.UPGRADE,
            requirement = QuestRequirement.UpgradeTool(UpgradeType.AXE, 2),
            rewardCoins = 90L,
            rewardXp = 60L
        ),
        QuestDefinition(
            id = "q_gather_stone_1",
            title = "Quarry Starter",
            description = "Mine boulders to gather 12 Stone",
            category = QuestCategory.GATHER,
            requirement = QuestRequirement.GatherResource("stone", 12),
            rewardCoins = 110L,
            rewardXp = 75L
        ),
        QuestDefinition(
            id = "q_build_sawmill",
            title = "Timber Industry",
            description = "Build the Timber Sawmill in Green Forest",
            category = QuestCategory.FACTORY,
            requirement = QuestRequirement.BuildFacility(BuildingType.SAWMILL, 1),
            rewardCoins = 150L,
            rewardXp = 100L
        ),
        QuestDefinition(
            id = "q_unlock_mountains",
            title = "Mountain Expedition",
            description = "Unlock the Rocky Mountains region",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.UnlockBiome(BiomeType.MOUNTAINS),
            rewardCoins = 200L,
            rewardXp = 140L,
            rewardItem = UniqueQuestItem.GOLDEN_COMPASS
        ),
        QuestDefinition(
            id = "q_upgrade_bag_3",
            title = "Copper Carrier",
            description = "Evolve Cargo Backpack to Tier 3",
            category = QuestCategory.UPGRADE,
            requirement = QuestRequirement.UpgradeTool(UpgradeType.BACKPACK, 3),
            rewardCoins = 220L,
            rewardXp = 120L
        ),
        QuestDefinition(
            id = "q_build_warehouse",
            title = "Trade Logistics",
            description = "Build the Grand Warehouse",
            category = QuestCategory.FACTORY,
            requirement = QuestRequirement.BuildFacility(BuildingType.WAREHOUSE, 1),
            rewardCoins = 240L,
            rewardXp = 160L
        ),
        QuestDefinition(
            id = "q_gather_iron_1",
            title = "Iron Prospecting",
            description = "Mine 15 Iron ore from mountain veins",
            category = QuestCategory.GATHER,
            requirement = QuestRequirement.GatherResource("iron", 15),
            rewardCoins = 350L,
            rewardXp = 150L,
            rewardItem = UniqueQuestItem.LUMBER_TOTEM
        ),
        QuestDefinition(
            id = "q_build_smelter",
            title = "Industrial Forge",
            description = "Build the Ore Smelter in Rocky Mountains",
            category = QuestCategory.FACTORY,
            requirement = QuestRequirement.BuildFacility(BuildingType.SMELTER, 1),
            rewardCoins = 480L,
            rewardXp = 260L
        ),
        QuestDefinition(
            id = "q_unlock_desert",
            title = "Dune Pioneer",
            description = "Unlock the Golden Desert region",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.UnlockBiome(BiomeType.DESERT),
            rewardCoins = 500L,
            rewardXp = 250L
        ),
        QuestDefinition(
            id = "q_upgrade_pick_5",
            title = "Steel Foundry Strike",
            description = "Evolve Mining Pickaxe to Tier 5 (Steel)",
            category = QuestCategory.UPGRADE,
            requirement = QuestRequirement.UpgradeTool(UpgradeType.PICKAXE, 5),
            rewardCoins = 900L,
            rewardXp = 320L
        ),
        QuestDefinition(
            id = "q_gather_gold_1",
            title = "Gold Rush",
            description = "Mine 18 Gold from desert or volcanic veins",
            category = QuestCategory.GATHER,
            requirement = QuestRequirement.GatherResource("gold", 18),
            rewardCoins = 950L,
            rewardXp = 300L,
            rewardItem = UniqueQuestItem.MINER_CREST
        ),
        QuestDefinition(
            id = "q_build_miners_3",
            title = "Automated Fleet",
            description = "Build 3 different Auto-Miner Rigs",
            category = QuestCategory.FACTORY,
            requirement = QuestRequirement.OwnAutoMiners(3),
            rewardCoins = 1200L,
            rewardXp = 420L
        ),
        QuestDefinition(
            id = "q_unlock_frozen",
            title = "Frostbreaker",
            description = "Unlock the Frozen Lands region",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.UnlockBiome(BiomeType.FROZEN),
            rewardCoins = 1400L,
            rewardXp = 450L
        ),
        QuestDefinition(
            id = "q_gather_crystal_1",
            title = "Crystal Resonance",
            description = "Harvest 20 Crystals from frost or jungle spires",
            category = QuestCategory.GATHER,
            requirement = QuestRequirement.GatherResource("crystal", 20),
            rewardCoins = 2200L,
            rewardXp = 500L,
            rewardItem = UniqueQuestItem.PRISM_LANTERN
        ),
        QuestDefinition(
            id = "q_unlock_volcanic",
            title = "Caldera Conqueror",
            description = "Unlock the Volcanic Region",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.UnlockBiome(BiomeType.VOLCANIC),
            rewardCoins = 3800L,
            rewardXp = 750L
        ),
        QuestDefinition(
            id = "q_upgrade_axe_7",
            title = "Diamond Edge",
            description = "Evolve Felling Axe to Tier 7 (Diamond)",
            category = QuestCategory.UPGRADE,
            requirement = QuestRequirement.UpgradeTool(UpgradeType.AXE, 7),
            rewardCoins = 6500L,
            rewardXp = 950L
        ),
        QuestDefinition(
            id = "q_unlock_jungle",
            title = "Jungle Sovereign",
            description = "Unlock the Ancient Jungle region",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.UnlockBiome(BiomeType.JUNGLE),
            rewardCoins = 9000L,
            rewardXp = 1200L
        ),
        QuestDefinition(
            id = "q_gather_rare_1",
            title = "Relic Hunter",
            description = "Collect 12 Rare Relics or Magma Cores",
            category = QuestCategory.GATHER,
            requirement = QuestRequirement.GatherResource("rare", 12),
            rewardCoins = 5500L,
            rewardXp = 900L,
            rewardItem = UniqueQuestItem.TYCOON_CROWN
        ),
        QuestDefinition(
            id = "q_legendary_pick",
            title = "Master Prospector",
            description = "Evolve Mining Pickaxe to Tier 8 (Legendary)",
            category = QuestCategory.UPGRADE,
            requirement = QuestRequirement.UpgradeTool(UpgradeType.PICKAXE, 8),
            rewardCoins = 15000L,
            rewardXp = 1800L
        ),
        QuestDefinition(
            id = "q_defeat_insects_5",
            title = "Frontier Defender",
            description = "Defeat 5 hostile insects across the wilds",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.DefeatInsects(5),
            rewardCoins = 420L,
            rewardXp = 180L
        ),
        QuestDefinition(
            id = "q_discover_caves_3",
            title = "Cavern Explorer",
            description = "Discover 3 hidden caves or secret caches",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.DiscoverSecrets(3),
            rewardCoins = 850L,
            rewardXp = 280L
        ),
        QuestDefinition(
            id = "q_unlock_crystal_desert",
            title = "Prism Expedition",
            description = "Unlock the Crystal Desert region",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.UnlockBiome(BiomeType.CRYSTAL_DESERT),
            rewardCoins = 18000L,
            rewardXp = 1500L
        ),
        QuestDefinition(
            id = "q_unlock_sky_islands",
            title = "Skyward Bound",
            description = "Unlock the floating Sky Islands",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.UnlockBiome(BiomeType.SKY_ISLANDS),
            rewardCoins = 36000L,
            rewardXp = 2200L
        ),
        QuestDefinition(
            id = "q_defeat_boss_1",
            title = "Titan Slayer",
            description = "Defeat any optional World Boss",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.DefeatBosses(1),
            rewardCoins = 12000L,
            rewardXp = 1200L
        ),
        QuestDefinition(
            id = "q_unlock_ancient_machine",
            title = "Clockwork Horizon",
            description = "Unlock the Ancient Machine World",
            category = QuestCategory.EXPLORE,
            requirement = QuestRequirement.UnlockBiome(BiomeType.ANCIENT_MACHINE_WORLD),
            rewardCoins = 250000L,
            rewardXp = 5000L
        )
    )
}

data class QuestProgress(
    val quest: QuestDefinition,
    val current: Int,
    val target: Int,
    val isClaimed: Boolean
) {
    val isCompleted: Boolean get() = current >= target
    val isReadyToClaim: Boolean get() = isCompleted && !isClaimed
    val fraction: Float get() = if (target <= 0) 1f else (current.toFloat() / target).coerceIn(0f, 1f)
}

data class TycoonSaveState(
    val coins: Long = 0L,
    val lifetimeEarned: Long = 0L,
    val xp: Long = 0L,
    val inventory: Map<String, Int> = emptyMap(),
    val gatheredTotals: Map<String, Int> = emptyMap(),
    val axeLevel: Int = 1,
    val pickaxeLevel: Int = 1,
    val backpackLevel: Int = 1,
    val moveSpeedLevel: Int = 1,
    val gatherSpeedLevel: Int = 1,
    val resourceUpgradeLevels: Map<String, Int> = emptyMap(),
    val buildingLevels: Map<String, Int> = emptyMap(),
    val buildingWorkers: Map<String, Int> = emptyMap(),
    val buildingLines: Map<String, Int> = emptyMap(),
    val machineLevels: Map<String, Int> = emptyMap(),
    val machineActive: Map<String, Boolean> = emptyMap(),
    val warehouseStock: Map<String, Int> = emptyMap(),
    val fleetLevels: Map<String, Int> = emptyMap(),
    val buyerReputationXp: Map<String, Int> = emptyMap(),
    val marketMultipliers: Map<String, Double> = emptyMap(),
    val activeContracts: List<ContractOrder> = emptyList(),
    val availableContracts: List<ContractOrder> = emptyList(),
    val totalContractsCompleted: Int = 0,
    val lastMarketTickEpochMs: Long = 0L,
    val unlockedBiomes: Set<String> = setOf("forest"),
    val claimedQuestIds: Set<String> = emptySet(),
    val uniqueItems: Set<String> = emptySet(),
    val discoveredSecrets: Set<String> = emptySet(),
    val defeatedBosses: Set<String> = emptySet(),
    val unlockedVehicles: Set<String> = emptySet(),
    val activeVehicleId: String? = null,
    val insectsDefeated: Int = 0,
    val playerHp: Int = 100,
    val isMuted: Boolean = false,
    val sfxVolume: Float = 0.85f,
    val musicVolume: Float = 0.45f,
    val graphicsQuality: Int = 1,
    val hapticsEnabled: Boolean = true,
    val lastSavedEpochMs: Long = 0L
) {
    val playerMaxHp: Int
        get() = 100 + (backpackLevel - 1) * 10 + (axeLevel - 1) * 5

    fun isVehicleUnlocked(type: DrivableVehicleType): Boolean = unlockedVehicles.contains(type.id)
    val playerLevel: Int
        get() {
            var lvl = 1
            var remaining = xp
            var threshold = 100L
            while (remaining >= threshold) {
                remaining -= threshold
                lvl++
                threshold = (threshold * 1.45).roundToLong()
            }
            return lvl
        }

    val xpIntoCurrentLevel: Long
        get() {
            var remaining = xp
            var threshold = 100L
            while (remaining >= threshold) {
                remaining -= threshold
                threshold = (threshold * 1.45).roundToLong()
            }
            return remaining
        }

    val xpNeededForNextLevel: Long
        get() {
            var remaining = xp
            var threshold = 100L
            while (remaining >= threshold) {
                remaining -= threshold
                threshold = (threshold * 1.45).roundToLong()
            }
            return threshold
        }

    val warehouseLevel: Int
        get() = buildingLevels[BuildingType.WAREHOUSE.id] ?: 0

    val backpackMaxCapacity: Int
        get() {
            val base = when (backpackLevel.coerceIn(1, 8)) {
                1 -> 12
                2 -> 22
                3 -> 36
                4 -> 56
                5 -> 84
                6 -> 126
                7 -> 184
                else -> 260
            }
            val storageLvl = resourceUpgradeLevel(ResourceUpgradeType.STORAGE_CAPACITY)
            val storageFlat = (storageLvl - 1) * 6
            val storageMult = 1.0 + (storageLvl - 1) * 0.10
            val whLines = buildingLineCount(BuildingType.WAREHOUSE)
            val whBonus = warehouseLevel * (8 + whLines * 4)
            val lanternBonus = if (uniqueItems.contains(UniqueQuestItem.PRISM_LANTERN.id)) 10 else 0
            val haulerBonus = if (unlockedVehicles.contains(DrivableVehicleType.HEAVY_HAULER.id)) 45 else 0
            return ((base + storageFlat + whBonus + lanternBonus + haulerBonus) * storageMult).roundToLong().toInt()
        }

    val backpackUsed: Int
        get() = inventory.values.sum()

    val isBackpackFull: Boolean
        get() = backpackUsed >= backpackMaxCapacity

    fun machineLevel(type: MachineType): Int = (machineLevels[type.id] ?: 0).coerceIn(0, type.maxLevel)

    fun isMachineRunning(type: MachineType): Boolean =
        machineLevel(type) > 0 && (machineActive[type.id] ?: true)

    fun fleetLevel(type: FleetEquipType): Int = (fleetLevels[type.id] ?: 0).coerceIn(0, type.maxLevel)

    val conveyorLevel: Int
        get() = machineLevel(MachineType.CONVEYOR_BELT)

    val warehouseStockCapacity: Int
        get() {
            val base = 60
            val whBonus = warehouseLevel * 45
            val beltBonus = conveyorLevel * 15
            val cartBonus = fleetLevel(FleetEquipType.MINING_CART) * 25
            val forkliftBonus = fleetLevel(FleetEquipType.FORKLIFT) * 20
            val containerBonus = fleetLevel(FleetEquipType.STORAGE_CONTAINER) * 45
            return base + whBonus + beltBonus + cartBonus + forkliftBonus + containerBonus
        }

    val warehouseStockUsed: Int
        get() = warehouseStock.values.sum()

    val maxActiveContractsAllowed: Int
        get() {
            val truckLvl = fleetLevel(FleetEquipType.DELIVERY_TRUCK)
            return when {
                truckLvl >= 4 -> 4
                truckLvl >= 2 -> 3
                else -> 2
            }
        }

    fun isMachineUnlocked(type: MachineType): Boolean =
        playerLevel >= type.requiredPlayerLevel && unlockedBiomes.contains(type.requiredBiomeId)

    fun isBuyerUnlocked(buyer: BuyerCompany): Boolean =
        playerLevel >= buyer.unlockLevel && unlockedBiomes.contains(buyer.unlockBiomeId)

    fun buyerReputation(buyer: BuyerCompany): Int = (buyerReputationXp[buyer.id] ?: 0).coerceAtLeast(0)

    fun buyerReputationLevel(buyer: BuyerCompany): Int = (buyerReputation(buyer) / 50) + 1

    fun marketMultiplier(goodId: String): Double =
        (marketMultipliers[goodId] ?: 1.0).coerceIn(0.80, 1.45)

    fun availableForTrade(goodId: String): Int =
        (warehouseStock[goodId] ?: 0) + (inventory[goodId] ?: 0)

    fun buyerOfferUnitPrice(buyer: BuyerCompany, goodId: String): Long {
        val good = TradeCatalog.find(goodId) ?: return 0L
        val base = if (!good.isManufactured) {
            ResourceType.fromId(goodId)?.let { effectiveSellValue(it).toDouble() } ?: good.baseValue.toDouble()
        } else {
            val tradeLvl = resourceUpgradeLevel(ResourceUpgradeType.TRADE_VALUE)
            val tradeMult = 1.0 + (tradeLvl - 1) * 0.15
            val whMult = 1.0 + warehouseLevel * 0.12
            good.baseValue * tradeMult * whMult
        }
        val marketMult = marketMultiplier(goodId)
        val specialty = if (buyer.preferredGoods.contains(goodId)) buyer.specialtyMultiplier else 0.90
        val repBonus = 1.0 + (buyerReputationLevel(buyer) - 1) * 0.06
        return (base * marketMult * specialty * repBonus).roundToLong().coerceAtLeast(1L)
    }

    val totalMachinePowerCostPerMinute: Int
        get() = MachineType.entries.sumOf { m ->
            val lvl = machineLevel(m)
            if (lvl > 0 && isMachineRunning(m)) m.powerCostPerMinute * lvl else 0
        }

    fun buildingEffectivePassive(building: BuildingType): Double {
        val lvl = buildingLevel(building)
        if (lvl <= 0) return 0.0
        val workers = buildingWorkerCount(building)
        val lines = buildingLineCount(building)
        val staffMult = (1.0 + workers * 0.35) * (1.0 + lines * 0.65)
        val autoLvl = resourceUpgradeLevel(ResourceUpgradeType.AUTO_PRODUCTION)
        val autoMult = 1.0 + (autoLvl - 1) * 0.20
        val crownMult = if (uniqueItems.contains(UniqueQuestItem.TYCOON_CROWN.id)) 1.35 else 1.0
        return lvl * building.passivePerLevel * staffMult * autoMult * crownMult
    }

    val passiveIncomePerSecond: Double
        get() {
            val buildingPassive = BuildingType.entries.sumOf { buildingEffectivePassive(it) }
            val excavatorMult = 1.0 + fleetLevel(FleetEquipType.EXCAVATOR) * 0.15
            return buildingPassive * excavatorMult
        }

    fun upgradeLevel(type: UpgradeType): Int = when (type) {
        UpgradeType.AXE -> axeLevel
        UpgradeType.PICKAXE -> pickaxeLevel
        UpgradeType.BACKPACK -> backpackLevel
        UpgradeType.MOVE_SPEED -> moveSpeedLevel
        UpgradeType.GATHER_SPEED -> gatherSpeedLevel
    }

    fun resourceUpgradeLevel(type: ResourceUpgradeType): Int =
        (resourceUpgradeLevels[type.id] ?: 1).coerceIn(1, type.maxLevel)

    fun buildingLevel(type: BuildingType): Int = (buildingLevels[type.id] ?: 0).coerceIn(0, type.maxLevel)

    fun buildingWorkerCount(type: BuildingType): Int =
        (buildingWorkers[type.id] ?: 0).coerceIn(0, type.maxWorkers)

    fun buildingLineCount(type: BuildingType): Int =
        (buildingLines[type.id] ?: 0).coerceIn(0, type.maxLines)

    fun isBiomeUnlocked(biome: BiomeType): Boolean = unlockedBiomes.contains(biome.id)

    fun meetsResourceRequirement(req: ResourceRequirement?): Boolean {
        if (req == null) return true
        return (gatheredTotals[req.resource.id] ?: 0) >= req.requiredTotal
    }

    fun gatheredCount(resource: ResourceType): Int = gatheredTotals[resource.id] ?: 0

    fun effectiveSellValue(resource: ResourceType): Int {
        val tradeLvl = resourceUpgradeLevel(ResourceUpgradeType.TRADE_VALUE)
        var mult = (1.0 + (tradeLvl - 1) * 0.15) + warehouseLevel * (0.20 + buildingLineCount(BuildingType.WAREHOUSE) * 0.08)
        if (resource == ResourceType.WOOD) {
            val sawLvl = buildingLevel(BuildingType.SAWMILL)
            val sawBoost = 1.0 + buildingWorkerCount(BuildingType.SAWMILL) * 0.25 + buildingLineCount(BuildingType.SAWMILL) * 0.35
            mult += sawLvl * 0.45 * sawBoost
            if (uniqueItems.contains(UniqueQuestItem.LUMBER_TOTEM.id)) mult += 0.25
        }
        if (resource == ResourceType.STONE || resource == ResourceType.COAL ||
            resource == ResourceType.COPPER || resource == ResourceType.IRON || resource == ResourceType.GOLD
        ) {
            val smLvl = buildingLevel(BuildingType.SMELTER)
            val smBoost = 1.0 + buildingWorkerCount(BuildingType.SMELTER) * 0.25 + buildingLineCount(BuildingType.SMELTER) * 0.35
            mult += smLvl * 0.45 * smBoost
            if (uniqueItems.contains(UniqueQuestItem.MINER_CREST.id)) mult += 0.25
        }
        if (resource == ResourceType.CRYSTAL && uniqueItems.contains(UniqueQuestItem.PRISM_LANTERN.id)) {
            mult += 0.25
        }
        if (uniqueItems.contains(UniqueQuestItem.GOLDEN_COMPASS.id)) mult += 0.15
        if (uniqueItems.contains(UniqueQuestItem.TYCOON_CROWN.id)) mult += 0.35
        return (resource.baseValue * mult).roundToLong().toInt().coerceAtLeast(1)
    }

    fun evaluateQuests(): List<QuestProgress> {
        val minerCount = listOf(
            BuildingType.MINER_FOREST,
            BuildingType.MINER_MOUNTAINS,
            BuildingType.MINER_DESERT,
            BuildingType.MINER_FROZEN,
            BuildingType.MINER_VOLCANIC,
            BuildingType.MINER_JUNGLE,
            BuildingType.MINER_CRYSTAL_DESERT,
            BuildingType.MINER_SKY_ISLANDS,
            BuildingType.MINER_TOXIC_SWAMP,
            BuildingType.MINER_FROZEN_MOUNTAINS,
            BuildingType.MINER_VOLCANIC_LANDS,
            BuildingType.MINER_ANCIENT_MACHINE
        ).count { buildingLevel(it) > 0 }

        return QuestCatalog.all.map { q ->
            val (cur, target) = when (val req = q.requirement) {
                is QuestRequirement.GatherResource -> (gatheredTotals[req.resourceId] ?: 0) to req.targetAmount
                is QuestRequirement.UpgradeTool -> upgradeLevel(req.upgradeType) to req.targetLevel
                is QuestRequirement.UnlockBiome -> (if (isBiomeUnlocked(req.biomeType)) 1 else 0) to 1
                is QuestRequirement.BuildFacility -> buildingLevel(req.buildingType) to req.targetLevel
                is QuestRequirement.OwnAutoMiners -> minerCount to req.targetCount
                is QuestRequirement.DiscoverSecrets -> discoveredSecrets.size to req.targetCount
                is QuestRequirement.DefeatInsects -> insectsDefeated to req.targetCount
                is QuestRequirement.DefeatBosses -> defeatedBosses.size to req.targetCount
            }
            QuestProgress(
                quest = q,
                current = cur.coerceAtMost(target),
                target = target,
                isClaimed = claimedQuestIds.contains(q.id)
            )
        }
    }

    val readyQuestCount: Int
        get() = evaluateQuests().count { it.isReadyToClaim }

    fun toContextSummary(): String {
        val bagItems = inventory.filterValues { it > 0 }
            .entries.joinToString(", ") { "${it.key}:${it.value}" }
            .ifEmpty { "empty" }
        val builtList = BuildingType.entries
            .mapNotNull { b ->
                val lvl = buildingLevel(b)
                if (lvl > 0) "${b.title} L$lvl" else null
            }.joinToString(", ").ifEmpty { "none yet" }
        val biomes = BiomeType.entries
            .filter { isBiomeUnlocked(it) }
            .joinToString(", ") { it.title }
        return "Lv$playerLevel ($xp XP), Coins=$coins, Passive=${passiveIncomePerSecond.toInt()}/s, Bag=$backpackUsed/$backpackMaxCapacity ($bagItems), " +
            "Axe=L$axeLevel, Pickaxe=L$pickaxeLevel, BagUpgrade=L$backpackLevel, Speed=L$moveSpeedLevel, " +
            "Gather=L$gatherSpeedLevel, Buildings=[$builtList], UnlockedBiomes=[$biomes], QuestsReady=$readyQuestCount"
    }
}
