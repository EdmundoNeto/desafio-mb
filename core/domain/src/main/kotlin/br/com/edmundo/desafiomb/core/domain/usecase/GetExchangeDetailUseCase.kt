package br.com.edmundo.desafiomb.core.domain.usecase

import br.com.edmundo.desafiomb.core.domain.model.ExchangeDetailBundle
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetExchangeDetailUseCase(
    private val repository: ExchangeRepository,
) {
    suspend operator fun invoke(id: Int): ExchangeDetailBundle =
        coroutineScope {
            val detail = async { repository.syncExchangeDetail(id) }
            val assets = async { repository.getExchangeAssets(id) }
            ExchangeDetailBundle(detailResult = detail.await(), assetsResult = assets.await())
        }
}
