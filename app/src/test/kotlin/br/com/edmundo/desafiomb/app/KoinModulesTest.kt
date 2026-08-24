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

/**
 * Gate que substitui a verificacao de grafo que o Hilt fazia em compilacao (SD-07, R-06).
 *
 * BLOQUEANTE no CI (spec 9.3): nao pode ser @Ignore nem movido para source set opcional.
 * Sem ele, um binding esquecido so apareceria como crash na primeira abertura da tela.
 */
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

        // `extraTypes` cobre o que vem do ambiente Android e nao de uma definicao:
        // androidContext() injeta o Context em runtime (spec 2.4.5).
        modules.forEach { it.verify(extraTypes = listOf(Context::class)) }
    }
}
