package br.com.edmundo.desafiomb.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_detail")
data class ExchangeDetailEntity(
    @PrimaryKey val id: Int,
    val description: String?,
    val websiteUrl: String?,
    val makerFee: Double?,
    val takerFee: Double?,
    val updatedAt: Long,
)
