package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "AmbientAudioEngine"
private const val AUDIO_ASSET_DIR = "audio"
private const val TICK_INTERVAL_MS = 40L
private const val CROSSFADE_WINDOW_MS = 1800
private const val CROSSFADE_DURATION_MS = 1600f

/**
 * Reuses dual MediaPlayer decks per looping track so long ambient MP3s loop
 * seamlessly without encoder-padding gaps or per-frame object allocation.
 */
internal class SeamlessLoopChannel(
    private val context: Context,
    private val assetRelativePath: String,
    private val audioAttributes: AudioAttributes
) {
    private val players = arrayOfNulls<MediaPlayer>(2)
    private val prepared = booleanArrayOf(false, false)
    private var activeIndex = 0
    private var crossfading = false
    private var durationMs = 0
    private var currentVolume = 0f
    private var isPausedByIdle = true

    init {
        initDeck(0)
        initDeck(1)
    }

    private fun initDeck(index: Int) {
        try {
            val afd = context.assets.openFd(assetRelativePath)
            val mp = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setVolume(0f, 0f)
                setOnPreparedListener { readyPlayer ->
                    prepared[index] = true
                    if (durationMs <= 0) {
                        durationMs = readyPlayer.duration.coerceAtLeast(0)
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.w(TAG, "MediaPlayer error on $assetRelativePath (deck=$index, what=$what, extra=$extra)")
                    true
                }
                prepareAsync()
            }
            players[index] = mp
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize audio deck $index for $assetRelativePath", e)
        }
    }

    fun update(volume: Float) {
        val target = volume.coerceIn(0f, 1f)
        currentVolume = target

        if (target <= 0f) {
            pauseAll()
            return
        }

        val active = players[activeIndex] ?: return
        if (!prepared[activeIndex]) return

        isPausedByIdle = false
        if (!active.isPlaying) {
            runCatching {
                active.setVolume(target, target)
                active.start()
            }
        }

        val dur = if (durationMs > 0) durationMs else active.duration
        if (dur <= CROSSFADE_WINDOW_MS * 2) {
            if (!active.isLooping) active.isLooping = true
            runCatching { active.setVolume(target, target) }
            return
        }

        val pos = runCatching { active.currentPosition }.getOrDefault(0)
        val remaining = dur - pos
        val nextIndex = 1 - activeIndex
        val next = players[nextIndex]

        if (!crossfading && remaining in 1..CROSSFADE_WINDOW_MS && next != null && prepared[nextIndex]) {
            crossfading = true
            runCatching {
                next.seekTo(0)
                next.setVolume(0f, 0f)
                next.start()
            }
        }

        if (crossfading && next != null && prepared[nextIndex]) {
            val elapsedFade = (CROSSFADE_WINDOW_MS - remaining).coerceIn(0, CROSSFADE_DURATION_MS.toInt())
            val t = (elapsedFade / CROSSFADE_DURATION_MS).coerceIn(0f, 1f)
            val fadeOutGain = target * (1f - t)
            val fadeInGain = target * t
            runCatching {
                active.setVolume(fadeOutGain, fadeOutGain)
                next.setVolume(fadeInGain, fadeInGain)
            }
            if (t >= 1f || remaining <= 40) {
                runCatching {
                    active.pause()
                    active.seekTo(0)
                    active.setVolume(0f, 0f)
                }
                activeIndex = nextIndex
                crossfading = false
            }
        } else {
            runCatching { active.setVolume(target, target) }
        }
    }

    fun pauseAll() {
        if (isPausedByIdle) return
        isPausedByIdle = true
        crossfading = false
        for (i in 0..1) {
            val mp = players[i] ?: continue
            if (prepared[i] && runCatching { mp.isPlaying }.getOrDefault(false)) {
                runCatching {
                    mp.setVolume(0f, 0f)
                    mp.pause()
                }
            }
        }
    }

    fun release() {
        for (i in 0..1) {
            val mp = players[i]
            players[i] = null
            prepared[i] = false
            if (mp != null) {
                runCatching {
                    if (mp.isPlaying) mp.stop()
                }
                runCatching { mp.release() }
            }
        }
    }
}

/**
 * Manages smooth ambient audio mixing (Nature, Rain, River) on Android using reused
 * SeamlessLoopChannel instances tied to the Activity/Application lifecycle.
 */
class AmbientAudioEngine(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val natureChannel: SeamlessLoopChannel?
    private val rainChannel: SeamlessLoopChannel?
    private val riverChannel: SeamlessLoopChannel?

    val discoveredAssets: Map<String, String>
    val hasNativeNatureAsset: Boolean

    @Volatile
    private var latestTelemetry = AmbientTelemetry()

    @Volatile
    private var latestSettings = AudioMixSettings()

    @Volatile
    private var isBackgrounded = false

    private var curNatureGain = 0f
    private var curRainGain = 0f
    private var curRiverGain = 0f
    private var mixJob: Job? = null

    init {
        val assetFiles = runCatching {
            appContext.assets.list(AUDIO_ASSET_DIR)?.toList().orEmpty()
        }.getOrDefault(emptyList())

        val riverFile = assetFiles.firstOrNull {
            it == "Flowing_river_ambience_a413463b.mp3" ||
                it.contains("river", ignoreCase = true)
        }
        val rainFile = assetFiles.firstOrNull {
            it == "Rain_ambience_fcacbf8a.mp3" ||
                it.contains("rain", ignoreCase = true)
        }
        val natureFile = assetFiles.firstOrNull {
            (it.contains("nature", ignoreCase = true) ||
                it.contains("bird", ignoreCase = true) ||
                it.contains("forest", ignoreCase = true)) &&
                it != riverFile && it != rainFile
        }

        discoveredAssets = buildMap {
            if (natureFile != null) put("nature", "$AUDIO_ASSET_DIR/$natureFile")
            if (rainFile != null) put("rain", "$AUDIO_ASSET_DIR/$rainFile")
            if (riverFile != null) put("river", "$AUDIO_ASSET_DIR/$riverFile")
        }
        hasNativeNatureAsset = natureFile != null

        if (!hasNativeNatureAsset) {
            Log.i(TAG, "No bundled general nature audio file found in assets/$AUDIO_ASSET_DIR; WebView procedural forest ambience fallback active.")
        }

        natureChannel = natureFile?.let { SeamlessLoopChannel(appContext, "$AUDIO_ASSET_DIR/$it", audioAttributes) }
        rainChannel = rainFile?.let { SeamlessLoopChannel(appContext, "$AUDIO_ASSET_DIR/$it", audioAttributes) }
        riverChannel = riverFile?.let { SeamlessLoopChannel(appContext, "$AUDIO_ASSET_DIR/$it", audioAttributes) }

        startMixerLoop()
    }

    private fun startMixerLoop() {
        mixJob?.cancel()
        mixJob = scope.launch {
            var lastNs = System.nanoTime()
            while (isActive) {
                delay(TICK_INTERVAL_MS)
                val nowNs = System.nanoTime()
                val dtSec = ((nowNs - lastNs) / 1_000_000_000f).coerceIn(0.01f, 0.25f)
                lastNs = nowNs

                val target = AudioMixCalculator.computeTargetMix(
                    telemetry = latestTelemetry,
                    settings = latestSettings,
                    isAppBackgrounded = isBackgrounded
                )

                curNatureGain = AudioMixCalculator.stepFade(
                    current = curNatureGain,
                    target = target.natureGain,
                    dtSeconds = dtSec,
                    fadeInTauSeconds = 0.85f,
                    fadeOutTauSeconds = 1.15f
                )
                curRainGain = AudioMixCalculator.stepFade(
                    current = curRainGain,
                    target = target.rainGain,
                    dtSeconds = dtSec,
                    fadeInTauSeconds = 1.25f,
                    fadeOutTauSeconds = 1.45f
                )
                curRiverGain = AudioMixCalculator.stepFade(
                    current = curRiverGain,
                    target = target.riverGain,
                    dtSeconds = dtSec,
                    fadeInTauSeconds = 0.45f,
                    fadeOutTauSeconds = 0.65f
                )

                natureChannel?.update(curNatureGain)
                rainChannel?.update(curRainGain)
                riverChannel?.update(curRiverGain)
            }
        }
    }

    fun updateTelemetry(telemetry: AmbientTelemetry) {
        latestTelemetry = telemetry
    }

    fun updateSettings(settings: AudioMixSettings) {
        latestSettings = settings
        if (settings.isMuted || settings.masterVolume <= 0.002f) {
            curNatureGain = 0f
            curRainGain = 0f
            curRiverGain = 0f
            natureChannel?.pauseAll()
            rainChannel?.pauseAll()
            riverChannel?.pauseAll()
        }
    }

    fun setBackgrounded(backgrounded: Boolean) {
        isBackgrounded = backgrounded
        if (backgrounded) {
            curNatureGain = 0f
            curRainGain = 0f
            curRiverGain = 0f
            natureChannel?.pauseAll()
            rainChannel?.pauseAll()
            riverChannel?.pauseAll()
        }
    }

    fun release() {
        mixJob?.cancel()
        natureChannel?.release()
        rainChannel?.release()
        riverChannel?.release()
        scope.cancel()
    }
}
