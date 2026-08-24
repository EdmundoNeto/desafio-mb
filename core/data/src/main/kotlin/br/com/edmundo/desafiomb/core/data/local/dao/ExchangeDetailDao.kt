package br.com.edmundo.desafiomb.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExchangeDetailEntity)

    @Query("SELECT * FROM exchange_detail WHERE id = :id")
    suspend fun getById(id: Int): ExchangeDetailEntity?

    @Query("SELECT * FROM exchange_detail WHERE id = :id")
    fun observeById(id: Int): Flow<ExchangeDetailEntity?>
}
