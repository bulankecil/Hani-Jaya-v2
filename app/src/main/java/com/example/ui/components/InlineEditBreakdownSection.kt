package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CnfOfferEntity
import com.example.data.local.FreightContainerItem
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CostYellowBg
import com.example.ui.theme.CostYellowBorder
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WalletIndigo
import com.example.ui.util.CurrencyFormatter

private data class FreightDraftRow(
    val key: Int,
    val containerType: String,
    val rateDigits: String,
    val qtyDigits: String
)

private fun initialDigits(value: Long): String = if (value <= 0L) "" else value.toString()

private fun initialDigits(value: Int): String = if (value <= 0) "" else value.toString()

private fun sanitizeDigits(input: String, maxLength: Int = 14): String {
    val digits = input.filter { it.isDigit() }.take(maxLength)
    return digits.trimStart('0')
}

private fun parseDigitsLong(digits: String): Long = digits.toLongOrNull() ?: 0L

private fun parseDigitsInt(digits: String): Int = digits.toIntOrNull() ?: 0

private class IndonesianNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val builder = StringBuilder(raw.length + raw.length / 3)
        val origToTrans = IntArray(raw.length + 1)
        val transToOrig = ArrayList<Int>(raw.length + raw.length / 3 + 1)

        for (i in raw.indices) {
            origToTrans[i] = builder.length
            builder.append(raw[i])
            transToOrig.add(i)
            val remaining = raw.length - 1 - i
            if (remaining > 0 && remaining % 3 == 0) {
                builder.append('.')
                transToOrig.add(i + 1)
            }
        }
        origToTrans[raw.length] = builder.length
        transToOrig.add(raw.length)

        val formatted = builder.toString()
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return origToTrans[offset.coerceIn(0, raw.length)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transToOrig[offset.coerceIn(0, formatted.length)]
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InlineEditBreakdownSection(
    offer: CnfOfferEntity,
    onSave: (CnfOfferEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var exchangeRateDigits by remember(offer.id) { mutableStateOf(initialDigits(offer.exchangeRateIdr)) }
    var spreadDigits by remember(offer.id) { mutableStateOf(initialDigits(offer.spreadIdr)) }
    var offerDateMillis by remember(offer.id) { mutableLongStateOf(offer.offerDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    var unitCount by remember(offer.id) { mutableIntStateOf(offer.unitCount.coerceIn(1, 4)) }
    var unit1Digits by remember(offer.id) { mutableStateOf(initialDigits(offer.unit1Idr)) }
    var unit2Digits by remember(offer.id) { mutableStateOf(initialDigits(offer.unit2Idr)) }
    var unit3Digits by remember(offer.id) { mutableStateOf(initialDigits(offer.unit3Idr)) }
    var unit4Digits by remember(offer.id) { mutableStateOf(initialDigits(offer.unit4Idr)) }
    var unitPriceCostDigits by remember(offer.id) { mutableStateOf(initialDigits(offer.unitPriceCostIdr)) }

    var mob1Digits by remember(offer.id) { mutableStateOf(initialDigits(offer.mob1Idr)) }
    var mob2Digits by remember(offer.id) { mutableStateOf(initialDigits(offer.mob2Idr)) }
    var mob3Digits by remember(offer.id) { mutableStateOf(initialDigits(offer.mob3Idr)) }
    var mobilisationManualDigits by remember(offer.id) {
        mutableStateOf(initialDigits(offer.mobilisationManualIdr))
    }
    var mobilisationCostDigits by remember(offer.id) {
        mutableStateOf(initialDigits(offer.mobilisationCostIdr))
    }

    var stuffingSellDigits by remember(offer.id) { mutableStateOf(initialDigits(offer.stuffingSellIdr)) }
    var stuffingCostDigits by remember(offer.id) { mutableStateOf(initialDigits(offer.stuffingCostIdr)) }

    var nextFreightRowKey by remember(offer.id) { mutableIntStateOf(100) }
    val freightRows = remember(offer.id) {
        mutableStateListOf<FreightDraftRow>().apply {
            offer.freightItems.forEachIndexed { idx, item ->
                add(
                    FreightDraftRow(
                        key = idx + 1,
                        containerType = item.containerType,
                        rateDigits = initialDigits(item.rateUsd),
                        qtyDigits = item.qty.coerceAtLeast(1).toString()
                    )
                )
            }
        }
    }
    var freightManualDigits by remember(offer.id) { mutableStateOf(initialDigits(offer.freightManualIdr)) }
    var freightCostDigits by remember(offer.id) { mutableStateOf(initialDigits(offer.freightCostIdr)) }

    var successFeeMultiplierDigits by remember(offer.id) {
        mutableStateOf(initialDigits(offer.successFeeMultiplier))
    }
    var othersMultiplierDigits by remember(offer.id) {
        mutableStateOf(initialDigits(offer.othersMultiplier))
    }
    var othersCostDigits by remember(offer.id) { mutableStateOf(initialDigits(offer.othersCostIdr)) }
    var uangMasukDigits by remember(offer.id) { mutableStateOf(initialDigits(offer.uangMasukIdr)) }

    val draftOffer = remember(
        offer,
        exchangeRateDigits,
        spreadDigits,
        offerDateMillis,
        unitCount,
        unit1Digits,
        unit2Digits,
        unit3Digits,
        unit4Digits,
        unitPriceCostDigits,
        mob1Digits,
        mob2Digits,
        mob3Digits,
        mobilisationManualDigits,
        mobilisationCostDigits,
        stuffingSellDigits,
        stuffingCostDigits,
        freightRows.toList(),
        freightManualDigits,
        freightCostDigits,
        successFeeMultiplierDigits,
        othersMultiplierDigits,
        othersCostDigits,
        uangMasukDigits
    ) {
        val encodedFreight = CnfOfferEntity.encodeFreightSpec(
            freightRows.map { row ->
                FreightContainerItem(
                    containerType = row.containerType,
                    rateUsd = parseDigitsLong(row.rateDigits),
                    qty = parseDigitsInt(row.qtyDigits)
                )
            }
        )
        offer.copy(
            offerDateMillis = offerDateMillis,
            exchangeRateIdr = parseDigitsLong(exchangeRateDigits).coerceAtLeast(1L),
            spreadIdr = parseDigitsLong(spreadDigits),
            unitCount = unitCount.coerceIn(1, 4),
            unit1Idr = parseDigitsLong(unit1Digits),
            unit2Idr = if (unitCount >= 2) parseDigitsLong(unit2Digits) else 0L,
            unit3Idr = if (unitCount >= 3) parseDigitsLong(unit3Digits) else 0L,
            unit4Idr = if (unitCount >= 4) parseDigitsLong(unit4Digits) else 0L,
            unitPriceCostIdr = parseDigitsLong(unitPriceCostDigits),
            mob1Idr = parseDigitsLong(mob1Digits),
            mob2Idr = parseDigitsLong(mob2Digits),
            mob3Idr = parseDigitsLong(mob3Digits),
            mobilisationManualIdr = parseDigitsLong(mobilisationManualDigits),
            mobilisationCostIdr = parseDigitsLong(mobilisationCostDigits),
            stuffingSellIdr = parseDigitsLong(stuffingSellDigits),
            stuffingCostIdr = parseDigitsLong(stuffingCostDigits),
            freightContainersSpec = encodedFreight,
            freightManualIdr = parseDigitsLong(freightManualDigits),
            freightCostIdr = parseDigitsLong(freightCostDigits),
            successFeeMultiplier = parseDigitsInt(successFeeMultiplierDigits),
            othersMultiplier = parseDigitsInt(othersMultiplierDigits),
            othersCostIdr = parseDigitsLong(othersCostDigits),
            uangMasukIdr = parseDigitsLong(uangMasukDigits)
        )
    }

    fun removeUnitAt(unitNumber: Int) {
        when (unitNumber) {
            2 -> {
                unit2Digits = unit3Digits
                unit3Digits = unit4Digits
                unit4Digits = ""
                unitCount = (unitCount - 1).coerceAtLeast(1)
            }
            3 -> {
                unit3Digits = unit4Digits
                unit4Digits = ""
                unitCount = (unitCount - 1).coerceAtLeast(1)
            }
            4 -> {
                unit4Digits = ""
                unitCount = (unitCount - 1).coerceAtLeast(1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(color = SubtleBorder, thickness = 1.dp)

        // Row 1: Kurs Saat Ini | Spread | Per Tanggal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            LabeledInlineNumberBox(
                label = "Kurs Saat Ini",
                prefix = "Rp",
                digits = exchangeRateDigits,
                onDigitsChange = { exchangeRateDigits = sanitizeDigits(it, 9) },
                modifier = Modifier.weight(1.02f),
                testTag = "inline_kurs_input"
            )
            LabeledInlineNumberBox(
                label = "Spread",
                prefix = "Rp",
                digits = spreadDigits,
                onDigitsChange = { spreadDigits = sanitizeDigits(it, 9) },
                modifier = Modifier.weight(0.86f),
                testTag = "inline_spread_input"
            )
            Column(
                modifier = Modifier.weight(1.18f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Per Tanggal",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("inline_date_picker_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = CurrencyFormatter.formatDateDayMonthYear(offerDateMillis),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = "Pilih tanggal kurs",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Kurs Efektif helper caption
        Text(
            text = "Kurs Efektif: ${CurrencyFormatter.formatRupiah(draftOffer.effectiveRateIdr)}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = TextMuted
        )

        // UNIT PRICE Section
        InlineSectionHeader(title = "UNIT PRICE")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LabeledInlineNumberBox(
                label = "Unit 1 (IDR)",
                prefix = "Rp",
                digits = unit1Digits,
                onDigitsChange = { unit1Digits = sanitizeDigits(it) },
                modifier = Modifier.weight(1f),
                testTag = "inline_unit_1_input"
            )
            if (unitCount >= 2) {
                LabeledInlineNumberBox(
                    label = "Unit 2 (IDR)",
                    prefix = "Rp",
                    digits = unit2Digits,
                    onDigitsChange = { unit2Digits = sanitizeDigits(it) },
                    onDeleteClick = { removeUnitAt(2) },
                    deleteContentDescription = "Hapus Unit 2",
                    deleteTestTag = "inline_delete_unit_2",
                    modifier = Modifier.weight(1f),
                    testTag = "inline_unit_2_input"
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (unitCount >= 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LabeledInlineNumberBox(
                    label = "Unit 3 (IDR)",
                    prefix = "Rp",
                    digits = unit3Digits,
                    onDigitsChange = { unit3Digits = sanitizeDigits(it) },
                    onDeleteClick = { removeUnitAt(3) },
                    deleteContentDescription = "Hapus Unit 3",
                    deleteTestTag = "inline_delete_unit_3",
                    modifier = Modifier.weight(1f),
                    testTag = "inline_unit_3_input"
                )
                if (unitCount >= 4) {
                    LabeledInlineNumberBox(
                        label = "Unit 4 (IDR)",
                        prefix = "Rp",
                        digits = unit4Digits,
                        onDigitsChange = { unit4Digits = sanitizeDigits(it) },
                        onDeleteClick = { removeUnitAt(4) },
                        deleteContentDescription = "Hapus Unit 4",
                        deleteTestTag = "inline_delete_unit_4",
                        modifier = Modifier.weight(1f),
                        testTag = "inline_unit_4_input"
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (unitCount < 4) {
            Surface(
                onClick = { unitCount = (unitCount + 1).coerceAtMost(4) },
                modifier = Modifier
                    .height(44.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("inline_add_unit_button"),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEEF0FF)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = WalletIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tambah Unit",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WalletIndigo
                    )
                }
            }
        }

        // Read-only Total Unit Price (Harga Jual)
        ReadOnlyMoneyBox(
            prefix = "Rp",
            amount = draftOffer.unitPriceSellIdr,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inline_unit_total_box")
        )

        // Yellow-shaded Harga Ekspektasi (Unit Price)
        LabeledInlineNumberBox(
            label = "Harga Ekspektasi (IDR)",
            prefix = "Rp",
            digits = unitPriceCostDigits,
            onDigitsChange = { unitPriceCostDigits = sanitizeDigits(it) },
            containerColor = CostYellowBg,
            borderColor = CostYellowBorder,
            testTag = "inline_unit_cost_input"
        )

        // MOBILISATION Section
        InlineSectionHeader(title = "MOBILISATION")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LabeledInlineNumberBox(
                label = "Mob 1 (IDR)",
                prefix = "Rp",
                digits = mob1Digits,
                onDigitsChange = { mob1Digits = sanitizeDigits(it) },
                modifier = Modifier.weight(1f),
                testTag = "inline_mob1_input"
            )
            LabeledInlineNumberBox(
                label = "Mob 2 (IDR)",
                prefix = "Rp",
                digits = mob2Digits,
                onDigitsChange = { mob2Digits = sanitizeDigits(it) },
                modifier = Modifier.weight(1f),
                testTag = "inline_mob2_input"
            )
            LabeledInlineNumberBox(
                label = "Mob 3 (IDR)",
                prefix = "Rp",
                digits = mob3Digits,
                onDigitsChange = { mob3Digits = sanitizeDigits(it) },
                modifier = Modifier.weight(1f),
                testTag = "inline_mob3_input"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LabeledInlineNumberBox(
                label = "Manual",
                prefix = "Rp",
                digits = mobilisationManualDigits,
                onDigitsChange = { mobilisationManualDigits = sanitizeDigits(it) },
                modifier = Modifier.weight(1f),
                testTag = "inline_mob_manual_input"
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Otomatis",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary
                )
                ReadOnlyMoneyBox(
                    prefix = "Rp",
                    amount = draftOffer.mobilisationAutoIdr,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inline_mob_auto_box")
                )
            }
        }

        LabeledInlineNumberBox(
            label = "Harga Ekspektasi (IDR)",
            prefix = "Rp",
            digits = mobilisationCostDigits,
            onDigitsChange = { mobilisationCostDigits = sanitizeDigits(it) },
            containerColor = CostYellowBg,
            borderColor = CostYellowBorder,
            testTag = "inline_mob_cost_input"
        )

        // STUFFING Section
        InlineSectionHeader(title = "STUFFING")

        LabeledInlineNumberBox(
            label = null,
            prefix = "Rp",
            digits = stuffingSellDigits,
            onDigitsChange = { stuffingSellDigits = sanitizeDigits(it) },
            testTag = "inline_stuffing_sell_input"
        )

        LabeledInlineNumberBox(
            label = "Harga Ekspektasi (IDR)",
            prefix = "Rp",
            digits = stuffingCostDigits,
            onDigitsChange = { stuffingCostDigits = sanitizeDigits(it) },
            containerColor = CostYellowBg,
            borderColor = CostYellowBorder,
            testTag = "inline_stuffing_cost_input"
        )

        // FREIGHT Section
        InlineSectionHeader(title = "FREIGHT")

        freightRows.forEachIndexed { index, row ->
            FreightContainerInputRow(
                row = row,
                isFirstRow = index == 0,
                onTypeChange = { newType ->
                    freightRows[index] = row.copy(containerType = newType)
                },
                onRateChange = { newRate ->
                    freightRows[index] = row.copy(rateDigits = sanitizeDigits(newRate, 8))
                },
                onQtyChange = { newQty ->
                    freightRows[index] = row.copy(qtyDigits = sanitizeDigits(newQty, 3))
                },
                onAddRow = {
                    val newKey = ++nextFreightRowKey
                    freightRows.add(
                        FreightDraftRow(
                            key = newKey,
                            containerType = "40'HC",
                            rateDigits = "",
                            qtyDigits = "1"
                        )
                    )
                },
                onRemoveRow = {
                    if (freightRows.size > 1) {
                        freightRows.removeAt(index)
                    }
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LabeledInlineNumberBox(
                label = "Manual",
                prefix = "Rp",
                digits = freightManualDigits,
                onDigitsChange = { freightManualDigits = sanitizeDigits(it) },
                modifier = Modifier.weight(1f),
                testTag = "inline_freight_manual_input"
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Otomatis",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary
                )
                ReadOnlyMoneyBox(
                    prefix = "Rp",
                    amount = draftOffer.freightAutoIdr,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inline_freight_auto_box")
                )
            }
        }

        LabeledInlineNumberBox(
            label = "Harga Ekspektasi (IDR)",
            prefix = "Rp",
            digits = freightCostDigits,
            onDigitsChange = { freightCostDigits = sanitizeDigits(it) },
            containerColor = CostYellowBg,
            borderColor = CostYellowBorder,
            testTag = "inline_freight_cost_input"
        )

        // Success Fee (IDR) (otomatis) + Multiplier
        AutoMultiplierRow(
            label = "Success Fee (IDR)",
            autoAmountIdr = draftOffer.successFeeSellIdr,
            multiplierDigits = successFeeMultiplierDigits,
            onMultiplierChange = { successFeeMultiplierDigits = sanitizeDigits(it, 3) },
            multiplierTestTag = "inline_success_fee_multiplier_input"
        )

        // Others (IDR) (otomatis) + Multiplier + Harga Ekspektasi
        AutoMultiplierRow(
            label = "Others (IDR)",
            autoAmountIdr = draftOffer.othersSellIdr,
            multiplierDigits = othersMultiplierDigits,
            onMultiplierChange = { othersMultiplierDigits = sanitizeDigits(it, 3) },
            multiplierTestTag = "inline_others_multiplier_input"
        )

        LabeledInlineNumberBox(
            label = "Harga Ekspektasi (IDR)",
            prefix = "Rp",
            digits = othersCostDigits,
            onDigitsChange = { othersCostDigits = sanitizeDigits(it) },
            containerColor = CostYellowBg,
            borderColor = CostYellowBorder,
            testTag = "inline_others_cost_input"
        )

        // PERKIRAAN R/L Section
        InlineSectionHeader(title = "PERKIRAAN R/L")

        LabeledInlineNumberBox(
            label = "Uang Masuk (Total) (IDR)",
            prefix = "Rp",
            digits = uangMasukDigits,
            onDigitsChange = { uangMasukDigits = sanitizeDigits(it) },
            testTag = "inline_uang_masuk_input"
        )

        // Live computed summary preview
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceMuted,
            border = BorderStroke(1.dp, SubtleBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Harga Deal ($)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                    Text(
                        text = CurrencyFormatter.formatUsd(draftOffer.dealPriceUsd),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = IncomeGreen,
                        modifier = Modifier.testTag("inline_preview_deal_usd")
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CnF Price ($)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                    Text(
                        text = CurrencyFormatter.formatUsd(draftOffer.cnfPriceUsd),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = WalletIndigo,
                        modifier = Modifier.testTag("inline_preview_cnf_usd")
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Perkiraan R/L",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                    Text(
                        text = CurrencyFormatter.formatUsd(draftOffer.profitLossUsd),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = if (draftOffer.profitLossUsd >= 0L) IncomeGreen else ExpenseRed,
                        modifier = Modifier.testTag("inline_preview_rl_usd")
                    )
                }
            }
        }

        // Bottom Action Buttons: Simpan & Batal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onSave(draftOffer) },
                modifier = Modifier
                    .weight(1.4f)
                    .height(48.dp)
                    .testTag("inline_save_breakdown_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WalletIndigo,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Simpan",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            Surface(
                onClick = onCancel,
                modifier = Modifier
                    .weight(0.8f)
                    .height(48.dp)
                    .testTag("inline_cancel_breakdown_button"),
                shape = RoundedCornerShape(14.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, SubtleBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Batal",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = TextPrimary
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = offerDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            offerDateMillis = selected
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih", fontWeight = FontWeight.Bold, color = WalletIndigo)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun InlineSectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = SubtleBorder
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = TextMuted
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = SubtleBorder
        )
    }
}

@Composable
private fun LabeledInlineNumberBox(
    label: String?,
    prefix: String,
    digits: String,
    onDigitsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CardSurface,
    borderColor: Color = SubtleBorder,
    useGrouping: Boolean = true,
    onDeleteClick: (() -> Unit)? = null,
    deleteContentDescription: String? = null,
    deleteTestTag: String? = null,
    testTag: String? = null
) {
    val visualTransformation = remember(useGrouping) {
        if (useGrouping) IndonesianNumberVisualTransformation() else VisualTransformation.None
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (label != null || onDeleteClick != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.orEmpty(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (onDeleteClick != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onDeleteClick)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .then(
                                if (deleteTestTag != null) Modifier.testTag(deleteTestTag) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = deleteContentDescription ?: "Hapus",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (prefix.isNotEmpty()) {
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (digits.isEmpty()) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextMuted
                    )
                }
                BasicTextField(
                    value = digits,
                    onValueChange = onDigitsChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = visualTransformation,
                    cursorBrush = SolidColor(WalletIndigo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyMoneyBox(
    prefix: String,
    amount: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMuted)
            .border(1.dp, SubtleBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prefix,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = TextMuted
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = CurrencyFormatter.formatGroupNumber(amount),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FreightContainerInputRow(
    row: FreightDraftRow,
    isFirstRow: Boolean,
    onTypeChange: (String) -> Unit,
    onRateChange: (String) -> Unit,
    onQtyChange: (String) -> Unit,
    onAddRow: () -> Unit,
    onRemoveRow: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.weight(1.3f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (isFirstRow) {
                Text(
                    text = "Container",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary
                )
            }
            Box {
                Surface(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("inline_freight_container_${row.key}"),
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = row.containerType,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Pilih tipe container",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    CnfOfferEntity.CONTAINER_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onTypeChange(option)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        LabeledInlineNumberBox(
            label = if (isFirstRow) "Rate" else null,
            prefix = "$",
            digits = row.rateDigits,
            onDigitsChange = onRateChange,
            modifier = Modifier.weight(0.95f),
            testTag = "inline_freight_rate_${row.key}"
        )

        LabeledInlineNumberBox(
            label = if (isFirstRow) "Qty" else null,
            prefix = "",
            digits = row.qtyDigits,
            onDigitsChange = onQtyChange,
            useGrouping = false,
            modifier = Modifier.weight(0.62f),
            testTag = "inline_freight_qty_${row.key}"
        )

        if (isFirstRow) {
            Surface(
                onClick = onAddRow,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("inline_add_freight_row"),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEEF0FF)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Tambah baris container",
                        tint = WalletIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Surface(
                onClick = onRemoveRow,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("inline_remove_freight_row_${row.key}"),
                shape = RoundedCornerShape(12.dp),
                color = ExpenseRed.copy(alpha = 0.10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Hapus baris container",
                        tint = ExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoMultiplierRow(
    label: String,
    autoAmountIdr: Long,
    multiplierDigits: String,
    onMultiplierChange: (String) -> Unit,
    multiplierTestTag: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "(otomatis)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextMuted
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReadOnlyMoneyBox(
                prefix = "Rp",
                amount = autoAmountIdr,
                modifier = Modifier.weight(1.45f)
            )

            Row(
                modifier = Modifier
                    .weight(0.65f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .border(1.dp, SubtleBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "×",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (multiplierDigits.isEmpty()) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextMuted
                        )
                    }
                    BasicTextField(
                        value = multiplierDigits,
                        onValueChange = onMultiplierChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(WalletIndigo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(multiplierTestTag)
                    )
                }
            }
        }
    }
}
