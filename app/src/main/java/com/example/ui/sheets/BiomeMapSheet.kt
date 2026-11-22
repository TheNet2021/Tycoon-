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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BiomeType
import com.example.data.TycoonSaveState
import com.example.ui.components.FrontierActionButton
import com.example.ui.components.FrontierBadgeChip
import com.example.ui.components.FrontierEmptyStateCard
import com.example.ui.components.FrontierFilterStrip
import com.example.ui.components.FrontierFilterTab
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSheetScaffold
import com.example.ui.components.FrontierStatTile
import com.example.ui.components.formatCompactCurrency
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CreamText
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.ObsidianForest
import com.example.ui.theme.PanelElevated

private enum class BiomeFilter(val label: String) {
    ALL("All (12)"),
    UNLOCKED("Unlocked"),
    LOCKED("Locked")
}

@Composable
fun BiomeMapSheet(
    state: TycoonSaveState,
    activeBiome: BiomeType,
    onDismiss: () -> Unit,
    onUnlockBiome: (BiomeType) -> Unit,
    onTeleportBiome: (BiomeType) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(BiomeFilter.ALL) }

    val visibleBiomes = remember(state.unlockedBiomes, selectedFilter) {
        when (selectedFilter) {
            BiomeFilter.ALL -> BiomeType.entries
            BiomeFilter.UNLOCKED -> BiomeType.entries.filter { state.isBiomeUnlocked(it) }
            BiomeFilter.LOCKED -> BiomeType.entries.filter { !state.isBiomeUnlocked(it) }
        }
    }

    FrontierSheetScaffold(
        title = "World Regions",
        subtitle = "Explore 12 unique biomes, caves & rare mineral deposits",
        headerIcon = Icons.Filled.Explore,
        accentColor = EmeraldCanopy,
        coins = state.coins,
        closeTestTag = "btn_close_biomes",
        onDismiss = onDismiss
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrontierStatTile(
                label = "Regions",
                value = "${state.unlockedBiomes.size}/${BiomeType.entries.size}",
                accent = EmeraldCanopy,
                modifier = Modifier.weight(1f)
            )
            FrontierStatTile(
                label = "Caves",
                value = "${state.discoveredSecrets.size}/${BiomeType.entries.size}",
                accent = CrystalCyan,
                modifier = Modifier.weight(1f)
            )
            FrontierStatTile(
                label = "Bosses",
                value = "${state.defeatedBosses.size}/4",
                accent = AmberBright,
                modifier = Modifier.weight(1f)
            )
        }

        val filters = BiomeFilter.entries
        FrontierFilterStrip(
            filters = filters.map { filter ->
                val label = when (filter) {
                    BiomeFilter.ALL -> "All (${BiomeType.entries.size})"
                    BiomeFilter.UNLOCKED -> "Unlocked (${state.unlockedBiomes.size})"
                    BiomeFilter.LOCKED -> "Locked (${BiomeType.entries.size - state.unlockedBiomes.size})"
                }
                FrontierFilterTab(
                    label = label,
                    accent = when (filter) {
                        BiomeFilter.ALL -> EmeraldCanopy
                        BiomeFilter.UNLOCKED -> CrystalCyan
                        BiomeFilter.LOCKED -> AmberGold
                    },
                    testTag = "filter_biome_${filter.name.lowercase()}"
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
            if (visibleBiomes.isEmpty()) {
                item(key = "biomes_empty") {
                    FrontierEmptyStateCard(
                        icon = Icons.Filled.Explore,
                        title = if (selectedFilter == BiomeFilter.LOCKED) {
                            "All 12 Regions Unlocked!"
                        } else {
                            "No Regions Found"
                        },
                        subtitle = if (selectedFilter == BiomeFilter.LOCKED) {
                            "You have conquered every corner of the frontier."
                        } else {
                            "Select another filter to view expedition areas."
                        },
                        accent = EmeraldCanopy
                    )
                }
            } else {
                items(visibleBiomes, key = { it.id }) { biome ->
                    val unlocked = state.isBiomeUnlocked(biome)
                    val isCurrent = biome == activeBiome
                    val gearReady = biome.meetsEquipmentRequirements(state)
                    val canAfford = gearReady && state.coins >= biome.unlockCost
                    val accent = Color(biome.accentHex)
                    val reqLabel = biome.requirementSummary()

                    FrontierGlassCard(
                        accentBorder = when {
                            isCurrent -> EmeraldCanopy
                            unlocked -> accent
                            else -> null
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(15.dp),
                                color = if (unlocked) accent.copy(alpha = 0.2f) else PanelElevated,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (unlocked) accent.copy(alpha = 0.6f) else LichenMuted.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (unlocked) Icons.Filled.Terrain else Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = if (unlocked) accent else LichenMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(11.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = biome.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (unlocked) CreamText else LichenMuted
                                    )
                                    if (isCurrent) {
                                        FrontierBadgeChip(
                                            text = "Active",
                                            accent = EmeraldCanopy
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = biome.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LichenMuted
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                FrontierBadgeChip(
                                    text = biome.resourcesSummary,
                                    accent = accent
                                )
                                if (!unlocked && reqLabel != null) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    FrontierBadgeChip(
                                        text = reqLabel,
                                        accent = if (gearReady) EmeraldCanopy else AmberBright,
                                        icon = if (gearReady) Icons.Filled.CheckCircle else Icons.Filled.Build
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            if (unlocked) {
                                Button(
                                    onClick = {
                                        onTeleportBiome(biome)
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCurrent) PanelElevated else EmeraldCanopy,
                                        contentColor = if (isCurrent) EmeraldCanopy else ObsidianForest
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("btn_travel_${biome.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FlightTakeoff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isCurrent) "Visit" else "Go",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                FrontierActionButton(
                                    text = if (gearReady) formatCompactCurrency(biome.unlockCost) else "Upgrade Gear",
                                    enabled = canAfford,
                                    accentColor = if (gearReady) AmberGold else CoralCrimson,
                                    testTag = "btn_unlock_${biome.id}",
                                    onClick = { onUnlockBiome(biome) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
