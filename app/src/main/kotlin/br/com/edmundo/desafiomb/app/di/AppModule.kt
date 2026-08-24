package br.com.edmundo.desafiomb.app.di

import br.com.edmundo.desafiomb.app.logging.AndroidLogger
import br.com.edmundo.desafiomb.core.domain.util.Logger
import org.koin.dsl.module

val appModule = module {
    single<Logger> { AndroidLogger() }
}
