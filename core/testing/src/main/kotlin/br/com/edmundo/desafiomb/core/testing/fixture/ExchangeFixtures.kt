package br.com.edmundo.desafiomb.core.testing.fixture

import br.com.edmundo.desafiomb.core.domain.model.Exchange
import br.com.edmundo.desafiomb.core.domain.model.ExchangeAsset
import br.com.edmundo.desafiomb.core.domain.model.ExchangeDetail
import java.time.Instant

object ExchangeFixtures {
    fun exchange(
        id: Int = 270,
        name: String = "Binance",
        logoUrl: String? = "https://example.com/$id.png",
        spotVolumeUsd: Double? = 66_930_000_000.0,
        dateLaunched: Instant? = Instant.parse("2017-07-14T00:00:00.000Z"),
    ): Exchange =
        Exchange(
            id = id,
            name = name,
            logoUrl = logoUrl,
            spotVolumeUsd = spotVolumeUsd,
            dateLaunched = dateLaunched,
        )

    fun exchangeList(
        count: Int,
        startId: Int = 1,
    ): List<Exchange> = (startId until startId + count).map { exchange(id = it, name = "Exchange $it") }

    fun exchangeDetail(
        id: Int = 270,
        name: String = "Binance",
        logoUrl: String? = "https://example.com/$id.png",
        description: String? = "Launched in Jul-2017, Binance is one of the largest crypto exchanges.",
        websiteUrl: String? = "https://www.binance.com",
        makerFee: Double? = 0.02,
        takerFee: Double? = 0.04,
        dateLaunched: Instant? = Instant.parse("2017-07-14T00:00:00.000Z"),
    ): ExchangeDetail =
        ExchangeDetail(
            id = id,
            name = name,
            logoUrl = logoUrl,
            description = description,
            websiteUrl = websiteUrl,
            makerFee = makerFee,
            takerFee = takerFee,
            dateLaunched = dateLaunched,
        )

    fun exchangeAsset(
        walletAddress: String = "0x1",
        currencyName: String = "Ethereum",
        currencyPriceUsd: Double = 3204.1,
        currencySymbol: String = "ETH",
        balance: Double = 10.0,
    ): ExchangeAsset =
        ExchangeAsset(
            walletAddress = walletAddress,
            currencyName = currencyName,
            currencyPriceUsd = currencyPriceUsd,
            currencySymbol = currencySymbol,
            balance = balance,
        )

    fun exchangeAssetList(count: Int): List<ExchangeAsset> =
        (1..count).map {
            exchangeAsset(walletAddress = "0x$it", currencyName = "Coin $it", currencyPriceUsd = it.toDouble())
        }
}
