package br.com.edmundo.desafiomb.feature.exchanges.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.edmundo.desafiomb.core.ui.component.AppTopBar
import br.com.edmundo.desafiomb.core.ui.component.EmptyState
import br.com.edmundo.desafiomb.core.ui.component.ErrorState
import br.com.edmundo.desafiomb.core.ui.component.LoadingSkeleton
import br.com.edmundo.desafiomb.core.ui.component.OfflineBanner
import br.com.edmundo.desafiomb.core.ui.component.RankBadge
import br.com.edmundo.desafiomb.core.ui.component.RemoteImage
import br.com.edmundo.desafiomb.core.ui.testing.TestTags
import br.com.edmundo.desafiomb.core.ui.text.asString
import br.com.edmundo.desafiomb.core.ui.theme.Spacing
import br.com.edmundo.desafiomb.feature.exchanges.R
import br.com.edmundo.desafiomb.feature.exchanges.model.ExchangeUiModel
import org.koin.androidx.compose.koinViewModel

private const val LOAD_MORE_VISIBLE_ITEMS_THRESHOLD = 5

@Composable
fun ExchangeListScreen(
    onExchangeClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExchangeListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.exchange_list_title),
                onRefresh = { viewModel.onEvent(ExchangeListEvent.Refresh) },
            )
        },
    ) { padding ->
        when {
            state.isLoadingFirstPage -> LoadingSkeleton(modifier = Modifier.padding(padding))

            state.fullScreenError != null ->
                ErrorState(
                    message = state.fullScreenError!!.message.asString(),
                    onRetry = { viewModel.onEvent(ExchangeListEvent.Retry) },
                    modifier = Modifier.padding(padding),
                )

            state.items.isEmpty() ->
                EmptyState(
                    message = stringResource(R.string.exchange_list_empty),
                    modifier = Modifier.padding(padding),
                )

            else ->
                ExchangeListContent(
                    state = state,
                    padding = padding,
                    onExchangeClick = onExchangeClick,
                    onLoadMore = { viewModel.onEvent(ExchangeListEvent.LoadMore) },
                    onRefresh = { viewModel.onEvent(ExchangeListEvent.Refresh) },
                    onRetryAppend = { viewModel.onEvent(ExchangeListEvent.RetryAppend) },
                )
        }
    }
}

@Composable
private fun ExchangeListContent(
    state: ExchangeListUiState,
    padding: PaddingValues,
    onExchangeClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onRetryAppend: () -> Unit,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible =
                listState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: 0
            lastVisible >= state.items.size - LOAD_MORE_VISIBLE_ITEMS_THRESHOLD
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = onRefresh, modifier = Modifier.padding(padding)) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.isStale) {
                OfflineBanner(message = stringResource(br.com.edmundo.desafiomb.core.ui.R.string.offline_banner_message))
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().testTag(TestTags.EXCHANGE_LIST),
                contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                itemsIndexed(state.items, key = { _, item -> item.id }) { index, item ->
                    ExchangeRow(rank = index + 1, item = item, onExchangeClick = onExchangeClick)
                }

                item {
                    when (state.appendState) {
                        AppendState.Loading ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(Spacing.md).testTag(TestTags.APPEND_LOADING),
                                horizontalArrangement = Arrangement.Center,
                            ) { CircularProgressIndicator() }

                        is AppendState.Error ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.md)
                                        .testTag(TestTags.APPEND_ERROR)
                                        .clickable { onRetryAppend() },
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.exchange_list_append_error),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }

                        AppendState.EndReached ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.exchange_list_end_of_list),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                        AppendState.Idle -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun ExchangeRow(
    rank: Int,
    item: ExchangeUiModel,
    onExchangeClick: (Int) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(TestTags.exchangeListItem(item.id))
                .clickable { onExchangeClick(item.id) }
                .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RankBadge(text = rank.toString())
            RemoteImage(
                url = item.logoUrl,
                contentDescription = stringResource(R.string.exchange_logo_content_description, item.name),
                modifier = Modifier.padding(start = Spacing.sm),
            )
            Column(modifier = Modifier.padding(start = Spacing.md).weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = stringResource(R.string.exchange_launched_at, item.launchedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = item.volume,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.xs),
            )
        }
    }
}
