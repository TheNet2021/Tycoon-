package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BiomeType
import com.example.data.SaveSlotSummary
import com.example.data.TycoonSaveState
import com.example.ui.sheets.formatPlaytime
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CreamText
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.ObsidianForest
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PanelDark
import com.example.ui.theme.PanelElevated
import com.example.ui.theme.localizedLabelsFor

@Composable
fun HomeMenuOverlay(
    state: TycoonSaveState,
    activeBiome: BiomeType,
    saveSlots: List<SaveSlotSummary> = emptyList(),
    activeSlotIndex: Int = 0,
    onContinue: () -> Unit,
    onNewGame: () -> Unit,
    onOpenWorldMap: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectSaveSlot: (Int) -> Unit = {},
    onToggleMute: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasProgress = state.hasSavedProgress
    val readyAchievements = state.readyQuestCount
    val labels = remember(state.languageCode) { localizedLabelsFor(state.languageCode) }
    var showNewGameConfirm by remember { mutableStateOf(false) }
    var showSaveSlotsDialog by remember { mutableStateOf(false) }

    val pulseTransition = rememberInfiniteTransition(label = "menuPulse")
    val ctaScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.018f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ctaScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianForest.copy(alpha = 0.62f),
                        Color.Transparent,
                        Color.Transparent,
                        ObsidianForest.copy(alpha = 0.84f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // =====================================================================
        // TOP BAR (SAVE SLOT SELECTOR + MUTE) & WILDCRAFT TYCOON HERO BRANDING
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelDark.copy(alpha = 0.84f),
                    border = BorderStroke(1.dp, EmeraldCanopy.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showSaveSlotsDialog = true }
                        .minimumInteractiveComponentSize()
                        .testTag("btn_home_slots")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = labels.saveSlotsBtn,
                            tint = EmeraldCanopy,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasProgress) {
                                "Slot ${activeSlotIndex + 1} • ${activeBiome.title}"
                            } else {
                                "Slot ${activeSlotIndex + 1} • New"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = CreamText,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelDark.copy(alpha = 0.84f),
                    border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.75f))
                ) {
                    IconButton(
                        onClick = onToggleMute,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (state.isMuted) CoralCrimson else CrystalCyan
                        ),
                        modifier = Modifier
                            .size(44.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("btn_home_mute")
                    ) {
                        Icon(
                            imageVector = if (state.isMuted) Icons.AutoMirrored.Filled.VolumeOff
                            else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = stringResource(R.string.cd_toggle_audio),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // WILDCRAFT TYCOON Hero Crest
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = PanelDark.copy(alpha = 0.76f),
                border = BorderStroke(
                    1.4.dp,
                    Brush.horizontalGradient(listOf(AmberGold, EmeraldCanopy, AmberBright))
                ),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WildcraftCrestEmblem(modifier = Modifier.size(44.dp))

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "WILDCRAFT",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 26.sp,
                                letterSpacing = 1.6.sp
                            ),
                            color = AmberBright,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldCanopy.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, EmeraldCanopy.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "TYCOON",
                                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.4.sp),
                                    color = EmeraldCanopy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = activeBiome.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = LichenMuted
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // BOTTOM START MENU DOCK (SEPARATE FROM IN-GAME HUD)
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = PanelDark.copy(alpha = 0.90f),
            border = BorderStroke(
                1.2.dp,
                Brush.verticalGradient(listOf(AmberGold.copy(alpha = 0.65f), PanelBorder.copy(alpha = 0.55f)))
            ),
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary Expedition Launch Controls: CONTINUE & NEW GAME
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onContinue,
                        enabled = hasProgress,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberGold,
                            contentColor = ObsidianForest,
                            disabledContainerColor = PanelElevated.copy(alpha = 0.65f),
                            disabledContentColor = LichenMuted
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1.45f)
                            .height(58.dp)
                            .scale(if (hasProgress) ctaScale else 1f)
                            .minimumInteractiveComponentSize()
                            .testTag("btn_home_continue")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = labels.continueBtn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (hasProgress) {
                                    "Lv.${state.playerLevel} • ${activeBiome.title}"
                                } else {
                                    "No Saved Run"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasProgress) ObsidianForest.copy(alpha = 0.80f) else LichenMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (hasProgress) {
                        OutlinedButton(
                            onClick = { showNewGameConfirm = true },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = PanelCard.copy(alpha = 0.9f),
                                contentColor = CreamText
                            ),
                            border = BorderStroke(1.2.dp, AmberGold.copy(alpha = 0.55f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("btn_home_new_game")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircleOutline,
                                contentDescription = null,
                                tint = AmberBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = labels.newGameBtn,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Button(
                            onClick = onNewGame,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldCanopy,
                                contentColor = ObsidianForest
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1.25f)
                                .height(58.dp)
                                .scale(ctaScale)
                                .minimumInteractiveComponentSize()
                                .testTag("btn_home_new_game")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = labels.newGameBtn,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Menu Row 1: WORLD MAP | PROFILE | ACHIEVEMENTS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StartMenuTile(
                        icon = Icons.Filled.Explore,
                        label = labels.worldMapBtn,
                        subLabel = "${state.unlockedBiomes.size}/${BiomeType.entries.size} Areas",
                        accent = EmeraldCanopy,
                        testTag = "btn_home_world_map",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenWorldMap
                    )
                    StartMenuTile(
                        icon = Icons.Filled.Person,
                        label = labels.profileBtn,
                        subLabel = "Lv.${state.playerLevel} Pioneer",
                        accent = CrystalCyan,
                        testTag = "btn_home_profile",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenProfile
                    )
                    StartMenuTile(
                        icon = Icons.Filled.EmojiEvents,
                        label = labels.achievementsBtn,
                        subLabel = if (readyAchievements > 0) "$readyAchievements Ready" else "Milestones",
                        accent = AmberBright,
                        badgeCount = readyAchievements,
                        testTag = "btn_home_achievements",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAchievements
                    )
                }

                // Menu Row 2: MARKET | SETTINGS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StartMenuWideButton(
                        icon = Icons.Filled.Storefront,
                        label = labels.marketBtn,
                        subLabel = "Products & Trade",
                        accent = AmberGold,
                        testTag = "btn_home_market",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenMarket
                    )
                    StartMenuWideButton(
                        icon = Icons.Filled.Settings,
                        label = labels.settingsBtn,
                        subLabel = "Graphics, Camera & Audio",
                        accent = CrystalCyan,
                        testTag = "btn_home_settings",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSettings
                    )
                }
            }
        }
    }

    // Explicit New Game Overwrite Confirmation Dialog
    if (showNewGameConfirm) {
        AlertDialog(
            onDismissRequest = { showNewGameConfirm = false },
            containerColor = PanelDark,
            titleContentColor = CreamText,
            textContentColor = LichenMuted,
            title = { Text(labels.confirmNewGameTitle) },
            text = { Text(labels.confirmNewGameBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNewGameConfirm = false
                        onNewGame()
                    },
                    modifier = Modifier.testTag("btn_confirm_new_game")
                ) {
                    Text(labels.confirmOverwriteBtn, color = CoralCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNewGameConfirm = false },
                    modifier = Modifier.testTag("btn_cancel_new_game")
                ) {
                    Text(labels.cancelBtn, color = CreamText)
                }
            },
            modifier = Modifier.testTag("dialog_confirm_new_game")
        )
    }

    // Multi-Slot Save Selection Modal
    if (showSaveSlotsDialog) {
        val slotsToRender = if (saveSlots.isNotEmpty()) {
            saveSlots
        } else {
            List(3) { idx ->
                SaveSlotSummary(
                    slotIndex = idx,
                    hasData = idx == activeSlotIndex && hasProgress,
                    playerLevel = if (idx == activeSlotIndex) state.playerLevel else 1,
                    coins = if (idx == activeSlotIndex) state.coins else 0L,
                    biomeId = if (idx == activeSlotIndex) state.lastBiomeId else "forest",
                    unlockedAreas = if (idx == activeSlotIndex) state.unlockedBiomes.size else 1,
                    playtimeSeconds = if (idx == activeSlotIndex) state.playtimeSeconds else 0L,
                    lastSavedEpochMs = if (idx == activeSlotIndex) state.lastSavedEpochMs else 0L
                )
            }
        }

        AlertDialog(
            onDismissRequest = { showSaveSlotsDialog = false },
            containerColor = PanelDark,
            titleContentColor = CreamText,
            textContentColor = LichenMuted,
            title = { Text(labels.saveSlotsBtn) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    slotsToRender.forEach { slot ->
                        val isCurrent = slot.slotIndex == activeSlotIndex
                        val slotBiome = BiomeType.fromId(slot.biomeId) ?: BiomeType.FOREST
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCurrent) EmeraldCanopy.copy(alpha = 0.14f) else PanelCard,
                            border = BorderStroke(
                                1.2.dp,
                                if (isCurrent) EmeraldCanopy else PanelBorder.copy(alpha = 0.65f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    showSaveSlotsDialog = false
                                    onSelectSaveSlot(slot.slotIndex)
                                }
                                .minimumInteractiveComponentSize()
                                .testTag("slot_card_${slot.slotIndex}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (slot.hasData) {
                                            "Slot ${slot.slotIndex + 1} • Lv.${slot.playerLevel} (${slotBiome.title})"
                                        } else {
                                            "Slot ${slot.slotIndex + 1} • Empty Expedition"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = CreamText,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (slot.hasData) {
                                            "${formatCompactCurrency(slot.coins)} • ${slot.unlockedAreas}/12 Areas • ${formatPlaytime(slot.playtimeSeconds)}"
                                        } else {
                                            "Tap to activate this save slot"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LichenMuted
                                    )
                                }
                                if (isCurrent) {
                                    FrontierBadgeChip(text = "Active", accent = EmeraldCanopy)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSaveSlotsDialog = false }) {
                    Text(labels.cancelBtn, color = CreamText)
                }
            },
            modifier = Modifier.testTag("dialog_save_slots")
        )
    }
}

@Composable
fun LoadingExpeditionOverlay(
    biome: BiomeType,
    playerLevel: Int,
    languageCode: String,
    modifier: Modifier = Modifier
) {
    val labels = remember(languageCode) { localizedLabelsFor(languageCode) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianForest.copy(alpha = 0.96f),
                        PanelDark.copy(alpha = 0.98f),
                        ObsidianForest.copy(alpha = 0.96f)
                    )
                )
            )
            .testTag("loading_expedition_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PanelCard.copy(alpha = 0.95f),
            border = BorderStroke(1.2.dp, AmberGold.copy(alpha = 0.55f)),
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WildcraftCrestEmblem(modifier = Modifier.size(48.dp))
                Text(
                    text = labels.loadingWorldTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = AmberBright,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${biome.title} • Lv.$playerLevel",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldCanopy,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = labels.loadingWorldSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LichenMuted
                )
                CircularProgressIndicator(
                    color = AmberGold,
                    trackColor = PanelElevated,
                    strokeWidth = 3.dp,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun WildcraftCrestEmblem(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val mountain = Path().apply {
            moveTo(w * 0.1f, h * 0.85f)
            lineTo(w * 0.45f, h * 0.2f)
            lineTo(w * 0.72f, h * 0.65f)
            lineTo(w * 0.82f, h * 0.48f)
            lineTo(w * 0.94f, h * 0.85f)
            close()
        }
        drawPath(mountain, color = Color(0xFF22C55E))
        val snowCap = Path().apply {
            moveTo(w * 0.45f, h * 0.2f)
            lineTo(w * 0.34f, h * 0.42f)
            lineTo(w * 0.56f, h * 0.42f)
            close()
        }
        drawPath(snowCap, color = Color(0xFFFEF0C7))
        drawCircle(
            color = Color(0xFFF6B73C),
            radius = w * 0.14f,
            center = Offset(w * 0.78f, h * 0.24f)
        )
    }
}

@Composable
private fun StartMenuTile(
    icon: ImageVector,
    label: String,
    subLabel: String,
    accent: Color,
    badgeCount: Int = 0,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "tileScale"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PanelCard.copy(alpha = 0.94f),
        border = BorderStroke(1.1.dp, accent.copy(alpha = 0.42f)),
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = accent.copy(alpha = 0.16f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = CoralCrimson, contentColor = CreamText) {
                                    Text("$badgeCount")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = accent,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = accent,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = CreamText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = LichenMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StartMenuWideButton(
    icon: ImageVector,
    label: String,
    subLabel: String,
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "wideBtnScale"
    )

    Surface(
        shape = RoundedCornerShape(15.dp),
        color = PanelElevated.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.36f)),
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(15.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accent.copy(alpha = 0.16f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = CreamText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = LichenMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
