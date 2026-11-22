package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.tycoonDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "wildcraft_tycoon_save"
)

class GameRepository(private val context: Context) {

    private val saveKey = stringPreferencesKey("tycoon_state_json")

    suspend fun loadState(): Pair<TycoonSaveState, Long> {
        val raw = context.tycoonDataStore.data.map { prefs -> prefs[saveKey] }.first()
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
        val stamped = state.copy(lastSavedEpochMs = System.currentTimeMillis())
        val json = serialize(stamped)
        context.tycoonDataStore.edit { prefs ->
            prefs[saveKey] = json
        }
    }

    suspend fun clearState() {
        context.tycoonDataStore.edit { prefs ->
            prefs.remove(saveKey)
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
        root.put("sfxVolume", state.sfxVolume.toDouble())
        root.put("musicVolume", state.musicVolume.toDouble())
        root.put("graphicsQuality", state.graphicsQuality)
        root.put("hapticsEnabled", state.hapticsEnabled)
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
                sfxVolume = root.optDouble("sfxVolume", 0.85).toFloat().coerceIn(0f, 1f),
                musicVolume = root.optDouble("musicVolume", 0.45).toFloat().coerceIn(0f, 1f),
                graphicsQuality = root.optInt("graphicsQuality", 1).coerceIn(0, 2),
                hapticsEnabled = root.optBoolean("hapticsEnabled", true),
                lastSavedEpochMs = root.optLong("lastSavedEpochMs", 0L)
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
