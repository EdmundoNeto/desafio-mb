package br.com.edmundo.desafiomb.core.domain.usecase

import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.repository.UI_PAGE_SIZE
import br.com.edmundo.desafiomb.core.domain.util.DomainResult

class LoadExchangesPageUseCase(private val repository: ExchangeRepository) {
    suspend operator fun invoke(page: Int, pageSize: Int = UI_PAGE_SIZE): DomainResult<PageLoad> =
        repository.loadPage(page, pageSize)
}
