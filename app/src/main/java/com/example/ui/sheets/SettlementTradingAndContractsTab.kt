package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.SettlementConstructionContract
import com.example.data.SettlementContractCategory
import com.example.data.SettlementFacilityType
import com.example.data.SettlementTransaction
import com.example.data.SurvivorTradeOffer
import com.example.data.TradeCatalog
import com.example.data.TycoonSaveState
import com.example.ui.components.FrontierActionButton
import com.example.ui.components.FrontierBadgeChip
import com.example.ui.components.FrontierEmptyStateCard
import com.example.ui.components.FrontierFilterStrip
import com.example.ui.components.FrontierFilterTab
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSectionHeader
import com.example.ui.components.formatCompactCurrency
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CreamText
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PanelElevated
import kotlin.math.roundToInt

@Composable
fun SettlementTradingTab(
    state: TycoonSaveState,
    onSellToSurvivors: (String, Int) -> Boolean,
    onBuyFromSurvivors: (String, Int) -> Boolean
) {
    val settlement = state.settlement
    var subTab by remember { mutableIntStateOf(0) }

    FrontierFilterStrip(
        filters = listOf(
            FrontierFilterTab("Sell Goods", EmeraldCanopy, "filter_settle_sell"),
            FrontierFilterTab("Buy Offers (${settlement.survivorOffersForSale.size})", CrystalCyan, "filter_settle_buy"),
            FrontierFilterTab("Ledger (${settlement.transactionHistory.size})", AmberGold, "filter_settle_history")
        ),
        selectedIndex = subTab,
        onSelect = { subTab = it }
    )

    when (subTab) {
        0 -> {
            val sortedGoods = remember(state.warehouseStock, state.inventory, settlement.demandSaturation) {
                TradeCatalog.allGoods.sortedWith(
                    compareByDescending<com.example.data.TradeGoodInfo> { state.availableForTrade(it.id) > 0 }
                        .thenByDescending { state.availableForTrade(it.id) }
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                if (settlement.survivors.isEmpty()) {
                    item {
                        FrontierEmptyStateCard(
                            icon = Icons.Filled.Storefront,
                            title = "No Settlers Yet",
                            subtitle = "Build a Small Tent and welcome at least 1 refugee in Overview to open survivor trading.",
                            accent = AmberGold
                        )
                    }
                } else {
                    item {
                        FrontierSectionHeader(
                            title = "Sell Stock to Settlers",
                            trailingBadge = "Pop Demand ${(0.95 + settlement.survivors.size * 0.015).coerceAtMost(1.30).times(100).roundToInt()}%",
                            accent = EmeraldCanopy
                        )
                    }
                    items(sortedGoods, key = { it.id }) { good ->
                        val available = state.availableForTrade(good.id)
                        val mult = settlement.demandMultiplierFor(good.id)
                        val demandPct = (mult * 100).roundToInt()
                        val bidPrice = settlement.survivorBidPrice(good, state.marketMultiplier(good.id))
                        val isHighDemand = demandPct >= 100

                        FrontierGlassCard(
                            accentBorder = if (available > 0) Color(good.colorHex) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(good.colorHex))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = good.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = CreamText,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        FrontierBadgeChip(
                                            text = if (good.isManufactured) "Craft" else "Raw",
                                            accent = if (good.isManufactured) AmberBright else CrystalCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Stock: $available",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (available > 0) CreamText else LichenMuted,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        FrontierBadgeChip(
                                            text = "Bid $$bidPrice",
                                            accent = AmberGold
                                        )
                                        FrontierBadgeChip(
                                            text = "Demand $demandPct%",
                                            accent = if (isHighDemand) EmeraldCanopy else CoralCrimson,
                                            icon = if (isHighDemand) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FrontierActionButton(
                                        text = "x1",
                                        enabled = available >= 1,
                                        accentColor = EmeraldCanopy,
                                        testTag = "btn_sell_settler_${good.id}_1",
                                        onClick = { onSellToSurvivors(good.id, 1) }
                                    )
                                    if (available >= 5) {
                                        FrontierActionButton(
                                            text = "x5",
                                            enabled = true,
                                            accentColor = AmberGold,
                                            testTag = "btn_sell_settler_${good.id}_5",
                                            onClick = { onSellToSurvivors(good.id, 5) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        1 -> {
            val storageFree = (state.warehouseStockCapacity - state.warehouseStockUsed).coerceAtLeast(0) +
                (state.backpackMaxCapacity - state.backpackUsed).coerceAtLeast(0)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    FrontierSectionHeader(
                        title = "Settler Goods For Sale",
                        trailingBadge = "$storageFree Free Slots",
                        accent = CrystalCyan
                    )
                }
                if (settlement.survivorOffersForSale.isEmpty()) {
                    item {
                        FrontierEmptyStateCard(
                            icon = Icons.Filled.ShoppingBag,
                            title = "All Settler Stock Bought",
                            subtitle = "New survivor trade offers restock automatically at the start of each settlement day.",
                            accent = CrystalCyan
                        )
                    }
                } else {
                    items(settlement.survivorOffersForSale, key = { it.goodId }) { offer ->
                        SurvivorOfferCard(
                            offer = offer,
                            coins = state.coins,
                            storageFree = storageFree,
                            onBuy = onBuyFromSurvivors
                        )
                    }
                }
            }
        }
        else -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    FrontierSectionHeader(
                        title = "Transaction Ledger",
                        trailingBadge = "${settlement.transactionHistory.size} Entries",
                        accent = AmberGold
                    )
                }
                if (settlement.transactionHistory.isEmpty()) {
                    item {
                        FrontierEmptyStateCard(
                            icon = Icons.Filled.History,
                            title = "No Transactions Yet",
                            subtitle = "Trades, ration purchases, contracts, and daily settlement wages appear here.",
                            accent = LichenMuted
                        )
                    }
                } else {
                    items(settlement.transactionHistory, key = { it.id }) { tx ->
                        SettlementTransactionRow(tx = tx)
                    }
                }
            }
        }
    }
}

@Composable
private fun SurvivorOfferCard(
    offer: SurvivorTradeOffer,
    coins: Long,
    storageFree: Int,
    onBuy: (String, Int) -> Boolean
) {
    val good = TradeCatalog.find(offer.goodId)
    val accent = if (good != null) Color(good.colorHex) else CrystalCyan
    val canBuyOne = coins >= offer.unitAskPrice && storageFree >= 1 && offer.availableQuantity >= 1
    val canBuyFive = offer.availableQuantity >= 5 && coins >= offer.unitAskPrice * 5 && storageFree >= 5

    FrontierGlassCard(accentBorder = accent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = good?.displayName ?: offer.goodId,
                        style = MaterialTheme.typography.titleSmall,
                        color = CreamText,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FrontierBadgeChip(
                        text = "${offer.availableQuantity} Left",
                        accent = CrystalCyan
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Offered by ${offer.sellerName} • ${formatCompactCurrency(offer.unitAskPrice)} each",
                    style = MaterialTheme.typography.bodySmall,
                    color = LichenMuted
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FrontierActionButton(
                    text = "Buy 1",
                    enabled = canBuyOne,
                    accentColor = CrystalCyan,
                    testTag = "btn_buy_settler_${offer.goodId}_1",
                    onClick = { onBuy(offer.goodId, 1) }
                )
                if (offer.availableQuantity >= 5) {
                    FrontierActionButton(
                        text = "x5",
                        enabled = canBuyFive,
                        accentColor = AmberGold,
                        testTag = "btn_buy_settler_${offer.goodId}_5",
                        onClick = { onBuy(offer.goodId, 5) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettlementTransactionRow(tx: SettlementTransaction) {
    val isGain = tx.coinDelta >= 0L
    val deltaColor = if (isGain) EmeraldCanopy else CoralCrimson
    val deltaLabel = if (isGain) "+${formatCompactCurrency(tx.coinDelta)}" else "-${formatCompactCurrency(-tx.coinDelta)}"

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PanelCard.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, PanelElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.type.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberBright,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = CreamText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = deltaLabel,
                style = MaterialTheme.typography.titleSmall,
                color = deltaColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettlementContractsTab(
    state: TycoonSaveState,
    onCompleteContract: (String) -> Boolean
) {
    val settlement = state.settlement
    var filterIdx by remember { mutableIntStateOf(0) }
    val categories = remember { SettlementContractCategory.entries }
    val filteredContracts = remember(settlement.openContracts, filterIdx) {
        if (filterIdx == 0) settlement.openContracts
        else {
            val cat = categories.getOrNull(filterIdx - 1)
            settlement.openContracts.filter { it.category == cat }
        }
    }

    FrontierFilterStrip(
        filters = listOf(FrontierFilterTab("All", AmberGold, "filter_contract_all")) +
            categories.map { cat ->
                FrontierFilterTab(cat.label, Color(cat.accentHex), "filter_contract_${cat.name.lowercase()}")
            },
        selectedIndex = filterIdx,
        onSelect = { filterIdx = it }
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            FrontierSectionHeader(
                title = "Settler Construction Requests",
                trailingBadge = "${settlement.completedContractsCount} Built",
                accent = AmberGold
            )
        }
        if (filteredContracts.isEmpty()) {
            item {
                FrontierEmptyStateCard(
                    icon = Icons.Filled.Assignment,
                    title = "No Requests in Category",
                    subtitle = "Switch category filters or complete open requests to cycle new settler construction projects.",
                    accent = AmberGold
                )
            }
        } else {
            items(filteredContracts, key = { it.id }) { contract ->
                SettlementContractCard(
                    contract = contract,
                    state = state,
                    onBuild = { onCompleteContract(contract.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettlementContractCard(
    contract: SettlementConstructionContract,
    state: TycoonSaveState,
    onBuild: () -> Unit
) {
    val accent = Color(contract.category.accentHex)
    val hasAllMaterials = contract.materialRequirements.all { (matId, req) ->
        state.availableForTrade(matId) >= req
    }

    FrontierGlassCard(accentBorder = if (hasAllMaterials) EmeraldCanopy else accent) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FrontierBadgeChip(text = contract.category.label, accent = accent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "by ${contract.requesterName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LichenMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = contract.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = CreamText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = contract.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LichenMuted
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FrontierActionButton(
                    text = "Build",
                    enabled = hasAllMaterials,
                    accentColor = EmeraldCanopy,
                    testTag = "btn_build_contract_${contract.id}",
                    onClick = onBuild
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                contract.materialRequirements.forEach { (matId, req) ->
                    val have = state.availableForTrade(matId)
                    val matName = TradeCatalog.find(matId)?.displayName ?: matId
                    FrontierBadgeChip(
                        text = "$matName $have/$req",
                        accent = if (have >= req) EmeraldCanopy else CoralCrimson
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FrontierBadgeChip(text = "+${formatCompactCurrency(contract.rewardCoins)}", accent = AmberGold)
                FrontierBadgeChip(text = "+${contract.rewardXp} XP", accent = CrystalCyan)
                FrontierBadgeChip(text = "+${contract.rewardReputation} Rep", accent = AmberBright)
                if (contract.happinessBonus > 0) {
                    FrontierBadgeChip(text = "+${contract.happinessBonus}% Mood", accent = EmeraldCanopy)
                }
                if (contract.foodCapBonus > 0) {
                    FrontierBadgeChip(text = "+${contract.foodCapBonus} Food Cap", accent = EmeraldCanopy)
                }
                if (contract.waterCapBonus > 0) {
                    FrontierBadgeChip(text = "+${contract.waterCapBonus} Water Cap", accent = CrystalCyan)
                }
                if (contract.passiveFoodBonus > 0) {
                    FrontierBadgeChip(text = "+${contract.passiveFoodBonus} Food/d", accent = EmeraldCanopy)
                }
                if (contract.passiveWaterBonus > 0) {
                    FrontierBadgeChip(text = "+${contract.passiveWaterBonus} Water/d", accent = CrystalCyan)
                }
                val targetFacility = contract.targetFacilityId?.let { SettlementFacilityType.fromId(it) }
                if (targetFacility != null) {
                    val curLvl = state.settlement.facilityLevel(targetFacility)
                    val label = if (curLvl == 0) "+Builds ${targetFacility.title}"
                    else if (curLvl < targetFacility.maxLevel) "+${targetFacility.title} Lv.${curLvl + 1}"
                    else "+8 Supplies"
                    FrontierBadgeChip(text = label, accent = Color(targetFacility.accentHex))
                }
            }
        }
    }
}
