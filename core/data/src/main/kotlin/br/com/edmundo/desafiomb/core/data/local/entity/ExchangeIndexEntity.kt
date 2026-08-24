package br.com.edmundo.desafiomb.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_index")
data class ExchangeIndexEntity(
    @PrimaryKey val id: Int,
    val rank: Int,
    val name: String,
    val syncedAt: Long,
)
