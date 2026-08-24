package br.com.edmundo.desafiomb.core.data.repository

import br.com.edmundo.desafiomb.core.data.remote.CmcExchangeApi
import br.com.edmundo.desafiomb.core.data.remote.dto.CmcResponse
import br.com.edmundo.desafiomb.core.data.remote.dto.CmcStatus
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeAssetDto
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeInfoDto
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeMapItemDto

class FakeCmcExchangeApi(
    var onGetExchangeMap: suspend () -> CmcResponse<List<ExchangeMapItemDto>> =
        { CmcResponse(CmcStatus(), emptyList()) },
    var onGetExchangeInfo: suspend (ids: String) -> CmcResponse<Map<String, ExchangeInfoDto>> =
        { CmcResponse(CmcStatus(), emptyMap()) },
    var onGetExchangeAssets: suspend (id: Int) -> CmcResponse<List<ExchangeAssetDto>> =
        { CmcResponse(CmcStatus(), emptyList()) },
) : CmcExchangeApi {

    var mapCallCount = 0
        private set
    var infoCallCount = 0
        private set
    var assetsCallCount = 0
        private set
    val infoRequestedIds = mutableListOf<String>()

    override suspend fun getExchangeMap(sort: String, limit: Int, listingStatus: String): CmcResponse<List<ExchangeMapItemDto>> {
        mapCallCount++
        return onGetExchangeMap()
    }

    override suspend fun getExchangeInfo(ids: String, aux: String): CmcResponse<Map<String, ExchangeInfoDto>> {
        infoCallCount++
        infoRequestedIds += ids
        return onGetExchangeInfo(ids)
    }

    override suspend fun getExchangeAssets(id: Int): CmcResponse<List<ExchangeAssetDto>> {
        assetsCallCount++
        return onGetExchangeAssets(id)
    }
}
