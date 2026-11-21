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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ResourceType
import com.example.data.TycoonSaveState
import com.example.data.UpgradeType
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
import com.example.ui.theme.PanelElevated

@Composable
fun BackpackSheet(
    state: TycoonSaveState,
    onDismiss: () -> Unit,
    onUpgradeBackpack: () -> Unit
) {
    val bagUsed = state.backpackUsed
    val bagMax = state.backpackMaxCapacity.coerceAtLeast(1)
    val fillFrac = (bagUsed.toFloat() / bagMax).coerceIn(0f, 1f)
    val totalValue = ResourceType.entries.sumOf { res ->
        val qty = state.inventory[res.id] ?: 0
        qty.toLong() * state.effectiveSellValue(res)
    }

    val bagLvl = state.backpackLevel
    val bagIsMax = bagLvl >= UpgradeType.BACKPACK.maxLevel
    val bagUpgradeCost = UpgradeType.BACKPACK.costForNextLevel(bagLvl)
    val bagReqMet = state.meetsResourceRequirement(UpgradeType.BACKPACK.resourceRequirementForNext(bagLvl))
    val canUpgradeBag = !bagIsMax && bagReqMet && state.coins >= bagUpgradeCost

    FrontierSheetScaffold(
        title = "Cargo Backpack",
        subtitle = "Walk into the Trading Post to auto-sell cargo",
        headerIcon = Icons.Filled.Backpack,
        accentColor = AmberGold,
        coins = state.coins,
        closeTestTag = "btn_close_backpack",
        onDismiss = onDismiss
    ) {
        // Capacity & Value Summary Card
        FrontierGlassCard(
            accentBorder = if (state.isBackpackFull) CoralCrimson else AmberGold,
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
                            text = "${UpgradeType.BACKPACK.tierName(bagLvl)} (Lv.$bagLvl/${UpgradeType.BACKPACK.maxLevel})",
                            style = MaterialTheme.typography.titleMedium,
                            color = CreamText
                        )
                        Text(
                            text = "Cargo Value: ${formatCompactCurrency(totalValue)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = AmberBright
                        )
                    }
                    FrontierActionButton(
                        text = when {
                            bagIsMax -> "MAX"
                            !bagReqMet -> "Locked"
                            else -> "+Slots ${formatCompactCurrency(bagUpgradeCost)}"
                        },
                        enabled = canUpgradeBag,
                        accentColor = AmberGold,
                        isCompletedState = bagIsMax,
                        testTag = "btn_bag_quick_upgrade",
                        onClick = onUpgradeBackpack
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Capacity Used",
                        style = MaterialTheme.typography.labelSmall,
                        color = LichenMuted
                    )
                    Text(
                        text = "$bagUsed / $bagMax",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.isBackpackFull) CoralCrimson else EmeraldCanopy,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { fillFrac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape),
                    color = if (state.isBackpackFull) CoralCrimson else EmeraldCanopy,
                    trackColor = PanelElevated,
                    strokeCap = StrokeCap.Round
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ResourceType.entries) { res ->
                val held = state.inventory[res.id] ?: 0
                val totalCollected = state.gatheredTotals[res.id] ?: 0
                val unitPrice = state.effectiveSellValue(res)
                val resColor = Color(res.colorHex)

                FrontierGlassCard(
                    accentBorder = if (held > 0) resColor else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = resColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, resColor.copy(alpha = 0.6f)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(resColor)
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
                                    text = res.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CreamText
                                )
                                FrontierBadgeChip(
                                    text = "$$unitPrice each",
                                    accent = AmberBright,
                                    icon = Icons.Filled.Storefront
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lifetime gathered: $totalCollected",
                                style = MaterialTheme.typography.labelSmall,
                                color = LichenMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "x$held",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (held > 0) CreamText else LichenMuted,
                                fontWeight = FontWeight.Bold
                            )
                            if (held > 0) {
                                Text(
                                    text = formatCompactCurrency(held.toLong() * unitPrice),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldCanopy
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
