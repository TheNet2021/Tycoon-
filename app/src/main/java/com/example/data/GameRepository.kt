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
        root.put("isMuted", state.isMuted)
        root.put("hapticsEnabled", state.hapticsEnabled)
        root.put("lastSavedEpochMs", state.lastSavedEpochMs)

        val invJson = JSONObject()
        state.inventory.forEach { (k, v) -> if (v > 0) invJson.put(k, v) }
        root.put("inventory", invJson)

        val gatheredJson = JSONObject()
        state.gatheredTotals.forEach { (k, v) -> if (v > 0) gatheredJson.put(k, v) }
        root.put("gatheredTotals", gatheredJson)

        val bldJson = JSONObject()
        state.buildingLevels.forEach { (k, v) -> if (v > 0) bldJson.put(k, v) }
        root.put("buildingLevels", bldJson)

        val bioArray = JSONArray()
        state.unlockedBiomes.forEach { bioArray.put(it) }
        root.put("unlockedBiomes", bioArray)

        val questsArray = JSONArray()
        state.claimedQuestIds.forEach { questsArray.put(it) }
        root.put("claimedQuestIds", questsArray)

        val itemsArray = JSONArray()
        state.uniqueItems.forEach { itemsArray.put(it) }
        root.put("uniqueItems", itemsArray)
        return root.toString()
    }

    fun deserialize(raw: String): TycoonSaveState {
        return try {
            val root = JSONObject(raw)
            val inventory = readIntMap(root.optJSONObject("inventory"))
            val gatheredTotals = readIntMap(root.optJSONObject("gatheredTotals"))
            val buildings = readIntMap(root.optJSONObject("buildingLevels"))

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

            val claimedQuestIds = readStringSet(root.optJSONArray("claimedQuestIds"))
            val uniqueItems = readStringSet(root.optJSONArray("uniqueItems"))

            TycoonSaveState(
                coins = root.optLong("coins", 0L).coerceAtLeast(0L),
                lifetimeEarned = root.optLong("lifetimeEarned", 0L).coerceAtLeast(0L),
                xp = root.optLong("xp", 0L).coerceAtLeast(0L),
                inventory = inventory,
                gatheredTotals = gatheredTotals,
                axeLevel = root.optInt("axeLevel", 1).coerceIn(1, 6),
                pickaxeLevel = root.optInt("pickaxeLevel", 1).coerceIn(1, 6),
                backpackLevel = root.optInt("backpackLevel", 1).coerceIn(1, 6),
                moveSpeedLevel = root.optInt("moveSpeedLevel", 1).coerceIn(1, 6),
                gatherSpeedLevel = root.optInt("gatherSpeedLevel", 1).coerceIn(1, 6),
                buildingLevels = buildings,
                unlockedBiomes = biomes,
                claimedQuestIds = claimedQuestIds,
                uniqueItems = uniqueItems,
                isMuted = root.optBoolean("isMuted", false),
                hapticsEnabled = root.optBoolean("hapticsEnabled", true),
                lastSavedEpochMs = root.optLong("lastSavedEpochMs", 0L)
            )
        } catch (_: Exception) {
            TycoonSaveState()
        }
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
