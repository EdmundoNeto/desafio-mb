package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object VolumeFormatter {

    private val scales = listOf(
        1_000_000_000.0 to "bi",
        1_000_000.0 to "mi",
        1_000.0 to "mil",
    )

    fun format(value: Double?, locale: Locale = Locale.getDefault()): String {
        if (value == null || value == 0.0) return "—"

        val (divisor, suffix) = scales.firstOrNull { (scale, _) -> value >= scale } ?: (1.0 to null)
        val scaled = BigDecimal(value).divide(BigDecimal(divisor), 2, RoundingMode.HALF_UP)
        val formatted = DecimalFormat("0.00", DecimalFormatSymbols.getInstance(locale)).format(scaled)
        return if (suffix == null) "US$ $formatted" else "US$ $formatted $suffix"
    }
}
