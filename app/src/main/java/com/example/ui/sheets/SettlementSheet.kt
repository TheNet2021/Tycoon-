package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.HousingBuilding
import com.example.data.HousingType
import com.example.data.RefugeeArrival
import com.example.data.SettlementState
import com.example.data.SettlementStats
import com.example.data.SupplyRestockKind
import com.example.data.SurvivorProfession
import com.example.data.SurvivorProfile
import com.example.data.TradeCatalog
import com.example.data.TycoonSaveState
import com.example.ui.components.FrontierActionButton
import com.example.ui.components.FrontierBadgeChip
import com.example.ui.components.FrontierEmptyStateCard
import com.example.ui.components.FrontierFilterStrip
import com.example.ui.components.FrontierFilterTab
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSectionHeader
import com.example.ui.components.FrontierSegmentedTabs
import com.example.ui.components.FrontierSheetScaffold
import com.example.ui.components.FrontierStatTile
import com.example.ui.components.FrontierTabSpec
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
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun SettlementSheet(
    state: TycoonSaveState,
    onDismiss: () -> Unit,
    onStartHousing: (HousingType) -> Boolean,
    onDemolishHousing: (String) -> Boolean,
    onAdmitRefugee: (String) -> Boolean,
    onDismissRefugee: (String) -> Unit,
    onScoutArrival: () -> Boolean,
    onAssignJob: (String, SurvivorProfession?) -> Boolean,
    onBuySupplies: (SupplyRestockKind, Int) -> Boolean,
    onSellToSurvivors: (String, Int) -> Boolean,
    onBuyFromSurvivors: (String, Int) -> Boolean,
    onCompleteContract: (String) -> Boolean
) {
    val settlement = state.settlement
    val stats = remember(settlement) { settlement.computeStats() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMs = System.currentTimeMillis()
        }
    }

    FrontierSheetScaffold(
        title = "Refugee Settlement",
        subtitle = "${stats.settlementTierName} • Day ${settlement.dayNumber} • Rep ${stats.reputation}",
        headerIcon = Icons.Filled.Groups,
        accentColor = EmeraldCanopy,
        coins = state.coins,
        passivePerSec = state.passiveIncomePerSecond,
        heightFraction = 0.89f,
        closeTestTag = "btn_close_settlement",
        onDismiss = onDismiss
    ) {
        FrontierSegmentedTabs(
            tabs = listOf(
                FrontierTabSpec("Town", EmeraldCanopy, "tab_settlement_overview"),
                FrontierTabSpec("Crew (${stats.population})", CrystalCyan, "tab_settlement_crew"),
                FrontierTabSpec("Homes", AmberGold, "tab_settlement_housing"),
                FrontierTabSpec("Trade", AmberBright, "tab_settlement_trade"),
                FrontierTabSpec("Build (${settlement.openContracts.size})", Color(0xFFE879F9), "tab_settlement_contracts")
            ),
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it }
        )

        when (selectedTab) {
            0 -> SettlementOverviewTab(
                state = state,
                stats = stats,
                onAdmitRefugee = onAdmitRefugee,
                onDismissRefugee = onDismissRefugee,
                onScoutArrival = onScoutArrival,
                onBuySupplies = onBuySupplies,
                onGoToHousing = { selectedTab = 2 }
            )
            1 -> SettlementSurvivorsTab(
                settlement = settlement,
                stats = stats,
                onAssignJob = onAssignJob
            )
            2 -> SettlementHousingTab(
                state = state,
                stats = stats,
                nowMs = nowMs,
                onBuildHousing = onStartHousing,
                onDemolishHousing = onDemolishHousing
            )
            3 -> SettlementTradingTab(
                state = state,
                onSellToSurvivors = onSellToSurvivors,
                onBuyFromSurvivors = onBuyFromSurvivors
            )
            else -> SettlementContractsTab(
                state = state,
                onCompleteContract = onCompleteContract
            )
        }
    }
}

@Composable
private fun SettlementOverviewTab(
    state: TycoonSaveState,
    stats: SettlementStats,
    onAdmitRefugee: (String) -> Boolean,
    onDismissRefugee: (String) -> Unit,
    onScoutArrival: () -> Boolean,
    onBuySupplies: (SupplyRestockKind, Int) -> Boolean,
    onGoToHousing: () -> Unit
) {
    val settlement = state.settlement
    val netDayCoin = stats.estimatedDailyTradeAndLogistics - stats.totalDailyMaintenanceCost
    val netColor = if (netDayCoin >= 0L) EmeraldCanopy else CoralCrimson

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FrontierStatTile(
                    label = "Pop / Beds",
                    value = "${stats.population}/${stats.totalHousingCapacity}",
                    accent = if (stats.population >= stats.totalHousingCapacity && stats.totalHousingCapacity > 0) AmberGold else EmeraldCanopy,
                    modifier = Modifier.weight(1f)
                )
                FrontierStatTile(
                    label = "Happiness",
                    value = "${stats.averageHappiness}%",
                    accent = if (stats.averageHappiness >= 70) EmeraldCanopy else if (stats.averageHappiness >= 45) AmberGold else CoralCrimson,
                    modifier = Modifier.weight(1f)
                )
                FrontierStatTile(
                    label = "Unemployed",
                    value = "${stats.unemploymentRatePercent}%",
                    accent = CrystalCyan,
                    modifier = Modifier.weight(1f)
                )
                FrontierStatTile(
                    label = "Day Net",
                    value = if (netDayCoin >= 0L) "+$$netDayCoin" else "-$${-netDayCoin}",
                    accent = netColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Community Vitality & Restock Controls
        item {
            FrontierGlassCard(accentBorder = CrystalCyan) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vitality & Community Reserves",
                            style = MaterialTheme.typography.titleSmall,
                            color = CreamText,
                            fontWeight = FontWeight.Bold
                        )
                        FrontierBadgeChip(
                            text = "Upkeep $${stats.totalDailyMaintenanceCost}/d",
                            accent = CoralCrimson
                        )
                    }

                    ReserveMeterRow(
                        icon = Icons.Filled.Restaurant,
                        label = "Food Rations",
                        stock = stats.foodStock,
                        capacity = stats.foodCapacity,
                        dailyNetLabel = "${if (stats.dailyFoodNet >= 0) "+" else ""}${stats.dailyFoodNet}/d",
                        accent = EmeraldCanopy,
                        costCoins = SupplyRestockKind.FOOD.coinCostPerBatch,
                        canBuy = state.coins >= SupplyRestockKind.FOOD.coinCostPerBatch && stats.foodStock < stats.foodCapacity,
                        testTag = "btn_restock_food",
                        onBuy = { onBuySupplies(SupplyRestockKind.FOOD, 1) }
                    )

                    ReserveMeterRow(
                        icon = Icons.Filled.WaterDrop,
                        label = "Clean Water",
                        stock = stats.waterStock,
                        capacity = stats.waterCapacity,
                        dailyNetLabel = "${if (stats.dailyWaterNet >= 0) "+" else ""}${stats.dailyWaterNet}/d",
                        accent = CrystalCyan,
                        costCoins = SupplyRestockKind.WATER.coinCostPerBatch,
                        canBuy = state.coins >= SupplyRestockKind.WATER.coinCostPerBatch && stats.waterStock < stats.waterCapacity,
                        testTag = "btn_restock_water",
                        onBuy = { onBuySupplies(SupplyRestockKind.WATER, 1) }
                    )

                    ReserveMeterRow(
                        icon = Icons.Filled.Inventory2,
                        label = "Basic Supplies",
                        stock = stats.suppliesStock,
                        capacity = stats.suppliesCapacity,
                        dailyNetLabel = "-${if (stats.population == 0) 0 else (stats.population + 1) / 2}/d",
                        accent = AmberGold,
                        costCoins = SupplyRestockKind.SUPPLIES.coinCostPerBatch,
                        canBuy = state.coins >= SupplyRestockKind.SUPPLIES.coinCostPerBatch && stats.suppliesStock < stats.suppliesCapacity,
                        testTag = "btn_restock_supplies",
                        onBuy = { onBuySupplies(SupplyRestockKind.SUPPLIES, 1) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FrontierBadgeChip(
                            text = "Occupancy ${(stats.housingOccupancyRate * 100).roundToInt()}%",
                            accent = AmberBright
                        )
                        FrontierBadgeChip(
                            text = "Wages $${stats.dailyWorkerSalaries}/d",
                            accent = AmberGold
                        )
                        FrontierBadgeChip(
                            text = "Housing $${stats.dailyHousingMaintenance}/d",
                            accent = CrystalCyan
                        )
                    }
                }
            }
        }

        // Gate Arrivals Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrontierSectionHeader(
                    title = "Refugees at Gate (${settlement.pendingArrivals.size})",
                    accent = AmberGold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FrontierActionButton(
                    text = "Scout Gate",
                    enabled = settlement.pendingArrivals.size < 5,
                    accentColor = CrystalCyan,
                    testTag = "btn_scout_arrival",
                    onClick = { onScoutArrival() }
                )
            }
        }

        if (stats.totalHousingCapacity <= stats.population) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AmberGold.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onGoToHousing)
                        .minimumInteractiveComponentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (stats.totalHousingCapacity == 0) {
                                "No housing built yet. Build a Small Tent in Homes to admit refugees."
                            } else {
                                "Housing is full (${stats.population}/${stats.totalHousingCapacity}). Build more Homes!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberBright,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FrontierBadgeChip(text = "Build Homes", accent = AmberGold)
                    }
                }
            }
        }

        if (settlement.pendingArrivals.isEmpty()) {
            item {
                FrontierEmptyStateCard(
                    icon = Icons.Filled.PersonAdd,
                    title = "Gate Queue Empty",
                    subtitle = "New survivors arrive automatically every ~30s, or tap Scout Gate to search nearby trails.",
                    accent = EmeraldCanopy
                )
            }
        } else {
            items(settlement.pendingArrivals, key = { it.id }) { arrival ->
                ArrivalCard(
                    arrival = arrival,
                    hasBed = settlement.availableHousingSlots > 0,
                    onAdmit = { onAdmitRefugee(arrival.id) },
                    onDismiss = { onDismissRefugee(arrival.id) }
                )
            }
        }
    }
}

@Composable
private fun ReserveMeterRow(
    icon: ImageVector,
    label: String,
    stock: Int,
    capacity: Int,
    dailyNetLabel: String,
    accent: Color,
    costCoins: Long,
    canBuy: Boolean,
    testTag: String,
    onBuy: () -> Unit
) {
    val frac = (stock.toFloat() / capacity.coerceAtLeast(1)).coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                        text = "$label ($stock/$capacity)",
                        style = MaterialTheme.typography.labelMedium,
                        color = CreamText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = dailyNetLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            LinearProgressIndicator(
                progress = { frac },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = accent,
                trackColor = PanelElevated,
                strokeCap = StrokeCap.Round
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        FrontierActionButton(
            text = "+10 $$costCoins",
            enabled = canBuy,
            accentColor = accent,
            testTag = testTag,
            onClick = onBuy
        )
    }
}

@Composable
private fun ArrivalCard(
    arrival: RefugeeArrival,
    hasBed: Boolean,
    onAdmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val profColor = Color(arrival.profession.accentHex)
    FrontierGlassCard(accentBorder = profColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = arrival.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = CreamText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FrontierBadgeChip(text = arrival.profession.title, accent = profColor)
                    FrontierBadgeChip(text = "Skill Lv.${arrival.skillLevel}", accent = AmberGold)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("btn_dismiss_arrival_${arrival.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Turn away refugee",
                        tint = LichenMuted
                    )
                }
                FrontierActionButton(
                    text = if (hasBed) "Welcome" else "Full",
                    enabled = hasBed,
                    accentColor = EmeraldCanopy,
                    testTag = "btn_admit_arrival_${arrival.id}",
                    onClick = onAdmit
                )
            }
        }
    }
}

@Composable
private fun SettlementSurvivorsTab(
    settlement: SettlementState,
    stats: SettlementStats,
    onAssignJob: (String, SurvivorProfession?) -> Boolean
) {
    var filterIdx by remember { mutableIntStateOf(0) }
    val list = remember(settlement.survivors, filterIdx) {
        when (filterIdx) {
            1 -> settlement.survivors.filter { it.isEmployed }
            2 -> settlement.survivors.filter { !it.isEmployed }
            else -> settlement.survivors
        }
    }

    FrontierFilterStrip(
        filters = listOf(
            FrontierFilterTab("All (${stats.population})", CrystalCyan, "filter_crew_all"),
            FrontierFilterTab("Working (${stats.employedCount})", EmeraldCanopy, "filter_crew_employed"),
            FrontierFilterTab("Idle (${stats.unemployedCount})", AmberGold, "filter_crew_unemployed")
        ),
        selectedIndex = filterIdx,
        onSelect = { filterIdx = it }
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        if (list.isEmpty()) {
            item {
                FrontierEmptyStateCard(
                    icon = Icons.Filled.Work,
                    title = "No Settlers in View",
                    subtitle = "Welcome refugees from the Town tab once housing beds are constructed.",
                    accent = CrystalCyan
                )
            }
        } else {
            items(list, key = { it.id }) { survivor ->
                SurvivorCard(
                    survivor = survivor,
                    onAssignJob = { role -> onAssignJob(survivor.id, role) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SurvivorCard(
    survivor: SurvivorProfile,
    onAssignJob: (SurvivorProfession?) -> Unit
) {
    val specialtyColor = Color(survivor.profession.accentHex)
    val moodColor = when {
        survivor.happiness >= 75 -> EmeraldCanopy
        survivor.happiness >= 45 -> AmberGold
        else -> CoralCrimson
    }

    FrontierGlassCard(accentBorder = if (survivor.isMatchedRole) EmeraldCanopy else specialtyColor) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = survivor.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = CreamText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FrontierBadgeChip(text = "Lv.${survivor.skillLevel}", accent = AmberGold)
                        if (survivor.isMatchedRole) {
                            Spacer(modifier = Modifier.width(6.dp))
                            FrontierBadgeChip(text = "+35% Match", accent = EmeraldCanopy)
                        }
                    }
                    Text(
                        text = "Specialty: ${survivor.profession.title} • Mood ${survivor.happiness}% • Wage $${survivor.dailySalary()}/d",
                        style = MaterialTheme.typography.bodySmall,
                        color = LichenMuted
                    )
                }
                FrontierBadgeChip(
                    text = survivor.assignedJob?.title ?: "Idle",
                    accent = if (survivor.isEmployed) moodColor else CoralCrimson
                )
            }

            // Need summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FrontierBadgeChip(text = "Food ${survivor.needs.food}%", accent = EmeraldCanopy)
                FrontierBadgeChip(text = "Water ${survivor.needs.water}%", accent = CrystalCyan)
                FrontierBadgeChip(text = "Gear ${survivor.needs.supplies}%", accent = AmberGold)
            }

            // Job Assignment Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SurvivorProfession.entries.forEach { role ->
                    val selected = survivor.assignedJob == role
                    val isNatural = survivor.profession == role
                    val chipAccent = Color(role.accentHex)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) chipAccent else PanelElevated,
                        border = BorderStroke(
                            1.dp,
                            if (selected) chipAccent else if (isNatural) chipAccent.copy(alpha = 0.7f) else PanelBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAssignJob(role) }
                            .minimumInteractiveComponentSize()
                            .testTag("btn_assign_${survivor.id}_${role.id}")
                    ) {
                        Text(
                            text = if (isNatural) "${role.title} ★" else role.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) ObsidianForest else CreamText,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
                if (survivor.isEmployed) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PanelCard,
                        border = BorderStroke(1.dp, CoralCrimson.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAssignJob(null) }
                            .minimumInteractiveComponentSize()
                            .testTag("btn_unassign_${survivor.id}")
                    ) {
                        Text(
                            text = "Rest",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralCrimson,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementHousingTab(
    state: TycoonSaveState,
    stats: SettlementStats,
    nowMs: Long,
    onBuildHousing: (HousingType) -> Boolean,
    onDemolishHousing: (String) -> Boolean
) {
    val settlement = state.settlement

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            FrontierSectionHeader(
                title = "Construct Housing (${stats.population}/${stats.totalHousingCapacity} Beds)",
                trailingBadge = if (stats.underConstructionCount > 0) "${stats.underConstructionCount} Building" else "5 Tiers",
                accent = AmberGold
            )
        }

        items(HousingType.entries, key = { it.id }) { housing ->
            HousingBlueprintCard(
                type = housing,
                state = state,
                onBuild = { onBuildHousing(housing) }
            )
        }

        item {
            FrontierSectionHeader(
                title = "Settlement Buildings (${settlement.housingUnits.size})",
                trailingBadge = "Upkeep $${stats.dailyHousingMaintenance}/d",
                accent = CrystalCyan
            )
        }

        if (settlement.housingUnits.isEmpty()) {
            item {
                FrontierEmptyStateCard(
                    icon = Icons.Filled.Home,
                    title = "No Shelters Built Yet",
                    subtitle = "Start with a Small Tent (2 beds) above to welcome arriving refugees.",
                    accent = AmberGold
                )
            }
        } else {
            items(settlement.housingUnits, key = { it.id }) { building ->
                BuiltHousingCard(
                    building = building,
                    occupants = settlement.occupantsIn(building.id),
                    canDemolish = (stats.totalHousingCapacity - (if (building.isCompleted) building.type.capacity else 0)) >= stats.population,
                    nowMs = nowMs,
                    onDemolish = { onDemolishHousing(building.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HousingBlueprintCard(
    type: HousingType,
    state: TycoonSaveState,
    onBuild: () -> Unit
) {
    val accent = Color(type.accentHex)
    val hasCoins = state.coins >= type.coinCost
    val hasMaterials = type.materialCosts.all { (matId, req) -> state.availableForTrade(matId) >= req }
    val canBuild = hasCoins && hasMaterials

    FrontierGlassCard(accentBorder = accent) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Apartment,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = type.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = CreamText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FrontierBadgeChip(text = "+${type.capacity} Beds", accent = EmeraldCanopy)
                    }
                    Text(
                        text = "${type.description} • ${type.buildTimeSeconds}s build • $${type.dailyMaintenanceCost}/d upkeep",
                        style = MaterialTheme.typography.bodySmall,
                        color = LichenMuted
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FrontierActionButton(
                    text = formatCompactCurrency(type.coinCost),
                    enabled = canBuild,
                    accentColor = accent,
                    testTag = "btn_build_housing_${type.id}",
                    onClick = onBuild
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                type.materialCosts.forEach { (matId, req) ->
                    val have = state.availableForTrade(matId)
                    val matName = TradeCatalog.find(matId)?.displayName ?: matId
                    FrontierBadgeChip(
                        text = "$matName $have/$req",
                        accent = if (have >= req) EmeraldCanopy else CoralCrimson
                    )
                }
            }
        }
    }
}

@Composable
private fun BuiltHousingCard(
    building: HousingBuilding,
    occupants: Int,
    canDemolish: Boolean,
    nowMs: Long,
    onDemolish: () -> Unit
) {
    val accent = Color(building.type.accentHex)
    val progress = building.progressFraction(nowMs)
    val remaining = building.remainingSeconds(nowMs)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PanelCard.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, if (building.isCompleted) accent.copy(alpha = 0.45f) else AmberGold.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = building.type.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (building.isCompleted) {
                            "Residents: $occupants/${building.type.capacity} • Upkeep $${building.type.dailyMaintenanceCost}/d"
                        } else {
                            "Under construction: ${remaining}s left (+${building.type.capacity} beds)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (building.isCompleted) LichenMuted else AmberBright
                    )
                }
                IconButton(
                    onClick = onDemolish,
                    enabled = canDemolish,
                    modifier = Modifier
                        .size(40.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("btn_demolish_${building.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Demolish ${building.type.title}",
                        tint = if (canDemolish) CoralCrimson else LichenMuted.copy(alpha = 0.35f)
                    )
                }
            }
            if (!building.isCompleted) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = AmberGold,
                    trackColor = PanelElevated,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
