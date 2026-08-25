package br.com.edmundo.desafiomb.feature.exchanges.format

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    fun format(
        instant: Instant?,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (instant == null) return MISSING_VALUE_PLACEHOLDER
        val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN, locale).withZone(ZoneOffset.UTC)
        return formatter.format(instant)
    }
}
