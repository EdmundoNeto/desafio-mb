package br.com.edmundo.desafiomb.core.data.di

import androidx.test.core.app.ApplicationProvider
import br.com.edmundo.desafiomb.core.data.local.CmcDatabase
import br.com.edmundo.desafiomb.core.data.remote.CmcExchangeApi
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.TimeProvider
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataModulesResolutionTest {
    @Before
    fun setUp() {
        stopKoin()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `dado os modulos de data, quando resolvidos, entao cada definicao constroi uma instancia valida`() {
        val koinApp =
            koinApplication {
                androidContext(ApplicationProvider.getApplicationContext())
                modules(networkModule, databaseModule, repositoryModule)
            }
        startKoin(koinApp)

        val okHttpClient = koinApp.koin.get<OkHttpClient>()
        val api = koinApp.koin.get<CmcExchangeApi>()
        val database = koinApp.koin.get<CmcDatabase>()
        val timeProvider = koinApp.koin.get<TimeProvider>()
        val repository = koinApp.koin.get<ExchangeRepository>()

        assertNotNull(okHttpClient)
        assertNotNull(api)
        assertNotNull(database)
        assertNotNull(timeProvider)
        assertNotNull(repository)
        assertNotNull(timeProvider.now())

        database.close()
    }
}
