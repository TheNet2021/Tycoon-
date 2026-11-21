package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.BuyerCompany
import com.example.data.ContractOrder
import com.example.data.TradeCatalog
import com.example.data.TradeGoodInfo
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
import com.example.ui.theme.CrystalCyan
import com.example.ui.theme.EmeraldCanopy
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PanelElevated
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun MarketplaceSheet(
    state: TycoonSaveState,
    onDismiss: () -> Unit,
    onSellToBuyer: (BuyerCompany, String, Int) -> Unit,
    onAcceptContract: (String) -> Unit,
    onDeliverContract: (String) -> Unit,
    onCancelContract: (String) -> Unit,
    onRefreshContracts: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val firstOwnedGood = remember(state.warehouseStock, state.inventory) {
        TradeCatalog.allGoods.firstOrNull { state.availableForTrade(it.id) > 0 }?.id ?: "wood"
    }
    var selectedGoodId by remember { mutableStateOf(firstOwnedGood) }
    val selectedGood = TradeCatalog.find(selectedGoodId) ?: TradeCatalog.allGoods.first()

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMs = System.currentTimeMillis()
        }
    }

    FrontierSheetScaffold(
        title = "Buyers & Contracts",
        subtitle = "Compare buyer offers, market demand & deliveries",
        headerIcon = Icons.Filled.Storefront,
        accentColor = AmberGold,
        coins = state.coins,
        passivePerSec = state.passiveIncomePerSecond,
        closeTestTag = "btn_close_market",
        onDismiss = onDismiss
    ) {
        // Two-tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MarketTabChip(
                label = "Buyers (${BuyerCompany.entries.count { state.isBuyerUnlocked(it) }}/${BuyerCompany.entries.size})",
                selected = selectedTab == 0,
                accent = AmberGold,
                testTag = "tab_market_buyers",
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 0 }
            )
            MarketTabChip(
                label = "Contracts (${state.activeContracts.size}/${state.maxActiveContractsAllowed})",
                selected = selectedTab == 1,
                accent = EmeraldCanopy,
                testTag = "tab_market_contracts",
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 1 }
            )
        }

        if (selectedTab == 0) {
            val mult = state.marketMultiplier(selectedGood.id)
            val pct = ((mult - 1.0) * 100.0).roundToInt()
            val isBull = pct >= 0
            val stockCount = state.availableForTrade(selectedGood.id)

            // Market Price Ticker Banner
            FrontierGlassCard(
                accentBorder = if (isBull) EmeraldCanopy else CoralCrimson,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (isBull) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (isBull) EmeraldCanopy else CoralCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "${selectedGood.displayName} Market Demand",
                                style = MaterialTheme.typography.labelLarge,
                                color = CreamText
                            )
                            Text(
                                text = "In Bag + Warehouse: $stockCount units",
                                style = MaterialTheme.typography.labelSmall,
                                color = LichenMuted
                            )
                        }
                    }
                    FrontierBadgeChip(
                        text = if (isBull) "+$pct% Surge" else "$pct% Dip",
                        accent = if (isBull) EmeraldCanopy else CoralCrimson
                    )
                }
            }

            // Selectable Resource & Product Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(TradeCatalog.allGoods, key = { it.id }) { good ->
                    GoodFilterChip(
                        good = good,
                        count = state.availableForTrade(good.id),
                        selected = good.id == selectedGood.id,
                        onClick = { selectedGoodId = good.id }
                    )
                }
            }

            // 6 Buyer Company Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(335.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(BuyerCompany.entries, key = { it.id }) { buyer ->
                    BuyerCard(
                        buyer = buyer,
                        good = selectedGood,
                        availableStock = stockCount,
                        state = state,
                        onSellOne = { onSellToBuyer(buyer, selectedGood.id, 1) },
                        onSellAll = { onSellToBuyer(buyer, selectedGood.id, stockCount) }
                    )
                }
            }
        } else {
            // Contracts Header Bar
            FrontierGlassCard(
                accentBorder = EmeraldCanopy,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Active Slots: ${state.activeContracts.size} / ${state.maxActiveContractsAllowed}",
                            style = MaterialTheme.typography.labelLarge,
                            color = CreamText
                        )
                        Text(
                            text = "Completed: ${state.totalContractsCompleted} • Delivery Truck Lv.2+/4+ unlocks more slots",
                            style = MaterialTheme.typography.labelSmall,
                            color = LichenMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PanelElevated,
                        border = BorderStroke(1.dp, CrystalCyan.copy(alpha = 0.5f))
                    ) {
                        IconButton(
                            onClick = onRefreshContracts,
                            modifier = Modifier
                                .size(42.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("btn_refresh_contracts")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh Contract Offers",
                                tint = CrystalCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(365.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.activeContracts.isNotEmpty()) {
                    item(key = "hdr_active") {
                        Text(
                            text = "ACTIVE DELIVERIES",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldCanopy,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(state.activeContracts, key = { "active_${it.id}" }) { contract ->
                        ActiveContractCard(
                            contract = contract,
                            state = state,
                            nowMs = nowMs,
                            onDeliver = { onDeliverContract(contract.id) },
                            onCancel = { onCancelContract(contract.id) }
                        )
                    }
                }

                item(key = "hdr_offers") {
                    Text(
                        text = "AVAILABLE BUYER CONTRACTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberBright,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = if (state.activeContracts.isEmpty()) 0.dp else 4.dp)
                    )
                }

                items(state.availableContracts, key = { "offer_${it.id}" }) { offer ->
                    AvailableContractCard(
                        contract = offer,
                        canAcceptMore = state.activeContracts.size < state.maxActiveContractsAllowed,
                        onAccept = { onAcceptContract(offer.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketTabChip(
    label: String,
    selected: Boolean,
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) accent.copy(alpha = 0.2f) else PanelCard,
        border = BorderStroke(1.2.dp, if (selected) accent else PanelBorder),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) accent else LichenMuted,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GoodFilterChip(
    good: TradeGoodInfo,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = Color(good.colorHex)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent.copy(alpha = 0.22f) else PanelCard,
        border = BorderStroke(
            1.dp,
            when {
                selected -> accent
                count > 0 -> accent.copy(alpha = 0.45f)
                else -> PanelBorder
            }
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag("chip_good_${good.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (count > 0) "${good.displayName} ($count)" else good.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) CreamText else if (count > 0) AmberBright else LichenMuted,
                fontWeight = if (selected || count > 0) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BuyerCard(
    buyer: BuyerCompany,
    good: TradeGoodInfo,
    availableStock: Int,
    state: TycoonSaveState,
    onSellOne: () -> Unit,
    onSellAll: () -> Unit
) {
    val unlocked = state.isBuyerUnlocked(buyer)
    val accent = Color(buyer.accentHex)
    val isSpecialty = buyer.preferredGoods.contains(good.id)
    val offerUnit = state.buyerOfferUnitPrice(buyer, good.id)
    val repXp = state.buyerReputation(buyer)
    val repLvl = state.buyerReputationLevel(buyer)
    val repIntoLevel = repXp % 50
    val repFrac = (repIntoLevel / 50f).coerceIn(0f, 1f)
    val biomeName = BiomeType.fromId(buyer.unlockBiomeId)?.title ?: buyer.unlockBiomeId

    FrontierGlassCard(accentBorder = if (unlocked && isSpecialty) accent else null) {
        Column(modifier = Modifier.padding(13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (unlocked) accent.copy(alpha = 0.2f) else PanelElevated,
                    border = BorderStroke(
                        1.5.dp,
                        if (unlocked) accent else LichenMuted.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (unlocked) {
                            Text(
                                text = buyer.badgeCode,
                                style = MaterialTheme.typography.titleMedium,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = LichenMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = buyer.companyName,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (unlocked) CreamText else LichenMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        FrontierBadgeChip(
                            text = buyer.categoryLabel,
                            accent = accent
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = buyer.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = LichenMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!unlocked) {
                Text(
                    text = "Unlocks at Player Lv.${buyer.unlockLevel} & $biomeName",
                    style = MaterialTheme.typography.labelMedium,
                    color = CoralCrimson
                )
            } else {
                // Reputation bar + Offer comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Rep Lv.$repLvl (+${(repLvl - 1) * 6}% Bonus)",
                                style = MaterialTheme.typography.labelSmall,
                                color = CreamText
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { repFrac },
                            modifier = Modifier
                                .width(130.dp)
                                .height(4.dp)
                                .clip(CircleShape),
                            color = AmberGold,
                            trackColor = PanelElevated,
                            strokeCap = StrokeCap.Round
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSpecialty) {
                            FrontierBadgeChip(
                                text = "Specialty +${((buyer.specialtyMultiplier - 1.0) * 100).roundToInt()}%",
                                accent = EmeraldCanopy
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PanelElevated,
                            border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${formatCompactCurrency(offerUnit)}/ea",
                                style = MaterialTheme.typography.labelLarge,
                                color = AmberBright,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FrontierActionButton(
                        text = "Sell 1 (+${formatCompactCurrency(offerUnit)})",
                        enabled = availableStock >= 1,
                        accentColor = CrystalCyan,
                        testTag = "btn_sell_one_${buyer.id}",
                        modifier = Modifier.weight(1f),
                        onClick = onSellOne
                    )
                    val totalPayout = offerUnit * availableStock.coerceAtLeast(1)
                    FrontierActionButton(
                        text = if (availableStock > 0) "Sell $availableStock (${formatCompactCurrency(totalPayout)})" else "No Stock",
                        enabled = availableStock >= 1,
                        accentColor = if (isSpecialty) EmeraldCanopy else AmberGold,
                        testTag = "btn_sell_all_${buyer.id}",
                        modifier = Modifier.weight(1.25f),
                        onClick = onSellAll
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveContractCard(
    contract: ContractOrder,
    state: TycoonSaveState,
    nowMs: Long,
    onDeliver: () -> Unit,
    onCancel: () -> Unit
) {
    val buyer = BuyerCompany.fromId(contract.buyerId)
    val good = TradeCatalog.find(contract.goodId)
    val have = state.availableForTrade(contract.goodId)
    val frac = (have.toFloat() / contract.quantity.coerceAtLeast(1)).coerceIn(0f, 1f)
    val ready = have >= contract.quantity
    val remaining = contract.remainingSeconds(nowMs)
    val expired = contract.isExpired(nowMs)
    val accent = buyer?.let { Color(it.accentHex) } ?: EmeraldCanopy

    FrontierGlassCard(accentBorder = if (ready) EmeraldCanopy else accent) {
        Column(modifier = Modifier.padding(13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.Assignment,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${buyer?.companyName ?: contract.buyerId} • ${contract.tier.label}",
                        style = MaterialTheme.typography.titleSmall,
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (remaining >= 0L) {
                    FrontierBadgeChip(
                        text = if (expired) "Late -35%" else "${remaining}s",
                        accent = if (expired) CoralCrimson else AmberBright,
                        icon = Icons.Filled.Timer
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Deliver ${contract.quantity}x ${good?.displayName ?: contract.goodId} ($have/${contract.quantity} ready)",
                style = MaterialTheme.typography.bodyMedium,
                color = if (ready) EmeraldCanopy else LichenMuted
            )
            Spacer(modifier = Modifier.height(5.dp))
            LinearProgressIndicator(
                progress = { frac },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (ready) EmeraldCanopy else AmberGold,
                trackColor = PanelElevated,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pay: ${formatCompactCurrency(contract.rewardCoins)} • +${contract.rewardXp} XP • +${contract.rewardRep} Rep",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberBright,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(40.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("btn_cancel_contract_${contract.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = "Cancel Contract",
                        tint = CoralCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                }
                FrontierActionButton(
                    text = "Deliver",
                    enabled = ready,
                    accentColor = EmeraldCanopy,
                    testTag = "btn_deliver_${contract.id}",
                    onClick = onDeliver
                )
            }
        }
    }
}

@Composable
private fun AvailableContractCard(
    contract: ContractOrder,
    canAcceptMore: Boolean,
    onAccept: () -> Unit
) {
    val buyer = BuyerCompany.fromId(contract.buyerId)
    val good = TradeCatalog.find(contract.goodId)
    val accent = buyer?.let { Color(it.accentHex) } ?: AmberGold

    FrontierGlassCard(accentBorder = accent.copy(alpha = 0.55f)) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.18f),
                border = BorderStroke(1.2.dp, accent),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = buyer?.badgeCode ?: "CT",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                        fontWeight = FontWeight.Bold
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
                        text = buyer?.companyName ?: contract.buyerId,
                        style = MaterialTheme.typography.titleSmall,
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    FrontierBadgeChip(
                        text = contract.tier.label,
                        accent = if (contract.tier.durationSeconds > 0) CoralCrimson else CrystalCyan
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Needs ${contract.quantity}x ${good?.displayName ?: contract.goodId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CreamText
                )
                Text(
                    text = "Payout: ${formatCompactCurrency(contract.rewardCoins)} • +${contract.rewardXp} XP • +${contract.rewardRep} Rep",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberBright
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            FrontierActionButton(
                text = if (canAcceptMore) "Accept" else "Full",
                enabled = canAcceptMore,
                accentColor = AmberGold,
                testTag = "btn_accept_${contract.id}",
                onClick = onAccept
            )
        }
    }
}
