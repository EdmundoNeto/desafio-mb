package br.com.edmundo.desafiomb.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_meta")
data class CacheMetaEntity(
    @PrimaryKey val key: String,
    val syncedAt: Long,
)

fun assetsSyncKey(exchangeId: Int): String = "assets:$exchangeId"
