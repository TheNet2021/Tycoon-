package com.example.audio

import kotlin.math.exp

enum class WeatherType {
    CLEAR,
    OVERCAST,
    RAIN;

    companion object {
        fun fromWire(value: String?): WeatherType = when (value?.uppercase()) {
            "RAIN" -> RAIN
            "OVERCAST" -> OVERCAST
            else -> CLEAR
        }
    }
}

data class AudioMixSettings(
    val isMuted: Boolean = false,
    val masterVolume: Float = 1.0f,
    val ambientVolume: Float = 0.70f,
    val weatherVolume: Float = 0.75f,
    val waterVolume: Float = 0.80f
)

data class AmbientTelemetry(
    val biomeId: String = "forest",
    val weather: WeatherType = WeatherType.CLEAR,
    val rainIntensity: Float = 0f,
    val nearestRiverDist: Float = 999f,
    val riverMaxDist: Float = 14f,
    val indoorFactor: Float = 0f,
    val isMenuMode: Boolean = true
)

data class TargetAudioMix(
    val natureGain: Float,
    val rainGain: Float,
    val riverGain: Float
) {
    companion object {
        val SILENT = TargetAudioMix(0f, 0f, 0f)
    }
}

object AudioMixCalculator {

    private const val MIN_AUDIBLE = 0.002f

    fun biomeNatureWeight(biomeId: String): Float = when (biomeId) {
        "forest", "jungle" -> 1.0f
        "mountains", "sky_islands" -> 0.82f
        "toxic_swamp" -> 0.62f
        "desert", "crystal_desert" -> 0.35f
        "frozen", "frozen_mountains" -> 0.28f
        "volcanic", "volcanic_lands", "ancient_machine_world" -> 0.12f
        else -> 0.75f
    }

    /**
     * Smoothstep falloff: 1.0 within [fullRadius], decaying smoothly to 0.0 at [maxDist].
     */
    fun computeRiverProximityFactor(
        distance: Float,
        maxDist: Float = 14f,
        fullRadius: Float = 2.4f
    ): Float {
        if (distance.isNaN() || distance.isInfinite() || maxDist <= fullRadius) return 0f
        if (distance >= maxDist) return 0f
        if (distance <= fullRadius) return 1f
        val t = ((maxDist - distance) / (maxDist - fullRadius)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    fun computeTargetMix(
        telemetry: AmbientTelemetry,
        settings: AudioMixSettings,
        isAppBackgrounded: Boolean
    ): TargetAudioMix {
        if (settings.isMuted || isAppBackgrounded) {
            return TargetAudioMix.SILENT
        }

        val overall = settings.masterVolume.coerceIn(0f, 1f)
        if (overall <= MIN_AUDIBLE) {
            return TargetAudioMix.SILENT
        }

        val indoor = telemetry.indoorFactor.coerceIn(0f, 1f)
        val menuScale = if (telemetry.isMenuMode) 0.55f else 1.0f

        // A. General Nature Ambience: outdoor biomes, reduced/stopped indoors
        val biomeWeight = biomeNatureWeight(telemetry.biomeId)
        val indoorAttenuation = if (indoor >= 0.98f) 0f else (1f - indoor * 0.88f)
        val rawRain = if (telemetry.weather == WeatherType.RAIN) {
            telemetry.rainIntensity.coerceIn(0f, 1f)
        } else {
            0f
        }
        // Subtle natural blend: heavy rain softly tucks birdsong down by up to 28%
        val rainDuckOnNature = 1f - rawRain * 0.28f
        val nature = (
            overall *
                settings.ambientVolume.coerceIn(0f, 1f) *
                biomeWeight *
                indoorAttenuation *
                menuScale *
                rainDuckOnNature
            ).coerceIn(0f, 1f)

        // B. Dynamic Rain: strictly active only during WeatherType.RAIN
        val rainIndoorMuffle = 1f - indoor * 0.45f
        val rain = if (telemetry.weather == WeatherType.RAIN && rawRain > MIN_AUDIBLE) {
            (
                overall *
                    settings.weatherVolume.coerceIn(0f, 1f) *
                    rawRain *
                    rainIndoorMuffle *
                    menuScale
                ).coerceIn(0f, 1f)
        } else {
            0f
        }

        // C. Distance-based River Audio: smooth spatial attenuation, stops when far
        val riverProx = computeRiverProximityFactor(
            distance = telemetry.nearestRiverDist,
            maxDist = telemetry.riverMaxDist
        )
        val riverIndoorMuffle = 1f - indoor * 0.35f
        val river = if (riverProx > MIN_AUDIBLE && !telemetry.isMenuMode) {
            (
                overall *
                    settings.waterVolume.coerceIn(0f, 1f) *
                    riverProx *
                    riverIndoorMuffle
                ).coerceIn(0f, 1f)
        } else {
            0f
        }

        return TargetAudioMix(
            natureGain = if (nature < MIN_AUDIBLE) 0f else nature,
            rainGain = if (rain < MIN_AUDIBLE) 0f else rain,
            riverGain = if (river < MIN_AUDIBLE) 0f else river
        )
    }

    fun stepFade(
        current: Float,
        target: Float,
        dtSeconds: Float,
        fadeInTauSeconds: Float = 0.75f,
        fadeOutTauSeconds: Float = 1.15f
    ): Float {
        val clampedTarget = target.coerceIn(0f, 1f)
        val clampedCurrent = current.coerceIn(0f, 1f)
        if (dtSeconds <= 0f) return clampedCurrent
        val tau = if (clampedTarget >= clampedCurrent) fadeInTauSeconds else fadeOutTauSeconds
        val alpha = 1f - exp(-dtSeconds / tau.coerceAtLeast(0.05f))
        val next = clampedCurrent + (clampedTarget - clampedCurrent) * alpha
        return when {
            clampedTarget == 0f && next < MIN_AUDIBLE -> 0f
            kotlin.math.abs(clampedTarget - next) < 0.0005f -> clampedTarget
            else -> next.coerceIn(0f, 1f)
        }
    }
}
