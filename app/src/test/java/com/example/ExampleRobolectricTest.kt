package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BiomeType
import com.example.data.BuildingType
import com.example.data.TycoonSaveState
import com.example.data.UniqueQuestItem
import org.junit.Assert.assertEquals
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
        assertEquals(22, state.backpackMaxCapacity) // 12 base + 10 Prism Lantern
    }
}
