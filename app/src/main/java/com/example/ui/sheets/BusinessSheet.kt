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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.FrontierEmptyStateCard
import com.example.ui.components.FrontierFilterStrip
import com.example.ui.components.FrontierFilterTab
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSheetScaffold
import com.example.ui.components.FrontierStatTile
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

private enum class BusinessFilter(val label: String) {
    ALL("All"),
    BUILT("Built"),
    AVAILABLE("Ready"),
    LOCKED("Locked")
}

@Composable
fun BusinessSheet(
    state: TycoonSaveState,
    onDismiss: () -> Unit,
    onBuildOrUpgradeBuilding: (BuildingType) -> Unit,
    onHireWorker: (BuildingType) -> Unit,
    onAddLine: (BuildingType) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(BusinessFilter.ALL) }

    val activeCount = BuildingType.entries.count { state.buildingLevel(it) > 0 }
    val totalWorkers = BuildingType.entries.sumOf { state.buildingWorkerCount(it) }
    val totalLines = BuildingType.entries.sumOf { state.buildingLineCount(it) }
    val autoSpeedPct = 100 + (state.resourceUpgradeLevel(ResourceUpgradeType.AUTO_PRODUCTION) - 1) * 20

    val filteredBuildings = remember(state, selectedFilter) {
        when (selectedFilter) {
            BusinessFilter.ALL -> BuildingType.entries
            BusinessFilter.BUILT -> BuildingType.entries.filter { state.buildingLevel(it) > 0 }
            BusinessFilter.AVAILABLE -> BuildingType.entries.filter {
                state.unlockedBiomes.contains(it.biomeId) && state.buildingLevel(it) == 0
            }
            BusinessFilter.LOCKED -> BuildingType.entries.filter {
                !state.unlockedBiomes.contains(it.biomeId)
            }
        }
    }

    FrontierSheetScaffold(
        title = "Industry & Factories",
        subtitle = "Overclock $autoSpeedPct% • Manage tiers, workers & assembly lines",
        headerIcon = Icons.Filled.Factory,
        accentColor = EmeraldCanopy,
        coins = state.coins,
        passivePerSec = state.passiveIncomePerSecond,
        closeTestTag = "btn_close_business",
        onDismiss = onDismiss
    ) {
        // Compact 4-Tile Economy Dashboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrontierStatTile(
                label = "Passive/s",
                value = "+$${state.passiveIncomePerSecond.roundToInt()}",
                accent = EmeraldCanopy,
                modifier = Modifier.weight(1f)
            )
            FrontierStatTile(
                label = "Facilities",
                value = "$activeCount/${BuildingType.entries.size}",
                accent = AmberBright,
                modifier = Modifier.weight(1f)
            )
            FrontierStatTile(
                label = "Workers",
                value = "$totalWorkers",
                accent = CrystalCyan,
                modifier = Modifier.weight(1f)
            )
            FrontierStatTile(
                label = "Lines",
                value = "$totalLines",
                accent = AmberGold,
                modifier = Modifier.weight(1f)
            )
        }

        val filters = BusinessFilter.entries
        FrontierFilterStrip(
            filters = filters.map { filter ->
                FrontierFilterTab(
                    label = filter.label,
                    accent = when (filter) {
                        BusinessFilter.ALL -> EmeraldCanopy
                        BusinessFilter.BUILT -> AmberGold
                        BusinessFilter.AVAILABLE -> CrystalCyan
                        BusinessFilter.LOCKED -> LichenMuted
                    },
                    testTag = "filter_business_${filter.name.lowercase()}"
                )
            },
            selectedIndex = filters.indexOf(selectedFilter).coerceAtLeast(0),
            onSelect = { idx -> selectedFilter = filters[idx] }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredBuildings.isEmpty()) {
                item(key = "business_empty") {
                    FrontierEmptyStateCard(
                        icon = Icons.Filled.Factory,
                        title = "No Factories In This View",
                        subtitle = "Unlock new world regions or switch filters to construct more facilities.",
                        accent = EmeraldCanopy
                    )
                }
            } else {
                items(filteredBuildings, key = { it.id }) { building ->
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
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (biomeUnlocked) biomeColor.copy(alpha = 0.16f) else PanelElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (biomeUnlocked) biomeColor.copy(alpha = 0.5f) else LichenMuted.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier.size(48.dp)
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
                                            modifier = Modifier.size(22.dp)
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
                                            text = building.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (biomeUnlocked) CreamText else LichenMuted
                                        )
                                        if (lvl > 0) {
                                            FrontierBadgeChip(
                                                text = "T$lvl/${building.maxLevel}",
                                                accent = EmeraldCanopy
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${biome?.title ?: ""} • ${building.description}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LichenMuted
                                    )
                                    if (lvl > 0) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "+$$effectiveOutput/sec",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = EmeraldCanopy,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
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

                            // Compact 2-Column Workers & Assembly Lines Controls once built
                            if (lvl > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val workerCost = building.costForNextWorker(workers)
                                val workersMax = workers >= building.maxWorkers
                                val canHireWorker = !workersMax && state.coins >= workerCost

                                val lineCost = building.costForNextLine(lines)
                                val linesMax = lines >= building.maxLines
                                val canAddLine = !linesMax && state.coins >= lineCost

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(13.dp),
                                        color = PanelElevated.copy(alpha = 0.6f),
                                        border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.45f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(9.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                text = "Workers ($workers/${building.maxWorkers})",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = CreamText,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "+${workers * 35}% output",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CrystalCyan
                                            )
                                            TierPipsBar(
                                                current = workers,
                                                max = building.maxWorkers,
                                                activeColor = CrystalCyan
                                            )
                                            FrontierActionButton(
                                                text = if (workersMax) "FULL" else "Hire ${formatCompactCurrency(workerCost)}",
                                                enabled = canHireWorker,
                                                accentColor = CrystalCyan,
                                                isCompletedState = workersMax,
                                                testTag = "btn_worker_${building.id}",
                                                modifier = Modifier.fillMaxWidth(),
                                                onClick = { onHireWorker(building) }
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(13.dp),
                                        color = PanelElevated.copy(alpha = 0.6f),
                                        border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.45f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(9.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                text = "Lines ($lines/${building.maxLines})",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = CreamText,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "+${lines * 65}% speed",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AmberBright
                                            )
                                            TierPipsBar(
                                                current = lines,
                                                max = building.maxLines,
                                                activeColor = AmberBright
                                            )
                                            FrontierActionButton(
                                                text = if (linesMax) "FULL" else "Line ${formatCompactCurrency(lineCost)}",
                                                enabled = canAddLine,
                                                accentColor = AmberGold,
                                                isCompletedState = linesMax,
                                                testTag = "btn_line_${building.id}",
                                                modifier = Modifier.fillMaxWidth(),
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
}
