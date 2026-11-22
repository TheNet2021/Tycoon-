package com.example

import com.example.data.BiomeType
import com.example.data.BuildingType
import com.example.data.DrivableVehicleType
import com.example.data.MachineType
import com.example.data.ProductType
import com.example.data.QuestCatalog
import com.example.data.QuestRequirement
import com.example.data.ResourceType
import com.example.data.TycoonSaveState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun worldExpansion_hasTwelveUniqueRegionsAndNewResources() {
    assertEquals(12, BiomeType.entries.size)
    assertEquals(12, BiomeType.entries.map { it.id }.toSet().size)
    assertNotNull(BiomeType.fromId("crystal_desert"))
    assertNotNull(BiomeType.fromId("sky_islands"))
    assertNotNull(BiomeType.fromId("toxic_swamp"))
    assertNotNull(BiomeType.fromId("frozen_mountains"))
    assertNotNull(BiomeType.fromId("volcanic_lands"))
    assertNotNull(BiomeType.fromId("ancient_machine_world"))

    assertEquals(14, ResourceType.entries.size)
    assertNotNull(ResourceType.fromId("quartz"))
    assertNotNull(ResourceType.fromId("aether"))
    assertNotNull(ResourceType.fromId("venom"))
    assertNotNull(ResourceType.fromId("frostite"))
    assertNotNull(ResourceType.fromId("obsidian"))
    assertNotNull(ResourceType.fromId("titan_alloy"))

    assertEquals(15, BuildingType.entries.size)
    assertEquals(12, MachineType.entries.size)
    assertNotNull(ProductType.fromId("aether_core"))
    assertNotNull(ProductType.fromId("titan_plate"))
  }

  @Test
  fun drivableVehicles_applyCapacityAndArmorBonusesCorrectly() {
    assertEquals(3, DrivableVehicleType.entries.size)
    val baseState = TycoonSaveState(backpackLevel = 1, axeLevel = 1)
    assertEquals(12, baseState.backpackMaxCapacity)
    assertEquals(100, baseState.playerMaxHp)
    assertFalse(baseState.isVehicleUnlocked(DrivableVehicleType.HEAVY_HAULER))

    val haulerState = baseState.copy(
      unlockedVehicles = setOf(DrivableVehicleType.HEAVY_HAULER.id),
      activeVehicleId = DrivableVehicleType.HEAVY_HAULER.id,
      backpackLevel = 4,
      axeLevel = 3
    )
    assertTrue(haulerState.isVehicleUnlocked(DrivableVehicleType.HEAVY_HAULER))
    assertEquals(56 + 45, haulerState.backpackMaxCapacity)
    assertEquals(100 + 30 + 10, haulerState.playerMaxHp)
  }

  @Test
  fun explorationAndCombatQuests_trackSecretsInsectsBossesAndNewBiomes() {
    val state = TycoonSaveState(
      unlockedBiomes = setOf("forest", "crystal_desert", "sky_islands", "ancient_machine_world"),
      discoveredSecrets = setOf("cave_forest", "cave_mountains", "cave_desert"),
      insectsDefeated = 6,
      defeatedBosses = setOf("stag_goliath")
    )
    val progressById = state.evaluateQuests().associateBy { it.quest.id }

    assertTrue(progressById.getValue("q_defeat_insects_5").isCompleted)
    assertTrue(progressById.getValue("q_discover_caves_3").isCompleted)
    assertTrue(progressById.getValue("q_defeat_boss_1").isCompleted)
    assertTrue(progressById.getValue("q_unlock_crystal_desert").isCompleted)
    assertTrue(progressById.getValue("q_unlock_sky_islands").isCompleted)
    assertTrue(progressById.getValue("q_unlock_ancient_machine").isCompleted)
    assertTrue(
      QuestCatalog.all.any { it.requirement is QuestRequirement.DiscoverSecrets } &&
        QuestCatalog.all.any { it.requirement is QuestRequirement.DefeatInsects } &&
        QuestCatalog.all.any { it.requirement is QuestRequirement.DefeatBosses }
    )
  }
}
