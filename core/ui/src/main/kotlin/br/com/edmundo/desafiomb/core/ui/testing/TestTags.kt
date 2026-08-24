package br.com.edmundo.desafiomb.core.ui.testing

/**
 * testTags centralizados (SD-04): producao e teste compartilham a mesma constante,
 * entao renomear nao quebra em silencio.
 */
object TestTags {
    const val EXCHANGE_LIST = "exchange_list"
    const val LIST_SKELETON = "list_skeleton"
    const val FULL_SCREEN_ERROR = "full_screen_error"
    const val RETRY_BUTTON = "retry_button"
    const val OFFLINE_BANNER = "offline_banner"
    const val APPEND_LOADING = "append_loading"
    const val APPEND_ERROR = "append_error"
    const val DETAIL_HEADER = "detail_header"
    const val ASSETS_SECTION = "assets_section"
    const val ASSETS_ERROR = "assets_error"
    const val ASSETS_EMPTY = "assets_empty"

    fun exchangeListItem(id: Int): String = "exchange_list_item_$id"

    fun detailField(name: String): String = "detail_field_$name"
}
