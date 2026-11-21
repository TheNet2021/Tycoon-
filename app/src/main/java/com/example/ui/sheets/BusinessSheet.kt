package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BiomeType
import com.example.data.BuildingType
import com.example.data.ResourceUpgradeType
import com.example.data.TycoonSaveState
import com.example.ui.components.FrontierActionButton
import com.example.ui.components.FrontierBadgeChip
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSheetScaffold
import com.example.ui.components.TierPipsBar
import com.example.ui.components.formatCompactCurrency
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CreamText
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelElevated
import kotlin.math.roundToInt

@Composable
fun BusinessSheet(
    state: TycoonSaveState,
    onDismiss: () -> Unit,
    onBuildOrUpgradeBuilding: (BuildingType) -> Unit,
    onHireWorker: (BuildingType) -> Unit,
    onAddLine: (BuildingType) -> Unit
) {
    val activeCount = BuildingType.entries.count { state.buildingLevel(it) > 0 }
    val totalWorkers = BuildingType.entries.sumOf { state.buildingWorkerCount(it) }
    val totalLines = BuildingType.entries.sumOf { state.buildingLineCount(it) }
    val autoSpeedPct = 100 + (state.resourceUpgradeLevel(ResourceUpgradeType.AUTO_PRODUCTION) - 1) * 20

    FrontierSheetScaffold(
        title = "Industry & Factories",
        subtitle = "Expand tiers, hire workers & add production lines",
        headerIcon = Icons.Filled.Factory,
        accentColor = EmeraldCanopy,
        coins = state.coins,
        passivePerSec = state.passiveIncomePerSecond,
        closeTestTag = "btn_close_business",
        onDismiss = onDismiss
    ) {
        // Executive Economy Summary Card
        FrontierGlassCard(
            accentBorder = EmeraldCanopy,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PASSIVE REVENUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = LichenMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = EmeraldCanopy,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+$${state.passiveIncomePerSecond.roundToInt()} / sec",
                                style = MaterialTheme.typography.titleLarge,
                                color = EmeraldCanopy
                            )
                        }
                    }
                    FrontierBadgeChip(
                        text = "$activeCount / ${BuildingType.entries.size} Built",
                        accent = AmberBright
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FrontierBadgeChip(
                        text = "$totalWorkers Workers",
                        accent = CrystalCyan,
                        icon = Icons.Filled.Groups
                    )
                    FrontierBadgeChip(
                        text = "$totalLines Lines",
                        accent = AmberGold,
                        icon = Icons.Filled.SettingsSuggest
                    )
                    FrontierBadgeChip(
                        text = "Overclock $autoSpeedPct%",
                        accent = EmeraldCanopy
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(385.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(BuildingType.entries, key = { it.id }) { building ->
                val lvl = state.buildingLevel(building)
                val workers = state.buildingWorkerCount(building)
                val lines = state.buildingLineCount(building)
                val biome = BiomeType.fromId(building.biomeId)
                val biomeUnlocked = state.unlockedBiomes.contains(building.biomeId)
                val isMax = lvl >= building.maxLevel
                val cost = building.costForNextLevel(lvl)
                val canAfford = biomeUnlocked && !isMax && state.coins >= cost
                val biomeColor = biome?.let { Color(it.accentHex) } ?: EmeraldCanopy

                val effectiveOutput = state.buildingEffectivePassive(building).roundToInt()

                val isProcessor = building == BuildingType.SAWMILL ||
                    building == BuildingType.WAREHOUSE ||
                    building == BuildingType.SMELTER

                val actionLabel = when {
                    !biomeUnlocked -> "Locked"
                    isMax -> "MAX"
                    lvl == 0 -> "Build ${formatCompactCurrency(cost)}"
                    else -> "Tier ${formatCompactCurrency(cost)}"
                }

                FrontierGlassCard(
                    accentBorder = if (lvl > 0) biomeColor else null
                ) {
                    Column(modifier = Modifier.padding(13.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(15.dp),
                                color = if (biomeUnlocked) biomeColor.copy(alpha = 0.16f) else PanelElevated,
                                border = BorderStroke(
                                    1.dp,
                                    if (biomeUnlocked) biomeColor.copy(alpha = 0.5f) else LichenMuted.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when {
                                            !biomeUnlocked -> Icons.Filled.Lock
                                            isProcessor -> Icons.Filled.Factory
                                            else -> Icons.Filled.PrecisionManufacturing
                                        },
                                        contentDescription = null,
                                        tint = if (biomeUnlocked) biomeColor else LichenMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = building.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (biomeUnlocked) CreamText else LichenMuted
                                    )
                                    if (lvl > 0) {
                                        FrontierBadgeChip(
                                            text = "Tier $lvl/${building.maxLevel}",
                                            accent = EmeraldCanopy
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${biome?.title ?: ""} • ${building.description}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LichenMuted
                                )
                                if (lvl > 0) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Output: +$$effectiveOutput/sec",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EmeraldCanopy,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                TierPipsBar(
                                    current = lvl,
                                    max = building.maxLevel,
                                    activeColor = if (lvl > 0) EmeraldCanopy else AmberGold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            FrontierActionButton(
                                text = actionLabel,
                                enabled = canAfford,
                                accentColor = if (lvl == 0) EmeraldCanopy else AmberGold,
                                isCompletedState = isMax,
                                testTag = "btn_building_${building.id}",
                                onClick = { onBuildOrUpgradeBuilding(building) }
                            )
                        }

                        // Workers & Production Lines management once facility is built
                        if (lvl > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = PanelElevated.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val workerCost = building.costForNextWorker(workers)
                                    val workersMax = workers >= building.maxWorkers
                                    val canHireWorker = !workersMax && state.coins >= workerCost

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Workers ($workers/${building.maxWorkers}) • +${workers * 35}% output",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = CreamText
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            TierPipsBar(
                                                current = workers,
                                                max = building.maxWorkers,
                                                activeColor = CrystalCyan
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        FrontierActionButton(
                                            text = if (workersMax) "FULL" else "Hire ${formatCompactCurrency(workerCost)}",
                                            enabled = canHireWorker,
                                            accentColor = CrystalCyan,
                                            isCompletedState = workersMax,
                                            testTag = "btn_worker_${building.id}",
                                            onClick = { onHireWorker(building) }
                                        )
                                    }

                                    val lineCost = building.costForNextLine(lines)
                                    val linesMax = lines >= building.maxLines
                                    val canAddLine = !linesMax && state.coins >= lineCost

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Production Lines ($lines/${building.maxLines}) • +${lines * 65}% speed",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = CreamText
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            TierPipsBar(
                                                current = lines,
                                                max = building.maxLines,
                                                activeColor = AmberBright
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        FrontierActionButton(
                                            text = if (linesMax) "FULL" else "Line ${formatCompactCurrency(lineCost)}",
                                            enabled = canAddLine,
                                            accentColor = AmberGold,
                                            isCompletedState = linesMax,
                                            testTag = "btn_line_${building.id}",
                                            onClick = { onAddLine(building) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
