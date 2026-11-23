package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BiomeType
import com.example.data.ResourceType
import com.example.data.TycoonSaveState
import com.example.data.UniqueQuestItem
import com.example.ui.components.FrontierBadgeChip
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSectionHeader
import com.example.ui.components.FrontierSheetScaffold
import com.example.ui.components.formatCompactCurrency
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CreamText
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PanelElevated
import com.example.ui.theme.localizedLabelsFor
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSheet(
    state: TycoonSaveState,
    activeSlotIndex: Int = 0,
    onOpenAchievements: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val labels = remember(state.languageCode) { localizedLabelsFor(state.languageCode) }
    val quests = remember(state) { state.evaluateQuests() }
    val completedAchievements = remember(quests) { quests.count { it.isClaimed } }
    val readyAchievements = remember(quests) { quests.count { it.isReadyToClaim } }
    val numberFmt = remember { NumberFormat.getIntegerInstance(Locale.US) }
    val xpFraction = (state.xpIntoCurrentLevel.toFloat() / state.xpNeededForNextLevel.coerceAtLeast(1L))
        .coerceIn(0f, 1f)
    val lastBiome = BiomeType.fromId(state.lastBiomeId) ?: BiomeType.FOREST

    FrontierSheetScaffold(
        title = labels.profileTitle,
        subtitle = "Save Slot ${activeSlotIndex + 1} • ${lastBiome.title}",
        headerIcon = Icons.Filled.Person,
        accentColor = CrystalCyan,
        closeTestTag = "btn_close_profile",
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
            // Player Level & XP Banner Card
            FrontierGlassCard(
                accentBorder = CrystalCyan,
                modifier = Modifier.testTag("profile_level_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = CrystalCyan.copy(alpha = 0.16f),
                                border = BorderStroke(1.2.dp, CrystalCyan.copy(alpha = 0.65f)),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${state.playerLevel}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = CrystalCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Pioneer Level ${state.playerLevel}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CreamText,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${numberFmt.format(state.xp)} Total XP earned",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LichenMuted
                                )
                            }
                        }

                        FrontierBadgeChip(
                            text = "${state.xpIntoCurrentLevel}/${state.xpNeededForNextLevel} XP",
                            accent = CrystalCyan
                        )
                    }

                    LinearProgressIndicator(
                        progress = { xpFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(CircleShape),
                        color = CrystalCyan,
                        trackColor = PanelElevated,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            // Core Career Statistics Grid
            FrontierGlassCard(
                accentBorder = AmberGold,
                modifier = Modifier.testTag("profile_stats_grid")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrontierSectionHeader(
                        title = "Expedition Statistics",
                        accent = AmberGold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatTile(
                            icon = Icons.Filled.MonetizationOn,
                            label = "Total Money",
                            value = formatCompactCurrency(state.coins),
                            subValue = "Lifetime: ${formatCompactCurrency(state.lifetimeEarned)}",
                            accent = AmberBright,
                            testTag = "profile_stat_money",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatTile(
                            icon = Icons.Filled.Schedule,
                            label = "Playtime",
                            value = formatPlaytime(state.playtimeSeconds),
                            subValue = "Active session tracked",
                            accent = CrystalCyan,
                            testTag = "profile_stat_playtime",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatTile(
                            icon = Icons.Filled.Explore,
                            label = "Unlocked Areas",
                            value = "${state.unlockedBiomes.size} / ${BiomeType.entries.size}",
                            subValue = "Current: ${lastBiome.title}",
                            accent = EmeraldCanopy,
                            testTag = "profile_stat_areas",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatTile(
                            icon = Icons.Filled.Apartment,
                            label = "Total Buildings",
                            value = "${state.totalBuildingsBuilt}",
                            subValue = "${state.buildingLevels.count { it.value > 0 }} factories • ${state.machineLevels.count { it.value > 0 }} rigs",
                            accent = AmberGold,
                            testTag = "profile_stat_buildings",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatTile(
                            icon = Icons.Filled.Inventory2,
                            label = "Resources Collected",
                            value = numberFmt.format(state.totalResourcesCollected),
                            subValue = "${state.gatheredTotals.count { it.value > 0 }} material types",
                            accent = EmeraldCanopy,
                            testTag = "profile_stat_resources",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatTile(
                            icon = Icons.Filled.EmojiEvents,
                            label = "Achievements",
                            value = "$completedAchievements / ${quests.size}",
                            subValue = if (readyAchievements > 0) "$readyAchievements ready to claim" else "${state.uniqueItems.size}/${UniqueQuestItem.entries.size} relics",
                            accent = AmberBright,
                            testTag = "profile_stat_achievements",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Unlocked Regions Overview
            FrontierGlassCard(accentBorder = EmeraldCanopy) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrontierSectionHeader(
                        title = "Unlocked Regions",
                        trailingBadge = "${state.unlockedBiomes.size}/${BiomeType.entries.size}",
                        accent = EmeraldCanopy
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BiomeType.entries.forEach { biome ->
                            val unlocked = state.isBiomeUnlocked(biome)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (unlocked) EmeraldCanopy.copy(alpha = 0.14f) else PanelCard,
                                border = BorderStroke(
                                    1.dp,
                                    if (unlocked) EmeraldCanopy.copy(alpha = 0.5f) else PanelBorder.copy(alpha = 0.45f)
                                )
                            ) {
                                Text(
                                    text = biome.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (unlocked) CreamText else LichenMuted.copy(alpha = 0.65f),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Top Gathered Materials + Achievements Shortcut
            FrontierGlassCard {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrontierSectionHeader(
                        title = "Harvest Ledger & Relics",
                        accent = AmberBright
                    )
                    val topResources = remember(state.gatheredTotals) {
                        ResourceType.entries
                            .map { it to (state.gatheredTotals[it.id] ?: 0) }
                            .filter { it.second > 0 }
                            .sortedByDescending { it.second }
                            .take(6)
                    }
                    if (topResources.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Forest,
                                contentDescription = null,
                                tint = LichenMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gather wood, stone, and ores in the world to build your harvest history.",
                                style = MaterialTheme.typography.bodySmall,
                                color = LichenMuted
                            )
                        }
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            topResources.forEach { (res, count) ->
                                FrontierBadgeChip(
                                    text = "${res.displayName}: ${numberFmt.format(count)}",
                                    accent = Color(res.colorHex)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenAchievements()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberBright),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("btn_profile_achievements")
                    ) {
                        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Inspect Achievements & Relics", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatTile(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PanelElevated.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = LichenMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = CreamText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                color = LichenMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun formatPlaytime(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0L)
    val h = safe / 3600L
    val m = (safe % 3600L) / 60L
    val s = safe % 60L
    return when {
        h > 0L -> String.format(Locale.US, "%dh %02dm", h, m)
        m > 0L -> String.format(Locale.US, "%dm %02ds", m, s)
        else -> String.format(Locale.US, "%ds", s)
    }
}
