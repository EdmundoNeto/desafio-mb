package br.com.edmundo.desafiomb.feature.exchanges.mapper

import br.com.edmundo.desafiomb.core.domain.error.AppError
import br.com.edmundo.desafiomb.core.domain.model.Exchange
import br.com.edmundo.desafiomb.core.domain.model.ExchangeAsset
import br.com.edmundo.desafiomb.core.domain.model.ExchangeDetail
import br.com.edmundo.desafiomb.core.ui.R
import br.com.edmundo.desafiomb.core.ui.text.UiText
import java.time.Instant
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UiMappersTest {

    private val ptBr = Locale.of("pt", "BR")

    @Test
    fun `dado um exchange completo, quando mapeado para ui, entao formata volume e data`() {
        val exchange = Exchange(
            id = 270,
            name = "Binance",
            logoUrl = "https://example.com/270.png",
            spotVolumeUsd = 66_930_000_000.0,
            dateLaunched = Instant.parse("2017-07-14T00:00:00.000Z"),
        )

        val uiModel = exchange.toUiModel(ptBr)

        assertEquals(270, uiModel.id)
        assertEquals("Binance", uiModel.name)
        assertEquals("US$ 66,93 bi", uiModel.volume)
        assertEquals("14/07/2017", uiModel.launchedAt)
    }

    @Test
    fun `dado um exchange sem volume nem data, quando mapeado para ui, entao usa travessao`() {
        val exchange = Exchange(id = 1, name = "X", logoUrl = null, spotVolumeUsd = null, dateLaunched = null)

        val uiModel = exchange.toUiModel(ptBr)

        assertEquals("—", uiModel.volume)
        assertEquals("—", uiModel.launchedAt)
    }

    @Test
    fun `dado NoConnection, quando mapeado para uierror, entao usa string de sem conexao e e retryable`() {
        val uiError = AppError.NoConnection.toUiError()

        assertTrue(uiError.isRetryable)
        assertEquals(R.string.error_no_connection, (uiError.message as UiText.Resource).id)
    }

    @Test
    fun `dado InvalidApiKey, quando mapeado para uierror, entao nao e retryable`() {
        val uiError = AppError.InvalidApiKey.toUiError()

        assertFalse(uiError.isRetryable)
    }

    @Test
    fun `dado RateLimited, quando mapeado para uierror, entao e retryable`() {
        val uiError = AppError.RateLimited(retryAfter = 5.seconds).toUiError()

        assertTrue(uiError.isRetryable)
    }

    @Test
    fun `dado um exchange detail completo, quando mapeado para ui, entao formata taxas e data`() {
        val detail = ExchangeDetail(
            id = 270,
            name = "Binance",
            logoUrl = "https://example.com/270.png",
            description = "Sobre a Binance",
            websiteUrl = "https://www.binance.com",
            makerFee = 0.02,
            takerFee = 0.04,
            dateLaunched = Instant.parse("2017-07-14T00:00:00.000Z"),
        )

        val uiModel = detail.toUiModel(ptBr)

        assertEquals(270, uiModel.id)
        assertEquals("0,02%", uiModel.makerFee)
        assertEquals("0,04%", uiModel.takerFee)
        assertEquals("14/07/2017", uiModel.launchedAt)
    }

    @Test
    fun `dado um exchange detail sem taxas nem data, quando mapeado para ui, entao usa travessao`() {
        val detail = ExchangeDetail(
            id = 1,
            name = "X",
            logoUrl = null,
            description = null,
            websiteUrl = null,
            makerFee = null,
            takerFee = null,
            dateLaunched = null,
        )

        val uiModel = detail.toUiModel(ptBr)

        assertEquals("—", uiModel.makerFee)
        assertEquals("—", uiModel.takerFee)
        assertEquals("—", uiModel.launchedAt)
    }

    @Test
    fun `dado um exchange asset, quando mapeado para ui, entao formata o preco`() {
        val asset = ExchangeAsset(
            walletAddress = "0x1",
            currencyName = "Ethereum",
            currencyPriceUsd = 3204.1,
            currencySymbol = "ETH",
            balance = 10.0,
        )

        val uiModel = asset.toUiModel(ptBr)

        assertEquals("Ethereum", uiModel.currencyName)
        assertEquals("US$ 3.204,10", uiModel.priceUsd)
    }
}
