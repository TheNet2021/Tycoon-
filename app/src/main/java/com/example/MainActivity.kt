package com.example

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GameHudOverlay
import com.example.ui.components.ThreeJsGameView
import com.example.ui.sheets.AdvisorChatSheet
import com.example.ui.sheets.BiomeMapSheet
import com.example.ui.sheets.ConceptStudioSheet
import com.example.ui.sheets.QuestBoardSheet
import com.example.ui.sheets.UpgradeShopSheet
import com.example.ui.theme.WildcraftTheme
import com.example.viewmodel.AiViewModel
import com.example.viewmodel.GameViewModel

private enum class ActiveSheet {
    NONE,
    SHOP,
    BIOMES,
    QUESTS,
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
                val offlineEarnings by gameViewModel.offlineEarningsBanner.collectAsStateWithLifecycle()
                val chatState by aiViewModel.chatState.collectAsStateWithLifecycle()
                val studioPrompt by aiViewModel.studioPrompt.collectAsStateWithLifecycle()
                val selectedSize by aiViewModel.selectedImageSize.collectAsStateWithLifecycle()
                val studioState by aiViewModel.studioState.collectAsStateWithLifecycle()

                var activeSheet by remember { mutableStateOf(ActiveSheet.NONE) }

                Box(modifier = Modifier.fillMaxSize()) {
                    ThreeJsGameView(
                        viewModel = gameViewModel,
                        onWebViewReady = { wv -> gameWebView = wv },
                        modifier = Modifier.fillMaxSize()
                    )

                    GameHudOverlay(
                        state = gameState,
                        offlineEarnings = offlineEarnings,
                        onDismissOfflineBanner = gameViewModel::dismissOfflineBanner,
                        onOpenShop = { activeSheet = ActiveSheet.SHOP },
                        onOpenBiomes = { activeSheet = ActiveSheet.BIOMES },
                        onOpenQuests = { activeSheet = ActiveSheet.QUESTS },
                        onOpenAdvisor = { activeSheet = ActiveSheet.ADVISOR },
                        onOpenStudio = { activeSheet = ActiveSheet.STUDIO },
                        onToggleMute = gameViewModel::toggleMute
                    )
                }

                when (activeSheet) {
                    ActiveSheet.NONE -> Unit
                    ActiveSheet.SHOP -> UpgradeShopSheet(
                        state = gameState,
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onPurchaseUpgrade = gameViewModel::purchaseUpgrade,
                        onBuildOrUpgradeBuilding = gameViewModel::buildOrUpgradeBuilding,
                        onToggleHaptics = gameViewModel::toggleHaptics,
                        onResetProgress = gameViewModel::resetProgress
                    )
                    ActiveSheet.BIOMES -> BiomeMapSheet(
                        state = gameState,
                        onDismiss = { activeSheet = ActiveSheet.NONE },
                        onUnlockBiome = gameViewModel::unlockBiome,
                        onTeleportBiome = gameViewModel::teleportToBiome
                    )
                    ActiveSheet.QUESTS -> QuestBoardSheet(
                        state = gameState,
                        onClaimQuest = gameViewModel::claimQuest,
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
