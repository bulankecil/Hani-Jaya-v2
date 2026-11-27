package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.roundToLong

data class FreightContainerItem(
    val containerType: String = "40'HC",
    val rateUsd: Long = 0L,
    val qty: Int = 1
)

@Entity(tableName = "cnf_offers")
data class CnfOfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val offerName: String,
    val serialNumber: String = "",
    val mediatorName: String = "",
    val customerName: String = "",
    val route: String = "",
    val offerDateMillis: Long = System.currentTimeMillis(),
    val exchangeRateIdr: Long = DEFAULT_EXCHANGE_RATE_IDR,
    val spreadIdr: Long = DEFAULT_SPREAD_IDR,
    val unitCount: Int = 1,
    val unit1Idr: Long = 0L,
    val unit2Idr: Long = 0L,
    val unit3Idr: Long = 0L,
    val unit4Idr: Long = 0L,
    val unitPriceCostIdr: Long = 0L,
    val mob1Idr: Long = 0L,
    val mob2Idr: Long = 0L,
    val mob3Idr: Long = 0L,
    val mobilisationManualIdr: Long = 0L,
    val mobilisationCostIdr: Long = 0L,
    val stuffingSellIdr: Long = 0L,
    val stuffingCostIdr: Long = 0L,
    val freightContainersSpec: String = DEFAULT_FREIGHT_SPEC,
    val freightManualIdr: Long = 0L,
    val freightCostIdr: Long = 0L,
    val successFeeMultiplier: Int = 0,
    val othersMultiplier: Int = 0,
    val othersCostIdr: Long = 0L,
    val uangMasukIdr: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayHeader: String
        get() = if (customerName.isNotBlank()) {
            "$offerName - $customerName"
        } else {
            offerName
        }

    val safeExchangeRateIdr: Long
        get() = exchangeRateIdr.coerceAtLeast(1L)

    val effectiveRateIdr: Long
        get() = (safeExchangeRateIdr - spreadIdr.coerceAtLeast(0L)).coerceAtLeast(1L)

    val unitPriceSellIdr: Long
        get() {
            val activeCount = unitCount.coerceIn(1, 4)
            var total = unit1Idr
            if (activeCount >= 2) total += unit2Idr
            if (activeCount >= 3) total += unit3Idr
            if (activeCount >= 4) total += unit4Idr
            return total
        }

    val mobilisationAutoIdr: Long
        get() = mob1Idr + mob2Idr + mob3Idr

    val mobilisationSellIdr: Long
        get() = if (mobilisationManualIdr > 0L) mobilisationManualIdr else mobilisationAutoIdr

    val freightItems: List<FreightContainerItem>
        get() = parseFreightSpec(freightContainersSpec)

    val freightAutoIdr: Long
        get() = freightItems.sumOf { item ->
            item.rateUsd.coerceAtLeast(0L) * item.qty.coerceAtLeast(0) * effectiveRateIdr
        }

    val freightSellIdr: Long
        get() = if (freightManualIdr > 0L) freightManualIdr else freightAutoIdr

    val successFeeSellIdr: Long
        get() = 1_000L * successFeeMultiplier.coerceAtLeast(0) * effectiveRateIdr

    val othersSellIdr: Long
        get() = 1_000L * othersMultiplier.coerceAtLeast(0) * effectiveRateIdr

    val totalSellIdr: Long
        get() = unitPriceSellIdr +
            mobilisationSellIdr +
            stuffingSellIdr +
            freightSellIdr +
            successFeeSellIdr +
            othersSellIdr

    val totalCostIdr: Long
        get() = unitPriceCostIdr +
            mobilisationCostIdr +
            stuffingCostIdr +
            freightCostIdr +
            othersCostIdr

    val dealPriceUsd: Long
        get() = (uangMasukIdr.toDouble() / effectiveRateIdr).roundToLong()

    val dealPriceIdr: Long
        get() = uangMasukIdr

    val cnfPriceUsd: Long
        get() = (totalSellIdr.toDouble() / effectiveRateIdr).roundToLong()

    val totalCostUsd: Long
        get() = (totalCostIdr.toDouble() / effectiveRateIdr).roundToLong()

    val successFeeUsd: Long
        get() = (successFeeSellIdr.toDouble() / effectiveRateIdr).roundToLong()

    val profitLossUsd: Long
        get() = (dealPriceUsd - totalCostUsd) + successFeeUsd

    val profitLossIdr: Long
        get() = (dealPriceIdr - totalCostIdr) + successFeeSellIdr

    companion object {
        const val DEFAULT_EXCHANGE_RATE_IDR = 17_000L
        const val DEFAULT_SPREAD_IDR = 1_000L
        const val DEFAULT_FREIGHT_SPEC = "40'HC|0|1"

        val CONTAINER_OPTIONS = listOf("20'GP", "40'GP", "40'HC", "Flat Rack")

        fun parseFreightSpec(spec: String): List<FreightContainerItem> {
            if (spec.isBlank()) return listOf(FreightContainerItem())
            val parsed = spec.split(";").mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size >= 3) {
                    FreightContainerItem(
                        containerType = parts[0].ifBlank { "40'HC" },
                        rateUsd = parts[1].toLongOrNull() ?: 0L,
                        qty = (parts[2].toIntOrNull() ?: 1).coerceAtLeast(0)
                    )
                } else {
                    null
                }
            }
            return parsed.ifEmpty { listOf(FreightContainerItem()) }
        }

        fun encodeFreightSpec(items: List<FreightContainerItem>): String {
            if (items.isEmpty()) return DEFAULT_FREIGHT_SPEC
            return items.joinToString(";") { item ->
                val cleanType = item.containerType.replace("|", "").replace(";", "")
                "$cleanType|${item.rateUsd.coerceAtLeast(0L)}|${item.qty.coerceAtLeast(0)}"
            }
        }
    }
}
