package br.com.edmundo.desafiomb.feature.exchanges.mapper

import br.com.edmundo.desafiomb.core.domain.error.AppError
import br.com.edmundo.desafiomb.core.domain.model.Exchange
import br.com.edmundo.desafiomb.core.ui.R
import br.com.edmundo.desafiomb.core.ui.state.UiError
import br.com.edmundo.desafiomb.core.ui.text.UiText
import br.com.edmundo.desafiomb.feature.exchanges.format.DateFormatter
import br.com.edmundo.desafiomb.feature.exchanges.format.VolumeFormatter
import br.com.edmundo.desafiomb.feature.exchanges.model.ExchangeUiModel
import java.util.Locale

fun Exchange.toUiModel(locale: Locale = Locale.getDefault()): ExchangeUiModel = ExchangeUiModel(
    id = id,
    name = name,
    logoUrl = logoUrl,
    volume = VolumeFormatter.format(spotVolumeUsd, locale),
    launchedAt = DateFormatter.format(dateLaunched, locale),
)

fun AppError.toUiError(): UiError = when (this) {
    AppError.NoConnection -> UiError(UiText.Resource(R.string.error_no_connection), isRetryable = true)
    AppError.Timeout -> UiError(UiText.Resource(R.string.error_timeout), isRetryable = true)
    AppError.InvalidApiKey -> UiError(UiText.Resource(R.string.error_invalid_api_key), isRetryable = false)
    AppError.PlanNotAuthorized -> UiError(UiText.Resource(R.string.error_plan_not_authorized), isRetryable = false)
    is AppError.RateLimited -> UiError(UiText.Resource(R.string.error_rate_limited), isRetryable = true)
    AppError.Server -> UiError(UiText.Resource(R.string.error_server), isRetryable = true)
    AppError.Serialization -> UiError(UiText.Resource(R.string.error_serialization), isRetryable = false)
    is AppError.Unknown -> UiError(UiText.Resource(R.string.error_unknown), isRetryable = true)
}
