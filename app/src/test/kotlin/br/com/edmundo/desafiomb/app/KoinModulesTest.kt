package br.com.edmundo.desafiomb.app

import android.content.Context
import br.com.edmundo.desafiomb.app.di.appModule
import br.com.edmundo.desafiomb.core.data.di.databaseModule
import br.com.edmundo.desafiomb.core.data.di.networkModule
import br.com.edmundo.desafiomb.core.data.di.repositoryModule
import br.com.edmundo.desafiomb.core.domain.di.domainModule
import br.com.edmundo.desafiomb.feature.exchanges.di.exchangesModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class KoinModulesTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `dado o grafo completo, quando verificado, entao toda definicao resolve`() {
        val modules = listOf(
            appModule,
            domainModule,
            networkModule,
            databaseModule,
            repositoryModule,
            exchangesModule,
        )

        modules.forEach { it.verify(extraTypes = listOf(Context::class)) }
    }
}
