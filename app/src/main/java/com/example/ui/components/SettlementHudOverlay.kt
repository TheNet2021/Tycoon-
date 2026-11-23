package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SettlementState
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CreamText
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelDark

@Composable
fun SettlementHudOverlay(
    settlement: SettlementState,
    onOpenSettlement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = remember(settlement) { settlement.computeStats() }
    val pendingArrivals = settlement.pendingArrivals.size

    val foodRatio = if (stats.foodCapacity > 0) stats.foodStock.toFloat() / stats.foodCapacity else 1f
    val waterRatio = if (stats.waterCapacity > 0) stats.waterStock.toFloat() / stats.waterCapacity else 1f

    val foodColor = when {
        foodRatio < 0.15f -> CoralCrimson
        foodRatio < 0.35f -> AmberGold
        else -> EmeraldCanopy
    }
    val waterColor = when {
        waterRatio < 0.15f -> CoralCrimson
        waterRatio < 0.35f -> AmberGold
        else -> CrystalCyan
    }
    val happinessColor = when {
        stats.averageHappiness >= 70 -> EmeraldCanopy
        stats.averageHappiness >= 45 -> AmberGold
        else -> CoralCrimson
    }
    val happinessIcon = when {
        stats.averageHappiness >= 70 -> Icons.Filled.SentimentSatisfied
        stats.averageHappiness >= 45 -> Icons.Filled.SentimentNeutral
        else -> Icons.Filled.SentimentDissatisfied
    }
    val popColor = when {
        stats.totalHousingCapacity > 0 && stats.population >= stats.totalHousingCapacity -> AmberGold
        stats.population > 0 -> EmeraldCanopy
        else -> CrystalCyan
    }

    val borderColor = when {
        foodRatio < 0.15f || waterRatio < 0.15f || stats.averageHappiness < 45 -> CoralCrimson.copy(alpha = 0.72f)
        pendingArrivals > 0 -> AmberGold.copy(alpha = 0.65f)
        else -> PanelBorder.copy(alpha = 0.72f)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PanelDark.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpenSettlement)
            .minimumInteractiveComponentSize()
            .testTag("hud_settlement_overlay")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Population & Beds
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("hud_settlement_population")
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = stringResource(R.string.cd_open_settlement),
                    tint = popColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stats.population}/${stats.totalHousingCapacity}",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                    color = CreamText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                if (pendingArrivals > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Badge(containerColor = CoralCrimson, contentColor = CreamText) {
                        Text("+$pendingArrivals")
                    }
                }
            }

            SettlementMetricChip(
                icon = Icons.Filled.Restaurant,
                value = "${stats.foodStock}/${stats.foodCapacity}",
                tint = foodColor,
                testTag = "hud_settlement_food"
            )

            SettlementMetricChip(
                icon = Icons.Filled.WaterDrop,
                value = "${stats.waterStock}/${stats.waterCapacity}",
                tint = waterColor,
                testTag = "hud_settlement_water"
            )

            SettlementMetricChip(
                icon = happinessIcon,
                value = "${stats.averageHappiness}%",
                tint = happinessColor,
                testTag = "hud_settlement_happiness"
            )
        }
    }
}

@Composable
private fun SettlementMetricChip(
    icon: ImageVector,
    value: String,
    tint: Color,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
            color = tint,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}
