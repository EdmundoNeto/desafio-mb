package br.com.edmundo.desafiomb.core.domain.model

import br.com.edmundo.desafiomb.core.domain.error.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ModelsTest {
    @Test
    fun `dado ExchangeAsset, quando comparado por igualdade, entao respeita todos os campos`() {
        val asset = ExchangeAsset("0xabc", "Bitcoin", 65000.0, "BTC", 1.5)
        val same = ExchangeAsset("0xabc", "Bitcoin", 65000.0, "BTC", 1.5)
        val different = asset.copy(balance = 2.0)

        assertEquals(asset, same)
        assertNotEquals(asset, different)
    }

    @Test
    fun `dado ExchangeDetail, quando comparado por igualdade, entao respeita todos os campos`() {
        val detail =
            ExchangeDetail(
                id = 270,
                name = "Binance",
                logoUrl = null,
                description = "desc",
                websiteUrl = "https://x.com",
                makerFee = 0.1,
                takerFee = 0.2,
                dateLaunched = null,
            )
        val same = detail.copy()
        val different = detail.copy(makerFee = 0.5)

        assertEquals(detail, same)
        assertNotEquals(detail, different)
    }

    @Test
    fun `dado PageLoad Cached, quando comparado por igualdade, entao respeita hasMore e error`() {
        val cached = PageLoad.Cached(hasMore = true, error = AppError.NoConnection)
        val same = PageLoad.Cached(hasMore = true, error = AppError.NoConnection)
        val different = PageLoad.Cached(hasMore = false, error = AppError.NoConnection)

        assertEquals(cached, same)
        assertNotEquals(cached, different)
        assertEquals(true, cached.hasMore)
    }

    @Test
    fun `dado AppError RateLimited, quando comparado por igualdade, entao respeita retryAfter`() {
        val limited = AppError.RateLimited(retryAfter = null)
        val same = AppError.RateLimited(retryAfter = null)

        assertEquals(limited, same)
    }

    @Test
    fun `dado AppError Unknown, quando comparado por igualdade, entao respeita cause`() {
        val unknown = AppError.Unknown(cause = null)
        val same = AppError.Unknown(cause = null)

        assertEquals(unknown, same)
    }

    @Test
    fun `dado AppError InvalidApiKey PlanNotAuthorized e Serialization, quando comparados, entao sao data objects estaveis`() {
        assertEquals(AppError.InvalidApiKey, AppError.InvalidApiKey)
        assertEquals(AppError.PlanNotAuthorized, AppError.PlanNotAuthorized)
        assertEquals(AppError.Serialization, AppError.Serialization)
    }
}
