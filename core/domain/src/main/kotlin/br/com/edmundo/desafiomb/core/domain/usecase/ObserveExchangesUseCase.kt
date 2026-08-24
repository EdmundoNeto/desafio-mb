package br.com.edmundo.desafiomb.core.domain.usecase

import br.com.edmundo.desafiomb.core.domain.model.Exchange
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import kotlinx.coroutines.flow.Flow

class ObserveExchangesUseCase(private val repository: ExchangeRepository) {
    operator fun invoke(): Flow<List<Exchange>> = repository.observeExchanges()
}
