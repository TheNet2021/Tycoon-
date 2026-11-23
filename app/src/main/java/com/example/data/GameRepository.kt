package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.tycoonDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "wildcraft_tycoon_save"
)

data class SaveSlotSummary(
    val slotIndex: Int,
    val hasData: Boolean,
    val playerLevel: Int,
    val coins: Long,
    val biomeId: String,
    val unlockedAreas: Int,
    val playtimeSeconds: Long,
    val lastSavedEpochMs: Long
)

class GameRepository(private val context: Context) {

    private val saveKey = stringPreferencesKey("tycoon_state_json")
    private val activeSlotKey = intPreferencesKey("active_save_slot")

    private fun keyForSlot(slotIndex: Int): Preferences.Key<String> = when (slotIndex.coerceIn(0, 2)) {
        0 -> saveKey
        1 -> stringPreferencesKey("tycoon_state_slot_1")
        else -> stringPreferencesKey("tycoon_state_slot_2")
    }

    suspend fun getActiveSlotIndex(): Int {
        return (context.tycoonDataStore.data.map { prefs -> prefs[activeSlotKey] }.first() ?: 0).coerceIn(0, 2)
    }

    suspend fun setActiveSlotIndex(slotIndex: Int) {
        val clamped = slotIndex.coerceIn(0, 2)
        context.tycoonDataStore.edit { prefs ->
            prefs[activeSlotKey] = clamped
        }
    }

    suspend fun listSaveSlots(): List<SaveSlotSummary> {
        val prefs = context.tycoonDataStore.data.first()
        return (0..2).map { slot ->
            val raw = prefs[keyForSlot(slot)]
            if (raw.isNullOrBlank()) {
                SaveSlotSummary(
                    slotIndex = slot,
                    hasData = false,
                    playerLevel = 1,
                    coins = 0L,
                    biomeId = "forest",
                    unlockedAreas = 1,
                    playtimeSeconds = 0L,
                    lastSavedEpochMs = 0L
                )
            } else {
                val decoded = deserialize(raw)
                SaveSlotSummary(
                    slotIndex = slot,
                    hasData = decoded.hasSavedProgress || decoded.lastSavedEpochMs > 0L,
                    playerLevel = decoded.playerLevel,
                    coins = decoded.coins,
                    biomeId = decoded.lastBiomeId,
                    unlockedAreas = decoded.unlockedBiomes.size,
                    playtimeSeconds = decoded.playtimeSeconds,
                    lastSavedEpochMs = decoded.lastSavedEpochMs
                )
            }
        }
    }

    suspend fun loadState(): Pair<TycoonSaveState, Long> {
        val slot = getActiveSlotIndex()
        return loadSlot(slot)
    }

    suspend fun loadSlot(slotIndex: Int): Pair<TycoonSaveState, Long> {
        val key = keyForSlot(slotIndex)
        val raw = context.tycoonDataStore.data.map { prefs -> prefs[key] }.first()
        if (raw.isNullOrBlank()) {
            return TycoonSaveState() to 0L
        }
        val state = deserialize(raw)
        val now = System.currentTimeMillis()
        var offlineEarned = 0L
        if (state.lastSavedEpochMs > 0L && state.passiveIncomePerSecond > 0.0) {
            val elapsedSec = ((now - state.lastSavedEpochMs) / 1000L).coerceIn(0L, 7200L)
            if (elapsedSec >= 10L) {
                offlineEarned = (elapsedSec * state.passiveIncomePerSecond).toLong()
            }
        }
        val hydrated = if (offlineEarned > 0L) {
            state.copy(
                coins = state.coins + offlineEarned,
                lifetimeEarned = state.lifetimeEarned + offlineEarned,
                lastSavedEpochMs = now
            )
        } else {
            state
        }
        return hydrated to offlineEarned
    }

    suspend fun saveState(state: TycoonSaveState) {
        val slot = getActiveSlotIndex()
        saveSlot(slot, state)
    }

    suspend fun saveSlot(slotIndex: Int, state: TycoonSaveState) {
        val stamped = state.copy(lastSavedEpochMs = System.currentTimeMillis())
        val json = serialize(stamped)
        val key = keyForSlot(slotIndex)
        context.tycoonDataStore.edit { prefs ->
            prefs[key] = json
        }
    }

    suspend fun clearState() {
        val slot = getActiveSlotIndex()
        clearSlot(slot)
    }

    suspend fun clearSlot(slotIndex: Int) {
        val key = keyForSlot(slotIndex)
        context.tycoonDataStore.edit { prefs ->
            prefs.remove(key)
        }
    }

    fun serialize(state: TycoonSaveState): String {
        val root = JSONObject()
        root.put("coins", state.coins)
        root.put("lifetimeEarned", state.lifetimeEarned)
        root.put("xp", state.xp)
        root.put("axeLevel", state.axeLevel)
        root.put("pickaxeLevel", state.pickaxeLevel)
        root.put("backpackLevel", state.backpackLevel)
        root.put("moveSpeedLevel", state.moveSpeedLevel)
        root.put("gatherSpeedLevel", state.gatherSpeedLevel)
        root.put("totalContractsCompleted", state.totalContractsCompleted)
        root.put("lastMarketTickEpochMs", state.lastMarketTickEpochMs)
        root.put("isMuted", state.isMuted)
        root.put("masterVolume", state.masterVolume.toDouble())
        root.put("ambientVolume", state.ambientVolume.toDouble())
        root.put("weatherVolume", state.weatherVolume.toDouble())
        root.put("waterVolume", state.waterVolume.toDouble())
        root.put("sfxVolume", state.sfxVolume.toDouble())
        root.put("musicVolume", state.musicVolume.toDouble())
        root.put("graphicsQuality", state.graphicsQuality)
        root.put("hapticsEnabled", state.hapticsEnabled)
        root.put("shadowQuality", state.shadowQuality)
        root.put("cameraSensitivity", state.cameraSensitivity.toDouble())
        root.put("cameraDistance", state.cameraDistance.toDouble())
        root.put("fpsLimit", state.fpsLimit)
        root.put("languageCode", state.languageCode)
        root.put("playerPosX", state.playerPosX)
        root.put("playerPosZ", state.playerPosZ)
        root.put("lastBiomeId", state.lastBiomeId)
        root.put("playtimeSeconds", state.playtimeSeconds)
        root.put("lastSavedEpochMs", state.lastSavedEpochMs)

        root.put("inventory", writeIntMap(state.inventory, minVal = 1))
        root.put("gatheredTotals", writeIntMap(state.gatheredTotals, minVal = 1))
        root.put("resourceUpgradeLevels", writeIntMap(state.resourceUpgradeLevels, minVal = 2))
        root.put("buildingLevels", writeIntMap(state.buildingLevels, minVal = 1))
        root.put("buildingWorkers", writeIntMap(state.buildingWorkers, minVal = 1))
        root.put("buildingLines", writeIntMap(state.buildingLines, minVal = 1))
        root.put("machineLevels", writeIntMap(state.machineLevels, minVal = 1))
        root.put("warehouseStock", writeIntMap(state.warehouseStock, minVal = 1))
        root.put("fleetLevels", writeIntMap(state.fleetLevels, minVal = 1))
        root.put("buyerReputationXp", writeIntMap(state.buyerReputationXp, minVal = 1))

        val activeMachJson = JSONObject()
        state.machineActive.forEach { (k, v) -> activeMachJson.put(k, v) }
        root.put("machineActive", activeMachJson)

        val marketJson = JSONObject()
        state.marketMultipliers.forEach { (k, v) -> marketJson.put(k, v) }
        root.put("marketMultipliers", marketJson)

        root.put("activeContracts", writeContracts(state.activeContracts))
        root.put("availableContracts", writeContracts(state.availableContracts))

        val bioArray = JSONArray()
        state.unlockedBiomes.forEach { bioArray.put(it) }
        root.put("unlockedBiomes", bioArray)

        val questsArray = JSONArray()
        state.claimedQuestIds.forEach { questsArray.put(it) }
        root.put("claimedQuestIds", questsArray)

        val itemsArray = JSONArray()
        state.uniqueItems.forEach { itemsArray.put(it) }
        root.put("uniqueItems", itemsArray)

        val secretsArr = JSONArray()
        state.discoveredSecrets.forEach { secretsArr.put(it) }
        root.put("discoveredSecrets", secretsArr)

        val bossesArr = JSONArray()
        state.defeatedBosses.forEach { bossesArr.put(it) }
        root.put("defeatedBosses", bossesArr)

        val vehiclesArr = JSONArray()
        state.unlockedVehicles.forEach { vehiclesArr.put(it) }
        root.put("unlockedVehicles", vehiclesArr)

        if (state.activeVehicleId != null) {
            root.put("activeVehicleId", state.activeVehicleId)
        }
        root.put("insectsDefeated", state.insectsDefeated)
        root.put("playerHp", state.playerHp)
        root.put("settlement", SettlementSerializer.toJson(state.settlement))
        return root.toString()
    }

    fun deserialize(raw: String): TycoonSaveState {
        return try {
            val root = JSONObject(raw)
            val bioArr = root.optJSONArray("unlockedBiomes")
            val biomes = buildSet {
                add("forest")
                if (bioArr != null) {
                    for (i in 0 until bioArr.length()) {
                        val id = bioArr.optString(i)
                        if (id.isNotBlank()) add(id)
                    }
                }
            }
            val parsedFps = root.optInt("fpsLimit", 60)
            val normalizedFps = if (parsedFps <= 35) 30 else 60
            val parsedLang = root.optString("languageCode", "en").ifBlank { "en" }
            val parsedBiome = root.optString("lastBiomeId", "forest").ifBlank { "forest" }
            val posX = root.optDouble("playerPosX", 0.0).let { if (it.isNaN() || it.isInfinite()) 0.0 else it }
            val posZ = root.optDouble("playerPosZ", 2.0).let { if (it.isNaN() || it.isInfinite()) 2.0 else it }

            TycoonSaveState(
                coins = root.optLong("coins", 0L).coerceAtLeast(0L),
                lifetimeEarned = root.optLong("lifetimeEarned", 0L).coerceAtLeast(0L),
                xp = root.optLong("xp", 0L).coerceAtLeast(0L),
                inventory = readIntMap(root.optJSONObject("inventory")),
                gatheredTotals = readIntMap(root.optJSONObject("gatheredTotals")),
                axeLevel = root.optInt("axeLevel", 1).coerceIn(1, 8),
                pickaxeLevel = root.optInt("pickaxeLevel", 1).coerceIn(1, 8),
                backpackLevel = root.optInt("backpackLevel", 1).coerceIn(1, 8),
                moveSpeedLevel = root.optInt("moveSpeedLevel", 1).coerceIn(1, 8),
                gatherSpeedLevel = root.optInt("gatherSpeedLevel", 1).coerceIn(1, 8),
                resourceUpgradeLevels = readIntMap(root.optJSONObject("resourceUpgradeLevels")),
                buildingLevels = readIntMap(root.optJSONObject("buildingLevels")),
                buildingWorkers = readIntMap(root.optJSONObject("buildingWorkers")),
                buildingLines = readIntMap(root.optJSONObject("buildingLines")),
                machineLevels = readIntMap(root.optJSONObject("machineLevels")),
                machineActive = readBooleanMap(root.optJSONObject("machineActive")),
                warehouseStock = readIntMap(root.optJSONObject("warehouseStock")),
                fleetLevels = readIntMap(root.optJSONObject("fleetLevels")),
                buyerReputationXp = readIntMap(root.optJSONObject("buyerReputationXp")),
                marketMultipliers = readDoubleMap(root.optJSONObject("marketMultipliers")),
                activeContracts = readContracts(root.optJSONArray("activeContracts")),
                availableContracts = readContracts(root.optJSONArray("availableContracts")),
                totalContractsCompleted = root.optInt("totalContractsCompleted", 0).coerceAtLeast(0),
                lastMarketTickEpochMs = root.optLong("lastMarketTickEpochMs", 0L),
                unlockedBiomes = biomes,
                claimedQuestIds = readStringSet(root.optJSONArray("claimedQuestIds")),
                uniqueItems = readStringSet(root.optJSONArray("uniqueItems")),
                discoveredSecrets = readStringSet(root.optJSONArray("discoveredSecrets")),
                defeatedBosses = readStringSet(root.optJSONArray("defeatedBosses")),
                unlockedVehicles = readStringSet(root.optJSONArray("unlockedVehicles")),
                activeVehicleId = root.optString("activeVehicleId", "").takeIf { it.isNotBlank() },
                insectsDefeated = root.optInt("insectsDefeated", 0).coerceAtLeast(0),
                playerHp = root.optInt("playerHp", 100).coerceAtLeast(1),
                isMuted = root.optBoolean("isMuted", false),
                masterVolume = root.optDouble("masterVolume", 1.0).toFloat().coerceIn(0f, 1f),
                ambientVolume = root.optDouble("ambientVolume", 0.70).toFloat().coerceIn(0f, 1f),
                weatherVolume = root.optDouble("weatherVolume", 0.75).toFloat().coerceIn(0f, 1f),
                waterVolume = root.optDouble("waterVolume", 0.80).toFloat().coerceIn(0f, 1f),
                sfxVolume = root.optDouble("sfxVolume", 0.85).toFloat().coerceIn(0f, 1f),
                musicVolume = root.optDouble("musicVolume", 0.45).toFloat().coerceIn(0f, 1f),
                graphicsQuality = root.optInt("graphicsQuality", 1).coerceIn(0, 2),
                hapticsEnabled = root.optBoolean("hapticsEnabled", true),
                shadowQuality = root.optInt("shadowQuality", 1).coerceIn(0, 2),
                cameraSensitivity = root.optDouble("cameraSensitivity", 1.0).toFloat().coerceIn(0.5f, 2.0f),
                cameraDistance = root.optDouble("cameraDistance", 12.2).toFloat().coerceIn(8.0f, 18.0f),
                fpsLimit = normalizedFps,
                languageCode = parsedLang,
                playerPosX = posX,
                playerPosZ = posZ,
                lastBiomeId = parsedBiome,
                playtimeSeconds = root.optLong("playtimeSeconds", 0L).coerceAtLeast(0L),
                lastSavedEpochMs = root.optLong("lastSavedEpochMs", 0L),
                settlement = SettlementSerializer.fromJson(root.optJSONObject("settlement"))
            )
        } catch (_: Exception) {
            TycoonSaveState()
        }
    }

    private fun writeIntMap(map: Map<String, Int>, minVal: Int): JSONObject {
        val obj = JSONObject()
        map.forEach { (k, v) -> if (v >= minVal) obj.put(k, v) }
        return obj
    }

    private fun readIntMap(obj: JSONObject?): Map<String, Int> {
        if (obj == null) return emptyMap()
        return buildMap {
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = obj.optInt(k, 0)
                if (v > 0) put(k, v)
            }
        }
    }

    private fun readBooleanMap(obj: JSONObject?): Map<String, Boolean> {
        if (obj == null) return emptyMap()
        return buildMap {
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                put(k, obj.optBoolean(k, true))
            }
        }
    }

    private fun readDoubleMap(obj: JSONObject?): Map<String, Double> {
        if (obj == null) return emptyMap()
        return buildMap {
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = obj.optDouble(k, 1.0)
                if (!v.isNaN() && v > 0.0) put(k, v)
            }
        }
    }

    private fun writeContracts(list: List<ContractOrder>): JSONArray {
        val arr = JSONArray()
        list.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("buyerId", c.buyerId)
            obj.put("goodId", c.goodId)
            obj.put("quantity", c.quantity)
            obj.put("tier", c.tier.name)
            obj.put("rewardCoins", c.rewardCoins)
            obj.put("rewardXp", c.rewardXp)
            obj.put("rewardRep", c.rewardRep)
            obj.put("acceptedEpochMs", c.acceptedEpochMs)
            obj.put("deadlineEpochMs", c.deadlineEpochMs)
            arr.put(obj)
        }
        return arr
    }

    private fun readContracts(arr: JSONArray?): List<ContractOrder> {
        if (arr == null) return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val tier = runCatching { ContractTier.valueOf(obj.optString("tier", "SMALL")) }
                    .getOrDefault(ContractTier.SMALL)
                add(
                    ContractOrder(
                        id = obj.optString("id"),
                        buyerId = obj.optString("buyerId"),
                        goodId = obj.optString("goodId"),
                        quantity = obj.optInt("quantity", 5),
                        tier = tier,
                        rewardCoins = obj.optLong("rewardCoins", 50L),
                        rewardXp = obj.optLong("rewardXp", 20L),
                        rewardRep = obj.optInt("rewardRep", 10),
                        acceptedEpochMs = obj.optLong("acceptedEpochMs", 0L),
                        deadlineEpochMs = obj.optLong("deadlineEpochMs", 0L)
                    )
                )
            }
        }
    }

    private fun readStringSet(arr: JSONArray?): Set<String> {
        if (arr == null) return emptySet()
        return buildSet {
            for (i in 0 until arr.length()) {
                val item = arr.optString(i)
                if (item.isNotBlank()) add(item)
            }
        }
    }
}
