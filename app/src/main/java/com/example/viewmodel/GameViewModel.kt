package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BiomeType
import com.example.data.BuildingType
import com.example.data.GameRepository
import com.example.data.QuestCatalog
import com.example.data.TycoonSaveState
import com.example.data.UpgradeType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application.applicationContext)

    private val _gameState = MutableStateFlow(TycoonSaveState())
    val gameState: StateFlow<TycoonSaveState> = _gameState.asStateFlow()

    private val _isHydrated = MutableStateFlow(false)
    val isHydrated: StateFlow<Boolean> = _isHydrated.asStateFlow()

    private val _isEngineReady = MutableStateFlow(false)
    val isEngineReady: StateFlow<Boolean> = _isEngineReady.asStateFlow()

    private val _offlineEarningsBanner = MutableStateFlow<Long?>(null)
    val offlineEarningsBanner: StateFlow<Long?> = _offlineEarningsBanner.asStateFlow()

    private val _jsCommands = MutableSharedFlow<String>(extraBufferCapacity = 32)
    val jsCommands: SharedFlow<String> = _jsCommands.asSharedFlow()

    private var debouncedSaveJob: Job? = null
    private var lastKotlinMutationElapsedMs: Long = 0L

    init {
        viewModelScope.launch {
            val (loaded, offlineEarned) = repository.loadState()
            _gameState.value = loaded
            _isHydrated.value = true
            if (offlineEarned > 0L) {
                _offlineEarningsBanner.value = offlineEarned
            }
            if (_isEngineReady.value) {
                pushStateToEngine(loaded)
            }
        }
    }

    fun dismissOfflineBanner() {
        _offlineEarningsBanner.value = null
    }

    fun onEngineReady() {
        _isEngineReady.value = true
        if (_isHydrated.value) {
            pushStateToEngine(_gameState.value)
        }
    }

    fun syncFromEngine(json: String) {
        if (SystemClock.elapsedRealtime() - lastKotlinMutationElapsedMs < 450L) {
            return
        }
        val parsed = repository.deserialize(json)
        val current = _gameState.value
        val merged = parsed.copy(
            xp = current.xp,
            claimedQuestIds = current.claimedQuestIds,
            uniqueItems = current.uniqueItems,
            isMuted = current.isMuted,
            hapticsEnabled = current.hapticsEnabled
        )
        _gameState.value = merged
        scheduleDebouncedSave()
    }

    fun purchaseUpgrade(type: UpgradeType): Boolean {
        val state = _gameState.value
        val currentLevel = state.upgradeLevel(type)
        if (currentLevel >= type.maxLevel) return false
        val cost = type.costForNextLevel(currentLevel)
        if (state.coins < cost) return false

        val updated = when (type) {
            UpgradeType.AXE -> state.copy(coins = state.coins - cost, axeLevel = currentLevel + 1)
            UpgradeType.PICKAXE -> state.copy(coins = state.coins - cost, pickaxeLevel = currentLevel + 1)
            UpgradeType.BACKPACK -> state.copy(coins = state.coins - cost, backpackLevel = currentLevel + 1)
            UpgradeType.MOVE_SPEED -> state.copy(coins = state.coins - cost, moveSpeedLevel = currentLevel + 1)
            UpgradeType.GATHER_SPEED -> state.copy(coins = state.coins - cost, gatherSpeedLevel = currentLevel + 1)
        }
        applyLocalStateChange(updated, notifyBanner = "${type.title} -> Tier ${currentLevel + 1}!")
        triggerHaptic("upgrade")
        return true
    }

    fun buildOrUpgradeBuilding(type: BuildingType): Boolean {
        val state = _gameState.value
        if (!state.unlockedBiomes.contains(type.biomeId)) return false
        val currentLevel = state.buildingLevel(type)
        if (currentLevel >= type.maxLevel) return false
        val cost = type.costForNextLevel(currentLevel)
        if (state.coins < cost) return false

        val newBuildings = state.buildingLevels.toMutableMap()
        newBuildings[type.id] = currentLevel + 1
        val updated = state.copy(
            coins = state.coins - cost,
            buildingLevels = newBuildings
        )
        val actionLabel = if (currentLevel == 0) "${type.title} Built!" else "${type.title} Lv.${currentLevel + 1}!"
        applyLocalStateChange(updated, notifyBanner = actionLabel)
        triggerHaptic("upgrade")
        return true
    }

    fun unlockBiome(biome: BiomeType): Boolean {
        val state = _gameState.value
        if (state.isBiomeUnlocked(biome)) return false
        if (state.coins < biome.unlockCost) return false

        val updated = state.copy(
            coins = state.coins - biome.unlockCost,
            unlockedBiomes = state.unlockedBiomes + biome.id
        )
        applyLocalStateChange(updated, notifyBanner = "${biome.title} Unlocked!")
        triggerHaptic("unlock")
        return true
    }

    fun claimQuest(questId: String): Boolean {
        val state = _gameState.value
        val progress = state.evaluateQuests().firstOrNull { it.quest.id == questId } ?: return false
        if (!progress.isReadyToClaim) return false

        val quest = progress.quest
        val newItems = if (quest.rewardItem != null) {
            state.uniqueItems + quest.rewardItem.id
        } else {
            state.uniqueItems
        }
        val prevLevel = state.playerLevel
        val updated = state.copy(
            coins = state.coins + quest.rewardCoins,
            lifetimeEarned = state.lifetimeEarned + quest.rewardCoins,
            xp = state.xp + quest.rewardXp,
            claimedQuestIds = state.claimedQuestIds + quest.id,
            uniqueItems = newItems
        )
        val newLevel = updated.playerLevel
        val banner = buildString {
            append("Quest Claimed: +$${quest.rewardCoins} & +${quest.rewardXp} XP")
            if (quest.rewardItem != null) append(" + ${quest.rewardItem.title}")
            if (newLevel > prevLevel) append(" (Level $newLevel!)")
        }
        applyLocalStateChange(updated, notifyBanner = banner)
        triggerHaptic("unlock")
        return true
    }

    fun teleportToBiome(biome: BiomeType) {
        if (!_gameState.value.isBiomeUnlocked(biome)) return
        _jsCommands.tryEmit("window.teleportPlayer(${biome.spawnX}, ${biome.spawnZ}, '${biome.id}');")
    }

    fun toggleMute() {
        val updated = _gameState.value.copy(isMuted = !_gameState.value.isMuted)
        lastKotlinMutationElapsedMs = SystemClock.elapsedRealtime()
        _gameState.value = updated
        pushStateToEngine(updated)
        scheduleImmediateSave(updated)
    }

    fun toggleHaptics() {
        val updated = _gameState.value.copy(hapticsEnabled = !_gameState.value.hapticsEnabled)
        lastKotlinMutationElapsedMs = SystemClock.elapsedRealtime()
        _gameState.value = updated
        scheduleImmediateSave(updated)
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.clearState()
            lastKotlinMutationElapsedMs = SystemClock.elapsedRealtime()
            val fresh = TycoonSaveState(
                isMuted = _gameState.value.isMuted,
                hapticsEnabled = _gameState.value.hapticsEnabled
            )
            _gameState.value = fresh
            pushStateToEngine(fresh)
            _jsCommands.tryEmit("window.resetWorld && window.resetWorld();")
        }
    }

    fun flushSave() {
        viewModelScope.launch {
            repository.saveState(_gameState.value)
        }
    }

    private fun applyLocalStateChange(updated: TycoonSaveState, notifyBanner: String? = null) {
        lastKotlinMutationElapsedMs = SystemClock.elapsedRealtime()
        _gameState.value = updated
        pushStateToEngine(updated)
        if (notifyBanner != null) {
            val safe = JSONObject.quote(notifyBanner)
            _jsCommands.tryEmit("window.showNativeBanner && window.showNativeBanner($safe);")
        }
        scheduleImmediateSave(updated)
    }

    private fun pushStateToEngine(state: TycoonSaveState) {
        val json = repository.serialize(state)
        _jsCommands.tryEmit("window.applyGameState && window.applyGameState($json);")
    }

    private fun scheduleDebouncedSave() {
        debouncedSaveJob?.cancel()
        debouncedSaveJob = viewModelScope.launch {
            delay(1500L)
            repository.saveState(_gameState.value)
        }
    }

    private fun scheduleImmediateSave(state: TycoonSaveState) {
        debouncedSaveJob?.cancel()
        viewModelScope.launch {
            repository.saveState(state)
        }
    }

    fun triggerHaptic(type: String) {
        if (!_gameState.value.hapticsEnabled) return
        val ctx = getApplication<Application>().applicationContext
        try {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator == null || !vibrator.hasVibrator()) return
            val (durationMs, amplitude) = when (type) {
                "unlock" -> 45L to 220
                "upgrade" -> 32L to 180
                "coin" -> 14L to 95
                else -> 12L to 75
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore haptic errors on devices without hardware vibrator
        }
    }
}
