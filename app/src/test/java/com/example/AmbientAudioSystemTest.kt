package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AmbientAudioEngine
import com.example.audio.AmbientTelemetry
import com.example.audio.AudioMixCalculator
import com.example.audio.AudioMixSettings
import com.example.audio.WeatherType
import com.example.data.GameRepository
import com.example.data.TycoonSaveState
import com.example.ui.sheets.SettingsSheet
import com.example.ui.theme.WildcraftTheme
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
class AmbientAudioSystemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `nature ambience responds to biome suitability, indoor attenuation, and rain ducking`() {
        val settings = AudioMixSettings(
            isMuted = false,
            masterVolume = 1.0f,
            ambientVolume = 0.8f,
            weatherVolume = 0.75f,
            waterVolume = 0.8f
        )

        val forestClear = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(
                biomeId = "forest",
                weather = WeatherType.CLEAR,
                indoorFactor = 0f,
                isMenuMode = false
            ),
            settings = settings,
            isAppBackgrounded = false
        )
        val volcanicClear = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(
                biomeId = "volcanic_lands",
                weather = WeatherType.CLEAR,
                indoorFactor = 0f,
                isMenuMode = false
            ),
            settings = settings,
            isAppBackgrounded = false
        )
        val forestIndoors = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(
                biomeId = "forest",
                weather = WeatherType.CLEAR,
                indoorFactor = 0.88f,
                isMenuMode = false
            ),
            settings = settings,
            isAppBackgrounded = false
        )
        val fullyIndoors = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(
                biomeId = "forest",
                weather = WeatherType.CLEAR,
                indoorFactor = 1.0f,
                isMenuMode = false
            ),
            settings = settings,
            isAppBackgrounded = false
        )
        val forestRain = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(
                biomeId = "forest",
                weather = WeatherType.RAIN,
                rainIntensity = 1.0f,
                indoorFactor = 0f,
                isMenuMode = false
            ),
            settings = settings,
            isAppBackgrounded = false
        )

        assertEquals(0.8f, forestClear.natureGain, 0.01f)
        assertTrue(forestClear.natureGain > volcanicClear.natureGain)
        assertTrue(forestIndoors.natureGain < forestClear.natureGain * 0.3f)
        assertEquals(0f, fullyIndoors.natureGain, 0.001f)
        assertTrue(forestRain.natureGain < forestClear.natureGain)
        assertEquals(0f, forestClear.rainGain, 0.001f)
    }

    @Test
    fun `rain audio only plays during WeatherType RAIN and never during CLEAR or OVERCAST`() {
        val settings = AudioMixSettings(masterVolume = 1.0f, weatherVolume = 0.9f)

        val clearMix = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(
                biomeId = "forest",
                weather = WeatherType.CLEAR,
                rainIntensity = 0.5f,
                isMenuMode = false
            ),
            settings = settings,
            isAppBackgrounded = false
        )
        val overcastMix = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(
                biomeId = "forest",
                weather = WeatherType.OVERCAST,
                rainIntensity = 0.5f,
                isMenuMode = false
            ),
            settings = settings,
            isAppBackgrounded = false
        )
        val rainMix = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(
                biomeId = "forest",
                weather = WeatherType.RAIN,
                rainIntensity = 0.8f,
                isMenuMode = false
            ),
            settings = settings,
            isAppBackgrounded = false
        )

        assertEquals(0f, clearMix.rainGain, 0.001f)
        assertEquals(0f, overcastMix.rainGain, 0.001f)
        assertEquals(0.72f, rainMix.rainGain, 0.01f)
    }

    @Test
    fun `distance-based river audio attenuates smoothly and stops beyond max distance`() {
        val settings = AudioMixSettings(masterVolume = 1.0f, waterVolume = 0.85f)

        val rightAtBank = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(nearestRiverDist = 1.5f, riverMaxDist = 14f, isMenuMode = false),
            settings = settings,
            isAppBackgrounded = false
        )
        val midDistance = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(nearestRiverDist = 7.5f, riverMaxDist = 14f, isMenuMode = false),
            settings = settings,
            isAppBackgrounded = false
        )
        val nearEdge = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(nearestRiverDist = 12.5f, riverMaxDist = 14f, isMenuMode = false),
            settings = settings,
            isAppBackgrounded = false
        )
        val farAway = AudioMixCalculator.computeTargetMix(
            telemetry = AmbientTelemetry(nearestRiverDist = 14.0f, riverMaxDist = 14f, isMenuMode = false),
            settings = settings,
            isAppBackgrounded = false
        )

        assertEquals(0.85f, rightAtBank.riverGain, 0.01f)
        assertTrue(rightAtBank.riverGain > midDistance.riverGain)
        assertTrue(midDistance.riverGain > nearEdge.riverGain)
        assertTrue(nearEdge.riverGain > 0f)
        assertEquals(0f, farAway.riverGain, 0.001f)
    }

    @Test
    fun `mute, overall zero, background state, and smooth fades behave correctly`() {
        val activeTelemetry = AmbientTelemetry(
            biomeId = "mountains",
            weather = WeatherType.RAIN,
            rainIntensity = 1.0f,
            nearestRiverDist = 2.0f,
            riverMaxDist = 14f,
            isMenuMode = false
        )
        val settings = AudioMixSettings()

        val muted = AudioMixCalculator.computeTargetMix(
            telemetry = activeTelemetry,
            settings = settings.copy(isMuted = true),
            isAppBackgrounded = false
        )
        assertEquals(0f, muted.natureGain, 0.001f)
        assertEquals(0f, muted.rainGain, 0.001f)
        assertEquals(0f, muted.riverGain, 0.001f)

        val backgrounded = AudioMixCalculator.computeTargetMix(
            telemetry = activeTelemetry,
            settings = settings,
            isAppBackgrounded = true
        )
        assertEquals(0f, backgrounded.natureGain, 0.001f)
        assertEquals(0f, backgrounded.rainGain, 0.001f)
        assertEquals(0f, backgrounded.riverGain, 0.001f)

        val overallZero = AudioMixCalculator.computeTargetMix(
            telemetry = activeTelemetry,
            settings = settings.copy(masterVolume = 0f),
            isAppBackgrounded = false
        )
        assertEquals(0f, overallZero.natureGain, 0.001f)
        assertEquals(0f, overallZero.rainGain, 0.001f)
        assertEquals(0f, overallZero.riverGain, 0.001f)

        // Smooth fade-in and fade-out without sudden spikes
        var gain = 0f
        repeat(10) {
            val prev = gain
            gain = AudioMixCalculator.stepFade(current = gain, target = 0.8f, dtSeconds = 0.05f)
            assertTrue(gain >= prev)
            assertTrue(gain - prev < 0.2f)
        }
        assertTrue(gain in 0.1f..0.75f)

        repeat(200) {
            gain = AudioMixCalculator.stepFade(current = gain, target = 0f, dtSeconds = 0.05f)
        }
        assertEquals(0f, gain, 0.0001f)
    }

    @Test
    fun `bundled audio assets are discovered and repository persists audio settings with backward compatibility`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = AmbientAudioEngine(context)
        try {
            assertEquals("audio/Flowing_river_ambience_a413463b.mp3", engine.discoveredAssets["river"])
            assertEquals("audio/Rain_ambience_fcacbf8a.mp3", engine.discoveredAssets["rain"])
        } finally {
            engine.release()
        }

        val repo = GameRepository(context)
        val saved = TycoonSaveState(
            isMuted = false,
            masterVolume = 0.85f,
            ambientVolume = 0.62f,
            weatherVolume = 0.44f,
            waterVolume = 0.91f,
            sfxVolume = 0.55f,
            musicVolume = 0.35f
        )
        val decoded = repo.deserialize(repo.serialize(saved))
        assertFalse(decoded.isMuted)
        assertEquals(0.85f, decoded.masterVolume, 0.005f)
        assertEquals(0.62f, decoded.ambientVolume, 0.005f)
        assertEquals(0.44f, decoded.weatherVolume, 0.005f)
        assertEquals(0.91f, decoded.waterVolume, 0.005f)

        // Legacy save JSON without the 4 new mixer fields must default cleanly
        val legacyJson = """{"coins":100,"isMuted":false,"sfxVolume":0.8,"musicVolume":0.4}"""
        val legacyDecoded = repo.deserialize(legacyJson)
        assertEquals(1.0f, legacyDecoded.masterVolume, 0.005f)
        assertEquals(0.70f, legacyDecoded.ambientVolume, 0.005f)
        assertEquals(0.75f, legacyDecoded.weatherVolume, 0.005f)
        assertEquals(0.80f, legacyDecoded.waterVolume, 0.005f)
    }

    @Test
    fun `settings sheet renders all audio mixer controls and mute disables sliders`() {
        val saveState = androidx.compose.runtime.mutableStateOf(
            TycoonSaveState(
                isMuted = false,
                masterVolume = 0.9f,
                ambientVolume = 0.7f,
                weatherVolume = 0.75f,
                waterVolume = 0.8f
            )
        )
        composeTestRule.setContent {
            WildcraftTheme {
                SettingsSheet(
                    state = saveState.value,
                    showReturnToHome = true,
                    onToggleMute = {
                        saveState.value = saveState.value.copy(isMuted = !saveState.value.isMuted)
                    },
                    onMasterVolumeChange = {},
                    onAmbientVolumeChange = {},
                    onWeatherVolumeChange = {},
                    onWaterVolumeChange = {},
                    onSfxVolumeChange = {},
                    onMusicVolumeChange = {},
                    onGraphicsQualityChange = {},
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
        composeTestRule.onNodeWithTag("btn_toggle_mute").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("slider_master_volume").performScrollTo().assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("slider_ambient_volume").performScrollTo().assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("slider_weather_volume").performScrollTo().assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("slider_water_volume").performScrollTo().assertIsDisplayed().assertIsEnabled()

        composeTestRule.onNodeWithTag("btn_toggle_mute").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue(saveState.value.isMuted)

        composeTestRule.onNodeWithTag("slider_master_volume").performScrollTo().assertIsNotEnabled()
        composeTestRule.onNodeWithTag("slider_ambient_volume").performScrollTo().assertIsNotEnabled()
        composeTestRule.onNodeWithTag("slider_weather_volume").performScrollTo().assertIsNotEnabled()
        composeTestRule.onNodeWithTag("slider_water_volume").performScrollTo().assertIsNotEnabled()
    }
}
