package br.com.edmundo.desafiomb.core.domain.di

import org.koin.dsl.module

/**
 * Koin puro, sem Android (SD-08): o dominio declara suas proprias dependencias
 * mesmo sendo um modulo JVM.
 *
 * E1 entrega o modulo vazio; os 5 use cases (spec 2.4.3) entram na E3, quando
 * ExchangeRepository existir.
 */
val domainModule = module {
    // E3: factoryOf(::ObserveExchangesUseCase) etc.
}
