package br.com.edmundo.desafiomb.app

import android.app.Application
import br.com.edmundo.desafiomb.app.di.appModule
import br.com.edmundo.desafiomb.core.data.di.databaseModule
import br.com.edmundo.desafiomb.core.data.di.networkModule
import br.com.edmundo.desafiomb.core.data.di.repositoryModule
import br.com.edmundo.desafiomb.core.domain.di.domainModule
import br.com.edmundo.desafiomb.feature.exchanges.di.exchangesModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import org.koin.core.context.startKoin

/**
 * Unico ponto que conhece o conjunto completo de modulos Koin (spec 2.4).
 * Nenhum modulo Gradle importa definicoes de outro - apenas a interface que resolve.
 */
class CmcApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CmcApplication)
            // Level.ERROR mesmo em debug: o nivel DEBUG do Koin e ruidoso e degrada
            // o startup medido pelo RNF-04 (spec 2.4.2).
            if (BuildConfig.DEBUG) androidLogger(Level.ERROR)
            modules(
                appModule,
                domainModule,
                networkModule,
                databaseModule,
                repositoryModule,
                exchangesModule,
            )
        }
    }
}
