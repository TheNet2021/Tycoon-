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
import com.example.data.BuyerCompany
import com.example.data.ContractGenerator
import com.example.data.DrivableVehicleType
import com.example.data.FleetEquipType
import com.example.data.GameRepository
import com.example.data.HousingType
import com.example.data.MachineType
import com.example.data.ResourceUpgradeType
import com.example.data.SettlementActionOutcome
import com.example.data.SettlementEngine
import com.example.data.SettlementFacilityType
import com.example.data.SupplyRestockKind
import com.example.data.SurvivorProfession
import com.example.data.TradeCatalog
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin

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

    private val _activeBiome = MutableStateFlow(BiomeType.FOREST)
    val activeBiome: StateFlow<BiomeType> = _activeBiome.asStateFlow()

    private val _jsCommands = MutableSharedFlow<String>(extraBufferCapacity = 32)
    val jsCommands: SharedFlow<String> = _jsCommands.asSharedFlow()

    private var debouncedSaveJob: Job? = null
    private var lastKotlinMutationElapsedMs: Long = 0L
    private val machineAccumulators = mutableMapOf<String, Double>()
    private var powerCostAccum: Double = 0.0
    private var marketSeedOffset: Long = 1L

    init {
        viewModelScope.launch {
            val (loaded, offlineEarned) = repository.loadState()
            val initialized = ensureMarketAndContracts(loaded)
            _gameState.value = initialized
            _isHydrated.value = true
            if (offlineEarned > 0L) {
                _offlineEarningsBanner.value = offlineEarned
            }
            if (_isEngineReady.value) {
                pushStateToEngine(initialized)
            }
            startIndustrialSimulationLoop()
        }
    }

    private fun startIndustrialSimulationLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if (!_isHydrated.value) continue
                val current = _gameState.value
                val now = System.currentTimeMillis()
                var updated = tickMachineryAndPower(current)
                val (settlementTicked, settlementBanner) = SettlementEngine.tick(updated, now)
                updated = settlementTicked
                if (now - updated.lastMarketTickEpochMs >= 45_000L || updated.marketMultipliers.isEmpty()) {
                    marketSeedOffset++
                    updated = tickMarketPrices(updated, now)
                }
                if (updated.availableContracts.isEmpty()) {
                    updated = updated.copy(
                        availableContracts = ContractGenerator.generateOffers(updated, marketSeedOffset)
                    )
                }
                if (updated != current) {
                    lastKotlinMutationElapsedMs = SystemClock.elapsedRealtime()
                    _gameState.value = updated
                    pushStateToEngine(updated)
                    if (settlementBanner != null) {
                        val safe = JSONObject.quote(settlementBanner)
                        _jsCommands.tryEmit("window.showNativeBanner && window.showNativeBanner($safe);")
                    }
                    scheduleDebouncedSave()
                }
            }
        }
    }

    private fun ensureMarketAndContracts(state: TycoonSaveState): TycoonSaveState {
        var s = state
        val now = System.currentTimeMillis()
        if (s.marketMultipliers.isEmpty()) {
            s = tickMarketPrices(s, now)
        }
        if (s.availableContracts.isEmpty()) {
            s = s.copy(availableContracts = ContractGenerator.generateOffers(s, marketSeedOffset))
        }
        s = SettlementEngine.ensureInitialized(s, now)
        return s
    }

    private fun tickMarketPrices(state: TycoonSaveState, nowMs: Long): TycoonSaveState {
        val nextMults = state.marketMultipliers.toMutableMap()
        val phase = (nowMs / 45_000L) + marketSeedOffset
        TradeCatalog.allGoods.forEachIndexed { idx, good ->
            val wave = sin((phase + idx * 1.73) * 0.85)
            val mult = (1.10 + wave * 0.30).coerceIn(0.82, 1.44)
            nextMults[good.id] = (mult * 100.0).roundToInt() / 100.0
        }
        return state.copy(
            marketMultipliers = nextMults,
            lastMarketTickEpochMs = nowMs
        )
    }

    private fun tickMachineryAndPower(state: TycoonSaveState): TycoonSaveState {
        val conveyorLvl = state.conveyorLevel
        val forkliftMult = 1.0 + state.fleetLevel(FleetEquipType.FORKLIFT) * 0.18
        val autoLvl = state.resourceUpgradeLevel(ResourceUpgradeType.AUTO_PRODUCTION)
        val autoMult = 1.0 + (autoLvl - 1) * 0.20
        val excavatorMult = 1.0 + state.fleetLevel(FleetEquipType.EXCAVATOR) * 0.15

        val stock = state.warehouseStock.toMutableMap()
        val bag = state.inventory.toMutableMap()
        var stockUsed = stock.values.sum()
        val stockCap = state.warehouseStockCapacity
        var changed = false

        for (machine in MachineType.entries) {
            if (!state.isMachineRunning(machine)) continue
            val lvl = state.machineLevel(machine)
            val hasInputSupply = machine.inputId == null ||
                (stock[machine.inputId] ?: 0) > 0 ||
                (bag[machine.inputId] ?: 0) > 0
            val feedFactor = if (machine.inputId == null) 1.0 else if (hasInputSupply) 1.65 else 0.50
            val minerBoost = if (machine == MachineType.AUTO_MINER_MACHINE) excavatorMult else 1.0
            val ratePerSec = (machine.unitsPerMinute(lvl, conveyorLvl) / 60.0) *
                forkliftMult * autoMult * feedFactor * minerBoost

            var acc = (machineAccumulators[machine.id] ?: 0.0) + ratePerSec
            while (acc >= 1.0 && stockUsed < stockCap) {
                val inputId = machine.inputId
                if (inputId != null) {
                    val ws = stock[inputId] ?: 0
                    val bg = bag[inputId] ?: 0
                    if (ws > 0) {
                        if (ws == 1) stock.remove(inputId) else stock[inputId] = ws - 1
                        stockUsed--
                    } else if (bg > 0) {
                        if (bg == 1) bag.remove(inputId) else bag[inputId] = bg - 1
                    }
                }
                val outId = machine.outputId
                stock[outId] = (stock[outId] ?: 0) + 1
                stockUsed++
                acc -= 1.0
                changed = true
            }
            machineAccumulators[machine.id] = acc.coerceAtMost(2.5)
        }

        val powerPerSec = state.totalMachinePowerCostPerMinute / 60.0
        var nextCoins = state.coins
        if (powerPerSec > 0.0) {
            powerCostAccum += powerPerSec
            if (powerCostAccum >= 1.0) {
                val deduct = powerCostAccum.toLong()
                powerCostAccum -= deduct.toDouble()
                if (nextCoins >= deduct) {
                    nextCoins -= deduct
                    changed = true
                }
            }
        }

        return if (changed) {
            state.copy(
                coins = nextCoins,
                warehouseStock = stock,
                inventory = bag
            )
        } else {
            state
        }
    }

    fun dismissOfflineBanner() {
        _offlineEarningsBanner.value = null
    }

    fun onBiomeChanged(biomeId: String) {
        BiomeType.fromId(biomeId)?.let { _activeBiome.value = it }
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
            xp = maxOf(current.xp, parsed.xp),
            resourceUpgradeLevels = current.resourceUpgradeLevels,
            buildingWorkers = current.buildingWorkers,
            buildingLines = current.buildingLines,
            machineLevels = current.machineLevels,
            machineActive = current.machineActive,
            warehouseStock = current.warehouseStock,
            fleetLevels = current.fleetLevels,
            unlockedVehicles = current.unlockedVehicles + parsed.unlockedVehicles,
            activeVehicleId = current.activeVehicleId,
            discoveredSecrets = current.discoveredSecrets + parsed.discoveredSecrets,
            defeatedBosses = current.defeatedBosses + parsed.defeatedBosses,
            insectsDefeated = maxOf(current.insectsDefeated, parsed.insectsDefeated),
            playerHp = parsed.playerHp.coerceIn(1, current.playerMaxHp),
            buyerReputationXp = current.buyerReputationXp,
            marketMultipliers = current.marketMultipliers,
            activeContracts = current.activeContracts,
            availableContracts = current.availableContracts,
            totalContractsCompleted = current.totalContractsCompleted,
            lastMarketTickEpochMs = current.lastMarketTickEpochMs,
            claimedQuestIds = current.claimedQuestIds,
            uniqueItems = current.uniqueItems,
            isMuted = current.isMuted,
            sfxVolume = current.sfxVolume,
            musicVolume = current.musicVolume,
            graphicsQuality = current.graphicsQuality,
            hapticsEnabled = current.hapticsEnabled,
            settlement = current.settlement
        )
        _gameState.value = merged
        scheduleDebouncedSave()
    }

    fun purchaseUpgrade(type: UpgradeType): Boolean {
        val state = _gameState.value
        val currentLevel = state.upgradeLevel(type)
        if (currentLevel >= type.maxLevel) return false
        val req = type.resourceRequirementForNext(currentLevel)
        if (!state.meetsResourceRequirement(req)) return false
        val cost = type.costForNextLevel(currentLevel)
        if (state.coins < cost) return false

        val nextLvl = currentLevel + 1
        val updated = when (type) {
            UpgradeType.AXE -> state.copy(coins = state.coins - cost, axeLevel = nextLvl)
            UpgradeType.PICKAXE -> state.copy(coins = state.coins - cost, pickaxeLevel = nextLvl)
            UpgradeType.BACKPACK -> state.copy(coins = state.coins - cost, backpackLevel = nextLvl)
            UpgradeType.MOVE_SPEED -> state.copy(coins = state.coins - cost, moveSpeedLevel = nextLvl)
            UpgradeType.GATHER_SPEED -> state.copy(coins = state.coins - cost, gatherSpeedLevel = nextLvl)
        }
        applyLocalStateChange(updated, notifyBanner = "${type.tierName(nextLvl)} Unlocked! (Tier $nextLvl)")
        _jsCommands.tryEmit("window.triggerUpgradeCelebration && window.triggerUpgradeCelebration();")
        triggerHaptic("upgrade")
        return true
    }

    fun purchaseResourceUpgrade(type: ResourceUpgradeType): Boolean {
        val state = _gameState.value
        val currentLevel = state.resourceUpgradeLevel(type)
        if (currentLevel >= type.maxLevel) return false
        val req = type.resourceRequirementForNext(currentLevel)
        if (!state.meetsResourceRequirement(req)) return false
        val cost = type.costForNextLevel(currentLevel)
        if (state.coins < cost) return false

        val newMap = state.resourceUpgradeLevels.toMutableMap()
        newMap[type.id] = currentLevel + 1
        val updated = state.copy(
            coins = state.coins - cost,
            resourceUpgradeLevels = newMap
        )
        applyLocalStateChange(updated, notifyBanner = "${type.title} -> Tier ${currentLevel + 1}!")
        _jsCommands.tryEmit("window.triggerUpgradeCelebration && window.triggerUpgradeCelebration();")
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
        val actionLabel = if (currentLevel == 0) "${type.title} Built!" else "${type.title} Tier ${currentLevel + 1}!"
        applyLocalStateChange(updated, notifyBanner = actionLabel)
        triggerHaptic("upgrade")
        return true
    }

    fun hireBuildingWorker(type: BuildingType): Boolean {
        val state = _gameState.value
        if (state.buildingLevel(type) <= 0) return false
        val currentWorkers = state.buildingWorkerCount(type)
        if (currentWorkers >= type.maxWorkers) return false
        val cost = type.costForNextWorker(currentWorkers)
        if (state.coins < cost) return false

        val newWorkers = state.buildingWorkers.toMutableMap()
        newWorkers[type.id] = currentWorkers + 1
        val updated = state.copy(
            coins = state.coins - cost,
            buildingWorkers = newWorkers
        )
        applyLocalStateChange(updated, notifyBanner = "${type.title}: Worker #${currentWorkers + 1} Hired!")
        triggerHaptic("upgrade")
        return true
    }

    fun addBuildingLine(type: BuildingType): Boolean {
        val state = _gameState.value
        if (state.buildingLevel(type) <= 0) return false
        val currentLines = state.buildingLineCount(type)
        if (currentLines >= type.maxLines) return false
        val cost = type.costForNextLine(currentLines)
        if (state.coins < cost) return false

        val newLines = state.buildingLines.toMutableMap()
        newLines[type.id] = currentLines + 1
        val updated = state.copy(
            coins = state.coins - cost,
            buildingLines = newLines
        )
        applyLocalStateChange(updated, notifyBanner = "${type.title}: Line #${currentLines + 1} Online!")
        triggerHaptic("upgrade")
        return true
    }

    fun buyOrUpgradeMachine(type: MachineType): Boolean {
        val state = _gameState.value
        if (!state.isMachineUnlocked(type)) return false
        val currentLevel = state.machineLevel(type)
        if (currentLevel >= type.maxLevel) return false
        val cost = type.costForNextLevel(currentLevel)
        if (state.coins < cost) return false

        val newLevels = state.machineLevels.toMutableMap()
        newLevels[type.id] = currentLevel + 1
        val newActive = state.machineActive.toMutableMap()
        if (!newActive.containsKey(type.id)) newActive[type.id] = true

        val updated = state.copy(
            coins = state.coins - cost,
            machineLevels = newLevels,
            machineActive = newActive
        )
        val banner = if (currentLevel == 0) "${type.title} Installed!" else "${type.title} Tier ${currentLevel + 1}!"
        applyLocalStateChange(updated, notifyBanner = banner)
        _jsCommands.tryEmit("window.triggerUpgradeCelebration && window.triggerUpgradeCelebration();")
        triggerHaptic("upgrade")
        return true
    }

    fun buyOrSelectVehicle(type: DrivableVehicleType): Boolean {
        val state = _gameState.value
        if (state.isVehicleUnlocked(type)) {
            val nextId = if (state.activeVehicleId == type.id) null else type.id
            val updated = state.copy(activeVehicleId = nextId)
            val msg = if (nextId != null) "${type.title} Deployed!" else "Dismounted ${type.title}"
            applyLocalStateChange(updated, notifyBanner = msg)
            triggerHaptic("light")
            return true
        }
        if (!state.unlockedBiomes.contains(type.requiredBiomeId)) return false
        if (state.coins < type.cost) return false
        val updated = state.copy(
            coins = state.coins - type.cost,
            unlockedVehicles = state.unlockedVehicles + type.id,
            activeVehicleId = type.id
        )
        applyLocalStateChange(updated, notifyBanner = "${type.title} Unlocked & Deployed!")
        _jsCommands.tryEmit("window.triggerUpgradeCelebration && window.triggerUpgradeCelebration();")
        triggerHaptic("unlock")
        return true
    }

    fun toggleQuickVehicle() {
        val state = _gameState.value
        val owned = DrivableVehicleType.entries.filter { state.isVehicleUnlocked(it) }
        if (owned.isEmpty()) return
        val nextId = if (state.activeVehicleId != null) {
            null
        } else {
            owned.last().id
        }
        val title = DrivableVehicleType.fromId(nextId)?.title ?: "On Foot"
        val updated = state.copy(activeVehicleId = nextId)
        applyLocalStateChange(updated, notifyBanner = if (nextId != null) "Driving: $title" else "On Foot")
        triggerHaptic("light")
    }

    fun toggleMachineActive(type: MachineType) {
        val state = _gameState.value
        if (state.machineLevel(type) <= 0) return
        val newActive = state.machineActive.toMutableMap()
        val nextRunning = !state.isMachineRunning(type)
        newActive[type.id] = nextRunning
        val updated = state.copy(machineActive = newActive)
        applyLocalStateChange(
            updated,
            notifyBanner = if (nextRunning) "${type.title} Started" else "${type.title} Paused"
        )
        triggerHaptic("light")
    }

    fun buyOrUpgradeFleet(type: FleetEquipType): Boolean {
        val state = _gameState.value
        val currentLevel = state.fleetLevel(type)
        if (currentLevel >= type.maxLevel) return false
        val cost = type.costForNextLevel(currentLevel)
        if (state.coins < cost) return false

        val newFleet = state.fleetLevels.toMutableMap()
        newFleet[type.id] = currentLevel + 1
        val updated = state.copy(
            coins = state.coins - cost,
            fleetLevels = newFleet
        )
        val banner = if (currentLevel == 0) "${type.title} Acquired!" else "${type.title} Tier ${currentLevel + 1}!"
        applyLocalStateChange(updated, notifyBanner = banner)
        _jsCommands.tryEmit("window.triggerUpgradeCelebration && window.triggerUpgradeCelebration();")
        triggerHaptic("upgrade")
        return true
    }

    fun depositBackpackToWarehouse(): Int {
        val state = _gameState.value
        if (state.inventory.isEmpty()) return 0
        val free = (state.warehouseStockCapacity - state.warehouseStockUsed).coerceAtLeast(0)
        if (free <= 0) return 0

        val nextBag = state.inventory.toMutableMap()
        val nextStock = state.warehouseStock.toMutableMap()
        var movedTotal = 0
        var remainingFree = free
        for ((resId, qty) in state.inventory) {
            if (qty <= 0 || remainingFree <= 0) continue
            val take = minOf(qty, remainingFree)
            nextStock[resId] = (nextStock[resId] ?: 0) + take
            val left = qty - take
            if (left <= 0) nextBag.remove(resId) else nextBag[resId] = left
            movedTotal += take
            remainingFree -= take
        }
        if (movedTotal <= 0) return 0
        val updated = state.copy(
            inventory = nextBag,
            warehouseStock = nextStock
        )
        applyLocalStateChange(updated, notifyBanner = "Deposited $movedTotal items to Warehouse")
        triggerHaptic("coin")
        return movedTotal
    }

    fun sellGoodToBuyer(buyer: BuyerCompany, goodId: String, quantity: Int): Boolean {
        val state = _gameState.value
        if (!state.isBuyerUnlocked(buyer)) return false
        val available = state.availableForTrade(goodId)
        val qty = quantity.coerceAtMost(available)
        if (qty <= 0) return false

        val unitPrice = state.buyerOfferUnitPrice(buyer, goodId)
        val payout = unitPrice * qty
        val (nextStock, nextBag) = deductFromStockAndBag(state, goodId, qty)
        val repGain = (qty * 2).coerceIn(2, 35)
        val xpGain = (qty * 2L).coerceIn(3L, 80L)
        val nextRepMap = state.buyerReputationXp.toMutableMap()
        nextRepMap[buyer.id] = (nextRepMap[buyer.id] ?: 0) + repGain

        val updated = state.copy(
            coins = state.coins + payout,
            lifetimeEarned = state.lifetimeEarned + payout,
            xp = state.xp + xpGain,
            warehouseStock = nextStock,
            inventory = nextBag,
            buyerReputationXp = nextRepMap
        )
        val goodName = TradeCatalog.find(goodId)?.displayName ?: goodId
        applyLocalStateChange(
            updated,
            notifyBanner = "Sold ${qty}x $goodName to ${buyer.companyName} (+$$payout)"
        )
        triggerHaptic("coin")
        return true
    }

    fun acceptContract(contractId: String): Boolean {
        val state = _gameState.value
        if (state.activeContracts.size >= state.maxActiveContractsAllowed) return false
        val offer = state.availableContracts.firstOrNull { it.id == contractId } ?: return false
        if (state.activeContracts.any { it.id == contractId }) return false

        val now = System.currentTimeMillis()
        val deadline = if (offer.tier.durationSeconds > 0L) {
            now + offer.tier.durationSeconds * 1000L
        } else 0L
        val accepted = offer.copy(acceptedEpochMs = now, deadlineEpochMs = deadline)
        val remaining = state.availableContracts.filterNot { it.id == contractId }
        val updated = state.copy(
            activeContracts = state.activeContracts + accepted,
            availableContracts = remaining
        )
        val buyerName = BuyerCompany.fromId(offer.buyerId)?.companyName ?: "Buyer"
        applyLocalStateChange(updated, notifyBanner = "Contract Accepted: $buyerName")
        triggerHaptic("light")
        return true
    }

    fun deliverContract(contractId: String): Boolean {
        val state = _gameState.value
        val contract = state.activeContracts.firstOrNull { it.id == contractId } ?: return false
        if (state.availableForTrade(contract.goodId) < contract.quantity) return false

        val now = System.currentTimeMillis()
        val late = contract.isExpired(now)
        val payout = if (late) (contract.rewardCoins * 0.65).roundToLong().coerceAtLeast(1L) else contract.rewardCoins
        val xpGain = if (late) (contract.rewardXp / 2L).coerceAtLeast(5L) else contract.rewardXp
        val repGain = if (late) (contract.rewardRep / 2).coerceAtLeast(3) else contract.rewardRep

        val (nextStock, nextBag) = deductFromStockAndBag(state, contract.goodId, contract.quantity)
        val nextRepMap = state.buyerReputationXp.toMutableMap()
        nextRepMap[contract.buyerId] = (nextRepMap[contract.buyerId] ?: 0) + repGain
        val nextActive = state.activeContracts.filterNot { it.id == contractId }

        var updated = state.copy(
            coins = state.coins + payout,
            lifetimeEarned = state.lifetimeEarned + payout,
            xp = state.xp + xpGain,
            warehouseStock = nextStock,
            inventory = nextBag,
            buyerReputationXp = nextRepMap,
            activeContracts = nextActive,
            totalContractsCompleted = state.totalContractsCompleted + 1
        )
        if (updated.availableContracts.size < 3) {
            marketSeedOffset++
            updated = updated.copy(
                availableContracts = ContractGenerator.generateOffers(updated, marketSeedOffset)
            )
        }
        val tag = if (late) "Delivered Late (-35%)" else "Contract Fulfilled!"
        applyLocalStateChange(updated, notifyBanner = "$tag +$$payout & +$repGain Rep")
        triggerHaptic("unlock")
        return true
    }

    fun cancelContract(contractId: String) {
        val state = _gameState.value
        if (state.activeContracts.none { it.id == contractId }) return
        val updated = state.copy(
            activeContracts = state.activeContracts.filterNot { it.id == contractId }
        )
        applyLocalStateChange(updated, notifyBanner = "Contract Cancelled")
    }

    fun refreshContracts() {
        val state = _gameState.value
        marketSeedOffset++
        val activeIds = state.activeContracts.map { it.id }.toSet()
        val fresh = ContractGenerator.generateOffers(state, marketSeedOffset)
            .filterNot { activeIds.contains(it.id) }
        val updated = state.copy(availableContracts = fresh)
        applyLocalStateChange(updated, notifyBanner = "New Buyer Contracts Posted!")
        triggerHaptic("light")
    }

    // =========================================================================
    // SURVIVOR SETTLEMENT, HOUSING, TRADING & EMPLOYMENT ACTIONS
    // =========================================================================

    fun startHousingConstruction(housingType: HousingType): Boolean =
        runSettlementAction("upgrade") {
            SettlementEngine.startHousingConstruction(_gameState.value, housingType, System.currentTimeMillis())
        }

    fun upgradeHousing(housingId: String): Boolean {
        val ok = runSettlementAction("upgrade") {
            SettlementEngine.upgradeHousing(_gameState.value, housingId, System.currentTimeMillis())
        }
        if (ok) {
            _jsCommands.tryEmit("window.triggerUpgradeCelebration && window.triggerUpgradeCelebration();")
        }
        return ok
    }

    fun buildOrUpgradeFacility(facilityType: SettlementFacilityType): Boolean {
        val ok = runSettlementAction("upgrade") {
            SettlementEngine.buildOrUpgradeFacility(_gameState.value, facilityType, System.currentTimeMillis())
        }
        if (ok) {
            _jsCommands.tryEmit("window.triggerUpgradeCelebration && window.triggerUpgradeCelebration();")
        }
        return ok
    }

    fun demolishHousing(housingId: String): Boolean =
        runSettlementAction("light") {
            SettlementEngine.demolishHousing(_gameState.value, housingId)
        }

    fun admitRefugee(arrivalId: String): Boolean =
        runSettlementAction("unlock") {
            SettlementEngine.admitRefugee(_gameState.value, arrivalId, System.currentTimeMillis())
        }

    fun dismissRefugee(arrivalId: String) {
        val updated = SettlementEngine.dismissRefugee(_gameState.value, arrivalId)
        applyLocalStateChange(updated)
    }

    fun scoutRefugeeArrival(): Boolean =
        runSettlementAction("light") {
            SettlementEngine.scoutRefugeeArrival(_gameState.value, System.currentTimeMillis())
        }

    fun assignSurvivorJob(survivorId: String, role: SurvivorProfession?): Boolean =
        runSettlementAction("light") {
            SettlementEngine.assignSurvivorJob(_gameState.value, survivorId, role)
        }

    fun buyCommunitySupplies(kind: SupplyRestockKind, batches: Int = 1): Boolean =
        runSettlementAction("coin") {
            SettlementEngine.buyCommunitySupplies(_gameState.value, kind, batches, System.currentTimeMillis())
        }

    fun sellGoodToSurvivors(goodId: String, quantity: Int): Boolean =
        runSettlementAction("coin") {
            SettlementEngine.sellGoodToSurvivors(_gameState.value, goodId, quantity, System.currentTimeMillis())
        }

    fun buyGoodFromSurvivors(goodId: String, quantity: Int): Boolean =
        runSettlementAction("coin") {
            SettlementEngine.buyGoodFromSurvivors(_gameState.value, goodId, quantity, System.currentTimeMillis())
        }

    fun completeSettlementContract(contractId: String): Boolean =
        runSettlementAction("unlock") {
            SettlementEngine.completeConstructionContract(_gameState.value, contractId, System.currentTimeMillis())
        }

    private inline fun runSettlementAction(
        hapticType: String,
        action: () -> SettlementActionOutcome
    ): Boolean {
        return when (val outcome = action()) {
            is SettlementActionOutcome.Success -> {
                applyLocalStateChange(outcome.updatedState, notifyBanner = outcome.bannerMessage)
                triggerHaptic(hapticType)
                true
            }
            is SettlementActionOutcome.Failure -> {
                val safe = JSONObject.quote(outcome.reason)
                _jsCommands.tryEmit("window.showNativeBanner && window.showNativeBanner($safe);")
                false
            }
        }
    }

    private fun deductFromStockAndBag(
        state: TycoonSaveState,
        goodId: String,
        amount: Int
    ): Pair<Map<String, Int>, Map<String, Int>> {
        var remaining = amount
        val stock = state.warehouseStock.toMutableMap()
        val bag = state.inventory.toMutableMap()

        val ws = stock[goodId] ?: 0
        if (ws > 0 && remaining > 0) {
            val take = minOf(ws, remaining)
            if (ws == take) stock.remove(goodId) else stock[goodId] = ws - take
            remaining -= take
        }
        val bg = bag[goodId] ?: 0
        if (bg > 0 && remaining > 0) {
            val take = minOf(bg, remaining)
            if (bg == take) bag.remove(goodId) else bag[goodId] = bg - take
        }
        return stock to bag
    }

    fun unlockBiome(biome: BiomeType): Boolean {
        val state = _gameState.value
        if (state.isBiomeUnlocked(biome)) return false
        if (!biome.meetsEquipmentRequirements(state)) return false
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

    fun teleportToSettlementEnclave() {
        if (!_gameState.value.isBiomeUnlocked(BiomeType.MOUNTAINS)) {
            val safe = JSONObject.quote("Unlock Rocky Mountains (Map 2) to walk into your 3D Hexagon Settlement!")
            _jsCommands.tryEmit("window.showNativeBanner && window.showNativeBanner($safe);")
            return
        }
        _jsCommands.tryEmit("window.teleportPlayer(-21.5, -34.0, 'mountains');")
    }

    fun setMenuMode(inMenu: Boolean) {
        _jsCommands.tryEmit("window.setMenuMode && window.setMenuMode($inMenu);")
    }

    fun toggleMute() {
        val updated = _gameState.value.copy(isMuted = !_gameState.value.isMuted)
        lastKotlinMutationElapsedMs = SystemClock.elapsedRealtime()
        _gameState.value = updated
        pushStateToEngine(updated)
        scheduleImmediateSave(updated)
    }

    fun setSfxVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val updated = _gameState.value.copy(sfxVolume = clamped)
        lastKotlinMutationElapsedMs = SystemClock.elapsedRealtime()
        _gameState.value = updated
        pushStateToEngine(updated)
        scheduleDebouncedSave()
    }

    fun setMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val updated = _gameState.value.copy(musicVolume = clamped)
        lastKotlinMutationElapsedMs = SystemClock.elapsedRealtime()
        _gameState.value = updated
        pushStateToEngine(updated)
        scheduleDebouncedSave()
    }

    fun setGraphicsQuality(quality: Int) {
        val clamped = quality.coerceIn(0, 2)
        val updated = _gameState.value.copy(graphicsQuality = clamped)
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
                sfxVolume = _gameState.value.sfxVolume,
                musicVolume = _gameState.value.musicVolume,
                graphicsQuality = _gameState.value.graphicsQuality,
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
