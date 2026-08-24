package br.com.edmundo.desafiomb.core.data.mapper

import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeAssetEntity
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeDetailEntity
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeEntity
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeIndexEntity
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeAssetDto
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeInfoDto
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeMapItemDto
import br.com.edmundo.desafiomb.core.domain.model.Exchange
import br.com.edmundo.desafiomb.core.domain.model.ExchangeAsset
import br.com.edmundo.desafiomb.core.domain.model.ExchangeDetail

fun ExchangeMapItemDto.toIndexEntity(rank: Int, syncedAt: Long): ExchangeIndexEntity = ExchangeIndexEntity(
    id = id,
    rank = rank,
    name = name,
    syncedAt = syncedAt,
)

fun ExchangeInfoDto.toExchangeEntity(now: Long): ExchangeEntity = ExchangeEntity(
    id = id,
    name = name,
    slug = slug,
    logoUrl = logo,
    spotVolumeUsd = spotVolumeUsd,
    dateLaunchedEpochMs = DateParsing.parseIsoToEpochMillis(dateLaunched),
    updatedAt = now,
)

fun ExchangeInfoDto.toDetailEntity(now: Long): ExchangeDetailEntity = ExchangeDetailEntity(
    id = id,
    description = description,
    websiteUrl = urls?.website?.firstOrNull(),
    makerFee = makerFee,
    takerFee = takerFee,
    updatedAt = now,
)

fun ExchangeAssetDto.toEntity(exchangeId: Int, now: Long): ExchangeAssetEntity = ExchangeAssetEntity(
    exchangeId = exchangeId,
    walletAddress = walletAddress,
    currencySymbol = currency.symbol.orEmpty(),
    currencyName = currency.name.orEmpty(),
    currencyPriceUsd = currency.priceUsd ?: 0.0,
    balance = balance ?: 0.0,
    updatedAt = now,
)

fun ExchangeEntity.toDomain(): Exchange = Exchange(
    id = id,
    name = name,
    logoUrl = logoUrl,
    spotVolumeUsd = spotVolumeUsd,
    dateLaunched = DateParsing.epochMillisToInstant(dateLaunchedEpochMs),
)

fun toExchangeDetail(base: ExchangeEntity, detail: ExchangeDetailEntity?): ExchangeDetail = ExchangeDetail(
    id = base.id,
    name = base.name,
    logoUrl = base.logoUrl,
    description = detail?.description,
    websiteUrl = detail?.websiteUrl,
    makerFee = detail?.makerFee,
    takerFee = detail?.takerFee,
    dateLaunched = DateParsing.epochMillisToInstant(base.dateLaunchedEpochMs),
)

fun ExchangeAssetEntity.toDomain(): ExchangeAsset = ExchangeAsset(
    walletAddress = walletAddress,
    currencyName = currencyName,
    currencyPriceUsd = currencyPriceUsd,
    currencySymbol = currencySymbol,
    balance = balance,
)
