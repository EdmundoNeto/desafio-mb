package br.com.edmundo.desafiomb.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeAssetEntity

@Dao
interface ExchangeAssetDao {
    @Query("SELECT * FROM exchange_asset WHERE exchangeId = :exchangeId ORDER BY currencyPriceUsd DESC")
    suspend fun getByExchangeId(exchangeId: Int): List<ExchangeAssetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ExchangeAssetEntity>)

    @Query("DELETE FROM exchange_asset WHERE exchangeId = :exchangeId")
    suspend fun deleteByExchangeId(exchangeId: Int)
}
