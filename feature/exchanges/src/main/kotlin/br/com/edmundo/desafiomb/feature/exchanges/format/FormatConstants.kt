package br.com.edmundo.desafiomb.feature.exchanges.format

import java.math.BigDecimal

internal const val MISSING_VALUE_PLACEHOLDER = "—"

internal const val DATE_PATTERN = "dd/MM/yyyy"

internal const val USD_PREFIX = "US$ "
internal const val PRICE_DECIMAL_PATTERN = "#,##0.00"
internal const val PRICE_SIGNIFICANT_DIGITS = 5

internal const val PERCENT_SUFFIX = "%"
internal const val ZERO_PERCENT_VALUE = "0%"
internal const val PERCENT_SCALE = 3

internal const val VOLUME_DECIMAL_PATTERN = "0.00"
internal const val VOLUME_BILLION_SUFFIX = "bi"
internal const val VOLUME_MILLION_SUFFIX = "mi"
internal const val VOLUME_THOUSAND_SUFFIX = "mil"
internal val VOLUME_BILLION_DIVISOR = BigDecimal("1000000000")
internal val VOLUME_MILLION_DIVISOR = BigDecimal("1000000")
internal val VOLUME_THOUSAND_DIVISOR = BigDecimal("1000")
internal val VOLUME_PROMOTION_THRESHOLD = BigDecimal("1000")
