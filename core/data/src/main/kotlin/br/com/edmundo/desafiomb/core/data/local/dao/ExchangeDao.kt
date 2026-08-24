package br.com.edmundo.desafiomb.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeDao {

    @Query(
        """
        SELECT e.* FROM exchange e
        INNER JOIN exchange_index i ON e.id = i.id
        ORDER BY i.rank ASC
        """,
    )
    fun observeHydrated(): Flow<List<ExchangeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExchangeEntity>)

    @Query("SELECT * FROM exchange WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<ExchangeEntity>

    @Query("SELECT * FROM exchange WHERE id = :id")
    suspend fun getById(id: Int): ExchangeEntity?

    @Query("SELECT * FROM exchange WHERE id = :id")
    fun observeById(id: Int): Flow<ExchangeEntity?>

    @Query("DELETE FROM exchange WHERE id NOT IN (SELECT id FROM exchange_index)")
    suspend fun deleteOrphans()
}
