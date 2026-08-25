package br.com.edmundo.desafiomb.core.testing.di

import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.testing.fake.FakeExchangeRepository
import org.koin.dsl.module

val fakeRepositoryModule =
    module {
        single<ExchangeRepository> { FakeExchangeRepository() }
    }
