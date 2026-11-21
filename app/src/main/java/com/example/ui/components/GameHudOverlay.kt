package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.TycoonSaveState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GameHudOverlay(
    state: TycoonSaveState,
    offlineEarnings: Long?,
    onDismissOfflineBanner: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenBiomes: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenAdvisor: () -> Unit,
    onOpenStudio: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val questList = remember(state) { state.evaluateQuests() }
    val readyCount = remember(questList) { questList.count { it.isReadyToClaim } }
    val trackedQuest = remember(questList) {
        questList.firstOrNull { it.isReadyToClaim } ?: questList.firstOrNull { !it.isClaimed }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Money & Backpack pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                    tonalElevation = 4.dp,
                    modifier = Modifier.testTag("hud_money_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = formatCompactCurrency(state.coins),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (state.passiveIncomePerSecond > 0) {
                                    "Lv.${state.playerLevel} • +$${state.passiveIncomePerSecond.toInt()}/s"
                                } else {
                                    "Pioneer Lv.${state.playerLevel}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                val fillFraction = (state.backpackUsed.toFloat() / state.backpackMaxCapacity.coerceAtLeast(1))
                    .coerceIn(0f, 1f)
                val animatedFill by animateFloatAsState(targetValue = fillFraction, label = "bagFill")
                val bagColor by animateColorAsState(
                    targetValue = if (state.isBackpackFull) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    label = "bagColor"
                )

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                    border = BorderStroke(
                        1.dp,
                        if (state.isBackpackFull) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    ),
                    tonalElevation = 4.dp,
                    modifier = Modifier.testTag("hud_backpack_pill")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Backpack,
                                contentDescription = null,
                                tint = bagColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (state.isBackpackFull) {
                                    "FULL ${state.backpackUsed}/${state.backpackMaxCapacity}"
                                } else {
                                    "${state.backpackUsed}/${state.backpackMaxCapacity}"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = if (state.isBackpackFull) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { animatedFill },
                            modifier = Modifier
                                .width(78.dp)
                                .height(4.dp)
                                .clip(CircleShape),
                            color = bagColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            // Second Compact Row: Top-bar Icon Buttons (Shop, Biomes, Quests, Advisor, Studio, Mute)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    HudIconButton(
                        icon = Icons.Filled.ShoppingBag,
                        contentDescription = stringResource(R.string.cd_open_shop),
                        tint = MaterialTheme.colorScheme.primary,
                        testTag = "btn_open_shop",
                        onClick = onOpenShop
                    )
                    HudIconButton(
                        icon = Icons.Filled.Explore,
                        contentDescription = stringResource(R.string.cd_open_biomes),
                        tint = MaterialTheme.colorScheme.secondary,
                        testTag = "btn_open_biomes",
                        onClick = onOpenBiomes
                    )
                    HudIconButton(
                        icon = Icons.Filled.EmojiEvents,
                        contentDescription = stringResource(R.string.cd_open_quests),
                        tint = MaterialTheme.colorScheme.primary,
                        badgeCount = readyCount,
                        testTag = "btn_open_quests",
                        onClick = onOpenQuests
                    )
                    HudIconButton(
                        icon = Icons.Filled.AutoAwesome,
                        contentDescription = stringResource(R.string.cd_open_advisor),
                        tint = MaterialTheme.colorScheme.tertiary,
                        testTag = "btn_open_advisor",
                        onClick = onOpenAdvisor
                    )
                    HudIconButton(
                        icon = Icons.Filled.Brush,
                        contentDescription = stringResource(R.string.cd_open_studio),
                        tint = MaterialTheme.colorScheme.tertiary,
                        testTag = "btn_open_studio",
                        onClick = onOpenStudio
                    )
                    HudIconButton(
                        icon = if (state.isMuted) Icons.AutoMirrored.Filled.VolumeOff
                        else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = stringResource(R.string.cd_toggle_audio),
                        tint = if (state.isMuted) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        testTag = "btn_toggle_mute",
                        onClick = onToggleMute
                    )
                }
            }

            // Compact Active Quest Tracker Pill (top-right, non-intrusive)
            if (trackedQuest != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (trackedQuest.isReadyToClaim) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (trackedQuest.isReadyToClaim) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onOpenQuests)
                            .testTag("hud_quest_tracker")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (trackedQuest.isReadyToClaim) {
                                    "Claim: ${trackedQuest.quest.title}"
                                } else {
                                    "${trackedQuest.quest.title} (${trackedQuest.current}/${trackedQuest.target})"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (trackedQuest.isReadyToClaim) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Offline Passive Catch-up Banner
            AnimatedVisibility(
                visible = (offlineEarnings ?: 0L) > 0L,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome Back!",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Auto-miners earned ${formatCompactCurrency(offlineEarnings ?: 0L)} while you were away.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        TextButton(
                            onClick = onDismissOfflineBanner,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = "Nice!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HudIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    badgeCount: Int = 0,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        tonalElevation = 4.dp
    ) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(contentColor = tint),
            modifier = Modifier
                .size(48.dp)
                .minimumInteractiveComponentSize()
                .testTag(testTag)
        ) {
            if (badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text("$badgeCount")
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

fun formatCompactCurrency(amount: Long): String {
    return when {
        amount >= 1_000_000L -> String.format(Locale.US, "$%.2fM", amount / 1_000_000.0)
        amount >= 100_000L -> String.format(Locale.US, "$%.1fK", amount / 1_000.0)
        else -> "$" + NumberFormat.getNumberInstance(Locale.US).format(amount)
    }
}
