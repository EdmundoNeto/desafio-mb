package br.com.edmundo.desafiomb.core.domain.di

import br.com.edmundo.desafiomb.core.domain.usecase.GetExchangeAssetsUseCase
import br.com.edmundo.desafiomb.core.domain.usecase.GetExchangeDetailUseCase
import br.com.edmundo.desafiomb.core.domain.usecase.LoadExchangesPageUseCase
import br.com.edmundo.desafiomb.core.domain.usecase.ObserveExchangesUseCase
import br.com.edmundo.desafiomb.core.domain.usecase.RefreshExchangesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::ObserveExchangesUseCase)
    factoryOf(::LoadExchangesPageUseCase)
    factoryOf(::RefreshExchangesUseCase)
    factoryOf(::GetExchangeDetailUseCase)
    factoryOf(::GetExchangeAssetsUseCase)
}
