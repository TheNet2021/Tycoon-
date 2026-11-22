package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.data.DrivableVehicleType
import com.example.data.ResourceType
import com.example.data.TycoonSaveState
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CreamText
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PanelDark
import com.example.ui.theme.PanelElevated

@Composable
fun GameHudOverlay(
    state: TycoonSaveState,
    activeBiome: BiomeType,
    offlineEarnings: Long?,
    onDismissOfflineBanner: () -> Unit,
    onOpenUpgrades: () -> Unit,
    onOpenMachinery: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenBusiness: () -> Unit,
    onOpenBackpack: () -> Unit,
    onOpenBiomes: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSettlement: () -> Unit = {},
    onToggleVehicle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val questList = remember(state) { state.evaluateQuests() }
    val readyCount = remember(questList) { questList.count { it.isReadyToClaim } }
    val deliverableContracts = remember(state.activeContracts, state.warehouseStock, state.inventory) {
        state.activeContracts.count { state.availableForTrade(it.goodId) >= it.quantity }
    }
    val trackedQuest = remember(questList) {
        questList.firstOrNull { it.isReadyToClaim } ?: questList.firstOrNull { !it.isClaimed }
    }
    val heldResources = remember(state.inventory) {
        state.inventory.entries
            .filter { it.value > 0 }
            .mapNotNull { (id, qty) -> ResourceType.fromId(id)?.let { it to qty } }
            .sortedByDescending { it.second }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // =====================================================================
        // TOP BAR: Clean Player Telemetry + Backpack + Region & Quest Strip
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val xpFraction = (state.xpIntoCurrentLevel.toFloat() / state.xpNeededForNextLevel.coerceAtLeast(1L))
                    .coerceIn(0f, 1f)
                val animatedXp by animateFloatAsState(
                    targetValue = xpFraction,
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                    label = "hudXp"
                )
                val hpFrac = (state.playerHp.toFloat() / state.playerMaxHp.coerceAtLeast(1))
                    .coerceIn(0f, 1f)
                val hpColor = when {
                    hpFrac <= 0.3f -> CoralCrimson
                    hpFrac <= 0.6f -> AmberGold
                    else -> EmeraldCanopy
                }

                // Player Money, Level, XP & HP Capsule
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PanelDark.copy(alpha = 0.88f),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(AmberGold.copy(alpha = 0.62f), PanelBorder.copy(alpha = 0.55f))
                        )
                    ),
                    tonalElevation = 4.dp,
                    modifier = Modifier.testTag("hud_money_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmberGold.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.72f)),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${state.playerLevel}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AmberBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.MonetizationOn,
                                    contentDescription = null,
                                    tint = AmberBright,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AnimatedCurrencyText(
                                    coins = state.coins,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CreamText
                                )
                                if (state.passiveIncomePerSecond > 0) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "+$${state.passiveIncomePerSecond.toInt()}/s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldCanopy,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // XP bar
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "XP",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = CrystalCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    LinearProgressIndicator(
                                        progress = { animatedXp },
                                        modifier = Modifier
                                            .width(42.dp)
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = CrystalCyan,
                                        trackColor = PanelElevated,
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                                // HP bar
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = null,
                                        tint = hpColor,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    LinearProgressIndicator(
                                        progress = { hpFrac },
                                        modifier = Modifier
                                            .width(42.dp)
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = hpColor,
                                        trackColor = PanelElevated,
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Top Cluster: Backpack Pill + Settings Button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fillFraction = (state.backpackUsed.toFloat() / state.backpackMaxCapacity.coerceAtLeast(1))
                        .coerceIn(0f, 1f)
                    val animatedFill by animateFloatAsState(
                        targetValue = fillFraction,
                        animationSpec = tween(250),
                        label = "bagFill"
                    )
                    val bagColor by animateColorAsState(
                        targetValue = when {
                            state.isBackpackFull -> CoralCrimson
                            fillFraction >= 0.8f -> AmberGold
                            else -> EmeraldCanopy
                        },
                        label = "bagColor"
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PanelDark.copy(alpha = 0.88f),
                        border = BorderStroke(
                            1.dp,
                            if (state.isBackpackFull) CoralCrimson.copy(alpha = 0.9f)
                            else PanelBorder.copy(alpha = 0.8f)
                        ),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onOpenBackpack)
                            .testTag("hud_backpack_pill")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Backpack,
                                    contentDescription = stringResource(R.string.cd_open_backpack),
                                    tint = bagColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.isBackpackFull) {
                                        "FULL ${state.backpackUsed}/${state.backpackMaxCapacity}"
                                    } else {
                                        "${state.backpackUsed}/${state.backpackMaxCapacity}"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (state.isBackpackFull) CoralCrimson else CreamText,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { animatedFill },
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = bagColor,
                                trackColor = PanelElevated,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        color = PanelDark.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.8f)),
                        tonalElevation = 4.dp
                    ) {
                        IconButton(
                            onClick = onOpenSettings,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = CreamText),
                            modifier = Modifier
                                .size(44.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("btn_open_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.cd_open_settings),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Second Bar: Active Region Map Pill (Left) + Quests Pill (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val biomeAccent = Color(activeBiome.accentHex)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelDark.copy(alpha = 0.84f),
                    border = BorderStroke(1.dp, biomeAccent.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenBiomes)
                        .testTag("btn_open_biomes")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Explore,
                            contentDescription = stringResource(R.string.cd_open_biomes),
                            tint = biomeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeBiome.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = CreamText,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.unlockedBiomes.size}/${BiomeType.entries.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = biomeAccent
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = LichenMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Quests Access Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (readyCount > 0) PanelCard.copy(alpha = 0.94f) else PanelDark.copy(alpha = 0.84f),
                    border = BorderStroke(
                        1.dp,
                        if (readyCount > 0) AmberGold.copy(alpha = 0.85f) else PanelBorder.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenQuests)
                        .testTag("btn_open_quests")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = stringResource(R.string.cd_open_quests),
                            tint = AmberBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (readyCount > 0) "$readyCount Claim!" else "Quests",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (readyCount > 0) AmberBright else CreamText,
                            fontWeight = FontWeight.Bold
                        )
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
                    color = PanelDark.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, EmeraldCanopy.copy(alpha = 0.7f)),
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
                                color = EmeraldCanopy
                            )
                            Text(
                                text = "Auto-miners earned ${formatCompactCurrency(offlineEarnings ?: 0L)} while away.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CreamText
                            )
                        }
                        TextButton(
                            onClick = onDismissOfflineBanner,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = "Collect",
                                fontWeight = FontWeight.Bold,
                                color = AmberBright
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // RIGHT-EDGE ORGANIZED MENU RAIL (Labeled Icons, Zero Guessing)
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelDark.copy(alpha = 0.86f),
            border = BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    listOf(AmberGold.copy(alpha = 0.48f), PanelBorder.copy(alpha = 0.6f))
                )
            ),
            tonalElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = 26.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                HudRailButton(
                    icon = Icons.Filled.Build,
                    label = "Forge",
                    contentDescription = stringResource(R.string.cd_open_shop),
                    accent = AmberGold,
                    testTag = "btn_open_shop",
                    onClick = onOpenUpgrades
                )
                HorizontalDivider(
                    color = PanelBorder.copy(alpha = 0.4f),
                    modifier = Modifier.width(34.dp)
                )
                HudRailButton(
                    icon = Icons.Filled.PrecisionManufacturing,
                    label = "Machines",
                    contentDescription = stringResource(R.string.cd_open_machinery),
                    accent = CrystalCyan,
                    testTag = "btn_open_machinery",
                    onClick = onOpenMachinery
                )
                HorizontalDivider(
                    color = PanelBorder.copy(alpha = 0.4f),
                    modifier = Modifier.width(34.dp)
                )
                HudRailButton(
                    icon = Icons.Filled.Storefront,
                    label = "Market",
                    contentDescription = stringResource(R.string.cd_open_market),
                    accent = AmberBright,
                    badgeCount = deliverableContracts,
                    testTag = "btn_open_market",
                    onClick = onOpenMarket
                )
                HorizontalDivider(
                    color = PanelBorder.copy(alpha = 0.4f),
                    modifier = Modifier.width(34.dp)
                )
                HudRailButton(
                    icon = Icons.Filled.Factory,
                    label = "Factory",
                    contentDescription = stringResource(R.string.cd_open_business),
                    accent = EmeraldCanopy,
                    testTag = "btn_open_business",
                    onClick = onOpenBusiness
                )
                HorizontalDivider(
                    color = PanelBorder.copy(alpha = 0.4f),
                    modifier = Modifier.width(34.dp)
                )
                HudRailButton(
                    icon = Icons.Filled.Groups,
                    label = "Town",
                    contentDescription = stringResource(R.string.cd_open_settlement),
                    accent = CrystalCyan,
                    badgeCount = state.settlement.pendingArrivals.size,
                    testTag = "btn_open_settlement",
                    onClick = onOpenSettlement
                )
            }
        }

        // =====================================================================
        // BOTTOM-RIGHT: Drive Toggle, Held Cargo Strip & Quest Tracker
        // =====================================================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp)
                .widthIn(max = 188.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (state.unlockedVehicles.isNotEmpty()) {
                val activeVehicle = DrivableVehicleType.fromId(state.activeVehicleId)
                val isDriving = activeVehicle != null
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDriving) EmeraldCanopy.copy(alpha = 0.24f) else PanelDark.copy(alpha = 0.88f),
                    border = BorderStroke(
                        1.dp,
                        if (isDriving) EmeraldCanopy else CrystalCyan.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onToggleVehicle)
                        .minimumInteractiveComponentSize()
                        .testTag("btn_toggle_vehicle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = stringResource(R.string.cd_toggle_vehicle),
                            tint = if (isDriving) EmeraldCanopy else CrystalCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDriving) "Exit ${activeVehicle?.title?.substringBefore(" ")}" else "Drive",
                            style = MaterialTheme.typography.labelMedium,
                            color = CreamText,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            if (heldResources.isNotEmpty() || state.isBackpackFull) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelDark.copy(alpha = 0.86f),
                    border = BorderStroke(
                        1.dp,
                        if (state.isBackpackFull) CoralCrimson.copy(alpha = 0.85f)
                        else PanelBorder.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenBackpack)
                        .testTag("bottom_backpack_strip")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        if (state.isBackpackFull) {
                            Text(
                                text = "Bag Full — Visit Market",
                                style = MaterialTheme.typography.labelSmall,
                                color = CoralCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                heldResources.take(3).forEach { (res, qty) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(res.colorHex))
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "$qty",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CreamText,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (trackedQuest != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (trackedQuest.isReadyToClaim) {
                        PanelCard.copy(alpha = 0.94f)
                    } else {
                        PanelDark.copy(alpha = 0.84f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (trackedQuest.isReadyToClaim) AmberGold.copy(alpha = 0.85f)
                        else PanelBorder.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenQuests)
                        .testTag("hud_quest_tracker")
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (trackedQuest.isReadyToClaim) Icons.Filled.Star else Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = if (trackedQuest.isReadyToClaim) AmberBright else AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (trackedQuest.isReadyToClaim) {
                                    "Claim: ${trackedQuest.quest.title}"
                                } else {
                                    trackedQuest.quest.title
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (trackedQuest.isReadyToClaim) AmberBright else CreamText,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { trackedQuest.fraction },
                                modifier = Modifier
                                    .width(86.dp)
                                    .height(3.5.dp)
                                    .clip(CircleShape),
                                color = if (trackedQuest.isReadyToClaim) EmeraldCanopy else AmberGold,
                                trackColor = PanelElevated,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "${trackedQuest.current}/${trackedQuest.target}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LichenMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HudRailButton(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    accent: Color,
    badgeCount: Int = 0,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .testTag(testTag)
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = CoralCrimson,
                        contentColor = CreamText
                    ) {
                        Text("$badgeCount")
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
            color = CreamText.copy(alpha = 0.9f),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}
