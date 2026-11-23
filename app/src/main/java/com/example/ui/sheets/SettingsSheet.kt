package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ZoomOutMap
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
import com.example.ui.theme.SupportedLanguages
import com.example.ui.theme.localizedLabelsFor
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    state: TycoonSaveState,
    showReturnToHome: Boolean,
    onToggleMute: () -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onAmbientVolumeChange: (Float) -> Unit,
    onWeatherVolumeChange: (Float) -> Unit,
    onWaterVolumeChange: (Float) -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onMusicVolumeChange: (Float) -> Unit,
    onGraphicsQualityChange: (Int) -> Unit,
    onShadowQualityChange: (Int) -> Unit = {},
    onCameraSensitivityChange: (Float) -> Unit = {},
    onCameraDistanceChange: (Float) -> Unit = {},
    onFpsLimitChange: (Int) -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    onResetSettings: () -> Unit = {},
    onToggleHaptics: () -> Unit,
    onReturnToHome: () -> Unit,
    onOpenAdvisor: () -> Unit,
    onOpenStudio: () -> Unit,
    onResetProgress: () -> Unit,
    onDismiss: () -> Unit
) {
    var showResetSaveConfirm by remember { mutableStateOf(false) }
    var showResetSettingsConfirm by remember { mutableStateOf(false) }
    val labels = remember(state.languageCode) { localizedLabelsFor(state.languageCode) }

    FrontierSheetScaffold(
        title = labels.settingsTitle,
        subtitle = "Graphics, shadows, camera, audio & language",
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
            // Graphics, Shadows & Frame Rate Card
            FrontierGlassCard(accentBorder = AmberGold) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FrontierSectionHeader(
                        title = "Graphics & Performance",
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
                            text = "Graphics Quality",
                            style = MaterialTheme.typography.titleSmall,
                            color = CreamText
                        )
                    }

                    val gfxPresets = listOf("Low", "Medium", "High")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        gfxPresets.forEachIndexed { idx, label ->
                            SegmentedButton(
                                selected = state.graphicsQuality == idx,
                                onClick = { onGraphicsQualityChange(idx) },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = gfxPresets.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = AmberGold,
                                    activeContentColor = ObsidianForest,
                                    inactiveContainerColor = PanelCard,
                                    inactiveContentColor = LichenMuted,
                                    activeBorderColor = AmberGold,
                                    inactiveBorderColor = PanelBorder
                                ),
                                modifier = Modifier.testTag("btn_gfx_$idx")
                            ) {
                                Text(label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.WbSunny,
                            contentDescription = null,
                            tint = AmberBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shadow Quality",
                            style = MaterialTheme.typography.titleSmall,
                            color = CreamText
                        )
                    }

                    val shadowPresets = listOf("Off", "Medium", "High")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        shadowPresets.forEachIndexed { idx, label ->
                            SegmentedButton(
                                selected = state.shadowQuality == idx,
                                onClick = { onShadowQualityChange(idx) },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = shadowPresets.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = AmberGold,
                                    activeContentColor = ObsidianForest,
                                    inactiveContainerColor = PanelCard,
                                    inactiveContentColor = LichenMuted,
                                    activeBorderColor = AmberGold,
                                    inactiveBorderColor = PanelBorder
                                ),
                                modifier = Modifier.testTag("btn_shadow_$idx")
                            ) {
                                Text(label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = null,
                            tint = EmeraldCanopy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FPS Limit",
                            style = MaterialTheme.typography.titleSmall,
                            color = CreamText
                        )
                    }

                    val fpsOptions = listOf(30, 60)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        fpsOptions.forEachIndexed { idx, fps ->
                            val selected = (if (state.fpsLimit <= 35) 30 else 60) == fps
                            SegmentedButton(
                                selected = selected,
                                onClick = { onFpsLimitChange(fps) },
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = fpsOptions.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = EmeraldCanopy,
                                    activeContentColor = ObsidianForest,
                                    inactiveContainerColor = PanelCard,
                                    inactiveContentColor = LichenMuted,
                                    activeBorderColor = EmeraldCanopy,
                                    inactiveBorderColor = PanelBorder
                                ),
                                modifier = Modifier.testTag("btn_fps_$fps")
                            ) {
                                Text("$fps FPS", style = MaterialTheme.typography.labelMedium)
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

            // Camera Controls Card
            FrontierGlassCard(accentBorder = CrystalCyan) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrontierSectionHeader(
                        title = "Camera Controls",
                        accent = CrystalCyan
                    )

                    FloatSliderRow(
                        icon = Icons.Filled.CameraAlt,
                        label = "Camera Sensitivity",
                        value = state.cameraSensitivity.coerceIn(0.5f, 2.0f),
                        valueRange = 0.5f..2.0f,
                        valueText = String.format(Locale.US, "%.1fx", state.cameraSensitivity),
                        enabled = true,
                        accent = CrystalCyan,
                        testTag = "slider_cam_sensitivity",
                        onValueChange = onCameraSensitivityChange
                    )

                    FloatSliderRow(
                        icon = Icons.Filled.ZoomOutMap,
                        label = "Camera Distance",
                        value = state.cameraDistance.coerceIn(8.0f, 18.0f),
                        valueRange = 8.0f..18.0f,
                        valueText = String.format(Locale.US, "%.1fm", state.cameraDistance),
                        enabled = true,
                        accent = CrystalCyan,
                        testTag = "slider_cam_distance",
                        onValueChange = onCameraDistanceChange
                    )
                }
            }

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
                        title = "Master Mute",
                        subtitle = if (state.isMuted) "All game audio muted" else "Audio active",
                        checked = state.isMuted,
                        accent = if (state.isMuted) CoralCrimson else EmeraldCanopy,
                        testTag = "btn_toggle_mute",
                        onToggle = onToggleMute
                    )

                    VolumeSliderRow(
                        icon = Icons.Filled.Tune,
                        label = "Overall Audio",
                        value = state.masterVolume,
                        enabled = !state.isMuted,
                        accent = EmeraldCanopy,
                        testTag = "slider_master_volume",
                        onValueChange = onMasterVolumeChange
                    )

                    VolumeSliderRow(
                        icon = Icons.Filled.Forest,
                        label = "Ambient Sounds",
                        value = state.ambientVolume,
                        enabled = !state.isMuted,
                        accent = EmeraldCanopy,
                        testTag = "slider_ambient_volume",
                        onValueChange = onAmbientVolumeChange
                    )

                    VolumeSliderRow(
                        icon = Icons.Filled.WaterDrop,
                        label = "Weather Sounds",
                        value = state.weatherVolume,
                        enabled = !state.isMuted,
                        accent = CrystalCyan,
                        testTag = "slider_weather_volume",
                        onValueChange = onWeatherVolumeChange
                    )

                    VolumeSliderRow(
                        icon = Icons.Filled.Waves,
                        label = "Water Sounds",
                        value = state.waterVolume,
                        enabled = !state.isMuted,
                        accent = CrystalCyan,
                        testTag = "slider_water_volume",
                        onValueChange = onWaterVolumeChange
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
                        label = "Music Volume",
                        value = state.musicVolume,
                        enabled = !state.isMuted,
                        accent = AmberBright,
                        testTag = "slider_music_volume",
                        onValueChange = onMusicVolumeChange
                    )
                }
            }

            // Language Selection Card
            FrontierGlassCard(accentBorder = AmberBright) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrontierSectionHeader(
                        title = "Language Selection",
                        accent = AmberBright
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                            tint = AmberBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select interface language",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LichenMuted
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SupportedLanguages.forEach { lang ->
                            val selected = state.languageCode == lang.code
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) AmberGold.copy(alpha = 0.2f) else PanelCard,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (selected) AmberGold else PanelBorder.copy(alpha = 0.65f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onLanguageChange(lang.code) }
                                    .minimumInteractiveComponentSize()
                                    .testTag("btn_lang_${lang.code}")
                            ) {
                                Text(
                                    text = lang.nativeLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) AmberBright else CreamText,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
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

            // Navigation, Reset Settings & Save Data Card
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
                                text = "Money, inventory, upgrades, regions & settings save locally.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LichenMuted
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showResetSettingsConfirm = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberBright),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("btn_reset_settings")
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(labels.resetSettingsBtn, style = MaterialTheme.typography.labelLarge)
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
                        onClick = { showResetSaveConfirm = true },
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

    if (showResetSettingsConfirm) {
        AlertDialog(
            onDismissRequest = { showResetSettingsConfirm = false },
            containerColor = PanelDark,
            titleContentColor = CreamText,
            textContentColor = LichenMuted,
            title = { Text("Reset Settings?") },
            text = { Text("This restores graphics, shadow quality, camera distance, sensitivity, FPS limit, language, and audio volumes to their defaults without erasing your saved world.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetSettingsConfirm = false
                        onResetSettings()
                    },
                    modifier = Modifier.testTag("btn_confirm_reset_settings")
                ) {
                    Text("Reset Settings", color = AmberBright, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsConfirm = false }) {
                    Text(labels.cancelBtn, color = CreamText)
                }
            }
        )
    }

    if (showResetSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showResetSaveConfirm = false },
            containerColor = PanelDark,
            titleContentColor = CreamText,
            textContentColor = LichenMuted,
            title = { Text("Reset Frontier Progress?") },
            text = { Text("This clears your coins, tools, factories, quests, and unlocked regions in the active save slot so you can start a fresh tycoon run.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetSaveConfirm = false
                        onResetProgress()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("btn_confirm_reset")
                ) {
                    Text("Reset", color = CoralCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSaveConfirm = false }) {
                    Text(labels.cancelBtn, color = CreamText)
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
    FloatSliderRow(
        icon = icon,
        label = label,
        value = value.coerceIn(0f, 1f),
        valueRange = 0f..1f,
        valueText = "${(value * 100f).roundToInt()}%",
        enabled = enabled,
        accent = accent,
        testTag = testTag,
        onValueChange = onValueChange
    )
}

@Composable
private fun FloatSliderRow(
    icon: ImageVector,
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
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
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) accent else LichenMuted,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
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
