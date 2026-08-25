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
        val symbols = DecimalFormatSymbols.getInstance(locale)
        return if (value >= 1.0) {
            "US$ ${DecimalFormat("#,##0.00", symbols).format(value)}"
        } else {
            val rounded = BigDecimal.valueOf(value).round(MathContext(SIGNIFICANT_DIGITS)).stripTrailingZeros()
            val magnitude = if (rounded.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else rounded
            val plain = magnitude.toPlainString()
            val localized = if (symbols.decimalSeparator != '.') plain.replace('.', symbols.decimalSeparator) else plain
            "US$ $localized"
        }
    }
}
