package br.com.edmundo.desafiomb.core.domain.repository

import br.com.edmundo.desafiomb.core.domain.model.Exchange
import br.com.edmundo.desafiomb.core.domain.model.ExchangeAsset
import br.com.edmundo.desafiomb.core.domain.model.ExchangeDetail
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import kotlinx.coroutines.flow.Flow

const val UI_PAGE_SIZE = 25

interface ExchangeRepository {
    fun observeExchanges(): Flow<List<Exchange>>

    suspend fun loadPage(
        page: Int,
        pageSize: Int = UI_PAGE_SIZE,
    ): DomainResult<PageLoad>

    suspend fun refresh(): DomainResult<Unit>

    fun observeExchangeDetail(id: Int): Flow<ExchangeDetail?>

    suspend fun syncExchangeDetail(id: Int): DomainResult<Unit>

    suspend fun getExchangeAssets(id: Int): DomainResult<List<ExchangeAsset>>
}
