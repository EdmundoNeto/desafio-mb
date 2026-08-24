package br.com.edmundo.desafiomb.core.ui.state

import br.com.edmundo.desafiomb.core.ui.text.UiText

data class UiError(val message: UiText, val isRetryable: Boolean)
