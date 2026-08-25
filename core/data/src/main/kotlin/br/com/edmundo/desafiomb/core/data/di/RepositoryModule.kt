package br.com.edmundo.desafiomb.core.data.di

import br.com.edmundo.desafiomb.core.data.repository.ExchangeRepositoryImpl
import br.com.edmundo.desafiomb.core.data.util.SystemTimeProvider
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val IO_DISPATCHER = "IoDispatcher"

val repositoryModule =
    module {
        single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }

        single<TimeProvider> { SystemTimeProvider() }

        single<ExchangeRepository> {
            ExchangeRepositoryImpl(
                api = get(),
                db = get(),
                indexDao = get(),
                exchangeDao = get(),
                detailDao = get(),
                assetDao = get(),
                cacheMetaDao = get(),
                timeProvider = get(),
                ioDispatcher = get(named(IO_DISPATCHER)),
            )
        }
    }
