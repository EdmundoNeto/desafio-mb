package br.com.edmundo.desafiomb.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.edmundo.desafiomb.core.data.local.entity.CacheMetaEntity

@Dao
interface CacheMetaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CacheMetaEntity)

    @Query("SELECT syncedAt FROM cache_meta WHERE `key` = :key")
    suspend fun getSyncedAt(key: String): Long?
}
