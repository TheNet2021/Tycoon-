package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BiomeType
import com.example.data.DrivableVehicleType
import com.example.data.FleetEquipType
import com.example.data.MachineType
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
import com.example.ui.components.FrontierTabSpec
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
import com.example.ui.theme.PanelElevated
import java.util.Locale

@Composable
fun MachinerySheet(
    state: TycoonSaveState,
    onDismiss: () -> Unit,
    onBuyOrUpgradeMachine: (MachineType) -> Unit,
    onToggleMachineActive: (MachineType) -> Unit,
    onBuyOrUpgradeFleet: (FleetEquipType) -> Unit,
    onBuyOrSelectVehicle: (DrivableVehicleType) -> Unit,
    onDepositBackpack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var machineFilterIdx by remember { mutableIntStateOf(0) }

    val activeMachines = MachineType.entries.count { state.isMachineRunning(it) }
    val installedMachines = MachineType.entries.count { state.machineLevel(it) > 0 }
    val readyMachines = MachineType.entries.count { state.machineLevel(it) == 0 && state.isMachineUnlocked(it) }
    val lockedMachines = MachineType.entries.count { !state.isMachineUnlocked(it) }
    val fleetOwned = FleetEquipType.entries.count { state.fleetLevel(it) > 0 } +
        DrivableVehicleType.entries.count { state.isVehicleUnlocked(it) }
    val totalFleetCount = FleetEquipType.entries.size + DrivableVehicleType.entries.size

    val stockFraction = (state.warehouseStockUsed.toFloat() / state.warehouseStockCapacity.coerceAtLeast(1))
        .coerceIn(0f, 1f)
    val stockEntries = remember(state.warehouseStock) {
        state.warehouseStock.entries
            .filter { it.value > 0 }
            .mapNotNull { (id, qty) -> TradeCatalog.find(id)?.let { it to qty } }
            .sortedByDescending { it.second }
    }

    val filteredMachines = remember(state, machineFilterIdx) {
        when (machineFilterIdx) {
            1 -> MachineType.entries.filter { state.isMachineRunning(it) }
            2 -> MachineType.entries.filter { state.machineLevel(it) == 0 && state.isMachineUnlocked(it) }
            3 -> MachineType.entries.filter { !state.isMachineUnlocked(it) }
            else -> MachineType.entries
        }
    }

    FrontierSheetScaffold(
        title = "Industrial Machinery",
        subtitle = "Automate processing, power lines & heavy fleet",
        headerIcon = Icons.Filled.PrecisionManufacturing,
        accentColor = CrystalCyan,
        coins = state.coins,
        passivePerSec = state.passiveIncomePerSecond,
        closeTestTag = "btn_close_machinery",
        onDismiss = onDismiss
    ) {
        // Warehouse Stockpile + Backpack Transfer Card
        FrontierGlassCard(
            accentBorder = CrystalCyan,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(11.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Warehouse,
                                contentDescription = null,
                                tint = CrystalCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Warehouse ${state.warehouseStockUsed} / ${state.warehouseStockCapacity}",
                                style = MaterialTheme.typography.titleSmall,
                                color = CreamText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { stockFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = if (stockFraction > 0.9f) CoralCrimson else CrystalCyan,
                            trackColor = PanelElevated,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FrontierBadgeChip(
                                text = "$activeMachines/$installedMachines Active",
                                accent = EmeraldCanopy
                            )
                            FrontierBadgeChip(
                                text = "-$${state.totalMachinePowerCostPerMinute}/m",
                                accent = AmberGold,
                                icon = Icons.Filled.Bolt
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    val freeSpace = (state.warehouseStockCapacity - state.warehouseStockUsed).coerceAtLeast(0)
                    val canDeposit = state.backpackUsed > 0 && freeSpace > 0
                    FrontierActionButton(
                        text = "Deposit (${state.backpackUsed})",
                        enabled = canDeposit,
                        accentColor = EmeraldCanopy,
                        testTag = "btn_deposit_warehouse",
                        onClick = onDepositBackpack
                    )
                }

                if (stockEntries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(end = 4.dp)
                    ) {
                        items(stockEntries, key = { it.first.id }) { (good, qty) ->
                            val c = Color(good.colorHex)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PanelElevated.copy(alpha = 0.78f),
                                border = BorderStroke(1.dp, c.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${good.displayName} x$qty",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CreamText,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Unified Tab Switcher
        FrontierSegmentedTabs(
            tabs = listOf(
                FrontierTabSpec(
                    label = "Machinery (${MachineType.entries.size})",
                    accent = CrystalCyan,
                    testTag = "tab_machinery_units",
                    icon = Icons.Filled.PrecisionManufacturing
                ),
                FrontierTabSpec(
                    label = "Fleet & Vehicles ($fleetOwned/$totalFleetCount)",
                    accent = AmberGold,
                    testTag = "tab_machinery_fleet",
                    icon = Icons.Filled.LocalShipping
                )
            ),
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it }
        )

        if (selectedTab == 0) {
            FrontierFilterStrip(
                filters = listOf(
                    FrontierFilterTab("All (${MachineType.entries.size})", CrystalCyan, "filter_machine_all"),
                    FrontierFilterTab("Running ($activeMachines)", EmeraldCanopy, "filter_machine_running"),
                    FrontierFilterTab("Ready ($readyMachines)", AmberGold, "filter_machine_ready"),
                    FrontierFilterTab("Locked ($lockedMachines)", LichenMuted, "filter_machine_locked")
                ),
                selectedIndex = machineFilterIdx,
                onSelect = { machineFilterIdx = it }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredMachines.isEmpty()) {
                    item {
                        FrontierEmptyStateCard(
                            icon = Icons.Filled.PrecisionManufacturing,
                            title = "No machines in this category",
                            subtitle = "Switch tabs or unlock new regions to install more machines.",
                            accent = CrystalCyan
                        )
                    }
                } else {
                    items(filteredMachines, key = { it.id }) { machine ->
                        MachineCard(
                            machine = machine,
                            state = state,
                            onBuyOrUpgrade = { onBuyOrUpgradeMachine(machine) },
                            onToggleActive = { onToggleMachineActive(machine) }
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "hdr_drivable_vehicles") {
                    FrontierSectionHeader(
                        title = "Drivable Mining Vehicles",
                        trailingBadge = "${DrivableVehicleType.entries.count { state.isVehicleUnlocked(it) }}/${DrivableVehicleType.entries.size}",
                        accent = CrystalCyan
                    )
                }
                items(DrivableVehicleType.entries, key = { "veh_${it.id}" }) { vehicle ->
                    DrivableVehicleCard(
                        vehicle = vehicle,
                        state = state,
                        onAction = { onBuyOrSelectVehicle(vehicle) }
                    )
                }
                item(key = "hdr_industrial_fleet") {
                    FrontierSectionHeader(
                        title = "Industrial Fleet & Gear",
                        trailingBadge = "${FleetEquipType.entries.count { state.fleetLevel(it) > 0 }}/${FleetEquipType.entries.size}",
                        accent = AmberGold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                items(FleetEquipType.entries, key = { it.id }) { fleet ->
                    FleetCard(
                        fleet = fleet,
                        state = state,
                        onBuyOrUpgrade = { onBuyOrUpgradeFleet(fleet) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MachineCard(
    machine: MachineType,
    state: TycoonSaveState,
    onBuyOrUpgrade: () -> Unit,
    onToggleActive: () -> Unit
) {
    val lvl = state.machineLevel(machine)
    val running = state.isMachineRunning(machine)
    val unlocked = state.isMachineUnlocked(machine)
    val isMax = lvl >= machine.maxLevel
    val cost = machine.costForNextLevel(lvl)
    val canAfford = unlocked && !isMax && state.coins >= cost
    val accent = Color(machine.accentHex)
    val biomeTitle = BiomeType.fromId(machine.requiredBiomeId)?.title ?: machine.requiredBiomeId
    val outGood = TradeCatalog.find(machine.outputId)
    val inGood = machine.inputId?.let { TradeCatalog.find(it) }
    val upm = machine.unitsPerMinute(lvl.coerceAtLeast(1), state.conveyorLevel)
    val speedLabel = String.format(Locale.US, "%.1f u/min", upm)

    FrontierGlassCard(accentBorder = if (lvl > 0) accent else null) {
        Column(modifier = Modifier.padding(13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (unlocked) accent.copy(alpha = 0.16f) else PanelElevated,
                    border = BorderStroke(
                        1.dp,
                        if (unlocked) accent.copy(alpha = 0.55f) else LichenMuted.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (unlocked) Icons.Filled.PrecisionManufacturing else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (unlocked) accent else LichenMuted,
                            modifier = Modifier.size(21.dp)
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
                            text = machine.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (unlocked) CreamText else LichenMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (lvl > 0) {
                            FrontierBadgeChip(
                                text = "T$lvl/${machine.maxLevel}",
                                accent = accent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = machine.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = LichenMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (lvl > 0) {
                    Switch(
                        checked = running,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianForest,
                            checkedTrackColor = EmeraldCanopy,
                            uncheckedThumbColor = LichenMuted,
                            uncheckedTrackColor = PanelElevated
                        ),
                        modifier = Modifier.testTag("switch_machine_${machine.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Processing Flow & Telemetry Row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PanelElevated.copy(alpha = 0.58f),
                border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoveToInbox,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = inGood?.displayName ?: "Auto-Extract",
                            style = MaterialTheme.typography.labelSmall,
                            color = CreamText,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = LichenMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = outGood?.displayName ?: machine.outputId,
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberBright,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "$speedLabel • -$${machine.powerCostPerMinute * lvl.coerceAtLeast(1)}/m",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrystalCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (!unlocked) {
                        Text(
                            text = "Requires Lv.${machine.requiredPlayerLevel} & $biomeTitle",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralCrimson
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    TierPipsBar(
                        current = lvl,
                        max = machine.maxLevel,
                        activeColor = accent
                    )
                }

                val btnLabel = when {
                    !unlocked -> "Locked"
                    isMax -> "MAXED"
                    lvl == 0 -> "Install ${formatCompactCurrency(cost)}"
                    else -> "Upgrade ${formatCompactCurrency(cost)}"
                }
                FrontierActionButton(
                    text = btnLabel,
                    enabled = canAfford,
                    accentColor = if (lvl == 0) EmeraldCanopy else accent,
                    isCompletedState = isMax,
                    testTag = "btn_machine_${machine.id}",
                    onClick = onBuyOrUpgrade
                )
            }
        }
    }
}

@Composable
private fun FleetCard(
    fleet: FleetEquipType,
    state: TycoonSaveState,
    onBuyOrUpgrade: () -> Unit
) {
    val lvl = state.fleetLevel(fleet)
    val isMax = lvl >= fleet.maxLevel
    val cost = fleet.costForNextLevel(lvl)
    val canAfford = !isMax && state.coins >= cost
    val accent = Color(fleet.accentHex)

    FrontierGlassCard(accentBorder = if (lvl > 0) accent else null) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accent.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.55f)),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (fleet == FleetEquipType.CHAINSAW_DRILL) {
                            Icons.Filled.Construction
                        } else {
                            Icons.Filled.LocalShipping
                        },
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
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
                        text = fleet.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CreamText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (lvl > 0) {
                        FrontierBadgeChip(
                            text = "Lv.$lvl/${fleet.maxLevel}",
                            accent = accent
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = fleet.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LichenMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                TierPipsBar(
                    current = lvl,
                    max = fleet.maxLevel,
                    activeColor = accent
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val label = when {
                isMax -> "MAX"
                lvl == 0 -> "Buy ${formatCompactCurrency(cost)}"
                else -> "Up ${formatCompactCurrency(cost)}"
            }
            FrontierActionButton(
                text = label,
                enabled = canAfford,
                accentColor = if (lvl == 0) EmeraldCanopy else accent,
                isCompletedState = isMax,
                testTag = "btn_fleet_${fleet.id}",
                onClick = onBuyOrUpgrade
            )
        }
    }
}

@Composable
private fun DrivableVehicleCard(
    vehicle: DrivableVehicleType,
    state: TycoonSaveState,
    onAction: () -> Unit
) {
    val owned = state.isVehicleUnlocked(vehicle)
    val isDriving = state.activeVehicleId == vehicle.id
    val biomeUnlocked = state.unlockedBiomes.contains(vehicle.requiredBiomeId)
    val biomeTitle = BiomeType.fromId(vehicle.requiredBiomeId)?.title ?: vehicle.requiredBiomeId
    val canAfford = !owned && biomeUnlocked && state.coins >= vehicle.cost
    val accent = Color(vehicle.accentHex)

    FrontierGlassCard(accentBorder = if (isDriving) EmeraldCanopy else if (owned) accent else null) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (biomeUnlocked || owned) accent.copy(alpha = 0.16f) else PanelElevated,
                border = BorderStroke(
                    1.dp,
                    if (isDriving) EmeraldCanopy else if (biomeUnlocked || owned) accent.copy(alpha = 0.55f) else PanelBorder
                ),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (owned || biomeUnlocked) Icons.Filled.DirectionsCar else Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (isDriving) EmeraldCanopy else if (owned || biomeUnlocked) accent else LichenMuted,
                        modifier = Modifier.size(22.dp)
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
                        text = vehicle.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (owned || biomeUnlocked) CreamText else LichenMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isDriving) {
                        FrontierBadgeChip(text = "ACTIVE", accent = EmeraldCanopy)
                    } else if (owned) {
                        FrontierBadgeChip(text = "OWNED", accent = accent)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = vehicle.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LichenMuted
                )
                if (!owned && !biomeUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Requires $biomeTitle",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoralCrimson
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            val btnLabel = when {
                isDriving -> "Exit"
                owned -> "Drive"
                !biomeUnlocked -> "Locked"
                else -> "Buy ${formatCompactCurrency(vehicle.cost)}"
            }
            FrontierActionButton(
                text = btnLabel,
                enabled = owned || canAfford,
                accentColor = if (isDriving) CoralCrimson else if (owned) EmeraldCanopy else accent,
                testTag = "btn_vehicle_${vehicle.id}",
                onClick = onAction
            )
        }
    }
}
