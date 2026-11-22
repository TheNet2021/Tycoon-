package com.example

import com.example.data.HousingBuilding
import com.example.data.HousingType
import com.example.data.RefugeeArrival
import com.example.data.SettlementActionOutcome
import com.example.data.SettlementContractCategory
import com.example.data.SettlementEngine
import com.example.data.SettlementState
import com.example.data.SettlementTransactionType
import com.example.data.SupplyRestockKind
import com.example.data.SurvivorProfession
import com.example.data.SurvivorProfile
import com.example.data.TradeCatalog
import com.example.data.TycoonSaveState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettlementSystemTest {

    @Test
    fun housingTiers_haveExpectedCapacitiesAndRespectBuildTimersAndDemolitionGuard() {
        assertEquals(2, HousingType.SMALL_TENT.capacity)
        assertEquals(5, HousingType.WOODEN_CABIN.capacity)
        assertEquals(10, HousingType.STONE_HOUSE.capacity)
        assertEquals(25, HousingType.APARTMENT_BUILDING.capacity)
        assertEquals(50, HousingType.RESIDENTIAL_COMPLEX.capacity)

        val now = 1_000_000L
        val initial = SettlementEngine.ensureInitialized(
            TycoonSaveState(
                coins = 500L,
                warehouseStock = mapOf("wood" to 20)
            ),
            now
        )

        val buildRes = SettlementEngine.startHousingConstruction(initial, HousingType.SMALL_TENT, now)
        assertTrue(buildRes is SettlementActionOutcome.Success)
        val afterStart = (buildRes as SettlementActionOutcome.Success).updatedState

        // Coins and wood deducted immediately, but incomplete housing grants 0 beds
        assertEquals(500L - HousingType.SMALL_TENT.coinCost, afterStart.coins)
        assertEquals(14, afterStart.availableForTrade("wood"))
        assertEquals(0, afterStart.settlement.totalHousingCapacity)
        assertEquals(2, afterStart.settlement.pendingConstructionCapacity)

        // Before timer finishes, still incomplete
        val midTick = SettlementEngine.tick(afterStart, now + 4_000L, allowArrivalSpawn = false).first
        assertEquals(0, midTick.settlement.totalHousingCapacity)

        // After timer completes, capacity becomes 2
        val doneTick = SettlementEngine.tick(
            midTick,
            now + HousingType.SMALL_TENT.buildTimeSeconds * 1000L + 100L,
            allowArrivalSpawn = false
        ).first
        assertEquals(2, doneTick.settlement.totalHousingCapacity)
        assertEquals(0, doneTick.settlement.pendingConstructionCapacity)

        // Admit a survivor into the tent, then demolition must fail because remaining cap < population
        val arrivalId = doneTick.settlement.pendingArrivals.first().id
        val admitRes = SettlementEngine.admitRefugee(doneTick, arrivalId, now + 10_000L)
        assertTrue(admitRes is SettlementActionOutcome.Success)
        val withResident = (admitRes as SettlementActionOutcome.Success).updatedState
        assertEquals(1, withResident.settlement.survivors.size)

        val houseId = withResident.settlement.housingUnits.first().id
        val demolishRes = SettlementEngine.demolishHousing(withResident, houseId)
        assertTrue(demolishRes is SettlementActionOutcome.Failure)
    }

    @Test
    fun populationManagement_preventsExceedingHousingCapacity() {
        val now = 2_000_000L
        val tent = HousingBuilding(
            id = "tent_1",
            type = HousingType.SMALL_TENT,
            startedEpochMs = now - 20_000L,
            completesEpochMs = now - 10_000L,
            isCompleted = true
        )
        val arrivals = listOf(
            RefugeeArrival("a1", "Elena Vance", SurvivorProfession.FARMER, 2, now),
            RefugeeArrival("a2", "Marcus Okafor", SurvivorProfession.MINER, 3, now),
            RefugeeArrival("a3", "Nadia Lind", SurvivorProfession.TRADER, 2, now)
        )
        var state = TycoonSaveState(
            settlement = SettlementState(
                housingUnits = listOf(tent),
                pendingArrivals = arrivals,
                lastDailyTickEpochMs = now,
                lastArrivalEpochMs = now,
                nextSequenceId = 10L
            )
        )

        // Small Tent has capacity 2 -> first two succeed, third is blocked cleanly
        val res1 = SettlementEngine.admitRefugee(state, "a1", now)
        assertTrue(res1 is SettlementActionOutcome.Success)
        state = (res1 as SettlementActionOutcome.Success).updatedState

        val res2 = SettlementEngine.admitRefugee(state, "a2", now)
        assertTrue(res2 is SettlementActionOutcome.Success)
        state = (res2 as SettlementActionOutcome.Success).updatedState

        assertEquals(2, state.settlement.survivors.size)
        assertEquals(0, state.settlement.availableHousingSlots)
        assertEquals(2, state.settlement.occupantsIn("tent_1"))

        val res3 = SettlementEngine.admitRefugee(state, "a3", now)
        assertTrue(res3 is SettlementActionOutcome.Failure)
        assertEquals(2, state.settlement.survivors.size)
    }

    @Test
    fun survivorTrading_enforcesNoArbitrageSpreadDynamicDemandAndSafeInventoryUpdates() {
        val now = 3_000_000L
        val smallState = SettlementState(
            survivors = List(2) { i ->
                SurvivorProfile("s$i", "Settler $i", SurvivorProfession.FARMER, 1, housingUnitId = "h1")
            }
        )
        val largeState = SettlementState(
            survivors = List(20) { i ->
                SurvivorProfile("s$i", "Settler $i", SurvivorProfession.FARMER, 1, housingUnitId = "h1")
            }
        )

        // Demand scales up gently with population and ask price always exceeds bid price (no infinite money arbitrage)
        assertTrue(largeState.demandMultiplierFor("wood") > smallState.demandMultiplierFor("wood"))
        for (good in TradeCatalog.allGoods) {
            val bid = largeState.survivorBidPrice(good, globalMarketMult = 1.25)
            val ask = largeState.survivorAskPrice(good, globalMarketMult = 1.0)
            assertTrue("Ask ($ask) must exceed bid ($bid) for ${good.id}", ask > bid)
        }

        val tent = HousingBuilding("h1", HousingType.SMALL_TENT, now - 10_000L, now - 1_000L, true)
        val initial = SettlementEngine.ensureInitialized(
            TycoonSaveState(
                coins = 1000L,
                warehouseStock = mapOf("wood" to 25),
                settlement = SettlementState(
                    housingUnits = listOf(tent),
                    survivors = listOf(
                        SurvivorProfile("s1", "Elena", SurvivorProfession.TRADER, 2, housingUnitId = "h1")
                    ),
                    lastDailyTickEpochMs = now,
                    lastArrivalEpochMs = now
                )
            ),
            now
        )

        // Sell 10 wood to settlers -> coins/xp rise, wood drops, demand saturation rises, ledger logs tx
        val sellOutcome = SettlementEngine.sellGoodToSurvivors(initial, "wood", 10, now)
        assertTrue(sellOutcome is SettlementActionOutcome.Success)
        val afterSell = (sellOutcome as SettlementActionOutcome.Success).updatedState
        assertEquals(15, afterSell.availableForTrade("wood"))
        assertTrue(afterSell.coins > initial.coins)
        assertTrue(afterSell.xp > initial.xp)
        assertTrue((afterSell.settlement.demandSaturation["wood"] ?: 0.0) > 0.0)
        assertEquals(
            SettlementTransactionType.SOLD_TO_SURVIVORS,
            afterSell.settlement.transactionHistory.first().type
        )

        // Buy from first available settler offer
        val offer = afterSell.settlement.survivorOffersForSale.first()
        val beforeGoodQty = afterSell.availableForTrade(offer.goodId)
        val beforeCoins = afterSell.coins
        val buyOutcome = SettlementEngine.buyGoodFromSurvivors(afterSell, offer.goodId, 1, now + 500L)
        assertTrue(buyOutcome is SettlementActionOutcome.Success)
        val afterBuy = (buyOutcome as SettlementActionOutcome.Success).updatedState
        assertEquals(beforeCoins - offer.unitAskPrice, afterBuy.coins)
        assertEquals(beforeGoodQty + 1, afterBuy.availableForTrade(offer.goodId))
        assertEquals(
            SettlementTransactionType.BOUGHT_FROM_SURVIVORS,
            afterBuy.settlement.transactionHistory.first().type
        )
    }

    @Test
    fun employmentAndDailyCycle_calculateSalariesProductionAndPreventInfiniteCrafting() {
        val now = 4_000_000L
        val house = HousingBuilding("h_cabin", HousingType.WOODEN_CABIN, now - 30_000L, now - 5_000L, true)
        val farmer = SurvivorProfile("f1", "Maya", SurvivorProfession.FARMER, skillLevel = 2, housingUnitId = "h_cabin")
        val miner = SurvivorProfile("m1", "Darius", SurvivorProfession.MINER, skillLevel = 2, housingUnitId = "h_cabin")
        val factory = SurvivorProfile("w1", "Kieran", SurvivorProfession.FACTORY_WORKER, skillLevel = 2, housingUnitId = "h_cabin")
        val idle = SurvivorProfile("i1", "Lina", SurvivorProfession.TRADER, skillLevel = 1, housingUnitId = "h_cabin")

        // Matched vs unmatched efficiency check (+35% specialty match bonus)
        val matched = factory.copy(assignedJob = SurvivorProfession.FACTORY_WORKER)
        val mismatched = factory.copy(assignedJob = SurvivorProfession.MINER)
        assertTrue(matched.effectiveEfficiencyMultiplier() > mismatched.effectiveEfficiencyMultiplier() * 1.30)

        var state = TycoonSaveState(
            coins = 400L,
            warehouseStock = mapOf("wood" to 4),
            settlement = SettlementState(
                housingUnits = listOf(house),
                survivors = listOf(farmer, miner, factory, idle),
                foodStock = 10,
                waterStock = 10,
                suppliesStock = 10,
                lastDailyTickEpochMs = now,
                lastArrivalEpochMs = now
            )
        )

        state = (SettlementEngine.assignSurvivorJob(state, "f1", SurvivorProfession.FARMER) as SettlementActionOutcome.Success).updatedState
        state = (SettlementEngine.assignSurvivorJob(state, "m1", SurvivorProfession.MINER) as SettlementActionOutcome.Success).updatedState
        state = (SettlementEngine.assignSurvivorJob(state, "w1", SurvivorProfession.FACTORY_WORKER) as SettlementActionOutcome.Success).updatedState

        val stats = state.settlement.computeStats()
        assertEquals(4, stats.population)
        assertEquals(3, stats.employedCount)
        assertEquals(1, stats.unemployedCount)
        assertEquals(25, stats.unemploymentRatePercent)
        assertEquals(
            12L + 3L + 16L + 3L + 18L + 3L,
            stats.dailyWorkerSalaries
        )
        assertEquals(HousingType.WOODEN_CABIN.dailyMaintenanceCost, stats.dailyHousingMaintenance)

        // Daily cycle consumes maintenance/salaries, runs factory conversion only with real wood inputs, and generates stone
        val afterDay = SettlementEngine.processDailyCycle(state, now + SettlementEngine.DAY_DURATION_MS)
        assertEquals(2, afterDay.settlement.dayNumber)
        assertEquals(400L - stats.totalDailyMaintenanceCost, afterDay.coins)
        assertTrue((afterDay.warehouseStock["planks"] ?: 0) >= 1)
        assertTrue((afterDay.warehouseStock["stone"] ?: 0) >= 1)
        assertTrue(afterDay.settlement.foodStock > 10)

        // Verify factory worker cannot synthesize products out of thin air when inputs are 0
        val emptyInputState = afterDay.copy(warehouseStock = emptyMap())
        val afterEmptyDay = SettlementEngine.processDailyCycle(emptyInputState, now + SettlementEngine.DAY_DURATION_MS * 2)
        assertEquals(0, afterEmptyDay.warehouseStock["planks"] ?: 0)
        assertEquals(0, afterEmptyDay.warehouseStock["metal_bars"] ?: 0)
    }

    @Test
    fun constructionContracts_coverAllCategoriesAndAwardRewardsSafely() {
        val now = 5_000_000L
        val initialized = SettlementEngine.ensureInitialized(
            TycoonSaveState(
                coins = 500L,
                warehouseStock = mapOf(
                    "wood" to 50,
                    "planks" to 50,
                    "stone" to 50,
                    "stone_blocks" to 50,
                    "wood_craft" to 50,
                    "metal_bars" to 50
                )
            ),
            now
        )

        assertEquals(5, SettlementContractCategory.entries.size)
        val firstContract = initialized.settlement.openContracts.first()
        val outcome = SettlementEngine.completeConstructionContract(initialized, firstContract.id, now)
        assertTrue(outcome is SettlementActionOutcome.Success)
        val updated = (outcome as SettlementActionOutcome.Success).updatedState

        assertEquals(500L + firstContract.rewardCoins, updated.coins)
        assertEquals(firstContract.rewardXp, updated.xp)
        assertEquals(firstContract.rewardReputation, updated.settlement.reputation)
        assertEquals(1, updated.settlement.completedContractsCount)
        assertEquals(4, updated.settlement.openContracts.size)
        assertEquals(
            SettlementTransactionType.CONTRACT_PAYOUT,
            updated.settlement.transactionHistory.first().type
        )
        // Restock community supplies works cleanly too
        val depleted = updated.copy(settlement = updated.settlement.copy(foodStock = 5))
        val restock = SettlementEngine.buyCommunitySupplies(depleted, SupplyRestockKind.FOOD, 1, now)
        assertNotNull(restock as? SettlementActionOutcome.Success)
        val afterRestock = (restock as SettlementActionOutcome.Success).updatedState
        assertEquals(15, afterRestock.settlement.foodStock)
        assertFalse(afterRestock.coins == depleted.coins)
    }
}
