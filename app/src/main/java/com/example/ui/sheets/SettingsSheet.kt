package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.TycoonSaveState
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSectionHeader
import com.example.ui.components.FrontierSheetScaffold
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
import kotlin.math.roundToInt

@Composable
fun SettingsSheet(
    state: TycoonSaveState,
    showReturnToHome: Boolean,
    onToggleMute: () -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onMusicVolumeChange: (Float) -> Unit,
    onGraphicsQualityChange: (Int) -> Unit,
    onToggleHaptics: () -> Unit,
    onReturnToHome: () -> Unit,
    onOpenAdvisor: () -> Unit,
    onOpenStudio: () -> Unit,
    onResetProgress: () -> Unit,
    onDismiss: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    FrontierSheetScaffold(
        title = "Settings & Audio",
        subtitle = "Sound, music, graphics quality & companion tools",
        headerIcon = Icons.Filled.Settings,
        accentColor = LichenMuted,
        closeTestTag = "btn_close_settings",
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Audio Mixer Card
            FrontierGlassCard(accentBorder = EmeraldCanopy) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrontierSectionHeader(
                        title = "Audio Mixer",
                        accent = EmeraldCanopy
                    )

                    SettingToggleRow(
                        icon = if (state.isMuted) Icons.AutoMirrored.Filled.VolumeOff
                        else Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Master Audio",
                        subtitle = if (state.isMuted) "All game audio muted" else "Audio active",
                        checked = !state.isMuted,
                        accent = EmeraldCanopy,
                        testTag = "btn_toggle_mute",
                        onToggle = onToggleMute
                    )

                    VolumeSliderRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        label = "Sound Effects",
                        value = state.sfxVolume,
                        enabled = !state.isMuted,
                        accent = AmberGold,
                        testTag = "slider_sfx_volume",
                        onValueChange = onSfxVolumeChange
                    )

                    VolumeSliderRow(
                        icon = Icons.Filled.MusicNote,
                        label = "Background Music",
                        value = state.musicVolume,
                        enabled = !state.isMuted,
                        accent = CrystalCyan,
                        testTag = "slider_music_volume",
                        onValueChange = onMusicVolumeChange
                    )
                }
            }

            // Graphics & Haptics Card
            FrontierGlassCard(accentBorder = AmberGold) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FrontierSectionHeader(
                        title = "Graphics & Haptics",
                        accent = AmberGold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = AmberBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3D Render Quality",
                            style = MaterialTheme.typography.titleSmall,
                            color = CreamText
                        )
                    }

                    val presets = listOf("Balanced", "High", "Ultra")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        presets.forEachIndexed { idx, label ->
                            SegmentedButton(
                                selected = state.graphicsQuality == idx,
                                onClick = { onGraphicsQualityChange(idx) },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = presets.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = AmberGold,
                                    activeContentColor = ObsidianForest,
                                    inactiveContainerColor = PanelCard,
                                    inactiveContentColor = LichenMuted,
                                    activeBorderColor = AmberGold,
                                    inactiveBorderColor = PanelBorder
                                ),
                                modifier = Modifier.testTag("btn_gfx_${label.lowercase()}")
                            ) {
                                Text(label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    SettingToggleRow(
                        icon = Icons.Filled.Vibration,
                        title = "Haptic Feedback",
                        subtitle = if (state.hapticsEnabled) "Tactile pulse on hits & sales" else "Vibration disabled",
                        checked = state.hapticsEnabled,
                        accent = AmberGold,
                        testTag = "switch_haptics",
                        onToggle = onToggleHaptics
                    )
                }
            }

            // Frontier Companion Tools
            FrontierGlassCard(accentBorder = CrystalCyan) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrontierSectionHeader(
                        title = "Frontier Companion Tools",
                        accent = CrystalCyan
                    )

                    ToolLaunchRow(
                        icon = Icons.Filled.AutoAwesome,
                        title = "Tycoon Strategy Advisor",
                        subtitle = "Ask for upgrade & biome expansion tips",
                        accent = CrystalCyan,
                        testTag = "btn_open_advisor",
                        onClick = {
                            onDismiss()
                            onOpenAdvisor()
                        }
                    )

                    ToolLaunchRow(
                        icon = Icons.Filled.Brush,
                        title = "Wildcraft Concept Studio",
                        subtitle = "Generate 1K, 2K & 4K frontier art",
                        accent = AmberGold,
                        testTag = "btn_open_studio",
                        onClick = {
                            onDismiss()
                            onOpenStudio()
                        }
                    )
                }
            }

            // Navigation, Autosave & Reset Card
            FrontierGlassCard {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrontierSectionHeader(
                        title = "Session & Save Data",
                        accent = EmeraldCanopy
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CloudDone,
                            contentDescription = null,
                            tint = EmeraldCanopy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "On-Device Autosave Active",
                                style = MaterialTheme.typography.titleSmall,
                                color = CreamText
                            )
                            Text(
                                text = "Money, inventory, upgrades & regions save automatically.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LichenMuted
                            )
                        }
                    }

                    if (showReturnToHome) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onReturnToHome()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberBright),
                            border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("btn_return_home")
                        ) {
                            Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Return to Main Menu", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralCrimson),
                        border = BorderStroke(1.dp, CoralCrimson.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("btn_reset_save")
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Frontier Progress", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = PanelDark,
            titleContentColor = CreamText,
            textContentColor = LichenMuted,
            title = { Text("Reset Frontier Progress?") },
            text = { Text("This clears your coins, tools, factories, quests, and unlocked regions so you can start a fresh tycoon run.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirm = false
                        onResetProgress()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("btn_confirm_reset")
                ) {
                    Text("Reset", color = CoralCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = CreamText)
                }
            }
        )
    }
}

@Composable
private fun VolumeSliderRow(
    icon: ImageVector,
    label: String,
    value: Float,
    enabled: Boolean,
    accent: Color,
    testTag: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) accent else LichenMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) CreamText else LichenMuted
                )
            }
            Text(
                text = "${(value * 100f).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) accent else LichenMuted,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = PanelElevated
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    accent: Color,
    testTag: String,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) accent else LichenMuted,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = CreamText
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LichenMuted
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianForest,
                checkedTrackColor = accent,
                uncheckedThumbColor = LichenMuted,
                uncheckedTrackColor = PanelElevated
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun ToolLaunchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PanelElevated.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = CreamText
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LichenMuted
                )
            }
            Text(
                text = "Open",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
