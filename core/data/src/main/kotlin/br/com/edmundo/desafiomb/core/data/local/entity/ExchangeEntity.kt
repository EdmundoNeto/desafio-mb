package br.com.edmundo.desafiomb.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange")
data class ExchangeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val slug: String?,
    val logoUrl: String?,
    val spotVolumeUsd: Double?,
    val dateLaunchedEpochMs: Long?,
    val updatedAt: Long,
)
