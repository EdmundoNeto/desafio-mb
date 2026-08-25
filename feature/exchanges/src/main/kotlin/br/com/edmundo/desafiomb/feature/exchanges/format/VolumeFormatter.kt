package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object VolumeFormatter {
    private enum class Scale(
        val divisor: BigDecimal,
        val suffix: String?,
    ) {
        BILLION(VOLUME_BILLION_DIVISOR, VOLUME_BILLION_SUFFIX),
        MILLION(VOLUME_MILLION_DIVISOR, VOLUME_MILLION_SUFFIX),
        THOUSAND(VOLUME_THOUSAND_DIVISOR, VOLUME_THOUSAND_SUFFIX),
        UNIT(BigDecimal.ONE, null),
    }

    fun format(
        value: Double?,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (value == null || value == 0.0) return MISSING_VALUE_PLACEHOLDER

        val (scaledValue, scaleSuffix) = resolveScale(BigDecimal.valueOf(value))
        val formattedValue = DecimalFormat(VOLUME_DECIMAL_PATTERN, DecimalFormatSymbols.getInstance(locale)).format(scaledValue)
        return if (scaleSuffix == null) "$USD_PREFIX$formattedValue" else "$USD_PREFIX$formattedValue $scaleSuffix"
    }

    private fun resolveScale(amount: BigDecimal): Pair<BigDecimal, String?> {
        val magnitude = amount.abs()
        val matchedScale = Scale.entries.firstOrNull { magnitude >= it.divisor } ?: Scale.UNIT
        val scaledValue = amount.divide(matchedScale.divisor, 2, RoundingMode.HALF_UP)

        val nextLargerScale = Scale.entries.getOrNull(matchedScale.ordinal - 1)
        return if (nextLargerScale != null && scaledValue.abs() >= VOLUME_PROMOTION_THRESHOLD) {
            amount.divide(nextLargerScale.divisor, 2, RoundingMode.HALF_UP) to nextLargerScale.suffix
        } else {
            scaledValue to matchedScale.suffix
        }
    }
}
