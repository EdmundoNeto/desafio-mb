package br.com.edmundo.desafiomb.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateParsingTest {

    @Test
    fun `dado ISO-8601 valido, quando parseIsoToEpochMillis, entao retorna o epoch correspondente`() {
        val epochMillis = DateParsing.parseIsoToEpochMillis("2017-07-14T00:00:00.000Z")
        assertEquals(1499990400000L, epochMillis)
    }

    @Test
    fun `dado string invalida, quando parseIsoToEpochMillis, entao retorna null sem lancar excecao`() {
        assertNull(DateParsing.parseIsoToEpochMillis("not-a-date"))
    }

    @Test
    fun `dado null, quando parseIsoToEpochMillis, entao retorna null`() {
        assertNull(DateParsing.parseIsoToEpochMillis(null))
    }

    @Test
    fun `dado epoch millis, quando epochMillisToInstant, entao retorna o Instant correspondente`() {
        val instant = DateParsing.epochMillisToInstant(1499990400000L)
        assertEquals("2017-07-14T00:00:00Z", instant.toString())
    }

    @Test
    fun `dado null, quando epochMillisToInstant, entao retorna null`() {
        assertNull(DateParsing.epochMillisToInstant(null))
    }
}
