package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.util.Locale

object PercentFormatter {

    fun format(value: Double?, locale: Locale = Locale.getDefault()): String {
        if (value == null) return "—"
        val rounded = BigDecimal(value).setScale(3, RoundingMode.HALF_UP)
        if (rounded.compareTo(BigDecimal.ZERO) == 0) return "0%"
        val symbols = DecimalFormatSymbols.getInstance(locale)
        val plain = rounded.stripTrailingZeros().toPlainString()
        val localized = if (symbols.decimalSeparator != '.') plain.replace('.', symbols.decimalSeparator) else plain
        return "$localized%"
    }
}
