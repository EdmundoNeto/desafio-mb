package br.com.edmundo.desafiomb.core.data.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Qualificador obrigatorio para dispatchers (spec 2.4.3): nenhuma classe de producao
 * referencia Dispatchers.IO diretamente, ou os testes de repositorio deixam de ser
 * deterministicos.
 */
const val IO_DISPATCHER = "IoDispatcher"

val repositoryModule = module {
    single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }
    // E2: single<TimeProvider> { SystemTimeProvider() }
    // E2: single<ExchangeRepository> { ExchangeRepositoryImpl(...) }
}
