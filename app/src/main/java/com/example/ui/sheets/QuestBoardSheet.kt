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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.QuestCategory
import com.example.data.QuestProgress
import com.example.data.TycoonSaveState
import com.example.data.UniqueQuestItem
import com.example.ui.components.FrontierActionButton
import com.example.ui.components.FrontierBadgeChip
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSheetScaffold
import com.example.ui.components.formatCompactCurrency
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CreamText
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.ObsidianForest
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PanelElevated

private enum class QuestFilter(val label: String) {
    ALL("All"),
    READY("Ready"),
    ACTIVE("In Progress"),
    CLAIMED("Completed")
}

@Composable
fun QuestBoardSheet(
    state: TycoonSaveState,
    onClaimQuest: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(QuestFilter.ALL) }
    val allQuests = remember(state) { state.evaluateQuests() }
    val filteredQuests = remember(allQuests, selectedFilter) {
        when (selectedFilter) {
            QuestFilter.ALL -> allQuests.sortedBy { if (it.isReadyToClaim) 0 else if (!it.isClaimed) 1 else 2 }
            QuestFilter.READY -> allQuests.filter { it.isReadyToClaim }
            QuestFilter.ACTIVE -> allQuests.filter { !it.isCompleted && !it.isClaimed }
            QuestFilter.CLAIMED -> allQuests.filter { it.isClaimed }
        }
    }
    val completedCount = remember(allQuests) { allQuests.count { it.isClaimed } }

    FrontierSheetScaffold(
        title = "Quests & Achievements",
        subtitle = "$completedCount of ${allQuests.size} milestones completed",
        headerIcon = Icons.Filled.EmojiEvents,
        accentColor = AmberBright,
        coins = state.coins,
        closeTestTag = "btn_close_quests",
        onDismiss = onDismiss
    ) {
        // Pioneer Level & XP Progress Card
        FrontierGlassCard(
            accentBorder = CrystalCyan,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(13.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pioneer Level ${state.playerLevel}",
                        style = MaterialTheme.typography.titleMedium,
                        color = CreamText
                    )
                    Text(
                        text = "${state.xpIntoCurrentLevel} / ${state.xpNeededForNextLevel} XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = CrystalCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val xpFrac = (state.xpIntoCurrentLevel.toFloat() / state.xpNeededForNextLevel.coerceAtLeast(1L))
                    .coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { xpFrac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = CrystalCyan,
                    trackColor = PanelElevated,
                    strokeCap = StrokeCap.Round
                )
            }
        }

        // Relics / Achievements Showcase Shelf (Unlocked + Locked Goals)
        Text(
            text = "Frontier Relics (${state.uniqueItems.size}/${UniqueQuestItem.entries.size})",
            style = MaterialTheme.typography.labelLarge,
            color = EmeraldCanopy
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(UniqueQuestItem.entries) { relic ->
                val unlocked = state.uniqueItems.contains(relic.id)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (unlocked) EmeraldCanopy.copy(alpha = 0.14f) else PanelCard,
                    border = BorderStroke(
                        1.dp,
                        if (unlocked) EmeraldCanopy.copy(alpha = 0.6f) else PanelBorder.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (unlocked) Icons.Filled.WorkspacePremium else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (unlocked) EmeraldCanopy else LichenMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = relic.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (unlocked) CreamText else LichenMuted
                            )
                            Text(
                                text = relic.perkDescription,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (unlocked) EmeraldCanopy else LichenMuted.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(QuestFilter.entries) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberGold,
                        selectedLabelColor = ObsidianForest,
                        containerColor = PanelCard,
                        labelColor = LichenMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = PanelBorder
                    ),
                    modifier = Modifier.testTag("filter_quest_${filter.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredQuests.isEmpty()) {
                item {
                    FrontierGlassCard {
                        Text(
                            text = "No quests in this filter yet. Keep exploring the frontier!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LichenMuted,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(filteredQuests, key = { it.quest.id }) { progress ->
                    QuestCard(
                        progress = progress,
                        onClaim = { onClaimQuest(progress.quest.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestCard(
    progress: QuestProgress,
    onClaim: () -> Unit
) {
    val quest = progress.quest
    FrontierGlassCard(
        accentBorder = when {
            progress.isReadyToClaim -> AmberGold
            progress.isClaimed -> EmeraldCanopy
            else -> null
        }
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = if (progress.isClaimed) EmeraldCanopy.copy(alpha = 0.16f) else PanelElevated,
                    border = BorderStroke(
                        1.dp,
                        if (progress.isReadyToClaim) AmberGold.copy(alpha = 0.6f)
                        else PanelBorder.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIcon(quest.category),
                            contentDescription = null,
                            tint = if (progress.isClaimed) EmeraldCanopy else AmberBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${quest.category.label.uppercase()} • ${quest.title}",
                        style = MaterialTheme.typography.titleSmall,
                        color = CreamText
                    )
                    Text(
                        text = quest.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LichenMuted
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                when {
                    progress.isClaimed -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Quest Claimed",
                            tint = EmeraldCanopy,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    progress.isReadyToClaim -> {
                        FrontierActionButton(
                            text = "Claim",
                            enabled = true,
                            accentColor = AmberGold,
                            testTag = "btn_claim_${quest.id}",
                            onClick = onClaim
                        )
                    }
                    else -> {
                        Text(
                            text = "${progress.current}/${progress.target}",
                            style = MaterialTheme.typography.labelLarge,
                            color = LichenMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = if (progress.isCompleted) EmeraldCanopy else AmberGold,
                trackColor = PanelElevated,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrontierBadgeChip(
                    text = "+${formatCompactCurrency(quest.rewardCoins)}",
                    accent = AmberBright
                )
                FrontierBadgeChip(
                    text = "+${quest.rewardXp} XP",
                    accent = CrystalCyan
                )
                quest.rewardItem?.let { item ->
                    FrontierBadgeChip(
                        text = item.title,
                        accent = EmeraldCanopy,
                        icon = Icons.Filled.Star
                    )
                }
            }
        }
    }
}

private fun categoryIcon(category: QuestCategory): ImageVector = when (category) {
    QuestCategory.GATHER -> Icons.Filled.Forest
    QuestCategory.UPGRADE -> Icons.Filled.Build
    QuestCategory.EXPLORE -> Icons.Filled.Explore
    QuestCategory.FACTORY -> Icons.Filled.Factory
}
