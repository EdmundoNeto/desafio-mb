package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal
import java.math.MathContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PriceFormatter {

    fun format(value: Double?, locale: Locale = Locale.getDefault()): String {
        if (value == null) return "—"
        val symbols = DecimalFormatSymbols.getInstance(locale)
        return if (value >= 1.0) {
            "US$ ${DecimalFormat("#,##0.00", symbols).format(value)}"
        } else {
            val rounded = BigDecimal(value).round(MathContext(5)).stripTrailingZeros()
            val magnitude = if (rounded.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else rounded
            val plain = magnitude.toPlainString()
            val localized = if (symbols.decimalSeparator != '.') plain.replace('.', symbols.decimalSeparator) else plain
            "US$ $localized"
        }
    }
}
