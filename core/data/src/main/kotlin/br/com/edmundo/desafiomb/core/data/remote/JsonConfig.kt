package br.com.edmundo.desafiomb.core.data.remote

import kotlinx.serialization.json.Json

val cmcJson =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = false
    }
