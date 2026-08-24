package br.com.edmundo.desafiomb.core.data.di

import br.com.edmundo.desafiomb.core.data.BuildConfig
import br.com.edmundo.desafiomb.core.data.remote.CmcExchangeApi
import br.com.edmundo.desafiomb.core.data.remote.cmcJson
import br.com.edmundo.desafiomb.core.data.remote.interceptor.ApiKeyInterceptor
import br.com.edmundo.desafiomb.core.data.remote.interceptor.RateLimitInterceptor
import br.com.edmundo.desafiomb.core.data.remote.interceptor.RetryInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://pro-api.coinmarketcap.com/"

val networkModule = module {
    single<Json> { cmcJson }

    single {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(ApiKeyInterceptor(apiKey = BuildConfig.CMC_API_KEY))
            .addInterceptor(RetryInterceptor())
            .addInterceptor(RateLimitInterceptor())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                            redactHeader("X-CMC_PRO_API_KEY")
                        },
                    )
                }
            }
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
            .create<CmcExchangeApi>()
    }
}
