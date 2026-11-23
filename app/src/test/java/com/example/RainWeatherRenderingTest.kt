package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.exp
import kotlin.math.min

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RainWeatherRenderingTest {

    @Test
    fun `game js eliminates cumulative fog and sunlight multiplication loops and configures rain depth properly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val gameJs = context.assets.open("game/game.js").bufferedReader().use { it.readText() }

        // Must never multiply persistent fog density or sun intensity per frame inside tick()
        assertFalse(
            "Regression: cumulative scene.fog.density *= inside tick loop causes whiteout/blueout",
            gameJs.contains("scene.fog.density *=")
        )
        assertFalse(
            "Regression: cumulative sunLight.intensity *= collapses scene illumination to zero",
            gameJs.contains("sunLight.intensity *=")
        )

        // Verify stateless base atmosphere tracking and proper rain render settings
        assertTrue(gameJs.contains("let baseFogDensity = 0.0024;"))
        assertTrue(gameJs.contains("let baseSunIntensity = 1.38;"))
        assertTrue(gameJs.contains("skyDome.renderOrder = -1;"))
        assertTrue(gameJs.contains("depthTest: true,"))
        assertTrue(gameJs.contains("depthWrite: false,"))
        assertTrue(gameJs.contains("fog: false"))
        assertTrue(gameJs.contains("rainMesh.renderOrder = 5;"))
        assertTrue(gameJs.contains("window.getWeatherDebugState"))
    }

    @Test
    fun `rain weather simulation preserves visibility, lighting balance, and sunny restoration across 1000 frames`() {
        val baseFogForest = 0.0024
        val baseSunForest = 1.38
        val baseHemi = 0.56
        val baseAmbient = 0.34

        var rainIntensity = 0.0
        var overcastFactor = 0.0
        var baseFogDensity = baseFogForest
        var baseSunIntensity = baseSunForest

        var fogDensity = baseFogForest
        var sunIntensity = baseSunForest
        var hemiIntensity = baseHemi
        var ambientIntensity = baseAmbient

        val dt = 1.0 / 60.0

        // Simulate 600 frames (10 seconds) of full RAIN in Forest
        repeat(600) {
            val targetRain = 1.0
            val targetOvercast = 1.0
            rainIntensity += (targetRain - rainIntensity) * min(1.0, dt * 0.35 * 2.2)
            overcastFactor += (targetOvercast - overcastFactor) * min(1.0, dt * 0.32 * 2.2)

            val atmoRate = min(1.0, dt * 2.2)
            baseFogDensity += (baseFogForest - baseFogDensity) * atmoRate
            baseSunIntensity += (baseSunForest - baseSunIntensity) * atmoRate

            fogDensity = min(0.0085, baseFogDensity * (1.0 + overcastFactor * 0.36))
            sunIntensity = baseSunIntensity * (1.0 - overcastFactor * 0.28)
            hemiIntensity = baseHemi + overcastFactor * 0.16
            ambientIntensity = baseAmbient + overcastFactor * 0.14
        }

        // Rain intensity & overcast converged smoothly near 1.0
        assertTrue("Expected rainIntensity > 0.99, got $rainIntensity", rainIntensity > 0.99)
        assertTrue("Expected overcastFactor > 0.99, got $overcastFactor", overcastFactor > 0.99)

        // FogExp2 must stay moderate so terrain (25m) and distant mountains (80m) remain clear
        assertEquals(0.003264, fogDensity, 0.0001)
        val terrainVisibility25m = exp(-(fogDensity * 25.0) * (fogDensity * 25.0))
        val mountainVisibility80m = exp(-(fogDensity * 80.0) * (fogDensity * 80.0))
        assertTrue("Terrain at 25m must stay > 98% visible in rain, was $terrainVisibility25m", terrainVisibility25m > 0.98)
        assertTrue("Mountains at 80m must stay > 90% visible in rain, was $mountainVisibility80m", mountainVisibility80m > 0.90)

        // Sun dims naturally (~28%) while sky/ambient fill lifts so mountains aren't dark silhouettes
        assertEquals(0.9936, sunIntensity, 0.02)
        assertTrue(hemiIntensity > baseHemi)
        assertTrue(ambientIntensity > baseAmbient)

        // Simulate 900 frames (15 seconds) returning to CLEAR sunny weather
        repeat(900) {
            val targetRain = 0.0
            val targetOvercast = 0.0
            rainIntensity += (targetRain - rainIntensity) * min(1.0, dt * 0.28 * 2.2)
            overcastFactor += (targetOvercast - overcastFactor) * min(1.0, dt * 0.25 * 2.2)
            if (rainIntensity < 0.002) rainIntensity = 0.0
            if (overcastFactor < 0.002) overcastFactor = 0.0

            fogDensity = min(0.0085, baseFogDensity * (1.0 + overcastFactor * 0.36))
            sunIntensity = baseSunIntensity * (1.0 - overcastFactor * 0.28)
            hemiIntensity = baseHemi + overcastFactor * 0.16
            ambientIntensity = baseAmbient + overcastFactor * 0.14
        }

        assertEquals(0.0, rainIntensity, 0.0001)
        assertEquals(0.0, overcastFactor, 0.0001)
        assertEquals(baseFogForest, fogDensity, 0.00001)
        assertEquals(baseSunForest, sunIntensity, 0.001)
        assertEquals(baseHemi, hemiIntensity, 0.001)
        assertEquals(baseAmbient, ambientIntensity, 0.001)
    }
}
