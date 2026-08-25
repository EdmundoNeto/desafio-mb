package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object VolumeFormatter {
    private enum class Scale(
        val floor: BigDecimal,
        val divisor: BigDecimal,
        val suffix: String?,
    ) {
        BILLION(BigDecimal("1000000000"), BigDecimal("1000000000"), "bi"),
        MILLION(BigDecimal("1000000"), BigDecimal("1000000"), "mi"),
        THOUSAND(BigDecimal("1000"), BigDecimal("1000"), "mil"),
        UNIT(BigDecimal.ZERO, BigDecimal.ONE, null),
    }

    fun format(
        value: Double?,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (value == null || value == 0.0) return MISSING_VALUE_PLACEHOLDER

        val (scaledAmount, suffix) = resolveScale(BigDecimal.valueOf(value))
        val formatted = DecimalFormat("0.00", DecimalFormatSymbols.getInstance(locale)).format(scaledAmount)
        return if (suffix == null) "US$ $formatted" else "US$ $formatted $suffix"
    }

    private fun resolveScale(amount: BigDecimal): Pair<BigDecimal, String?> {
        val magnitude = amount.abs()
        val scale = Scale.entries.first { magnitude >= it.floor }
        val divided = amount.divide(scale.divisor, 2, RoundingMode.HALF_UP)

        val biggerScale = Scale.entries.getOrNull(scale.ordinal - 1)
        return if (biggerScale != null && divided.abs() >= BigDecimal(1000)) {
            amount.divide(biggerScale.divisor, 2, RoundingMode.HALF_UP) to biggerScale.suffix
        } else {
            divided to scale.suffix
        }
    }
}
