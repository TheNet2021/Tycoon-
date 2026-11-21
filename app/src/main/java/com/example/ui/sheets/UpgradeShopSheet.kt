package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BiomeType
import com.example.data.BuildingType
import com.example.data.TycoonSaveState
import com.example.data.UpgradeType
import com.example.ui.components.formatCompactCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeShopSheet(
    state: TycoonSaveState,
    onDismiss: () -> Unit,
    onPurchaseUpgrade: (UpgradeType) -> Unit,
    onBuildOrUpgradeBuilding: (BuildingType) -> Unit,
    onToggleHaptics: () -> Unit,
    onResetProgress: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    var showResetConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Frontier Outfitter",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Balance: ${formatCompactCurrency(state.coins)} • +$${state.passiveIncomePerSecond.toInt()}/s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("btn_close_shop")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close_sheet)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("tab_tools_gear")
                ) {
                    Text("Tools & Gear")
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("tab_factories_bots")
                ) {
                    Text("Factories & Bots")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) {
                    items(UpgradeType.entries) { upgrade ->
                        val lvl = state.upgradeLevel(upgrade)
                        val isMax = lvl >= upgrade.maxLevel
                        val cost = upgrade.costForNextLevel(lvl)
                        val canAfford = !isMax && state.coins >= cost
                        UpgradeCard(
                            icon = upgradeIcon(upgrade),
                            title = upgrade.title,
                            subtitle = "${upgrade.tierName(lvl)} (Lv.$lvl/${upgrade.maxLevel}) • ${upgrade.subtitle}",
                            buttonText = if (isMax) "MAX" else formatCompactCurrency(cost),
                            enabled = canAfford,
                            isMax = isMax,
                            testTag = "btn_upgrade_${upgrade.id}",
                            onClick = { onPurchaseUpgrade(upgrade) }
                        )
                    }
                } else {
                    items(BuildingType.entries) { building ->
                        val lvl = state.buildingLevel(building)
                        val biome = BiomeType.fromId(building.biomeId)
                        val biomeUnlocked = state.unlockedBiomes.contains(building.biomeId)
                        val isMax = lvl >= building.maxLevel
                        val cost = building.costForNextLevel(lvl)
                        val canAfford = biomeUnlocked && !isMax && state.coins >= cost
                        val actionLabel = when {
                            !biomeUnlocked -> "Unlock ${biome?.title ?: "Region"}"
                            isMax -> "MAX"
                            lvl == 0 -> "Build ${formatCompactCurrency(cost)}"
                            else -> "Up ${formatCompactCurrency(cost)}"
                        }
                        UpgradeCard(
                            icon = buildingIcon(building),
                            title = if (lvl > 0) "${building.title} (Lv.$lvl)" else building.title,
                            subtitle = "${biome?.title ?: ""} • ${building.description}",
                            buttonText = actionLabel,
                            enabled = canAfford,
                            isMax = isMax,
                            testTag = "btn_building_${building.id}",
                            onClick = { onBuildOrUpgradeBuilding(building) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Vibration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Haptic Feedback",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Switch(
                            checked = state.hapticsEnabled,
                            onCheckedChange = { onToggleHaptics() },
                            modifier = Modifier.testTag("switch_haptics")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("btn_reset_save")
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Frontier Progress")
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Frontier Progress?") },
            text = { Text("This clears your coins, tools, factories, and unlocked regions so you can start a fresh tycoon run.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirm = false
                        onResetProgress()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("btn_confirm_reset")
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun UpgradeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    enabled: Boolean,
    isMax: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMax) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag(testTag)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun upgradeIcon(type: UpgradeType): ImageVector = when (type) {
    UpgradeType.AXE -> Icons.Filled.Build
    UpgradeType.PICKAXE -> Icons.Filled.Hardware
    UpgradeType.BACKPACK -> Icons.Filled.Backpack
    UpgradeType.MOVE_SPEED -> Icons.Filled.DirectionsRun
    UpgradeType.GATHER_SPEED -> Icons.Filled.Bolt
}

private fun buildingIcon(type: BuildingType): ImageVector = when (type) {
    BuildingType.SAWMILL, BuildingType.SMELTER, BuildingType.WAREHOUSE -> Icons.Filled.Factory
    else -> Icons.Filled.PrecisionManufacturing
}
