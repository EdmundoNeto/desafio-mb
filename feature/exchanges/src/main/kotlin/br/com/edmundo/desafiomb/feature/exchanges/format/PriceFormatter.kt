package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal
import java.math.MathContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PriceFormatter {
    fun format(
        value: Double?,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (value == null) return MISSING_VALUE_PLACEHOLDER
        return if (value >= 1.0) {
            val symbols = DecimalFormatSymbols.getInstance(locale)
            "$USD_PREFIX${DecimalFormat(PRICE_DECIMAL_PATTERN, symbols).format(value)}"
        } else {
            val rounded = BigDecimal.valueOf(value).round(MathContext(PRICE_SIGNIFICANT_DIGITS))
            val magnitude = if (rounded.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else rounded
            "$USD_PREFIX${magnitude.toLocalizedPlainString(locale)}"
        }
    }
}
