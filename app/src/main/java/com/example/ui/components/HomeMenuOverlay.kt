package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.MachineType
import com.example.data.TycoonSaveState
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

@Composable
fun HomeMenuOverlay(
    state: TycoonSaveState,
    activeBiome: BiomeType,
    onPlay: () -> Unit,
    onOpenUpgrades: () -> Unit,
    onOpenMachinery: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenWorldMap: () -> Unit,
    onOpenBusiness: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSettlement: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasProgress = state.lifetimeEarned > 0L ||
        state.xp > 0L ||
        state.axeLevel > 1 ||
        state.pickaxeLevel > 1 ||
        state.unlockedBiomes.size > 1
    val readyQuests = state.readyQuestCount
    val activeMachines = MachineType.entries.count { state.isMachineRunning(it) }
    val deliverableContracts = state.activeContracts.count { state.availableForTrade(it.goodId) >= it.quantity }

    val pulseTransition = rememberInfiniteTransition(label = "menuPulse")
    val ctaScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
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
                        ObsidianForest.copy(alpha = 0.56f),
                        Color.Transparent,
                        Color.Transparent,
                        ObsidianForest.copy(alpha = 0.80f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // =====================================================================
        // TOP STATUS BAR + SETTINGS + WILDCRAFT TYCOON HERO LOGO
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Money + Level Pill
                val xpFrac = (state.xpIntoCurrentLevel.toFloat() / state.xpNeededForNextLevel.coerceAtLeast(1L))
                    .coerceIn(0f, 1f)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PanelDark.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = AmberBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            AnimatedCurrencyText(
                                coins = state.coins,
                                style = MaterialTheme.typography.titleSmall,
                                color = CreamText
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Lv.${state.playerLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrystalCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                LinearProgressIndicator(
                                    progress = { xpFrac },
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = CrystalCyan,
                                    trackColor = PanelElevated,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                // Region Summary + Top-Right Settings Gear
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PanelDark.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, EmeraldCanopy.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onOpenWorldMap)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Explore,
                                contentDescription = null,
                                tint = EmeraldCanopy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${state.unlockedBiomes.size}/${BiomeType.entries.size} Areas",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CreamText,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (state.passiveIncomePerSecond > 0) "+$${state.passiveIncomePerSecond.toInt()}/s"
                                    else activeBiome.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldCanopy
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        color = PanelDark.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.8f))
                    ) {
                        IconButton(
                            onClick = onOpenSettings,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = CrystalCyan),
                            modifier = Modifier
                                .size(44.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("btn_home_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.cd_open_settings),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prominent Stylized WILDCRAFT TYCOON Emblem
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = PanelDark.copy(alpha = 0.78f),
                border = BorderStroke(
                    1.4.dp,
                    Brush.horizontalGradient(listOf(AmberGold, EmeraldCanopy, AmberBright))
                ),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(40.dp)) {
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

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "WILDCRAFT",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 25.sp,
                                letterSpacing = 1.4.sp
                            ),
                            color = AmberBright,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldCanopy.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, EmeraldCanopy.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = "TYCOON",
                                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.2.sp),
                                    color = EmeraldCanopy,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "3D FRONTIER",
                                style = MaterialTheme.typography.labelSmall,
                                color = LichenMuted
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // BOTTOM ORGANIZED MENU DOCK (Hero CTA + 3 Primary Hubs + 4 Utilities)
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = PanelDark.copy(alpha = 0.90f),
            border = BorderStroke(
                1.2.dp,
                Brush.verticalGradient(listOf(AmberGold.copy(alpha = 0.62f), PanelBorder.copy(alpha = 0.55f)))
            ),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary PLAY / CONTINUE Button
                Button(
                    onClick = onPlay,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = ObsidianForest
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(ctaScale)
                        .minimumInteractiveComponentSize()
                        .testTag("btn_home_play")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = if (hasProgress) "CONTINUE EXPEDITION" else "PLAY WILDCRAFT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasProgress) {
                                "${activeBiome.title} • Bag ${state.backpackUsed}/${state.backpackMaxCapacity}"
                            } else {
                                "Explore, gather & build your industrial empire"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = ObsidianForest.copy(alpha = 0.78f)
                        )
                    }
                }

                // Tier 1: Core Progression Loops (3 Spacious Feature Cards)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomePrimaryCard(
                        icon = Icons.Filled.Build,
                        label = "Upgrades",
                        subLabel = "Axe Lv.${state.axeLevel}",
                        accent = AmberGold,
                        testTag = "btn_home_upgrades",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenUpgrades
                    )
                    HomePrimaryCard(
                        icon = Icons.Filled.PrecisionManufacturing,
                        label = "Machinery",
                        subLabel = "$activeMachines Running",
                        accent = CrystalCyan,
                        testTag = "btn_home_machinery",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenMachinery
                    )
                    HomePrimaryCard(
                        icon = Icons.Filled.Storefront,
                        label = "Market",
                        subLabel = if (deliverableContracts > 0) "$deliverableContracts Ready" else "${state.activeContracts.size} Orders",
                        accent = AmberBright,
                        badgeCount = deliverableContracts,
                        testTag = "btn_home_market",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenMarket
                    )
                }

                // Tier 2: World & Management Strip (4 Clean Utility Chips)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HomeUtilityChip(
                        icon = Icons.Filled.Explore,
                        label = "Map",
                        subLabel = "${state.unlockedBiomes.size}/${BiomeType.entries.size}",
                        accent = EmeraldCanopy,
                        testTag = "btn_home_world_map",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenWorldMap
                    )
                    HomeUtilityChip(
                        icon = Icons.Filled.Factory,
                        label = "Factories",
                        subLabel = "+$${state.passiveIncomePerSecond.toInt()}/s",
                        accent = EmeraldCanopy,
                        testTag = "btn_home_business",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenBusiness
                    )
                    HomeUtilityChip(
                        icon = Icons.Filled.Backpack,
                        label = "Bag",
                        subLabel = "${state.backpackUsed}/${state.backpackMaxCapacity}",
                        accent = AmberGold,
                        testTag = "btn_home_inventory",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenInventory
                    )
                    HomeUtilityChip(
                        icon = Icons.Filled.EmojiEvents,
                        label = "Quests",
                        subLabel = if (readyQuests > 0) "$readyQuests Ready" else "Lv.${state.playerLevel}",
                        accent = AmberBright,
                        badgeCount = readyQuests,
                        testTag = "btn_home_quests",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenQuests
                    )
                }

                // Compact Settlement Status Strip
                val stats = state.settlement.computeStats()
                val arrivals = state.settlement.pendingArrivals.size
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelCard.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, EmeraldCanopy.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenSettlement)
                        .minimumInteractiveComponentSize()
                        .testTag("btn_home_settlement")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Groups,
                                contentDescription = stringResource(R.string.cd_open_settlement),
                                tint = EmeraldCanopy,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${stats.settlementTierName} • ${stats.population}/${stats.totalHousingCapacity} Beds • ${stats.averageHappiness}% Mood",
                                style = MaterialTheme.typography.labelMedium,
                                color = CreamText,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (arrivals > 0) {
                                Badge(containerColor = CoralCrimson, contentColor = CreamText) {
                                    Text("$arrivals Gate")
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = LichenMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomePrimaryCard(
    icon: ImageVector,
    label: String,
    subLabel: String,
    accent: Color,
    badgeCount: Int = 0,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PanelCard.copy(alpha = 0.94f),
        border = BorderStroke(1.1.dp, accent.copy(alpha = 0.42f)),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
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
private fun HomeUtilityChip(
    icon: ImageVector,
    label: String,
    subLabel: String,
    accent: Color,
    badgeCount: Int = 0,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PanelElevated.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.7f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = CreamText,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = LichenMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
