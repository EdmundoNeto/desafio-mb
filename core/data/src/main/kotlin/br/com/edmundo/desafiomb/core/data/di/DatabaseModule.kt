package br.com.edmundo.desafiomb.core.data.di

import android.content.Context
import androidx.room.Room
import br.com.edmundo.desafiomb.core.data.local.CmcDatabase
import org.koin.dsl.module

private const val DATABASE_NAME = "cmc-exchanges.db"

val databaseModule = module {
    single {
        Room.databaseBuilder(get<Context>(), CmcDatabase::class.java, DATABASE_NAME).build()
    }
    single { get<CmcDatabase>().exchangeIndexDao() }
    single { get<CmcDatabase>().exchangeDao() }
    single { get<CmcDatabase>().exchangeDetailDao() }
    single { get<CmcDatabase>().exchangeAssetDao() }
    single { get<CmcDatabase>().cacheMetaDao() }
}
