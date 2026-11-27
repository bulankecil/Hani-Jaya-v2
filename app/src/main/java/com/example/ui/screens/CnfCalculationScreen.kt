package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.data.local.CnfOfferEntity
import com.example.ui.CnfCalculationUiState
import com.example.ui.components.CreateOfferBottomSheet
import com.example.ui.components.ExcelPreviewSheet
import com.example.ui.components.InlineEditBreakdownSection
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CnfEmeraldEnd
import com.example.ui.theme.CnfEmeraldSoft
import com.example.ui.theme.CnfEmeraldStart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.NavyTopBar
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WalletIndigo
import com.example.ui.util.CurrencyFormatter
import kotlinx.coroutines.launch

@Composable
fun CnfCalculationScreen(
    uiState: CnfCalculationUiState,
    onToggleExpand: (Long) -> Unit,
    onToggleCurrency: (Long) -> Unit,
    onCreateOffer: (
        offerName: String,
        serialNumber: String,
        mediatorName: String,
        customerName: String,
        route: String,
        offerDateMillis: Long
    ) -> Unit,
    onUpdateOffer: (CnfOfferEntity) -> Unit,
    onDeleteOffer: (Long) -> Unit,
    onExportOfferCsv: (Context, Uri, CnfOfferEntity) -> Unit,
    onClearFeedback: () -> Unit,
    onReturnHome: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    BackHandler(onBack = onReturnHome)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCreateSheet by remember { mutableStateOf(false) }
    var editingOfferId by remember { mutableStateOf<Long?>(null) }
    var excelPreviewOffer by remember { mutableStateOf<CnfOfferEntity?>(null) }
    var pendingDeleteOffer by remember { mutableStateOf<CnfOfferEntity?>(null) }

    BackHandler(enabled = editingOfferId != null) {
        editingOfferId = null
    }

    LaunchedEffect(uiState.feedbackMessage) {
        val msg = uiState.feedbackMessage
        if (msg != null) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
            onClearFeedback()
        }
    }

    fun showPendingMessage(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CnfCalculationHeader(
                onSearchClick = {
                    showPendingMessage("Fitur pencarian penawaran akan ditambahkan pada tahap berikutnya.")
                }
            )

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
                        bottom = 132.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(key = "expected_pl_card") {
                        ExpectedProfitHeroCard(
                            totalExpectedProfitIdr = uiState.totalExpectedProfitIdr,
                            offerCount = uiState.offers.size
                        )
                    }

                    items(
                        items = uiState.offers,
                        key = { it.id }
                    ) { offer ->
                        val isExpanded = offer.id in uiState.expandedOfferIds
                        val isEditing = editingOfferId == offer.id
                        CnfOfferCard(
                            offer = offer,
                            isExpanded = isExpanded || isEditing,
                            isEditing = isEditing,
                            isIdrMode = offer.id in uiState.idrModeOfferIds,
                            onCardClick = {
                                if (!isEditing) {
                                    onToggleExpand(offer.id)
                                }
                            },
                            onPriceAreaClick = { onToggleCurrency(offer.id) },
                            onEditClick = {
                                if (!isExpanded) {
                                    onToggleExpand(offer.id)
                                }
                                editingOfferId = offer.id
                            },
                            onSaveEdit = { updated ->
                                onUpdateOffer(updated)
                                editingOfferId = null
                            },
                            onCancelEdit = { editingOfferId = null },
                            onExcelClick = { excelPreviewOffer = offer },
                            onDeleteClick = { pendingDeleteOffer = offer }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 92.dp)
        )

        FloatingActionButton(
            onClick = { showCreateSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 92.dp)
                .size(56.dp)
                .testTag("cnf_add_fab"),
            shape = CircleShape,
            containerColor = WalletIndigo,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.cd_add_offer),
                modifier = Modifier.size(26.dp)
            )
        }
    }

    if (showCreateSheet) {
        CreateOfferBottomSheet(
            onDismiss = { showCreateSheet = false },
            onCreateOffer = { offerName, serialNumber, mediatorName, customerName, route, dateMillis ->
                onCreateOffer(offerName, serialNumber, mediatorName, customerName, route, dateMillis)
                showCreateSheet = false
            }
        )
    }

    excelPreviewOffer?.let { offer ->
        ExcelPreviewSheet(
            offer = offer,
            onDismiss = { excelPreviewOffer = null },
            onExportCsvToUri = onExportOfferCsv
        )
    }

    pendingDeleteOffer?.let { offer ->
        AlertDialog(
            onDismissRequest = { pendingDeleteOffer = null },
            title = { Text("Hapus Penawaran?") },
            text = {
                Text("Penawaran \"${offer.displayHeader}\" akan dihapus permanen dari database aplikasi.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteOffer(offer.id)
                        pendingDeleteOffer = null
                    },
                    modifier = Modifier.testTag("confirm_delete_offer_button")
                ) {
                    Text("Hapus", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteOffer = null }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun CnfCalculationHeader(onSearchClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
    ) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = WalletIndigo,
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Folder,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.label_cnf_calculation),
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.cnf_calc_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(48.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("cnf_search_button"),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceMuted,
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.cd_search_offers),
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpectedProfitHeroCard(
    totalExpectedProfitIdr: Long,
    offerCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 5.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            CnfEmeraldStart,
                            CnfEmeraldEnd,
                            Color(0xFF047857)
                        )
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.height * 0.65f,
                    center = Offset(size.width * 0.90f, size.height * 0.38f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.height * 0.40f,
                    center = Offset(size.width * 0.78f, size.height * 0.80f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.cnf_expected_pl_title),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = CurrencyFormatter.formatRupiah(totalExpectedProfitIdr),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("cnf_expected_pl_amount")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Total R/L dari $offerCount penawaran",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.84f)
                )
            }
        }
    }
}

@Composable
private fun CnfOfferCard(
    offer: CnfOfferEntity,
    isExpanded: Boolean,
    isEditing: Boolean,
    isIdrMode: Boolean,
    onCardClick: () -> Unit,
    onPriceAreaClick: () -> Unit,
    onEditClick: () -> Unit,
    onSaveEdit: (CnfOfferEntity) -> Unit,
    onCancelEdit: () -> Unit,
    onExcelClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    fun formatHeaderValue(usdAmount: Long, idrAmount: Long): String {
        return if (isIdrMode) {
            CurrencyFormatter.formatRupiah(idrAmount)
        } else {
            CurrencyFormatter.formatUsd(usdAmount)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onCardClick
            )
            .testTag("cnf_offer_card_${offer.id}"),
        shape = RoundedCornerShape(18.dp),
        color = CardSurface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = offer.displayHeader,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("cnf_edit_offer_${offer.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.cd_edit_offer),
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (offer.route.isNotBlank()) {
                        Text(
                            text = offer.route,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right Side: Clickable Price Area (toggles USD <-> IDR)
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isIdrMode) WalletIndigo.copy(alpha = 0.05f) else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = onPriceAreaClick
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("cnf_price_area_${offer.id}"),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = WalletIndigo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isIdrMode) 11.sp else 15.sp
                                )
                            ) {
                                append(formatHeaderValue(offer.cnfPriceUsd, offer.totalSellIdr))
                            }
                            withStyle(
                                SpanStyle(
                                    color = TextMuted,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            ) {
                                append(" / ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isIdrMode) 11.sp else 15.sp
                                )
                            ) {
                                append(formatHeaderValue(offer.dealPriceUsd, offer.dealPriceIdr))
                            }
                        },
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )

                    HorizontalDivider(
                        modifier = Modifier
                            .width(if (isIdrMode) 172.dp else 135.dp)
                            .padding(vertical = 5.dp),
                        thickness = 1.dp,
                        color = SubtleBorder
                    )

                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = if (isIdrMode) 10.sp else 13.sp
                                )
                            ) {
                                append(formatHeaderValue(offer.totalCostUsd, offer.totalCostIdr))
                            }
                            withStyle(
                                SpanStyle(
                                    color = TextMuted,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            ) {
                                append(" / R/L ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = if (offer.profitLossUsd >= 0L) IncomeGreen else ExpenseRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isIdrMode) 10.sp else 13.sp
                                )
                            ) {
                                append(formatHeaderValue(offer.profitLossUsd, offer.profitLossIdr))
                            }
                        },
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }

            // Expanded Breakdown View or Inline Edit Form
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (isEditing) {
                    InlineEditBreakdownSection(
                        offer = offer,
                        onSave = onSaveEdit,
                        onCancel = onCancelEdit
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    HorizontalDivider(color = SubtleBorder, thickness = 1.dp)

                    DualPriceBreakdownRow(
                        label = "Unit Price",
                        sellIdr = offer.unitPriceSellIdr,
                        costIdr = offer.unitPriceCostIdr
                    )
                    DualPriceBreakdownRow(
                        label = "Mobilisation",
                        sellIdr = offer.mobilisationSellIdr,
                        costIdr = offer.mobilisationCostIdr
                    )
                    DualPriceBreakdownRow(
                        label = "Stuffing",
                        sellIdr = offer.stuffingSellIdr,
                        costIdr = offer.stuffingCostIdr
                    )
                    DualPriceBreakdownRow(
                        label = "Freight",
                        sellIdr = offer.freightSellIdr,
                        costIdr = offer.freightCostIdr
                    )

                    // Success Fee (Single Price Only)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Success Fee",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = CurrencyFormatter.formatRupiah(offer.successFeeSellIdr),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = TextPrimary
                        )
                    }

                    DualPriceBreakdownRow(
                        label = "Others",
                        sellIdr = offer.othersSellIdr,
                        costIdr = offer.othersCostIdr
                    )

                    HorizontalDivider(color = SubtleBorder, thickness = 1.dp)

                    // CnF Price (Syncs USD <-> IDR with Header Price Area)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CnF Price",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary
                        )
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.testTag("cnf_detail_price_${offer.id}")
                        ) {
                            Text(
                                text = if (isIdrMode) {
                                    CurrencyFormatter.formatRupiah(offer.totalSellIdr)
                                } else {
                                    CurrencyFormatter.formatUsd(offer.cnfPriceUsd)
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = WalletIndigo
                            )
                            Text(
                                text = if (isIdrMode) {
                                    CurrencyFormatter.formatUsd(offer.cnfPriceUsd)
                                } else {
                                    CurrencyFormatter.formatRupiah(offer.totalSellIdr)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    // Kurs Saat Ini (Tanggal Penawaran)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                ) {
                                    append("Kurs Saat Ini ")
                                }
                                withStyle(
                                    SpanStyle(
                                        color = TextMuted,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                ) {
                                    append("(${CurrencyFormatter.formatDateMedium(offer.offerDateMillis)})")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = CurrencyFormatter.formatRupiah(offer.safeExchangeRateIdr),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary
                        )
                    }

                    // Action Buttons Row: Edit Breakdown | Excel | Hapus
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onEditClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("cnf_edit_breakdown_${offer.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEEF0FF)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Calculate,
                                    contentDescription = null,
                                    tint = WalletIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Breakdown",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = WalletIndigo
                                )
                            }
                        }

                        Surface(
                            onClick = onExcelClick,
                            modifier = Modifier
                                .size(46.dp)
                                .testTag("cnf_excel_button_${offer.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = CnfEmeraldSoft
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = "Preview export ke Excel",
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(46.dp)
                                .testTag("cnf_delete_button_${offer.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = ExpenseRed.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Hapus penawaran",
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun DualPriceBreakdownRow(
    label: String,
    sellIdr: Long,
    costIdr: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        if (sellIdr == 0L && costIdr == 0L) {
            Text(
                text = CurrencyFormatter.formatRupiah(0L),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = TextPrimary
            )
        } else {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    ) {
                        append(CurrencyFormatter.formatRupiah(sellIdr))
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
                            color = IncomeGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    ) {
                        append(CurrencyFormatter.formatRupiah(costIdr))
                    }
                },
                textAlign = TextAlign.End
            )
        }
    }
}
