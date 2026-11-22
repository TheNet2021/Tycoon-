package com.example.data

import org.json.JSONArray
import org.json.JSONObject

object SettlementSerializer {

    fun toJson(state: SettlementState): JSONObject {
        val root = JSONObject()
        root.put("foodStock", state.foodStock)
        root.put("waterStock", state.waterStock)
        root.put("suppliesStock", state.suppliesStock)
        root.put("reputation", state.reputation)
        root.put("dayNumber", state.dayNumber)
        root.put("lastDailyTickEpochMs", state.lastDailyTickEpochMs)
        root.put("lastArrivalEpochMs", state.lastArrivalEpochMs)
        root.put("nextSequenceId", state.nextSequenceId)
        root.put("completedContractsCount", state.completedContractsCount)
        root.put("communityHappinessBonus", state.communityHappinessBonus)
        root.put("bonusFoodCap", state.bonusFoodCap)
        root.put("bonusWaterCap", state.bonusWaterCap)
        root.put("bonusDailyFoodProd", state.bonusDailyFoodProd)
        root.put("bonusDailyWaterProd", state.bonusDailyWaterProd)

        val satObj = JSONObject()
        state.demandSaturation.forEach { (k, v) -> satObj.put(k, v) }
        root.put("demandSaturation", satObj)

        val survArr = JSONArray()
        state.survivors.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("profession", s.profession.id)
            obj.put("skillLevel", s.skillLevel)
            if (s.assignedJob != null) obj.put("assignedJob", s.assignedJob.id)
            if (s.housingUnitId != null) obj.put("housingUnitId", s.housingUnitId)
            obj.put("happiness", s.happiness)
            obj.put("arrivedEpochMs", s.arrivedEpochMs)
            val needsObj = JSONObject().apply {
                put("food", s.needs.food)
                put("water", s.needs.water)
                put("supplies", s.needs.supplies)
                put("housing", s.needs.housing)
            }
            obj.put("needs", needsObj)
            survArr.put(obj)
        }
        root.put("survivors", survArr)

        val arrArr = JSONArray()
        state.pendingArrivals.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("name", a.name)
            obj.put("profession", a.profession.id)
            obj.put("skillLevel", a.skillLevel)
            obj.put("arrivedEpochMs", a.arrivedEpochMs)
            arrArr.put(obj)
        }
        root.put("pendingArrivals", arrArr)

        val houseArr = JSONArray()
        state.housingUnits.forEach { h ->
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("typeId", h.type.id)
            obj.put("startedEpochMs", h.startedEpochMs)
            obj.put("completesEpochMs", h.completesEpochMs)
            obj.put("isCompleted", h.isCompleted)
            houseArr.put(obj)
        }
        root.put("housingUnits", houseArr)

        val offerArr = JSONArray()
        state.survivorOffersForSale.forEach { o ->
            val obj = JSONObject()
            obj.put("goodId", o.goodId)
            obj.put("availableQuantity", o.availableQuantity)
            obj.put("unitAskPrice", o.unitAskPrice)
            obj.put("sellerName", o.sellerName)
            offerArr.put(obj)
        }
        root.put("survivorOffersForSale", offerArr)

        val contractArr = JSONArray()
        state.openContracts.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("title", c.title)
            obj.put("description", c.description)
            obj.put("category", c.category.name)
            obj.put("requesterName", c.requesterName)
            val reqObj = JSONObject()
            c.materialRequirements.forEach { (k, v) -> reqObj.put(k, v) }
            obj.put("materialRequirements", reqObj)
            obj.put("rewardCoins", c.rewardCoins)
            obj.put("rewardXp", c.rewardXp)
            obj.put("rewardReputation", c.rewardReputation)
            obj.put("happinessBonus", c.happinessBonus)
            obj.put("foodCapBonus", c.foodCapBonus)
            obj.put("waterCapBonus", c.waterCapBonus)
            obj.put("passiveFoodBonus", c.passiveFoodBonus)
            obj.put("passiveWaterBonus", c.passiveWaterBonus)
            contractArr.put(obj)
        }
        root.put("openContracts", contractArr)

        val txArr = JSONArray()
        state.transactionHistory.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("type", t.type.name)
            obj.put("description", t.description)
            obj.put("coinDelta", t.coinDelta)
            obj.put("timestampEpochMs", t.timestampEpochMs)
            txArr.put(obj)
        }
        root.put("transactionHistory", txArr)

        return root
    }

    fun fromJson(obj: JSONObject?): SettlementState {
        if (obj == null) return SettlementState()
        return try {
            val survivors = buildList {
                val arr = obj.optJSONArray("survivors") ?: return@buildList
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val prof = SurvivorProfession.fromId(o.optString("profession")) ?: SurvivorProfession.MINER
                    val job = o.optString("assignedJob", "").takeIf { it.isNotBlank() }?.let {
                        SurvivorProfession.fromId(it)
                    }
                    val needsObj = o.optJSONObject("needs")
                    val needs = if (needsObj != null) {
                        SurvivorNeeds(
                            food = needsObj.optInt("food", 80).coerceIn(0, 100),
                            water = needsObj.optInt("water", 80).coerceIn(0, 100),
                            supplies = needsObj.optInt("supplies", 75).coerceIn(0, 100),
                            housing = needsObj.optInt("housing", 100).coerceIn(0, 100)
                        )
                    } else SurvivorNeeds()

                    add(
                        SurvivorProfile(
                            id = o.optString("id"),
                            name = o.optString("name", "Settler"),
                            profession = prof,
                            skillLevel = o.optInt("skillLevel", 1).coerceIn(1, 10),
                            assignedJob = job,
                            housingUnitId = o.optString("housingUnitId", "").takeIf { it.isNotBlank() },
                            needs = needs,
                            happiness = o.optInt("happiness", 80).coerceIn(0, 100),
                            arrivedEpochMs = o.optLong("arrivedEpochMs", 0L)
                        )
                    )
                }
            }

            val arrivals = buildList {
                val arr = obj.optJSONArray("pendingArrivals") ?: return@buildList
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val prof = SurvivorProfession.fromId(o.optString("profession")) ?: SurvivorProfession.FARMER
                    add(
                        RefugeeArrival(
                            id = o.optString("id"),
                            name = o.optString("name", "Traveler"),
                            profession = prof,
                            skillLevel = o.optInt("skillLevel", 1).coerceIn(1, 10),
                            arrivedEpochMs = o.optLong("arrivedEpochMs", 0L)
                        )
                    )
                }
            }

            val housing = buildList {
                val arr = obj.optJSONArray("housingUnits") ?: return@buildList
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val type = HousingType.fromId(o.optString("typeId")) ?: continue
                    add(
                        HousingBuilding(
                            id = o.optString("id"),
                            type = type,
                            startedEpochMs = o.optLong("startedEpochMs", 0L),
                            completesEpochMs = o.optLong("completesEpochMs", 0L),
                            isCompleted = o.optBoolean("isCompleted", true)
                        )
                    )
                }
            }

            val offers = buildList {
                val arr = obj.optJSONArray("survivorOffersForSale") ?: return@buildList
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val goodId = o.optString("goodId")
                    if (goodId.isBlank()) continue
                    add(
                        SurvivorTradeOffer(
                            goodId = goodId,
                            availableQuantity = o.optInt("availableQuantity", 1).coerceAtLeast(1),
                            unitAskPrice = o.optLong("unitAskPrice", 10L).coerceAtLeast(1L),
                            sellerName = o.optString("sellerName", "Settler")
                        )
                    )
                }
            }

            val contracts = buildList {
                val arr = obj.optJSONArray("openContracts") ?: return@buildList
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val cat = SettlementContractCategory.fromId(o.optString("category"))
                        ?: SettlementContractCategory.HOUSES
                    val reqs = buildMap {
                        val reqObj = o.optJSONObject("materialRequirements")
                        if (reqObj != null) {
                            val keys = reqObj.keys()
                            while (keys.hasNext()) {
                                val k = keys.next()
                                val v = reqObj.optInt(k, 0)
                                if (v > 0) put(k, v)
                            }
                        }
                    }
                    add(
                        SettlementConstructionContract(
                            id = o.optString("id"),
                            title = o.optString("title", "Build Contract"),
                            description = o.optString("description", ""),
                            category = cat,
                            requesterName = o.optString("requesterName", "Council"),
                            materialRequirements = reqs,
                            rewardCoins = o.optLong("rewardCoins", 100L).coerceAtLeast(1L),
                            rewardXp = o.optLong("rewardXp", 40L).coerceAtLeast(0L),
                            rewardReputation = o.optInt("rewardReputation", 10).coerceAtLeast(0),
                            happinessBonus = o.optInt("happinessBonus", 0),
                            foodCapBonus = o.optInt("foodCapBonus", 0),
                            waterCapBonus = o.optInt("waterCapBonus", 0),
                            passiveFoodBonus = o.optInt("passiveFoodBonus", 0),
                            passiveWaterBonus = o.optInt("passiveWaterBonus", 0)
                        )
                    )
                }
            }

            val txs = buildList {
                val arr = obj.optJSONArray("transactionHistory") ?: return@buildList
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val type = runCatching {
                        SettlementTransactionType.valueOf(o.optString("type"))
                    }.getOrDefault(SettlementTransactionType.SOLD_TO_SURVIVORS)
                    add(
                        SettlementTransaction(
                            id = o.optString("id"),
                            type = type,
                            description = o.optString("description", ""),
                            coinDelta = o.optLong("coinDelta", 0L),
                            timestampEpochMs = o.optLong("timestampEpochMs", 0L)
                        )
                    )
                }
            }

            val satMap = buildMap {
                val satObj = obj.optJSONObject("demandSaturation")
                if (satObj != null) {
                    val keys = satObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = satObj.optDouble(k, 0.0)
                        if (!v.isNaN() && v > 0.0) put(k, v.coerceIn(0.0, 0.45))
                    }
                }
            }

            SettlementState(
                survivors = survivors,
                pendingArrivals = arrivals,
                housingUnits = housing,
                foodStock = obj.optInt("foodStock", 20).coerceAtLeast(0),
                waterStock = obj.optInt("waterStock", 20).coerceAtLeast(0),
                suppliesStock = obj.optInt("suppliesStock", 15).coerceAtLeast(0),
                reputation = obj.optInt("reputation", 0).coerceAtLeast(0),
                dayNumber = obj.optInt("dayNumber", 1).coerceAtLeast(1),
                lastDailyTickEpochMs = obj.optLong("lastDailyTickEpochMs", 0L),
                lastArrivalEpochMs = obj.optLong("lastArrivalEpochMs", 0L),
                nextSequenceId = obj.optLong("nextSequenceId", 1L).coerceAtLeast(1L),
                demandSaturation = satMap,
                survivorOffersForSale = offers,
                openContracts = contracts,
                completedContractsCount = obj.optInt("completedContractsCount", 0).coerceAtLeast(0),
                communityHappinessBonus = obj.optInt("communityHappinessBonus", 0).coerceAtLeast(0),
                bonusFoodCap = obj.optInt("bonusFoodCap", 0).coerceAtLeast(0),
                bonusWaterCap = obj.optInt("bonusWaterCap", 0).coerceAtLeast(0),
                bonusDailyFoodProd = obj.optInt("bonusDailyFoodProd", 0).coerceAtLeast(0),
                bonusDailyWaterProd = obj.optInt("bonusDailyWaterProd", 0).coerceAtLeast(0),
                transactionHistory = txs
            )
        } catch (_: Exception) {
            SettlementState()
        }
    }
}
