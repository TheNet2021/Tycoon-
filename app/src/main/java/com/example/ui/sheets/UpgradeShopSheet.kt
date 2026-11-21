package com.example.ui.sheets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ResourceRequirement
import com.example.data.ResourceUpgradeType
import com.example.data.TycoonSaveState
import com.example.data.UpgradeStatMetric
import com.example.data.UpgradeType
import com.example.ui.components.FrontierActionButton
import com.example.ui.components.FrontierBadgeChip
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSheetScaffold
import com.example.ui.components.TierPipsBar
import com.example.ui.components.formatCompactCurrency
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
import com.example.ui.theme.PanelElevated
import kotlin.math.cos
import kotlin.math.sin

private enum class UpgradeCategoryFilter(val label: String) {
    ALL("All"),
    AXE("Axe"),
    PICKAXE("Pickaxe"),
    BACKPACK("Backpack"),
    CHARACTER("Character"),
    RESOURCES("Resources")
}

private sealed interface SelectedUpgradeItem {
    data class Equipment(val type: UpgradeType) : SelectedUpgradeItem
    data class ResourceTech(val type: ResourceUpgradeType) : SelectedUpgradeItem
}

@Composable
fun UpgradeShopSheet(
    state: TycoonSaveState,
    onDismiss: () -> Unit,
    onPurchaseUpgrade: (UpgradeType) -> Unit,
    onPurchaseResourceUpgrade: (ResourceUpgradeType) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(UpgradeCategoryFilter.ALL) }
    var selectedItem by remember { mutableStateOf<SelectedUpgradeItem>(SelectedUpgradeItem.Equipment(UpgradeType.AXE)) }
    var celebrationBurstKey by remember { mutableIntStateOf(0) }

    val visibleEquipment = remember(selectedCategory) {
        when (selectedCategory) {
            UpgradeCategoryFilter.ALL -> UpgradeType.entries
            UpgradeCategoryFilter.AXE -> listOf(UpgradeType.AXE)
            UpgradeCategoryFilter.PICKAXE -> listOf(UpgradeType.PICKAXE)
            UpgradeCategoryFilter.BACKPACK -> listOf(UpgradeType.BACKPACK)
            UpgradeCategoryFilter.CHARACTER -> listOf(UpgradeType.MOVE_SPEED, UpgradeType.GATHER_SPEED)
            UpgradeCategoryFilter.RESOURCES -> emptyList()
        }
    }
    val visibleResourceUpgrades = remember(selectedCategory) {
        when (selectedCategory) {
            UpgradeCategoryFilter.ALL, UpgradeCategoryFilter.RESOURCES -> ResourceUpgradeType.entries
            else -> emptyList()
        }
    }

    LaunchedEffect(selectedCategory) {
        when (selectedCategory) {
            UpgradeCategoryFilter.AXE -> selectedItem = SelectedUpgradeItem.Equipment(UpgradeType.AXE)
            UpgradeCategoryFilter.PICKAXE -> selectedItem = SelectedUpgradeItem.Equipment(UpgradeType.PICKAXE)
            UpgradeCategoryFilter.BACKPACK -> selectedItem = SelectedUpgradeItem.Equipment(UpgradeType.BACKPACK)
            UpgradeCategoryFilter.CHARACTER -> selectedItem = SelectedUpgradeItem.Equipment(UpgradeType.MOVE_SPEED)
            UpgradeCategoryFilter.RESOURCES -> selectedItem = SelectedUpgradeItem.ResourceTech(ResourceUpgradeType.TRADE_VALUE)
            UpgradeCategoryFilter.ALL -> Unit
        }
    }

    FrontierSheetScaffold(
        title = "Forge & Upgrades",
        subtitle = "8 evolution tiers: Wooden → Stone → Copper → Iron → Steel → Gold → Diamond → Legendary",
        headerIcon = Icons.Filled.Build,
        accentColor = AmberGold,
        coins = state.coins,
        passivePerSec = state.passiveIncomePerSecond,
        closeTestTag = "btn_close_shop",
        onDismiss = onDismiss
    ) {
        // Category Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            items(UpgradeCategoryFilter.entries) { filter ->
                FilterChip(
                    selected = selectedCategory == filter,
                    onClick = { selectedCategory = filter },
                    label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberGold,
                        selectedLabelColor = ObsidianForest,
                        containerColor = PanelCard,
                        labelColor = LichenMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategory == filter,
                        borderColor = PanelBorder
                    ),
                    modifier = Modifier.testTag("filter_upgrade_${filter.name.lowercase()}")
                )
            }
        }

        // Dedicated Large 3D Equipment / Tech Showcase Stage
        ShowcaseStageCard(
            state = state,
            selectedItem = selectedItem,
            celebrationKey = celebrationBurstKey,
            onUpgradeSelected = {
                when (val item = selectedItem) {
                    is SelectedUpgradeItem.Equipment -> {
                        onPurchaseUpgrade(item.type)
                        celebrationBurstKey++
                    }
                    is SelectedUpgradeItem.ResourceTech -> {
                        onPurchaseResourceUpgrade(item.type)
                        celebrationBurstKey++
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Upgrade Paths List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(visibleEquipment, key = { "eq_${it.id}" }) { upgrade ->
                val lvl = state.upgradeLevel(upgrade)
                val isMax = lvl >= upgrade.maxLevel
                val cost = upgrade.costForNextLevel(lvl)
                val req = upgrade.resourceRequirementForNext(lvl)
                val reqMet = state.meetsResourceRequirement(req)
                val canAfford = !isMax && reqMet && state.coins >= cost
                val tierAccent = tierColor(lvl)
                val isSelected = selectedItem == SelectedUpgradeItem.Equipment(upgrade)

                FrontierGlassCard(
                    accentBorder = when {
                        isSelected -> AmberBright
                        canAfford -> tierAccent
                        else -> null
                    },
                    modifier = Modifier.clickable {
                        selectedItem = SelectedUpgradeItem.Equipment(upgrade)
                    }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = PanelElevated,
                                border = BorderStroke(1.3.dp, tierAccent.copy(alpha = 0.65f)),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    EquipmentPreviewCanvas(
                                        type = upgrade,
                                        level = lvl,
                                        accent = tierAccent,
                                        animateOrbit = false,
                                        celebrationProgress = 0f,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = upgrade.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = CreamText
                                    )
                                    FrontierBadgeChip(
                                        text = "${upgrade.materialTierLabel(lvl)} • $lvl/${upgrade.maxLevel}",
                                        accent = tierAccent
                                    )
                                }
                                Text(
                                    text = upgrade.tierName(lvl),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberBright
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TierPipsBar(
                                    current = lvl,
                                    max = upgrade.maxLevel,
                                    activeColor = tierAccent
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            FrontierActionButton(
                                text = when {
                                    isMax -> "MAX"
                                    !reqMet -> "Locked"
                                    else -> formatCompactCurrency(cost)
                                },
                                enabled = canAfford,
                                accentColor = AmberGold,
                                isCompletedState = isMax,
                                testTag = "btn_upgrade_${upgrade.id}",
                                onClick = {
                                    selectedItem = SelectedUpgradeItem.Equipment(upgrade)
                                    onPurchaseUpgrade(upgrade)
                                    celebrationBurstKey++
                                }
                            )
                        }

                        if (!isMax && req != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            ResourceRequirementBar(
                                req = req,
                                gathered = state.gatheredCount(req.resource),
                                met = reqMet
                            )
                        }
                    }
                }
            }

            items(visibleResourceUpgrades, key = { "res_${it.id}" }) { resUp ->
                val lvl = state.resourceUpgradeLevel(resUp)
                val isMax = lvl >= resUp.maxLevel
                val cost = resUp.costForNextLevel(lvl)
                val req = resUp.resourceRequirementForNext(lvl)
                val reqMet = state.meetsResourceRequirement(req)
                val canAfford = !isMax && reqMet && state.coins >= cost
                val accent = Color(resUp.accentHex)
                val isSelected = selectedItem == SelectedUpgradeItem.ResourceTech(resUp)

                FrontierGlassCard(
                    accentBorder = when {
                        isSelected -> AmberBright
                        canAfford -> accent
                        else -> null
                    },
                    modifier = Modifier.clickable {
                        selectedItem = SelectedUpgradeItem.ResourceTech(resUp)
                    }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = PanelElevated,
                                border = BorderStroke(1.3.dp, accent.copy(alpha = 0.6f)),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = resUp.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = CreamText
                                    )
                                    FrontierBadgeChip(
                                        text = "Lv.$lvl/${resUp.maxLevel}",
                                        accent = accent
                                    )
                                }
                                Text(
                                    text = resUp.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LichenMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TierPipsBar(
                                    current = lvl,
                                    max = resUp.maxLevel,
                                    activeColor = accent
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            FrontierActionButton(
                                text = when {
                                    isMax -> "MAX"
                                    !reqMet -> "Locked"
                                    else -> formatCompactCurrency(cost)
                                },
                                enabled = canAfford,
                                accentColor = accent,
                                isCompletedState = isMax,
                                testTag = "btn_res_upgrade_${resUp.id}",
                                onClick = {
                                    selectedItem = SelectedUpgradeItem.ResourceTech(resUp)
                                    onPurchaseResourceUpgrade(resUp)
                                    celebrationBurstKey++
                                }
                            )
                        }

                        if (!isMax && req != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            ResourceRequirementBar(
                                req = req,
                                gathered = state.gatheredCount(req.resource),
                                met = reqMet
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowcaseStageCard(
    state: TycoonSaveState,
    selectedItem: SelectedUpgradeItem,
    celebrationKey: Int,
    onUpgradeSelected: () -> Unit
) {
    val burstAnim = remember { Animatable(0f) }
    LaunchedEffect(celebrationKey) {
        if (celebrationKey > 0) {
            burstAnim.snapTo(1f)
            burstAnim.animateTo(0f, tween(durationMillis = 650, easing = FastOutSlowInEasing))
        }
    }

    val title: String
    val tierBadge: String
    val nextTierLabel: String
    val accent: Color
    val level: Int
    val maxLevel: Int
    val cost: Long
    val req: ResourceRequirement?
    val stats: List<UpgradeStatMetric>

    when (selectedItem) {
        is SelectedUpgradeItem.Equipment -> {
            val u = selectedItem.type
            level = state.upgradeLevel(u)
            maxLevel = u.maxLevel
            title = u.tierName(level)
            tierBadge = "${u.materialTierLabel(level)} • Lv.$level/$maxLevel"
            nextTierLabel = if (level >= maxLevel) "Max Evolution Reached" else "Next: ${u.tierName(level + 1)}"
            accent = tierColor(level)
            cost = u.costForNextLevel(level)
            req = u.resourceRequirementForNext(level)
            stats = u.statComparisonLines(level)
        }
        is SelectedUpgradeItem.ResourceTech -> {
            val r = selectedItem.type
            level = state.resourceUpgradeLevel(r)
            maxLevel = r.maxLevel
            title = r.title
            tierBadge = "Resource Tech • Lv.$level/$maxLevel"
            nextTierLabel = if (level >= maxLevel) "Max Mastery Reached" else "Next: Tier ${level + 1}"
            accent = Color(r.accentHex)
            cost = r.costForNextLevel(level)
            req = r.resourceRequirementForNext(level)
            stats = r.statComparisonLines(level)
        }
    }

    val isMax = level >= maxLevel
    val reqMet = state.meetsResourceRequirement(req)
    val canAfford = !isMax && reqMet && state.coins >= cost

    FrontierGlassCard(accentBorder = accent) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Large 3D Preview Stage
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PanelElevated,
                    border = BorderStroke(1.5.dp, accent.copy(alpha = 0.75f)),
                    modifier = Modifier.size(92.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when (selectedItem) {
                            is SelectedUpgradeItem.Equipment -> EquipmentPreviewCanvas(
                                type = selectedItem.type,
                                level = level,
                                accent = accent,
                                animateOrbit = true,
                                celebrationProgress = burstAnim.value,
                                modifier = Modifier.size(82.dp)
                            )
                            is SelectedUpgradeItem.ResourceTech -> ResourceTechPreviewCanvas(
                                accent = accent,
                                level = level,
                                celebrationProgress = burstAnim.value,
                                modifier = Modifier.size(82.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    FrontierBadgeChip(text = tierBadge, accent = accent)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CreamText
                    )
                    Text(
                        text = nextTierLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMax) EmeraldCanopy else AmberBright
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TierPipsBar(current = level, max = maxLevel, activeColor = accent)
                }

                Spacer(modifier = Modifier.width(8.dp))

                FrontierActionButton(
                    text = when {
                        isMax -> "MAX"
                        !reqMet -> "Locked"
                        else -> formatCompactCurrency(cost)
                    },
                    enabled = canAfford,
                    accentColor = accent,
                    isCompletedState = isMax,
                    testTag = "btn_upgrade_showcase",
                    onClick = onUpgradeSelected
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Before -> After Statistics Grid
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PanelElevated.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stats.forEach { metric ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = metric.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = LichenMuted
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = metric.currentText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CreamText,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (metric.nextText != null) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(12.dp)
                                    )
                                    Text(
                                        text = metric.nextText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EmeraldCanopy,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (metric.improvementPct > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(+${metric.improvementPct}%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AmberBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!isMax && req != null) {
                Spacer(modifier = Modifier.height(6.dp))
                ResourceRequirementBar(
                    req = req,
                    gathered = state.gatheredCount(req.resource),
                    met = reqMet
                )
            }
        }
    }
}

@Composable
private fun ResourceRequirementBar(
    req: ResourceRequirement,
    gathered: Int,
    met: Boolean
) {
    val frac = (gathered.toFloat() / req.requiredTotal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val resColor = Color(req.resource.colorHex)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (met) EmeraldCanopy else AmberBright,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Requires ${req.requiredTotal} ${req.resource.displayName} gathered",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (met) EmeraldCanopy else CreamText
                )
            }
            Text(
                text = "${gathered.coerceAtMost(req.requiredTotal)} / ${req.requiredTotal}",
                style = MaterialTheme.typography.labelSmall,
                color = if (met) EmeraldCanopy else CoralCrimson,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { frac },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape),
            color = if (met) EmeraldCanopy else resColor,
            trackColor = PanelElevated,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun ResourceTechPreviewCanvas(
    accent: Color,
    level: Int,
    celebrationProgress: Float,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "resTechOrbit")
    val phase by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(
            brush = Brush.radialGradient(listOf(accent.copy(alpha = 0.42f), Color.Transparent)),
            radius = w * 0.48f
        )
        rotate(phase, pivot = Offset(w / 2f, h / 2f)) {
            drawRoundRect(
                color = accent.copy(alpha = 0.85f),
                topLeft = Offset(w * 0.28f, h * 0.28f),
                size = Size(w * 0.44f, h * 0.44f),
                cornerRadius = CornerRadius(w * 0.1f, w * 0.1f)
            )
        }
        drawCircle(
            color = Color.White.copy(alpha = 0.88f),
            radius = w * (0.12f + level * 0.01f),
            center = Offset(w / 2f, h / 2f)
        )
        if (celebrationProgress > 0.01f) {
            for (i in 0 until 8) {
                val ang = i * (Math.PI / 4.0)
                val dist = w * 0.22f + (1f - celebrationProgress) * w * 0.34f
                drawCircle(
                    color = AmberBright.copy(alpha = celebrationProgress),
                    radius = w * 0.055f * celebrationProgress,
                    center = Offset((w / 2f + cos(ang) * dist).toFloat(), (h / 2f + sin(ang) * dist).toFloat())
                )
            }
        }
    }
}

@Composable
private fun EquipmentPreviewCanvas(
    type: UpgradeType,
    level: Int,
    accent: Color,
    animateOrbit: Boolean,
    celebrationProgress: Float,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "equipStage")
    val bob by inf.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val tilt = if (animateOrbit) bob else 0f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Pedestal shadow & ambient radial glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(accent.copy(alpha = 0.44f), Color.Transparent)
            ),
            radius = w * 0.5f
        )
        if (animateOrbit) {
            drawOval(
                color = accent.copy(alpha = 0.26f),
                topLeft = Offset(w * 0.14f, h * 0.78f),
                size = Size(w * 0.72f, h * 0.14f)
            )
        }

        val haftColor = when {
            level >= 7 -> Color(0xFF1E293B)
            level >= 5 -> Color(0xFF475569)
            else -> Color(0xFF8B5A2B)
        }

        rotate(tilt, pivot = Offset(w / 2f, h / 2f)) {
            when (type) {
                UpgradeType.AXE -> {
                    drawLine(
                        color = haftColor,
                        start = Offset(w * 0.22f, h * 0.80f),
                        end = Offset(w * 0.72f, h * 0.20f),
                        strokeWidth = w * 0.11f
                    )
                    val blade = Path().apply {
                        moveTo(w * 0.56f, h * 0.22f)
                        lineTo(w * 0.88f, h * 0.12f)
                        lineTo(w * 0.90f, h * 0.48f)
                        lineTo(w * 0.60f, h * 0.40f)
                        close()
                    }
                    drawPath(blade, color = accent)
                    if (level >= 5) {
                        val backBit = Path().apply {
                            moveTo(w * 0.58f, h * 0.22f)
                            lineTo(w * 0.34f, h * 0.14f)
                            lineTo(w * 0.38f, h * 0.40f)
                            close()
                        }
                        drawPath(backBit, color = accent.copy(alpha = 0.85f))
                    }
                    if (level >= 7) {
                        drawCircle(color = Color.White, radius = w * 0.08f, center = Offset(w * 0.65f, h * 0.28f))
                    }
                }
                UpgradeType.PICKAXE -> {
                    drawLine(
                        color = haftColor,
                        start = Offset(w * 0.24f, h * 0.80f),
                        end = Offset(w * 0.68f, h * 0.24f),
                        strokeWidth = w * 0.11f
                    )
                    val pickArc = Path().apply {
                        moveTo(w * 0.26f, h * 0.20f)
                        lineTo(w * 0.70f, h * 0.18f)
                        lineTo(w * 0.90f, h * 0.50f)
                        lineTo(w * 0.66f, h * 0.34f)
                        lineTo(w * 0.40f, h * 0.36f)
                        close()
                    }
                    drawPath(pickArc, color = accent)
                    if (level >= 6) {
                        drawCircle(color = Color.White, radius = w * 0.08f, center = Offset(w * 0.66f, h * 0.26f))
                    }
                }
                UpgradeType.BACKPACK -> {
                    val scale = 0.66f + level * 0.032f
                    val bw = w * 0.52f * scale
                    val bh = h * 0.60f * scale
                    val bx = (w - bw) / 2f
                    val by = (h - bh) / 2f + h * 0.02f
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(bx, by),
                        size = Size(bw, bh),
                        cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
                    )
                    drawRoundRect(
                        color = Color(0xFFF7F4EA).copy(alpha = 0.88f),
                        topLeft = Offset(bx + bw * 0.14f, by + bh * 0.28f),
                        size = Size(bw * 0.72f, bh * 0.12f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    if (level >= 7) {
                        drawCircle(
                            color = Color.White,
                            radius = w * 0.08f,
                            center = Offset(w / 2f, by + bh * 0.64f)
                        )
                    }
                }
                UpgradeType.MOVE_SPEED -> {
                    val boot = Path().apply {
                        moveTo(w * 0.26f, h * 0.22f)
                        lineTo(w * 0.50f, h * 0.22f)
                        lineTo(w * 0.54f, h * 0.56f)
                        lineTo(w * 0.82f, h * 0.60f)
                        lineTo(w * 0.82f, h * 0.76f)
                        lineTo(w * 0.24f, h * 0.76f)
                        close()
                    }
                    drawPath(boot, color = accent)
                    if (level >= 6) {
                        drawLine(
                            color = Color.White,
                            start = Offset(w * 0.16f, h * 0.34f),
                            end = Offset(w * 0.32f, h * 0.26f),
                            strokeWidth = w * 0.05f
                        )
                    }
                }
                UpgradeType.GATHER_SPEED -> {
                    val bolt = Path().apply {
                        moveTo(w * 0.58f, h * 0.14f)
                        lineTo(w * 0.28f, h * 0.52f)
                        lineTo(w * 0.50f, h * 0.52f)
                        lineTo(w * 0.42f, h * 0.84f)
                        lineTo(w * 0.74f, h * 0.42f)
                        lineTo(w * 0.52f, h * 0.42f)
                        close()
                    }
                    drawPath(bolt, color = accent)
                }
            }
        }

        // Purchase Celebration Spark Burst
        if (celebrationProgress > 0.01f) {
            for (i in 0 until 8) {
                val ang = i * (Math.PI / 4.0)
                val dist = w * 0.20f + (1f - celebrationProgress) * w * 0.36f
                drawCircle(
                    color = AmberBright.copy(alpha = celebrationProgress),
                    radius = w * 0.06f * celebrationProgress,
                    center = Offset((w / 2f + cos(ang) * dist).toFloat(), (h / 2f + sin(ang) * dist).toFloat())
                )
            }
        }
    }
}

private fun tierColor(level: Int): Color = when (level.coerceIn(1, 8)) {
    1 -> Color(0xFFB45309) // Wooden
    2 -> Color(0xFF94A3B8) // Stone
    3 -> Color(0xFFEA580C) // Copper
    4 -> Color(0xFFCBD5E1) // Iron
    5 -> Color(0xFF64748B) // Steel
    6 -> AmberBright       // Gold
    7 -> CrystalCyan       // Diamond
    else -> Color(0xFFE879F9) // Legendary
}
