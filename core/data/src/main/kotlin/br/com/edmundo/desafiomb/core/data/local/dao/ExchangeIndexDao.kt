package br.com.edmundo.desafiomb.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeIndexEntity

@Dao
interface ExchangeIndexDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ExchangeIndexEntity>)

    @Query("DELETE FROM exchange_index")
    suspend fun clear()

    @Query("SELECT id FROM exchange_index ORDER BY rank ASC LIMIT :limit OFFSET :offset")
    suspend fun idsForRange(offset: Int, limit: Int): List<Int>

    @Query("SELECT COUNT(*) FROM exchange_index")
    suspend fun indexSize(): Int

    @Query("SELECT MIN(syncedAt) FROM exchange_index")
    suspend fun oldestIndexSync(): Long?
}
