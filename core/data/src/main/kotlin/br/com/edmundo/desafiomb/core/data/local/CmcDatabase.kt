package br.com.edmundo.desafiomb.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.edmundo.desafiomb.core.data.local.dao.CacheMetaDao
import br.com.edmundo.desafiomb.core.data.local.dao.ExchangeAssetDao
import br.com.edmundo.desafiomb.core.data.local.dao.ExchangeDao
import br.com.edmundo.desafiomb.core.data.local.dao.ExchangeDetailDao
import br.com.edmundo.desafiomb.core.data.local.dao.ExchangeIndexDao
import br.com.edmundo.desafiomb.core.data.local.entity.CacheMetaEntity
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeAssetEntity
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeDetailEntity
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeEntity
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeIndexEntity

@Database(
    entities = [
        ExchangeIndexEntity::class,
        ExchangeEntity::class,
        ExchangeDetailEntity::class,
        ExchangeAssetEntity::class,
        CacheMetaEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class CmcDatabase : RoomDatabase() {
    abstract fun exchangeIndexDao(): ExchangeIndexDao
    abstract fun exchangeDao(): ExchangeDao
    abstract fun exchangeDetailDao(): ExchangeDetailDao
    abstract fun exchangeAssetDao(): ExchangeAssetDao
    abstract fun cacheMetaDao(): CacheMetaDao
}
