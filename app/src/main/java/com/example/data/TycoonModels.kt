package com.example.data

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
    RARE("rare", "Rare Relic", 220, 0xFFE879F9);

    companion object {
        fun fromId(id: String): ResourceType? = entries.firstOrNull { it.id == id }
    }
}

enum class UpgradeType(
    val id: String,
    val title: String,
    val subtitle: String,
    val maxLevel: Int = 6
) {
    AXE("axe", "Felling Axe", "Chops trees faster & yields more timber"),
    PICKAXE("pickaxe", "Mining Pickaxe", "Breaks tougher ores & crystals"),
    BACKPACK("backpack", "Cargo Backpack", "Expands resource carry capacity"),
    MOVE_SPEED("moveSpeed", "Swift Boots", "Increases frontier movement speed"),
    GATHER_SPEED("gatherSpeed", "Haste Gloves", "Increases automatic swing cadence");

    fun tierName(level: Int): String = when (this) {
        AXE, PICKAXE -> when (level.coerceIn(1, 6)) {
            1 -> "Primitive Timber"
            2 -> "Forged Iron"
            3 -> "Copper-Steel"
            4 -> "Gilded Gold"
            5 -> "Prism Crystal"
            else -> "Mythic Reactor"
        }
        BACKPACK -> when (level.coerceIn(1, 6)) {
            1 -> "Burlap Sack"
            2 -> "Explorer Pack"
            3 -> "Iron Frame"
            4 -> "Brass Twin-Pack"
            5 -> "Gold Hopper"
            else -> "Quantum Core"
        }
        MOVE_SPEED -> when (level.coerceIn(1, 6)) {
            1 -> "Cloth Sandals"
            2 -> "Trail Boots"
            3 -> "Sprinter Greaves"
            4 -> "Jet Striders"
            5 -> "Turbo Thrusters"
            else -> "Hyper Stride"
        }
        GATHER_SPEED -> when (level.coerceIn(1, 6)) {
            1 -> "Bare Hands"
            2 -> "Leather Wraps"
            3 -> "Power Grips"
            4 -> "Servo Gauntlets"
            5 -> "Plasma Cuffs"
            else -> "Overdrive Arms"
        }
    }

    fun costForNextLevel(currentLevel: Int): Long {
        if (currentLevel >= maxLevel) return 0L
        return when (this) {
            AXE, PICKAXE -> (50.0 * 2.4.pow(currentLevel - 1)).roundToLong()
            BACKPACK -> (40.0 * 2.5.pow(currentLevel - 1)).roundToLong()
            MOVE_SPEED -> (45.0 * 2.45.pow(currentLevel - 1)).roundToLong()
            GATHER_SPEED -> (60.0 * 2.4.pow(currentLevel - 1)).roundToLong()
        }
    }
}

enum class BuildingType(
    val id: String,
    val title: String,
    val description: String,
    val biomeId: String,
    val baseCost: Double,
    val costGrowth: Double,
    val passivePerLevel: Double,
    val maxLevel: Int = 5
) {
    SAWMILL(
        id = "sawmill",
        title = "Timber Sawmill",
        description = "+45% Wood value & +$4/s per level",
        biomeId = "forest",
        baseCost = 120.0,
        costGrowth = 2.3,
        passivePerLevel = 4.0
    ),
    WAREHOUSE(
        id = "warehouse",
        title = "Trading Warehouse",
        description = "+20% all sell prices & +8 bag capacity/lvl",
        biomeId = "forest",
        baseCost = 180.0,
        costGrowth = 2.4,
        passivePerLevel = 2.0
    ),
    SMELTER(
        id = "smelter",
        title = "Ore Smelter",
        description = "+45% mineral/ore value & +$10/s per level",
        biomeId = "mountains",
        baseCost = 320.0,
        costGrowth = 2.4,
        passivePerLevel = 10.0
    ),
    MINER_FOREST(
        id = "miner_forest",
        title = "Forest Lumber Bot",
        description = "Automated timber collector (+$3/s per level)",
        biomeId = "forest",
        baseCost = 90.0,
        costGrowth = 2.2,
        passivePerLevel = 3.0
    ),
    MINER_MOUNTAINS(
        id = "miner_mountains",
        title = "Mountain Drill Rig",
        description = "Automated iron & coal drill (+$12/s per level)",
        biomeId = "mountains",
        baseCost = 350.0,
        costGrowth = 2.2,
        passivePerLevel = 12.0
    ),
    MINER_DESERT(
        id = "miner_desert",
        title = "Solar Ore Extractor",
        description = "Automated copper & gold rig (+$30/s per level)",
        biomeId = "desert",
        baseCost = 950.0,
        costGrowth = 2.2,
        passivePerLevel = 30.0
    ),
    MINER_FROZEN(
        id = "miner_frozen",
        title = "Cryo Crystal Harvester",
        description = "Automated frost crystal rig (+$75/s per level)",
        biomeId = "frozen",
        baseCost = 2800.0,
        costGrowth = 2.2,
        passivePerLevel = 75.0
    ),
    MINER_VOLCANIC(
        id = "miner_volcanic",
        title = "Magma Core Extractor",
        description = "Automated obsidian & core rig (+$190/s per level)",
        biomeId = "volcanic",
        baseCost = 8000.0,
        costGrowth = 2.2,
        passivePerLevel = 190.0
    ),
    MINER_JUNGLE(
        id = "miner_jungle",
        title = "Relic Synthesizer",
        description = "Automated ancient relic rig (+$450/s per level)",
        biomeId = "jungle",
        baseCost = 22000.0,
        costGrowth = 2.2,
        passivePerLevel = 450.0
    );

    fun costForNextLevel(currentLevel: Int): Long {
        if (currentLevel >= maxLevel) return 0L
        return (baseCost * costGrowth.pow(currentLevel)).roundToLong()
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
    val spawnZ: Double
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
        spawnZ = -26.0
    ),
    DESERT(
        id = "desert",
        title = "Golden Desert",
        subtitle = "Sunlit sandstone mesas with copper & gold seams",
        unlockCost = 650L,
        accentHex = 0xFFF59E0B,
        resourcesSummary = "Copper, Gold, Stone",
        spawnX = 26.0,
        spawnZ = 0.0
    ),
    FROZEN(
        id = "frozen",
        title = "Frozen Lands",
        subtitle = "Aurora ice spires sparkling with pure crystals",
        unlockCost = 2000L,
        accentHex = 0xFF38BDF8,
        resourcesSummary = "Crystals, Iron, Wood",
        spawnX = -26.0,
        spawnZ = 0.0
    ),
    VOLCANIC(
        id = "volcanic",
        title = "Volcanic Region",
        subtitle = "Basalt caldera hiding magma cores & gold veins",
        unlockCost = 6500L,
        accentHex = 0xFFEF4444,
        resourcesSummary = "Coal, Gold, Magma Cores",
        spawnX = 0.0,
        spawnZ = 26.0
    ),
    JUNGLE(
        id = "jungle",
        title = "Ancient Jungle",
        subtitle = "Mossy temple ruins brimming with rare relics",
        unlockCost = 18000L,
        accentHex = 0xFF14B8A6,
        resourcesSummary = "Wood, Crystals, Ancient Relics",
        spawnX = 26.0,
        spawnZ = -26.0
    );

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
            title = "Sharper Edge",
            description = "Upgrade Felling Axe to Tier 2",
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
            title = "Deep Pockets",
            description = "Upgrade Cargo Backpack to Tier 3",
            category = QuestCategory.UPGRADE,
            requirement = QuestRequirement.UpgradeTool(UpgradeType.BACKPACK, 3),
            rewardCoins = 220L,
            rewardXp = 120L
        ),
        QuestDefinition(
            id = "q_build_warehouse",
            title = "Trade Logistics",
            description = "Build the Trading Warehouse",
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
            id = "q_upgrade_pick_4",
            title = "Gilded Striker",
            description = "Upgrade Mining Pickaxe to Tier 4",
            category = QuestCategory.UPGRADE,
            requirement = QuestRequirement.UpgradeTool(UpgradeType.PICKAXE, 4),
            rewardCoins = 750L,
            rewardXp = 280L
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
    val buildingLevels: Map<String, Int> = emptyMap(),
    val unlockedBiomes: Set<String> = setOf("forest"),
    val claimedQuestIds: Set<String> = emptySet(),
    val uniqueItems: Set<String> = emptySet(),
    val isMuted: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val lastSavedEpochMs: Long = 0L
) {
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
            val base = when (backpackLevel.coerceIn(1, 6)) {
                1 -> 12
                2 -> 22
                3 -> 36
                4 -> 56
                5 -> 85
                else -> 130
            }
            val lanternBonus = if (uniqueItems.contains(UniqueQuestItem.PRISM_LANTERN.id)) 10 else 0
            return base + (warehouseLevel * 8) + lanternBonus
        }

    val backpackUsed: Int
        get() = inventory.values.sum()

    val isBackpackFull: Boolean
        get() = backpackUsed >= backpackMaxCapacity

    val passiveIncomePerSecond: Double
        get() {
            val raw = BuildingType.entries.sumOf { building ->
                val lvl = buildingLevels[building.id] ?: 0
                lvl * building.passivePerLevel
            }
            val crownMult = if (uniqueItems.contains(UniqueQuestItem.TYCOON_CROWN.id)) 1.35 else 1.0
            return raw * crownMult
        }

    fun upgradeLevel(type: UpgradeType): Int = when (type) {
        UpgradeType.AXE -> axeLevel
        UpgradeType.PICKAXE -> pickaxeLevel
        UpgradeType.BACKPACK -> backpackLevel
        UpgradeType.MOVE_SPEED -> moveSpeedLevel
        UpgradeType.GATHER_SPEED -> gatherSpeedLevel
    }

    fun buildingLevel(type: BuildingType): Int = buildingLevels[type.id] ?: 0

    fun isBiomeUnlocked(biome: BiomeType): Boolean = unlockedBiomes.contains(biome.id)

    fun evaluateQuests(): List<QuestProgress> {
        val minerCount = listOf(
            BuildingType.MINER_FOREST,
            BuildingType.MINER_MOUNTAINS,
            BuildingType.MINER_DESERT,
            BuildingType.MINER_FROZEN,
            BuildingType.MINER_VOLCANIC,
            BuildingType.MINER_JUNGLE
        ).count { buildingLevel(it) > 0 }

        return QuestCatalog.all.map { q ->
            val (cur, target) = when (val req = q.requirement) {
                is QuestRequirement.GatherResource -> (gatheredTotals[req.resourceId] ?: 0) to req.targetAmount
                is QuestRequirement.UpgradeTool -> upgradeLevel(req.upgradeType) to req.targetLevel
                is QuestRequirement.UnlockBiome -> (if (isBiomeUnlocked(req.biomeType)) 1 else 0) to 1
                is QuestRequirement.BuildFacility -> buildingLevel(req.buildingType) to req.targetLevel
                is QuestRequirement.OwnAutoMiners -> minerCount to req.targetCount
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
