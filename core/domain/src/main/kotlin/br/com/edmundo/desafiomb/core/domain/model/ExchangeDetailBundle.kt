package br.com.edmundo.desafiomb.core.domain.model

import br.com.edmundo.desafiomb.core.domain.util.DomainResult

data class ExchangeDetailBundle(
    val detailResult: DomainResult<Unit>,
    val assetsResult: DomainResult<List<ExchangeAsset>>,
)
