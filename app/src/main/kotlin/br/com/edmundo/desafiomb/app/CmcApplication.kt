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

class CmcApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CmcApplication)
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
