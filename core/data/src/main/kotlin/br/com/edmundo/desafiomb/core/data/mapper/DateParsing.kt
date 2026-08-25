package br.com.edmundo.desafiomb.core.data.mapper

import java.time.Instant

object DateParsing {
    fun parseIsoToEpochMillis(value: String?): Long? =
        value?.let {
            runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
        }

    fun epochMillisToInstant(epochMillis: Long?): Instant? = epochMillis?.let(Instant::ofEpochMilli)
}
