package com.example

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.BiomeType
import com.example.data.BuildingType
import com.example.data.GameRepository
import com.example.data.MachineType
import com.example.data.TycoonSaveState
import com.example.ui.components.HomeMenuOverlay
import com.example.ui.sheets.ProfileSheet
import com.example.ui.sheets.SettingsSheet
import com.example.ui.theme.WildcraftTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainMenuAndSettingsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `home menu displays separated start screen buttons and omits in-game HUD`() {
        val savedState = TycoonSaveState(
            coins = 850L,
            lifetimeEarned = 1400L,
            xp = 240L,
            unlockedBiomes = setOf("forest", "mountains")
        )

        composeTestRule.setContent {
            WildcraftTheme {
                HomeMenuOverlay(
                    state = savedState,
                    activeBiome = BiomeType.FOREST,
                    onContinue = {},
                    onNewGame = {},
                    onOpenWorldMap = {},
                    onOpenProfile = {},
                    onOpenAchievements = {},
                    onOpenMarket = {},
                    onOpenSettings = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_home_continue").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("btn_home_new_game").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_home_world_map").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_home_profile").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_home_achievements").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_home_market").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_home_settings").assertIsDisplayed()

        // Ensure in-game HUD widgets and old dashboard buttons are NOT mixed onto Main Menu
        composeTestRule.onNodeWithTag("hud_backpack_pill").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_home_upgrades").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_home_machinery").assertDoesNotExist()
        composeTestRule.onNodeWithTag("btn_home_settlement").assertDoesNotExist()
    }

    @Test
    fun `new game button opens confirmation dialog when save exists and preserves save if cancelled`() {
        var newGameConfirmed = false
        val savedState = TycoonSaveState(
            coins = 1200L,
            lifetimeEarned = 2000L,
            axeLevel = 3
        )

        composeTestRule.setContent {
            WildcraftTheme {
                HomeMenuOverlay(
                    state = savedState,
                    activeBiome = BiomeType.FOREST,
                    onContinue = {},
                    onNewGame = { newGameConfirmed = true },
                    onOpenWorldMap = {},
                    onOpenProfile = {},
                    onOpenAchievements = {},
                    onOpenMarket = {},
                    onOpenSettings = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_home_new_game").performClick()
        composeTestRule.waitForIdle()

        // Dialog must appear before overwriting
        composeTestRule.onNodeWithTag("dialog_confirm_new_game").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_cancel_new_game").performClick()
        composeTestRule.waitForIdle()
        assertFalse(newGameConfirmed)

        // Now confirm overwrite
        composeTestRule.onNodeWithTag("btn_home_new_game").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_confirm_new_game").performClick()
        composeTestRule.waitForIdle()
        assertTrue(newGameConfirmed)
    }

    @Test
    fun `continue disabled cleanly when slot has no saved progress`() {
        val emptyState = TycoonSaveState()
        composeTestRule.setContent {
            WildcraftTheme {
                HomeMenuOverlay(
                    state = emptyState,
                    activeBiome = BiomeType.FOREST,
                    onContinue = {},
                    onNewGame = {},
                    onOpenWorldMap = {},
                    onOpenProfile = {},
                    onOpenAchievements = {},
                    onOpenMarket = {},
                    onOpenSettings = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_home_continue").assertIsDisplayed().assertIsNotEnabled()
        composeTestRule.onNodeWithTag("btn_home_new_game").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun `profile sheet displays player level, xp, money, unlocked areas, buildings, resources, playtime and achievements`() {
        val profileState = TycoonSaveState(
            coins = 4320L,
            lifetimeEarned = 9800L,
            xp = 360L,
            unlockedBiomes = setOf("forest", "mountains", "desert"),
            buildingLevels = mapOf(BuildingType.SAWMILL.id to 2, BuildingType.WAREHOUSE.id to 1),
            machineLevels = mapOf(MachineType.WOOD_CUTTER.id to 1),
            gatheredTotals = mapOf("wood" to 42, "stone" to 19),
            claimedQuestIds = setOf("q_gather_wood_1"),
            playtimeSeconds = 3725L
        )

        composeTestRule.setContent {
            WildcraftTheme {
                ProfileSheet(
                    state = profileState,
                    activeSlotIndex = 0,
                    onOpenAchievements = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_level_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_stat_money").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_stat_playtime").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_stat_areas").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_stat_buildings").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_stat_resources").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_stat_achievements").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("3 / 12", substring = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("1h 02m", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `settings sheet renders graphics, shadows, fps, camera controls, language selection and reset confirmation`() {
        var settingsResetCalled = false
        val state = mutableStateOf(
            TycoonSaveState(
                coins = 5000L,
                graphicsQuality = 1,
                shadowQuality = 2,
                fpsLimit = 60,
                cameraSensitivity = 1.2f,
                cameraDistance = 14.0f,
                languageCode = "en"
            )
        )

        composeTestRule.setContent {
            WildcraftTheme {
                SettingsSheet(
                    state = state.value,
                    showReturnToHome = false,
                    onToggleMute = {},
                    onMasterVolumeChange = {},
                    onAmbientVolumeChange = {},
                    onWeatherVolumeChange = {},
                    onWaterVolumeChange = {},
                    onSfxVolumeChange = {},
                    onMusicVolumeChange = {},
                    onGraphicsQualityChange = { state.value = state.value.copy(graphicsQuality = it) },
                    onShadowQualityChange = { state.value = state.value.copy(shadowQuality = it) },
                    onCameraSensitivityChange = { state.value = state.value.copy(cameraSensitivity = it) },
                    onCameraDistanceChange = { state.value = state.value.copy(cameraDistance = it) },
                    onFpsLimitChange = { state.value = state.value.copy(fpsLimit = it) },
                    onLanguageChange = { state.value = state.value.copy(languageCode = it) },
                    onResetSettings = {
                        settingsResetCalled = true
                        state.value = state.value.copy(
                            graphicsQuality = 1,
                            shadowQuality = 1,
                            fpsLimit = 60,
                            languageCode = "en"
                        )
                    },
                    onToggleHaptics = {},
                    onReturnToHome = {},
                    onOpenAdvisor = {},
                    onOpenStudio = {},
                    onResetProgress = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_gfx_0").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_shadow_2").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_fps_30").performScrollTo().performClick()
        assertEquals(30, state.value.fpsLimit)

        composeTestRule.onNodeWithTag("slider_cam_sensitivity").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("slider_cam_distance").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_lang_es").performScrollTo().performClick()
        assertEquals("es", state.value.languageCode)

        composeTestRule.onNodeWithTag("btn_reset_settings").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_confirm_reset_settings").performClick()
        composeTestRule.waitForIdle()
        assertTrue(settingsResetCalled)
        assertEquals("en", state.value.languageCode)
        // Confirm progress coins were untouched by Reset Settings
        assertEquals(5000L, state.value.coins)
    }

    @Test
    fun `repository supports multiple save slots and persists extended settings and coordinates`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)

        val slot0 = TycoonSaveState(
            coins = 950L,
            lifetimeEarned = 1800L,
            shadowQuality = 2,
            cameraSensitivity = 1.6f,
            cameraDistance = 15.5f,
            fpsLimit = 30,
            languageCode = "fr",
            playerPosX = 12.5,
            playerPosZ = -34.0,
            lastBiomeId = "mountains",
            playtimeSeconds = 840L
        )
        val slot1 = TycoonSaveState(
            coins = 320L,
            lifetimeEarned = 400L,
            lastBiomeId = "forest",
            playtimeSeconds = 95L
        )

        repo.saveSlot(0, slot0)
        repo.saveSlot(1, slot1)

        val (loaded0, _) = repo.loadSlot(0)
        val (loaded1, _) = repo.loadSlot(1)

        assertEquals(950L, loaded0.coins)
        assertEquals(2, loaded0.shadowQuality)
        assertEquals(1.6f, loaded0.cameraSensitivity, 0.01f)
        assertEquals(15.5f, loaded0.cameraDistance, 0.01f)
        assertEquals(30, loaded0.fpsLimit)
        assertEquals("fr", loaded0.languageCode)
        assertEquals(12.5, loaded0.playerPosX, 0.01)
        assertEquals(-34.0, loaded0.playerPosZ, 0.01)
        assertEquals("mountains", loaded0.lastBiomeId)
        assertEquals(840L, loaded0.playtimeSeconds)

        assertEquals(320L, loaded1.coins)
        assertEquals("forest", loaded1.lastBiomeId)
        assertEquals(95L, loaded1.playtimeSeconds)

        val slots = repo.listSaveSlots()
        assertEquals(3, slots.size)
        assertTrue(slots[0].hasData)
        assertTrue(slots[1].hasData)
        assertFalse(slots[2].hasData)
    }
}
