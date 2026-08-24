package br.com.edmundo.desafiomb.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "exchange_asset",
    primaryKeys = ["exchangeId", "walletAddress", "currencySymbol"],
    indices = [Index("exchangeId")],
)
data class ExchangeAssetEntity(
    val exchangeId: Int,
    val walletAddress: String,
    val currencySymbol: String,
    val currencyName: String,
    val currencyPriceUsd: Double,
    val balance: Double,
    val updatedAt: Long,
)
