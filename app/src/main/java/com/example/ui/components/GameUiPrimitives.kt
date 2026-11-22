package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CreamText
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.ObsidianForest
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PanelDark
import com.example.ui.theme.PanelElevated
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun AnimatedCurrencyText(
    coins: Long,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = CreamText
) {
    val animated by animateFloatAsState(
        targetValue = coins.toFloat(),
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "animatedCoins"
    )
    val display = if (coins == 0L) 0L else animated.roundToLong().coerceAtLeast(0L)
    Text(
        text = formatCompactCurrency(display),
        style = style,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrontierSheetScaffold(
    title: String,
    subtitle: String,
    headerIcon: ImageVector,
    accentColor: Color = AmberGold,
    coins: Long? = null,
    passivePerSec: Double? = null,
    heightFraction: Float = 0.87f,
    closeTestTag: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PanelDark.copy(alpha = 0.98f),
        contentColor = CreamText,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(PanelBorder.copy(alpha = 0.85f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(heightFraction)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Unified Commercial Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        color = accentColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.55f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = headerIcon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = CreamText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = LichenMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (coins != null) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = PanelCard,
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.42f)),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MonetizationOn,
                                contentDescription = null,
                                tint = AmberBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                AnimatedCurrencyText(
                                    coins = coins,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CreamText
                                )
                                if (passivePerSec != null && passivePerSec > 0) {
                                    Text(
                                        text = "+$${passivePerSec.toInt()}/s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldCanopy
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = PanelCard,
                    border = BorderStroke(1.dp, PanelBorder)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .minimumInteractiveComponentSize()
                            .testTag(closeTestTag)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_close_sheet),
                            tint = LichenMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = PanelBorder.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            content()
        }
    }
}

data class FrontierTabSpec(
    val label: String,
    val accent: Color,
    val testTag: String,
    val icon: ImageVector? = null
)

@Composable
fun FrontierSegmentedTabs(
    tabs: List<FrontierTabSpec>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PanelCard.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, PanelBorder.copy(alpha = 0.65f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { idx, tab ->
                val selected = idx == selectedIndex
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) tab.accent.copy(alpha = 0.22f) else Color.Transparent,
                    border = if (selected) BorderStroke(1.dp, tab.accent.copy(alpha = 0.75f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(idx) }
                        .minimumInteractiveComponentSize()
                        .testTag(tab.testTag)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tab.icon != null) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (selected) tab.accent else LichenMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) tab.accent else LichenMuted,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

data class FrontierFilterTab(
    val label: String,
    val accent: Color = AmberGold,
    val testTag: String
)

@Composable
fun FrontierFilterStrip(
    filters: List<FrontierFilterTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(filters) { idx, filter ->
            val selected = idx == selectedIndex
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selected) filter.accent else PanelCard,
                border = BorderStroke(
                    1.dp,
                    if (selected) filter.accent else PanelBorder.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(idx) }
                    .testTag(filter.testTag)
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) ObsidianForest else LichenMuted,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun FrontierSectionHeader(
    title: String,
    trailingBadge: String? = null,
    accent: Color = AmberGold,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.Bold
        )
        if (trailingBadge != null) {
            FrontierBadgeChip(text = trailingBadge, accent = accent)
        }
    }
}

@Composable
fun FrontierEmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color = LichenMuted,
    modifier: Modifier = Modifier
) {
    FrontierGlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = CreamText,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LichenMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FrontierStatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PanelElevated.copy(alpha = 0.62f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.32f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = LichenMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FrontierGlassCard(
    modifier: Modifier = Modifier,
    accentBorder: Color? = null,
    containerColor: Color = PanelCard,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            (accentBorder ?: PanelBorder).copy(alpha = 0.72f),
            PanelBorder.copy(alpha = 0.35f)
        )
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor.copy(alpha = 0.94f),
        border = BorderStroke(1.1.dp, borderBrush),
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun TierPipsBar(
    current: Int,
    max: Int,
    activeColor: Color = AmberGold,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..max) {
            val filled = i <= current
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .width(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) activeColor else PanelElevated
                    )
            )
        }
    }
}

@Composable
fun FrontierBadgeChip(
    text: String,
    accent: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.36f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FrontierActionButton(
    text: String,
    enabled: Boolean,
    accentColor: Color = AmberGold,
    isCompletedState: Boolean = false,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = when {
        isCompletedState -> EmeraldCanopy.copy(alpha = 0.22f)
        enabled -> accentColor
        else -> PanelElevated
    }
    val fg = when {
        isCompletedState -> EmeraldCanopy
        enabled -> ObsidianForest
        else -> LichenMuted
    }
    Button(
        onClick = onClick,
        enabled = enabled && !isCompletedState,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = bg,
            disabledContentColor = fg
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

fun formatCompactCurrency(amount: Long): String {
    return when {
        amount >= 1_000_000L -> String.format(Locale.US, "$%.2fM", amount / 1_000_000.0)
        amount >= 100_000L -> String.format(Locale.US, "$%.1fK", amount / 1_000.0)
        else -> "$" + NumberFormat.getNumberInstance(Locale.US).format(amount)
    }
}
