package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.util.Locale

internal fun BigDecimal.toLocalizedPlainString(locale: Locale): String {
    val plain = stripTrailingZeros().toPlainString()
    val decimalSeparator = DecimalFormatSymbols.getInstance(locale).decimalSeparator
    return if (decimalSeparator != '.') plain.replace('.', decimalSeparator) else plain
}
