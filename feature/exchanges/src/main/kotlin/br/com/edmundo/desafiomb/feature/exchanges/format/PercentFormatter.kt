package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

private const val PERCENT_SCALE = 3

object PercentFormatter {
    fun format(
        value: Double?,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (value == null) return MISSING_VALUE_PLACEHOLDER
        val rounded = BigDecimal.valueOf(value).setScale(PERCENT_SCALE, RoundingMode.HALF_UP)
        if (rounded.compareTo(BigDecimal.ZERO) == 0) return "0%"
        return "${rounded.toLocalizedPlainString(locale)}%"
    }
}
