package br.com.edmundo.desafiomb.feature.exchanges.di

import br.com.edmundo.desafiomb.feature.exchanges.list.ExchangeListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val exchangesModule = module {
    viewModelOf(::ExchangeListViewModel)
}
