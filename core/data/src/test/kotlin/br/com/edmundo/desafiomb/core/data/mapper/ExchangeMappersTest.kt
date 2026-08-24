package br.com.edmundo.desafiomb.core.data.mapper

import br.com.edmundo.desafiomb.core.data.remote.cmcJson
import br.com.edmundo.desafiomb.core.data.remote.dto.CmcResponse
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeInfoDto
import br.com.edmundo.desafiomb.core.testing.fixture.JsonFixtures
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ExchangeMappersTest {

    private val now = 1_700_000_000_000L

    private fun decodeInfo(json: String): ExchangeInfoDto {
        val response = cmcJson.decodeFromString(
            CmcResponse.serializer(MapSerializer(String.serializer(), ExchangeInfoDto.serializer())),
            json,
        )
        return response.data!!.getValue("270")
    }

    @Test
    fun `dado ExchangeInfoDto completo, quando mapeado para entity e domain, entao todos os campos exigidos sao preenchidos`() {
        val dto = decodeInfo(JsonFixtures.exchangeInfoResponse)

        val exchangeEntity = dto.toExchangeEntity(now)
        val detailEntity = dto.toDetailEntity(now)
        val detail = toExchangeDetail(exchangeEntity, detailEntity)

        assertEquals(270, detail.id)
        assertEquals("Binance", detail.name)
        assertEquals("https://example.com/270.png", detail.logoUrl)
        assertEquals("Binance is a cryptocurrency exchange.", detail.description)
        assertEquals("https://binance.com", detail.websiteUrl)
        assertEquals(0.001, detail.makerFee)
        assertEquals(0.001, detail.takerFee)
        assertEquals(Instant.parse("2017-07-14T00:00:00.000Z"), detail.dateLaunched)
    }

    @Test
    fun `dado date_launched invalido, quando mapeado, entao dateLaunched e null sem excecao`() {
        val dto = decodeInfo(JsonFixtures.exchangeInfoResponseMinimalFields)

        val exchangeEntity = dto.toExchangeEntity(now)

        assertNull(exchangeEntity.dateLaunchedEpochMs)
        assertNull(exchangeEntity.toDomain().dateLaunched)
    }

    @Test
    fun `dado urls ausente, quando mapeado, entao websiteUrl e null`() {
        val dto = decodeInfo(JsonFixtures.exchangeInfoResponseMinimalFields)

        val detailEntity = dto.toDetailEntity(now)

        assertNull(detailEntity.websiteUrl)
    }

    @Test
    fun `dado campo desconhecido no JSON, quando desserializado, entao nao quebra`() {
        val dto = decodeInfo(JsonFixtures.exchangeInfoResponse)

        assertEquals(270, dto.id)
    }

    @Test
    fun `dado spot_volume_usd ausente, quando mapeado, entao spotVolumeUsd e null e nao zero`() {
        val dto = decodeInfo(JsonFixtures.exchangeInfoResponseMinimalFields)

        val exchangeEntity = dto.toExchangeEntity(now)

        assertNull(exchangeEntity.spotVolumeUsd)
    }
}
