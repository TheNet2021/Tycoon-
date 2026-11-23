package com.example

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UpgradeType
import com.example.ui.components.GameHudOverlay
import com.example.ui.components.HomeMenuOverlay
import com.example.ui.components.LoadingExpeditionOverlay
import com.example.ui.components.ThreeJsGameView
import com.example.ui.sheets.AdvisorChatSheet
import com.example.ui.sheets.BackpackSheet
import com.example.ui.sheets.BiomeMapSheet
import com.example.ui.sheets.BusinessSheet
import com.example.ui.sheets.ConceptStudioSheet
import com.example.ui.sheets.MachinerySheet
import com.example.ui.sheets.MarketplaceSheet
import com.example.ui.sheets.ProfileSheet
import com.example.ui.sheets.QuestBoardSheet
import com.example.ui.sheets.SettingsSheet
import com.example.ui.sheets.SettlementSheet
import com.example.ui.sheets.UpgradeShopSheet
import com.example.ui.theme.WildcraftTheme
import com.example.viewmodel.AiViewModel
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ActiveSheet {
    NONE,
    UPGRADES,
    MACHINERY,
    MARKET,
    BUSINESS,
    SETTLEMENT,
    BACKPACK,
    BIOMES,
    PROFILE,
    QUESTS,
    SETTINGS,
    ADVISOR,
    STUDIO
}

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()
    private val aiViewModel: AiViewModel by viewModels()
    private var gameWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WildcraftTheme {
                val gameState by gameViewModel.gameState.collectAsStateWithLifecycle()
                val activeBiome by gameViewModel.activeBiome.collectAsStateWithLifecycle()
                val saveSlots by gameViewModel.saveSlots.collectAsStateWithLifecycle()
                val activeSlotIndex by gameViewModel.activeSlotIndex.collectAsStateWithLifecycle()
                val isEngineReady by gameViewModel.isEngineReady.collectAsStateWithLifecycle()
                val offlineEarnings by gameViewModel.offlineEarningsBanner.collectAsStateWithLifecycle()
                val chatState by aiViewModel.chatState.collectAsStateWithLifecycle()
                val studioPrompt by aiViewModel.studioPrompt.collectAsStateWithLifecycle()
                val selectedSize by aiViewModel.selectedImageSize.collectAsStateWithLifecycle()
                val studioState by aiViewModel.studioState.collectAsStateWithLifecycle()

                val coroutineScope = rememberCoroutineScope()
                var isInHomeMenu by remember { mutableStateOf(true) }
                var isLoadingExpedition by remember { mutableStateOf(false) }
                var activeSheet by remember { mutableStateOf(ActiveSheet.NONE) }

                LaunchedEffect(isEngineReady) {
                    if (isEngineReady && isInHomeMenu) {
                        gameViewModel.setMenuMode(true)
                    }
                }

                BackHandler(
                    enabled = !isInHomeMenu && !isLoadingExpedition && activeSheet == ActiveSheet.NONE
                ) {
                    isInHomeMenu = true
                    gameViewModel.setMenuMode(true)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    ThreeJsGameView(
                        viewModel = gameViewModel,
                        onWebViewReady = { wv -> gameWebView = wv },
                        modifier = Modifier.fillMaxSize()
                    )

                    AnimatedVisibility(
                        visible = !isInHomeMenu && !isLoadingExpedition,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        GameHudOverlay(
                            state = gameState,
                            activeBiome = activeBiome,
                            offlineEarnings = offlineEarnings,
                            onDismissOfflineBanner = gameViewModel::dismissOfflineBanner,
                            onOpenUpgrades = { activeSheet = ActiveSheet.UPGRADES },
                            onOpenMachinery = { activeSheet = ActiveSheet.MACHINERY },
                            onOpenMarket = { activeSheet = ActiveSheet.MARKET },
                            onOpenBusiness = { activeSheet = ActiveSheet.BUSINESS },
                            onOpenBackpack = { activeSheet = ActiveSheet.BACKPACK },
                            onOpenBiomes = { activeSheet = ActiveSheet.BIOMES },
                            onOpenQuests = { activeSheet = ActiveSheet.QUESTS },
                            onOpenSettings = { activeSheet = ActiveSheet.SETTINGS },
                            onOpenSettlement = { activeSheet = ActiveSheet.SETTLEMENT },
                            onToggleVehicle = gameViewModel::toggleQuickVehicle
                        )
                    }

                    AnimatedVisibility(
                        visible = isInHomeMenu && !isLoadingExpedition,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        HomeMenuOverlay(
                            state = gameState,
                            activeBiome = activeBiome,
                            saveSlots = saveSlots,
                            activeSlotIndex = activeSlotIndex,
                            onContinue = {
                                if (!isLoadingExpedition) {
                                    isLoadingExpedition = true
                                    isInHomeMenu = false
                                    gameViewModel.beginContinueExpedition()
                                    coroutineScope.launch {
                                        delay(650L)
                                        isLoadingExpedition = false
                                    }
                                }
                            },
                            onNewGame = {
                                if (!isLoadingExpedition) {
                                    isLoadingExpedition = true
                                    isInHomeMenu = false
                                    if (gameState.hasSavedProgress) {
                                        gameViewModel.resetProgress()
                                    }
                                    gameViewModel.setMenuMode(false)
                                    coroutineScope.launch {
                                        delay(520L)
                                        isLoadingExpedition = false
                                    }
                                }
                            },
                            onOpenWorldMap = { activeSheet = ActiveSheet.BIOMES },
                            onOpenProfile = { activeSheet = ActiveSheet.PROFILE },
                            onOpenAchievements = { activeSheet = ActiveSheet.QUESTS },
                            onOpenMarket = { activeSheet = ActiveSheet.MARKET },
                            onOpenSettings = { activeSheet = ActiveSheet.SETTINGS },
                            onSelectSaveSlot = gameViewModel::selectSaveSlot,
                            onToggleMute = gameViewModel::toggleMute
                        )
                    }

                    AnimatedVisibility(
                        visible = isLoadingExpedition,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        LoadingExpeditionOverlay(
                            biome = activeBiome,
                            playerLevel = gameState.playerLevel,
                            languageCode = gameState.languageCode
                        )
                    }
                }

                when (activeSheet) {
                    ActiveSheet.NONE -> Unit
                    ActiveSheet.UPGRADES -> UpgradeShopSheet(
                        state = gameState,
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onPurchaseUpgrade = gameViewModel::purchaseUpgrade,
                        onPurchaseResourceUpgrade = gameViewModel::purchaseResourceUpgrade
                    )
                    ActiveSheet.MACHINERY -> MachinerySheet(
                        state = gameState,
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onBuyOrUpgradeMachine = gameViewModel::buyOrUpgradeMachine,
                        onToggleMachineActive = gameViewModel::toggleMachineActive,
                        onBuyOrUpgradeFleet = gameViewModel::buyOrUpgradeFleet,
                        onBuyOrSelectVehicle = gameViewModel::buyOrSelectVehicle,
                        onDepositBackpack = { gameViewModel.depositBackpackToWarehouse() }
                    )
                    ActiveSheet.MARKET -> MarketplaceSheet(
                        state = gameState,
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onSellToBuyer = gameViewModel::sellGoodToBuyer,
                        onAcceptContract = gameViewModel::acceptContract,
                        onDeliverContract = gameViewModel::deliverContract,
                        onCancelContract = gameViewModel::cancelContract,
                        onRefreshContracts = gameViewModel::refreshContracts
                    )
                    ActiveSheet.BUSINESS -> BusinessSheet(
                        state = gameState,
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onBuildOrUpgradeBuilding = gameViewModel::buildOrUpgradeBuilding,
                        onHireWorker = gameViewModel::hireBuildingWorker,
                        onAddLine = gameViewModel::addBuildingLine
                    )
                    ActiveSheet.SETTLEMENT -> SettlementSheet(
                        state = gameState,
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onStartHousing = gameViewModel::startHousingConstruction,
                        onUpgradeHousing = gameViewModel::upgradeHousing,
                        onBuildOrUpgradeFacility = gameViewModel::buildOrUpgradeFacility,
                        onDemolishHousing = gameViewModel::demolishHousing,
                        onAdmitRefugee = gameViewModel::admitRefugee,
                        onDismissRefugee = gameViewModel::dismissRefugee,
                        onScoutArrival = gameViewModel::scoutRefugeeArrival,
                        onAssignJob = gameViewModel::assignSurvivorJob,
                        onBuySupplies = gameViewModel::buyCommunitySupplies,
                        onSellToSurvivors = gameViewModel::sellGoodToSurvivors,
                        onBuyFromSurvivors = gameViewModel::buyGoodFromSurvivors,
                        onCompleteContract = gameViewModel::completeSettlementContract,
                        onVisitSettlement3D = {
                            activeSheet = ActiveSheet.NONE
                            isInHomeMenu = false
                            gameViewModel.setMenuMode(false)
                            gameViewModel.teleportToSettlementEnclave()
                        }
                    )
                    ActiveSheet.BACKPACK -> BackpackSheet(
                        state = gameState,
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onUpgradeBackpack = { gameViewModel.purchaseUpgrade(UpgradeType.BACKPACK) }
                    )
                    ActiveSheet.BIOMES -> BiomeMapSheet(
                        state = gameState,
                        activeBiome = activeBiome,
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onUnlockBiome = gameViewModel::unlockBiome,
                        onTeleportBiome = { biome ->
                            activeSheet = ActiveSheet.NONE
                            isInHomeMenu = false
                            gameViewModel.setMenuMode(false)
                            gameViewModel.teleportToBiome(biome)
                        }
                    )
                    ActiveSheet.PROFILE -> ProfileSheet(
                        state = gameState,
                        activeSlotIndex = activeSlotIndex,
                        onOpenAchievements = { activeSheet = ActiveSheet.QUESTS },
                        onDismiss = { activeSheet = ActiveSheet.NONE }
                    )
                    ActiveSheet.QUESTS -> QuestBoardSheet(
                        state = gameState,
                        onClaimQuest = gameViewModel::claimQuest,
                        onDismiss = { activeSheet = ActiveSheet.NONE }
                    )
                    ActiveSheet.SETTINGS -> SettingsSheet(
                        state = gameState,
                        showReturnToHome = !isInHomeMenu,
                        onToggleMute = gameViewModel::toggleMute,
                        onMasterVolumeChange = gameViewModel::setMasterVolume,
                        onAmbientVolumeChange = gameViewModel::setAmbientVolume,
                        onWeatherVolumeChange = gameViewModel::setWeatherVolume,
                        onWaterVolumeChange = gameViewModel::setWaterVolume,
                        onSfxVolumeChange = gameViewModel::setSfxVolume,
                        onMusicVolumeChange = gameViewModel::setMusicVolume,
                        onGraphicsQualityChange = gameViewModel::setGraphicsQuality,
                        onShadowQualityChange = gameViewModel::setShadowQuality,
                        onCameraSensitivityChange = gameViewModel::setCameraSensitivity,
                        onCameraDistanceChange = gameViewModel::setCameraDistance,
                        onFpsLimitChange = gameViewModel::setFpsLimit,
                        onLanguageChange = gameViewModel::setLanguageCode,
                        onResetSettings = gameViewModel::resetSettingsToDefault,
                        onToggleHaptics = gameViewModel::toggleHaptics,
                        onReturnToHome = {
                            isLoadingExpedition = false
                            isInHomeMenu = true
                            gameViewModel.setMenuMode(true)
                        },
                        onOpenAdvisor = { activeSheet = ActiveSheet.ADVISOR },
                        onOpenStudio = { activeSheet = ActiveSheet.STUDIO },
                        onResetProgress = gameViewModel::resetProgress,
                        onDismiss = { activeSheet = ActiveSheet.NONE }
                    )
                    ActiveSheet.ADVISOR -> AdvisorChatSheet(
                        chatState = chatState,
                        isFirebaseEnabled = aiViewModel.isFirebaseEnabled,
                        systemInstruction = aiViewModel.systemInstruction,
                        gameContextSummary = gameState.toContextSummary(),
                        onSendMessage = { msg ->
                            aiViewModel.sendAdvisorMessage(msg, gameState.toContextSummary())
                        },
                        onClearChat = aiViewModel::clearChat,
                        onDismiss = { activeSheet = ActiveSheet.NONE }
                    )
                    ActiveSheet.STUDIO -> ConceptStudioSheet(
                        prompt = studioPrompt,
                        selectedSize = selectedSize,
                        studioState = studioState,
                        isFirebaseEnabled = aiViewModel.isFirebaseEnabled,
                        onPromptChange = aiViewModel::setStudioPrompt,
                        onSelectSize = aiViewModel::setSelectedImageSize,
                        onGenerate = aiViewModel::generateConceptImage,
                        onDismiss = { activeSheet = ActiveSheet.NONE }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gameWebView?.onResume()
        gameWebView?.resumeTimers()
        gameViewModel.setAppBackgrounded(false)
    }

    override fun onPause() {
        gameViewModel.setAppBackgrounded(true)
        gameWebView?.evaluateJavascript("window.flushSaveToAndroid && window.flushSaveToAndroid();", null)
        gameWebView?.onPause()
        gameWebView?.pauseTimers()
        gameViewModel.flushSave()
        super.onPause()
    }
}
