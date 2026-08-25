package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal
import java.math.MathContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private const val SIGNIFICANT_DIGITS = 5

object PriceFormatter {
    fun format(
        value: Double?,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (value == null) return MISSING_VALUE_PLACEHOLDER
        return if (value >= 1.0) {
            val symbols = DecimalFormatSymbols.getInstance(locale)
            "US$ ${DecimalFormat("#,##0.00", symbols).format(value)}"
        } else {
            val rounded = BigDecimal.valueOf(value).round(MathContext(SIGNIFICANT_DIGITS))
            val magnitude = if (rounded.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else rounded
            "US$ ${magnitude.toLocalizedPlainString(locale)}"
        }
    }
}
