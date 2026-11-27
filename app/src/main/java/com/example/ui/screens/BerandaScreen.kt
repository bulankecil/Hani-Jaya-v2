package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.TransactionEntity
import com.example.ui.HomeUiState
import com.example.ui.components.HomeTab
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CnfBlueEnd
import com.example.ui.theme.CnfBlueSoft
import com.example.ui.theme.CnfBlueStart
import com.example.ui.theme.CnfEmeraldEnd
import com.example.ui.theme.CnfEmeraldSoft
import com.example.ui.theme.CnfEmeraldStart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientMid
import com.example.ui.theme.HeroGradientStart
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.NavyTopBar
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WalletIndigo
import com.example.ui.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BerandaScreen(
    uiState: HomeUiState,
    onWalletClick: () -> Unit,
    onNavigateTab: (HomeTab) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    var showAllTransactionsSheet by remember { mutableStateOf(false) }
    var pendingDeleteItem by remember { mutableStateOf<TransactionEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Signature Navy Blue Top Bar Strip + Header
        TopHeaderBar(onWalletClick = onWalletClick)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Balance Card
                item(key = "hero_balance") {
                    BalanceHeroCard(
                        netBalance = uiState.netBalance,
                        totalIncome = uiState.totalIncome,
                        totalExpense = uiState.totalExpense
                    )
                }

                // Two CnF Summary Feature Cards
                item(key = "cnf_cards") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CnfSummaryCard(
                            title = stringResource(R.string.label_cnf_calculation),
                            subtitle = stringResource(R.string.subtitle_cnf_calculation),
                            count = uiState.cnfCalculationCount,
                            unitLabel = stringResource(R.string.unit_offers),
                            gradientStart = CnfBlueStart,
                            gradientEnd = CnfBlueEnd,
                            badgeSoftColor = CnfBlueSoft,
                            icon = Icons.Outlined.Folder,
                            testTag = "card_cnf_calculation",
                            onClick = { onNavigateTab(HomeTab.CNF_CALCULATION) },
                            modifier = Modifier.weight(1f)
                        )
                        CnfSummaryCard(
                            title = stringResource(R.string.label_cnf_price),
                            subtitle = stringResource(R.string.subtitle_cnf_price),
                            count = uiState.cnfPriceCount,
                            unitLabel = stringResource(R.string.unit_transactions),
                            gradientStart = CnfEmeraldStart,
                            gradientEnd = CnfEmeraldEnd,
                            badgeSoftColor = CnfEmeraldSoft,
                            icon = Icons.Outlined.LocalOffer,
                            testTag = "card_cnf_price",
                            onClick = { onNavigateTab(HomeTab.CNF_PRICE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Recent Transactions Header
                item(key = "recent_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.section_recent_transactions),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )

                        TextButton(
                            onClick = {
                                if (uiState.allTransactions.isNotEmpty()) {
                                    showAllTransactionsSheet = true
                                } else {
                                    onNavigateTab(HomeTab.TRANSAKSI)
                                }
                            },
                            modifier = Modifier.testTag("view_all_transactions_button")
                        ) {
                            Text(
                                text = stringResource(R.string.action_view_all),
                                style = MaterialTheme.typography.labelLarge,
                                color = WalletIndigo
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = WalletIndigo
                            )
                        }
                    }
                }

                // Empty State or Transaction Items
                if (uiState.recentTransactions.isEmpty()) {
                    item(key = "empty_state") {
                        EmptyTransactionsCard()
                    }
                } else {
                    items(
                        items = uiState.recentTransactions,
                        key = { it.id }
                    ) { transaction ->
                        TransactionItemCard(
                            item = transaction,
                            onDeleteClick = { pendingDeleteItem = transaction }
                        )
                    }
                }
            }
        }
    }

    if (showAllTransactionsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAllTransactionsSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = AppBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Semua Transaksi (${uiState.allTransactions.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    TextButton(onClick = { showAllTransactionsSheet = false }) {
                        Text("Tutup")
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.allTransactions, key = { it.id }) { transaction ->
                        TransactionItemCard(
                            item = transaction,
                            onDeleteClick = { pendingDeleteItem = transaction }
                        )
                    }
                }
            }
        }
    }

    pendingDeleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDeleteItem = null },
            title = { Text("Hapus Transaksi?") },
            text = {
                Text("Catatan \"${item.displayTitle}\" akan dihapus permanen dari database lokal.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(item.id)
                        pendingDeleteItem = null
                    }
                ) {
                    Text("Hapus", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteItem = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun TopHeaderBar(onWalletClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
    ) {
        // Signature Top Navy Blue Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyTopBar)
                .statusBarsPadding()
                .height(8.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.company_header),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.screen_title_financial_notes),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    onClick = onWalletClick,
                    modifier = Modifier
                        .size(48.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("wallet_button"),
                    shape = RoundedCornerShape(14.dp),
                    color = WalletIndigo,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = stringResource(R.string.cd_wallet_button),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceHeroCard(
    netBalance: Long,
    totalIncome: Long,
    totalExpense: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(HeroGradientStart, HeroGradientMid, HeroGradientEnd)
                    )
                )
        ) {
            // Decorative circular highlight on right matching reference screenshot
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.height * 0.65f,
                    center = Offset(size.width * 0.9f, size.height * 0.35f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.height * 0.42f,
                    center = Offset(size.width * 0.78f, size.height * 0.78f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_net_balance),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.86f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = CurrencyFormatter.formatRupiah(netBalance),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("hero_net_balance_text")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroMetricItem(
                        icon = Icons.AutoMirrored.Filled.CallReceived,
                        label = stringResource(R.string.label_income),
                        amount = CurrencyFormatter.formatRupiah(totalIncome),
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .width(1.dp)
                            .height(34.dp)
                            .background(Color.White.copy(alpha = 0.24f))
                    )

                    HeroMetricItem(
                        icon = Icons.AutoMirrored.Filled.CallMade,
                        label = stringResource(R.string.label_expenses),
                        amount = CurrencyFormatter.formatRupiah(totalExpense),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricItem(
    icon: ImageVector,
    label: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CnfSummaryCard(
    title: String,
    subtitle: String,
    count: Int,
    unitLabel: String,
    gradientStart: Color,
    gradientEnd: Color,
    badgeSoftColor: Color,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Wavy Colored Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(gradientStart, gradientEnd)
                        )
                    )
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val path1 = Path().apply {
                        moveTo(0f, size.height * 0.55f)
                        quadraticTo(
                            size.width * 0.35f,
                            size.height * 0.25f,
                            size.width * 0.7f,
                            size.height * 0.58f
                        )
                        quadraticTo(
                            size.width * 0.88f,
                            size.height * 0.72f,
                            size.width,
                            size.height * 0.48f
                        )
                    }
                    drawPath(
                        path = path1,
                        color = Color.White.copy(alpha = 0.16f),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    val path2 = Path().apply {
                        moveTo(0f, size.height * 0.85f)
                        quadraticTo(
                            size.width * 0.45f,
                            size.height * 0.55f,
                            size.width,
                            size.height * 0.82f
                        )
                    }
                    drawPath(
                        path = path2,
                        color = Color.White.copy(alpha = 0.12f),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(14.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.White.copy(alpha = 0.24f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(badgeSoftColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = gradientStart,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            ) {
                                append("$count ")
                            }
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Normal,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            ) {
                                append(unitLabel)
                            }
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactionsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CnfBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = WalletIndigo,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.empty_transactions_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.empty_transactions_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TransactionItemCard(
    item: TransactionEntity,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        color = CardSurface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.displayTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    ),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(start = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Hapus transaksi",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (item.locationRoute.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.locationRoute,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SubtleBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.formatRupiah(item.netBalance),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = if (item.netBalance >= 0L) IncomeGreen else ExpenseRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Income / Expense",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            ) {
                                append(CurrencyFormatter.formatRupiah(item.incomeAmount))
                            }
                            withStyle(
                                SpanStyle(
                                    color = TextMuted,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            ) {
                                append(" / ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            ) {
                                append(CurrencyFormatter.formatRupiah(item.expenseAmount))
                            }
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
