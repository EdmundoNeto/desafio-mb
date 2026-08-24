package br.com.edmundo.desafiomb.core.domain.usecase

import br.com.edmundo.desafiomb.core.domain.model.ExchangeAsset
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.DomainResult

class GetExchangeAssetsUseCase(private val repository: ExchangeRepository) {
    suspend operator fun invoke(id: Int): DomainResult<List<ExchangeAsset>> = repository.getExchangeAssets(id)
}
