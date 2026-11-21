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
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSheetScaffold
import com.example.ui.components.formatCompactCurrency
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CreamText
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.ObsidianForest
import com.example.ui.theme.PanelElevated

@Composable
fun BiomeMapSheet(
    state: TycoonSaveState,
    activeBiome: BiomeType,
    onDismiss: () -> Unit,
    onUnlockBiome: (BiomeType) -> Unit,
    onTeleportBiome: (BiomeType) -> Unit
) {
    FrontierSheetScaffold(
        title = "World Regions",
        subtitle = "Higher frontiers require stronger equipment tiers",
        headerIcon = Icons.Filled.Explore,
        accentColor = EmeraldCanopy,
        coins = state.coins,
        closeTestTag = "btn_close_biomes",
        onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(BiomeType.entries, key = { it.id }) { biome ->
                val unlocked = state.isBiomeUnlocked(biome)
                val isCurrent = biome == activeBiome
                val gearReady = biome.meetsEquipmentRequirements(state)
                val canAfford = gearReady && state.coins >= biome.unlockCost
                val accent = Color(biome.accentHex)
                val reqLabel = biome.requirementSummary()

                FrontierGlassCard(
                    accentBorder = if (unlocked) accent else null
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (unlocked) accent.copy(alpha = 0.2f) else PanelElevated,
                            border = BorderStroke(
                                1.2.dp,
                                if (unlocked) accent.copy(alpha = 0.6f) else LichenMuted.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (unlocked) Icons.Filled.Terrain else Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = if (unlocked) accent else LichenMuted,
                                    modifier = Modifier.size(26.dp)
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
                                    text = biome.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (unlocked) CreamText else LichenMuted
                                )
                                if (isCurrent) {
                                    FrontierBadgeChip(
                                        text = "Here",
                                        accent = EmeraldCanopy
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = biome.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LichenMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FrontierBadgeChip(
                                    text = biome.resourcesSummary,
                                    accent = accent
                                )
                            }
                            if (!unlocked && reqLabel != null) {
                                Spacer(modifier = Modifier.height(6.dp))
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
                                    containerColor = EmeraldCanopy,
                                    contentColor = ObsidianForest
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
                                    text = "Go",
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
