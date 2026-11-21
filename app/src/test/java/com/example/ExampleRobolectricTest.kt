package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BiomeType
import com.example.data.BuildingType
import com.example.data.GameRepository
import com.example.data.ResourceType
import com.example.data.ResourceUpgradeType
import com.example.data.TycoonSaveState
import com.example.data.UniqueQuestItem
import com.example.data.UpgradeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Wildcraft Tycoon", appName)
    }

    @Test
    fun `quest system tracks gathering upgrades regions and factories`() {
        val state = TycoonSaveState(
            coins = 500L,
            xp = 120L,
            gatheredTotals = mapOf("wood" to 10),
            axeLevel = 2,
            buildingLevels = mapOf(BuildingType.SAWMILL.id to 1),
            unlockedBiomes = setOf("forest", BiomeType.MOUNTAINS.id),
            uniqueItems = setOf(UniqueQuestItem.PRISM_LANTERN.id)
        )
        val quests = state.evaluateQuests().associateBy { it.quest.id }
        assertTrue(quests.getValue("q_gather_wood_1").isReadyToClaim)
        assertTrue(quests.getValue("q_upgrade_axe_2").isReadyToClaim)
        assertTrue(quests.getValue("q_build_sawmill").isReadyToClaim)
        assertTrue(quests.getValue("q_unlock_mountains").isReadyToClaim)
        assertEquals(2, state.playerLevel)
        assertEquals(22, state.backpackMaxCapacity)
    }

    @Test
    fun `upgrade metrics and audio graphics settings round trip cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)
        val original = TycoonSaveState(
            coins = 1250L,
            axeLevel = 7,
            pickaxeLevel = 8,
            resourceUpgradeLevels = mapOf(
                ResourceUpgradeType.TRADE_VALUE.id to 4,
                ResourceUpgradeType.AUTO_PRODUCTION.id to 3
            ),
            buildingLevels = mapOf(BuildingType.SAWMILL.id to 3),
            buildingWorkers = mapOf(BuildingType.SAWMILL.id to 2),
            buildingLines = mapOf(BuildingType.SAWMILL.id to 1),
            sfxVolume = 0.7f,
            musicVolume = 0.3f,
            graphicsQuality = 2
        )
        val json = repo.serialize(original)
        val decoded = repo.deserialize(json)
        assertEquals(1250L, decoded.coins)
        assertEquals(7, decoded.axeLevel)
        assertEquals(8, decoded.pickaxeLevel)
        assertEquals(4, decoded.resourceUpgradeLevel(ResourceUpgradeType.TRADE_VALUE))
        assertEquals(3, decoded.resourceUpgradeLevel(ResourceUpgradeType.AUTO_PRODUCTION))
        assertEquals(2, decoded.buildingWorkerCount(BuildingType.SAWMILL))
        assertEquals(1, decoded.buildingLineCount(BuildingType.SAWMILL))
        assertEquals(0.7f, decoded.sfxVolume, 0.01f)
        assertEquals(0.3f, decoded.musicVolume, 0.01f)
        assertEquals(2, decoded.graphicsQuality)

        val axeStats = UpgradeType.AXE.statComparisonLines(1)
        assertEquals(3, axeStats.size)
        assertTrue(axeStats.first().improvementPct > 0)
        assertEquals("Diamond", UpgradeType.AXE.materialTierLabel(7))
        assertEquals("Legendary", UpgradeType.AXE.materialTierLabel(8))
    }

    @Test
    fun `advanced progression rules gate biomes and scale factories and resources`() {
        val starter = TycoonSaveState(
            coins = 1000L,
            axeLevel = 1,
            pickaxeLevel = 1,
            gatheredTotals = mapOf("wood" to 4)
        )
        // Desert requires Pickaxe Lv.3
        assertFalse(BiomeType.DESERT.meetsEquipmentRequirements(starter))
        // Axe Lv.2 requires 8 Wood gathered
        assertFalse(starter.meetsResourceRequirement(UpgradeType.AXE.resourceRequirementForNext(1)))

        val advanced = TycoonSaveState(
            coins = 50000L,
            axeLevel = 6,
            pickaxeLevel = 6,
            backpackLevel = 4,
            gatheredTotals = mapOf("wood" to 50, "iron" to 40),
            resourceUpgradeLevels = mapOf(
                ResourceUpgradeType.TRADE_VALUE.id to 3,
                ResourceUpgradeType.STORAGE_CAPACITY.id to 3,
                ResourceUpgradeType.AUTO_PRODUCTION.id to 2
            ),
            buildingLevels = mapOf(BuildingType.MINER_FOREST.id to 2),
            buildingWorkers = mapOf(BuildingType.MINER_FOREST.id to 2),
            buildingLines = mapOf(BuildingType.MINER_FOREST.id to 1)
        )
        assertTrue(BiomeType.JUNGLE.meetsEquipmentRequirements(advanced))
        assertTrue(advanced.effectiveSellValue(ResourceType.GOLD) > ResourceType.GOLD.baseValue)
        assertTrue(advanced.backpackMaxCapacity > 56)
        assertTrue(advanced.passiveIncomePerSecond > 15.0)
    }

    @Test
    fun `machinery fleet buyers contracts and persistence round trip cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)

        val state = TycoonSaveState(
            coins = 25000L,
            xp = 5000L,
            unlockedBiomes = setOf("forest", "mountains", "desert"),
            inventory = mapOf("wood" to 10),
            warehouseStock = mapOf("planks" to 25, "stone_blocks" to 15),
            machineLevels = mapOf(
                com.example.data.MachineType.WOOD_CUTTER.id to 2,
                com.example.data.MachineType.CONVEYOR_BELT.id to 2,
                com.example.data.MachineType.STONE_CRUSHER.id to 1
            ),
            machineActive = mapOf(
                com.example.data.MachineType.WOOD_CUTTER.id to true,
                com.example.data.MachineType.STONE_CRUSHER.id to false
            ),
            fleetLevels = mapOf(
                com.example.data.FleetEquipType.STORAGE_CONTAINER.id to 2,
                com.example.data.FleetEquipType.MINING_CART.id to 1,
                com.example.data.FleetEquipType.FORKLIFT.id to 1,
                com.example.data.FleetEquipType.DELIVERY_TRUCK.id to 4,
                com.example.data.FleetEquipType.EXCAVATOR.id to 2
            ),
            buildingLevels = mapOf(BuildingType.SAWMILL.id to 2),
            buyerReputationXp = mapOf(com.example.data.BuyerCompany.OAKHEART_TIMBER.id to 110),
            marketMultipliers = mapOf("planks" to 1.35)
        )

        // Machine speed boosted by conveyor belt & power cost ignores paused machines
        val baseRate = com.example.data.MachineType.WOOD_CUTTER.unitsPerMinute(2, 0)
        val beltRate = com.example.data.MachineType.WOOD_CUTTER.unitsPerMinute(2, 2)
        assertTrue(beltRate > baseRate)
        assertEquals(
            com.example.data.MachineType.WOOD_CUTTER.powerCostPerMinute * 2 +
                com.example.data.MachineType.CONVEYOR_BELT.powerCostPerMinute * 2,
            state.totalMachinePowerCostPerMinute
        )

        // Fleet expansions
        assertTrue(state.warehouseStockCapacity > 60)
        assertEquals(4, state.maxActiveContractsAllowed)
        val noExcavator = state.copy(fleetLevels = emptyMap())
        assertTrue(state.passiveIncomePerSecond > noExcavator.passiveIncomePerSecond)

        // Buyer pricing & reputation
        assertEquals(3, state.buyerReputationLevel(com.example.data.BuyerCompany.OAKHEART_TIMBER))
        val specialtyPrice = state.buyerOfferUnitPrice(com.example.data.BuyerCompany.OAKHEART_TIMBER, "planks")
        val nonSpecialtyPrice = state.buyerOfferUnitPrice(com.example.data.BuyerCompany.VULCAN_FOUNDRY, "planks")
        assertTrue(specialtyPrice > nonSpecialtyPrice)
        assertEquals(25, state.availableForTrade("planks"))

        // Contract generation & expiration
        val offers = com.example.data.ContractGenerator.generateOffers(state, seedOffset = 3L)
        assertTrue(offers.isNotEmpty())
        val rush = com.example.data.ContractOrder(
            id = "c_test",
            buyerId = com.example.data.BuyerCompany.OAKHEART_TIMBER.id,
            goodId = "planks",
            quantity = 10,
            tier = com.example.data.ContractTier.RUSH,
            rewardCoins = 500L,
            rewardXp = 80L,
            rewardRep = 30,
            acceptedEpochMs = 1000L,
            deadlineEpochMs = 5000L
        )
        assertFalse(rush.isExpired(4000L))
        assertTrue(rush.isExpired(6000L))

        val withContracts = state.copy(
            activeContracts = listOf(rush),
            availableContracts = offers,
            totalContractsCompleted = 4
        )
        val decoded = repo.deserialize(repo.serialize(withContracts))
        assertEquals(2, decoded.machineLevel(com.example.data.MachineType.WOOD_CUTTER))
        assertFalse(decoded.isMachineRunning(com.example.data.MachineType.STONE_CRUSHER))
        assertEquals(25, decoded.warehouseStock["planks"])
        assertEquals(4, decoded.fleetLevel(com.example.data.FleetEquipType.DELIVERY_TRUCK))
        assertEquals(110, decoded.buyerReputation(com.example.data.BuyerCompany.OAKHEART_TIMBER))
        assertEquals(1.35, decoded.marketMultiplier("planks"), 0.01)
        assertEquals(1, decoded.activeContracts.size)
        assertEquals(offers.size, decoded.availableContracts.size)
        assertEquals(4, decoded.totalContractsCompleted)
    }
}
