package br.com.edmundo.desafiomb.feature.exchanges.format

import java.time.Instant
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    private val ptBr = Locale.of("pt", "BR")

    @Test
    fun `dado 66930000000, quando formatado como volume, entao retorna US dolar 66,93 bi`() {
        assertEquals("US$ 66,93 bi", VolumeFormatter.format(66_930_000_000.0, ptBr))
    }

    @Test
    fun `dado 412500000, quando formatado como volume, entao retorna US dolar 412,50 mi`() {
        assertEquals("US$ 412,50 mi", VolumeFormatter.format(412_500_000.0, ptBr))
    }

    @Test
    fun `dado 950000, quando formatado como volume, entao retorna US dolar 950,00 mil`() {
        assertEquals("US$ 950,00 mil", VolumeFormatter.format(950_000.0, ptBr))
    }

    @Test
    fun `dado 842, quando formatado como volume, entao retorna US dolar 842,00`() {
        assertEquals("US$ 842,00", VolumeFormatter.format(842.0, ptBr))
    }

    @Test
    fun `dado zero, quando formatado como volume, entao retorna travessao`() {
        assertEquals("—", VolumeFormatter.format(0.0, ptBr))
    }

    @Test
    fun `dado nulo, quando formatado como volume, entao retorna travessao`() {
        assertEquals("—", VolumeFormatter.format(null, ptBr))
    }

    @Test
    fun `dado um instant valido, quando formatado como data, entao retorna dd mm yyyy`() {
        assertEquals("14/07/2017", DateFormatter.format(Instant.parse("2017-07-14T00:00:00.000Z"), ptBr))
    }

    @Test
    fun `dado nulo, quando formatado como data, entao retorna travessao`() {
        assertEquals("—", DateFormatter.format(null, ptBr))
    }
}
