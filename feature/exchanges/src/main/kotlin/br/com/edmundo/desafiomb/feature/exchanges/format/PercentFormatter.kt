package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object PercentFormatter {
    fun format(
        value: Double?,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (value == null) return MISSING_VALUE_PLACEHOLDER
        val rounded = BigDecimal.valueOf(value).setScale(PERCENT_SCALE, RoundingMode.HALF_UP)
        if (rounded.compareTo(BigDecimal.ZERO) == 0) return ZERO_PERCENT_VALUE
        return "${rounded.toLocalizedPlainString(locale)}$PERCENT_SUFFIX"
    }
}
