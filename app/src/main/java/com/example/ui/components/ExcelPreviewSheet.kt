package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CnfOfferEntity
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CnfEmeraldSoft
import com.example.ui.theme.CnfEmeraldStart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WalletIndigo
import com.example.ui.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelPreviewSheet(
    offer: CnfOfferEntity,
    onDismiss: () -> Unit,
    onExportCsvToUri: (Context, Uri, CnfOfferEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            onExportCsvToUri(context, uri, offer)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .navigationBarsPadding()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Preview Export Excel",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "${offer.displayHeader} • ${CurrencyFormatter.formatDateMedium(offer.offerDateMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .minimumInteractiveComponentSize()
                ) {
                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = CardSurface,
                        border = BorderStroke(1.2.dp, TextSecondary.copy(alpha = 0.55f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Tutup preview",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Spreadsheet Table Preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, SubtleBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CnfEmeraldSoft)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Komponen",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CnfEmeraldStart,
                            modifier = Modifier.weight(1.1f)
                        )
                        Text(
                            text = "Harga Jual",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Harga Asli",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = IncomeGreen,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    ExcelTableRow("Unit Price", CurrencyFormatter.formatRupiah(offer.unitPriceSellIdr), CurrencyFormatter.formatRupiah(offer.unitPriceCostIdr))
                    ExcelTableRow("Mobilisation", CurrencyFormatter.formatRupiah(offer.mobilisationSellIdr), CurrencyFormatter.formatRupiah(offer.mobilisationCostIdr))
                    ExcelTableRow("Stuffing", CurrencyFormatter.formatRupiah(offer.stuffingSellIdr), CurrencyFormatter.formatRupiah(offer.stuffingCostIdr))
                    ExcelTableRow("Freight", CurrencyFormatter.formatRupiah(offer.freightSellIdr), CurrencyFormatter.formatRupiah(offer.freightCostIdr))
                    ExcelTableRow("Success Fee", CurrencyFormatter.formatRupiah(offer.successFeeSellIdr), "-")
                    ExcelTableRow("Others", CurrencyFormatter.formatRupiah(offer.othersSellIdr), CurrencyFormatter.formatRupiah(offer.othersCostIdr))

                    HorizontalDivider(color = SubtleBorder)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceMuted)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExcelSummaryRow(
                            label = "CnF Price",
                            value = "${CurrencyFormatter.formatUsd(offer.cnfPriceUsd)} (${CurrencyFormatter.formatRupiah(offer.totalSellIdr)})",
                            color = WalletIndigo
                        )
                        ExcelSummaryRow(
                            label = "Harga Deal",
                            value = CurrencyFormatter.formatUsd(offer.dealPriceUsd),
                            color = IncomeGreen
                        )
                        ExcelSummaryRow(
                            label = "Kurs Saat Ini",
                            value = CurrencyFormatter.formatRupiah(offer.safeExchangeRateIdr),
                            color = TextPrimary
                        )
                        ExcelSummaryRow(
                            label = "Ekspektasi R/L",
                            value = "${CurrencyFormatter.formatUsd(offer.profitLossUsd)} (${CurrencyFormatter.formatRupiah(offer.profitLossIdr)})",
                            color = if (offer.profitLossUsd >= 0L) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Breakdown CnF - ${offer.displayHeader}")
                            putExtra(Intent.EXTRA_TEXT, CurrencyFormatter.buildOfferExcelCsv(offer))
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, "Bagikan Breakdown Excel/CSV")
                        )
                    },
                    modifier = Modifier
                        .weight(0.95f)
                        .height(50.dp)
                        .testTag("excel_share_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        csvLauncher.launch(CurrencyFormatter.formatExcelFileName(offer.offerName))
                    },
                    modifier = Modifier
                        .weight(1.35f)
                        .height(50.dp)
                        .testTag("excel_save_csv_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IncomeGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan CSV (Excel)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ExcelTableRow(
    name: String,
    sell: String,
    cost: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary,
                modifier = Modifier.weight(1.1f)
            )
            Text(
                text = sell,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = cost,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (cost == "-") TextSecondary else IncomeGreen,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = SubtleBorder.copy(alpha = 0.6f))
    }
}

@Composable
private fun ExcelSummaryRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}
