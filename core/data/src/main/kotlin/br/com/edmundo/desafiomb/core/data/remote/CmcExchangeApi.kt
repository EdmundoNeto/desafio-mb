package br.com.edmundo.desafiomb.core.data.remote

import br.com.edmundo.desafiomb.core.data.remote.dto.CmcResponse
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeAssetDto
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeInfoDto
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeMapItemDto
import retrofit2.http.GET
import retrofit2.http.Query

const val MAP_LIMIT = 5000

interface CmcExchangeApi {

    @GET("v1/exchange/map")
    suspend fun getExchangeMap(
        @Query("sort") sort: String = "volume_24h",
        @Query("limit") limit: Int = MAP_LIMIT,
        @Query("listing_status") listingStatus: String = "active",
    ): CmcResponse<List<ExchangeMapItemDto>>

    @GET("v1/exchange/info")
    suspend fun getExchangeInfo(
        @Query("id") ids: String,
        @Query("aux") aux: String = "urls,logo,description,date_launched",
    ): CmcResponse<Map<String, ExchangeInfoDto>>

    @GET("v1/exchange/assets")
    suspend fun getExchangeAssets(
        @Query("id") id: Int,
    ): CmcResponse<List<ExchangeAssetDto>>
}
