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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UpgradeType
import com.example.ui.components.GameHudOverlay
import com.example.ui.components.HomeMenuOverlay
import com.example.ui.components.ThreeJsGameView
import com.example.ui.sheets.AdvisorChatSheet
import com.example.ui.sheets.BackpackSheet
import com.example.ui.sheets.BiomeMapSheet
import com.example.ui.sheets.BusinessSheet
import com.example.ui.sheets.ConceptStudioSheet
import com.example.ui.sheets.MachinerySheet
import com.example.ui.sheets.MarketplaceSheet
import com.example.ui.sheets.QuestBoardSheet
import com.example.ui.sheets.SettingsSheet
import com.example.ui.sheets.SettlementSheet
import com.example.ui.sheets.UpgradeShopSheet
import com.example.ui.theme.WildcraftTheme
import com.example.viewmodel.AiViewModel
import com.example.viewmodel.GameViewModel

private enum class ActiveSheet {
    NONE,
    UPGRADES,
    MACHINERY,
    MARKET,
    BUSINESS,
    SETTLEMENT,
    BACKPACK,
    BIOMES,
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
                val isEngineReady by gameViewModel.isEngineReady.collectAsStateWithLifecycle()
                val offlineEarnings by gameViewModel.offlineEarningsBanner.collectAsStateWithLifecycle()
                val chatState by aiViewModel.chatState.collectAsStateWithLifecycle()
                val studioPrompt by aiViewModel.studioPrompt.collectAsStateWithLifecycle()
                val selectedSize by aiViewModel.selectedImageSize.collectAsStateWithLifecycle()
                val studioState by aiViewModel.studioState.collectAsStateWithLifecycle()

                var isInHomeMenu by remember { mutableStateOf(true) }
                var activeSheet by remember { mutableStateOf(ActiveSheet.NONE) }

                LaunchedEffect(isEngineReady, isInHomeMenu) {
                    if (isEngineReady) {
                        gameViewModel.setMenuMode(isInHomeMenu)
                    }
                }

                BackHandler(enabled = !isInHomeMenu && activeSheet == ActiveSheet.NONE) {
                    isInHomeMenu = true
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    ThreeJsGameView(
                        viewModel = gameViewModel,
                        onWebViewReady = { wv -> gameWebView = wv },
                        modifier = Modifier.fillMaxSize()
                    )

                    AnimatedVisibility(
                        visible = !isInHomeMenu,
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
                        visible = isInHomeMenu,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        HomeMenuOverlay(
                            state = gameState,
                            activeBiome = activeBiome,
                            onPlay = {
                                isInHomeMenu = false
                                gameViewModel.setMenuMode(false)
                            },
                            onOpenUpgrades = { activeSheet = ActiveSheet.UPGRADES },
                            onOpenMachinery = { activeSheet = ActiveSheet.MACHINERY },
                            onOpenMarket = { activeSheet = ActiveSheet.MARKET },
                            onOpenInventory = { activeSheet = ActiveSheet.BACKPACK },
                            onOpenWorldMap = { activeSheet = ActiveSheet.BIOMES },
                            onOpenBusiness = { activeSheet = ActiveSheet.BUSINESS },
                            onOpenQuests = { activeSheet = ActiveSheet.QUESTS },
                            onOpenSettings = { activeSheet = ActiveSheet.SETTINGS },
                            onOpenSettlement = { activeSheet = ActiveSheet.SETTLEMENT }
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
                        onDemolishHousing = gameViewModel::demolishHousing,
                        onAdmitRefugee = gameViewModel::admitRefugee,
                        onDismissRefugee = gameViewModel::dismissRefugee,
                        onScoutArrival = gameViewModel::scoutRefugeeArrival,
                        onAssignJob = gameViewModel::assignSurvivorJob,
                        onBuySupplies = gameViewModel::buyCommunitySupplies,
                        onSellToSurvivors = gameViewModel::sellGoodToSurvivors,
                        onBuyFromSurvivors = gameViewModel::buyGoodFromSurvivors,
                        onCompleteContract = gameViewModel::completeSettlementContract
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
                            isInHomeMenu = false
                            gameViewModel.setMenuMode(false)
                            gameViewModel.teleportToBiome(biome)
                        }
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
                        onSfxVolumeChange = gameViewModel::setSfxVolume,
                        onMusicVolumeChange = gameViewModel::setMusicVolume,
                        onGraphicsQualityChange = gameViewModel::setGraphicsQuality,
                        onToggleHaptics = gameViewModel::toggleHaptics,
                        onReturnToHome = {
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

    override fun onPause() {
        super.onPause()
        gameWebView?.evaluateJavascript("window.flushSaveToAndroid && window.flushSaveToAndroid();", null)
        gameViewModel.flushSave()
    }
}
