package br.com.edmundo.desafiomb.core.domain.usecase

import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.DomainResult

class RefreshExchangesUseCase(private val repository: ExchangeRepository) {
    suspend operator fun invoke(): DomainResult<Unit> = repository.refresh()
}
