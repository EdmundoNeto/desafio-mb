package br.com.edmundo.desafiomb.core.testing.fixture

object JsonFixtures {
    val exchangeMapResponse =
        """
        {
          "status": {"error_code":0,"error_message":null,"credit_count":1,"elapsed":10},
          "data": [
            {"id":270,"name":"Binance","slug":"binance","is_active":1},
            {"id":89,"name":"Coinbase Exchange","slug":"coinbase-exchange","is_active":1}
          ]
        }
        """.trimIndent()

    val exchangeInfoResponse =
        """
        {
          "status": {"error_code":0,"error_message":null,"credit_count":1,"elapsed":12},
          "data": {
            "270": {
              "id":270,
              "name":"Binance",
              "slug":"binance",
              "logo":"https://example.com/270.png",
              "description":"Binance is a cryptocurrency exchange.",
              "date_launched":"2017-07-14T00:00:00.000Z",
              "maker_fee":0.001,
              "taker_fee":0.001,
              "spot_volume_usd":66930000000.0,
              "urls":{"website":["https://binance.com"],"fee":[],"twitter":["https://twitter.com/binance"]},
              "unexpected_field":"deve ser ignorado"
            }
          }
        }
        """.trimIndent()

    val exchangeInfoResponseMinimalFields =
        """
        {
          "status": {"error_code":0,"error_message":null,"credit_count":1,"elapsed":12},
          "data": {
            "270": {
              "id":270,
              "name":"Binance",
              "date_launched":"not-a-date"
            }
          }
        }
        """.trimIndent()

    val exchangeAssetsResponse =
        """
        {
          "status": {"error_code":0,"error_message":null,"credit_count":1,"elapsed":8},
          "data": [
            {
              "wallet_address":"0xabc",
              "balance":1234.5,
              "platform":{"crypto_id":1027,"symbol":"ETH","name":"Ethereum"},
              "currency":{"crypto_id":2,"price_usd":3204.1,"symbol":"ETH","name":"Ethereum"}
            }
          ]
        }
        """.trimIndent()

    val emptyDataResponse =
        """
        {"status":{"error_code":0,"error_message":null,"credit_count":0,"elapsed":1},"data":null}
        """.trimIndent()

    val invalidApiKeyResponse =
        """
        {"status":{"error_code":1001,"error_message":"API key missing.","credit_count":0,"elapsed":1},"data":null}
        """.trimIndent()
}
