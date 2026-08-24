package br.com.edmundo.desafiomb.core.testing.fixture

import br.com.edmundo.desafiomb.core.domain.model.Exchange
import java.time.Instant

object ExchangeFixtures {

    fun exchange(
        id: Int = 270,
        name: String = "Binance",
        logoUrl: String? = "https://example.com/$id.png",
        spotVolumeUsd: Double? = 66_930_000_000.0,
        dateLaunched: Instant? = Instant.parse("2017-07-14T00:00:00.000Z"),
    ): Exchange = Exchange(
        id = id,
        name = name,
        logoUrl = logoUrl,
        spotVolumeUsd = spotVolumeUsd,
        dateLaunched = dateLaunched,
    )

    fun exchangeList(count: Int, startId: Int = 1): List<Exchange> =
        (startId until startId + count).map { exchange(id = it, name = "Exchange $it") }
}
