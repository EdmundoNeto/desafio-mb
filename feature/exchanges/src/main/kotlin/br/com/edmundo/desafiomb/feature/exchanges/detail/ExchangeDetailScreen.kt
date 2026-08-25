package br.com.edmundo.desafiomb.feature.exchanges.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import br.com.edmundo.desafiomb.core.ui.component.AppTopBar
import br.com.edmundo.desafiomb.core.ui.component.ErrorState
import br.com.edmundo.desafiomb.core.ui.component.ExpandableText
import br.com.edmundo.desafiomb.core.ui.component.LoadingSkeleton
import br.com.edmundo.desafiomb.core.ui.component.RemoteImage
import br.com.edmundo.desafiomb.core.ui.testing.TestTags
import br.com.edmundo.desafiomb.core.ui.text.asString
import br.com.edmundo.desafiomb.core.ui.theme.Spacing
import br.com.edmundo.desafiomb.feature.exchanges.R
import br.com.edmundo.desafiomb.feature.exchanges.model.ExchangeAssetUiModel
import br.com.edmundo.desafiomb.feature.exchanges.model.ExchangeDetailUiModel
import org.koin.androidx.compose.navigation.koinNavViewModel

@Composable
fun ExchangeDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExchangeDetailViewModel = koinNavViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val openUrlErrorMessage = stringResource(R.string.exchange_detail_open_url_error)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is ExchangeDetailEvent.OpenUrl ->
                        openExternalUrl(context, event.url) {
                            snackbarHostState.showSnackbar(openUrlErrorMessage)
                        }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(title = state.header?.name ?: stringResource(R.string.exchange_detail_title), onBack = onBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoadingHeader -> LoadingSkeleton(modifier = Modifier.padding(padding), itemCount = 5)

            state.headerError != null ->
                ErrorState(
                    message = state.headerError!!.message.asString(),
                    onRetry = viewModel::onRetryHeader,
                    modifier = Modifier.padding(padding),
                )

            else ->
                state.header?.let { header ->
                    ExchangeDetailContent(
                        header = header,
                        assetsState = state.assetsState,
                        padding = padding,
                        onRetryAssets = viewModel::onRetryAssets,
                        onWebsiteClick = viewModel::onWebsiteClick,
                    )
                }
        }
    }
}

private suspend fun openExternalUrl(
    context: Context,
    url: String,
    onFailure: suspend () -> Unit,
) {
    val uri = Uri.parse(url)
    try {
        CustomTabsIntent.Builder().build().launchUrl(context, uri)
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            onFailure()
        }
    }
}

@Composable
private fun ExchangeDetailContent(
    header: ExchangeDetailUiModel,
    assetsState: AssetsState,
    padding: PaddingValues,
    onRetryAssets: () -> Unit,
    onWebsiteClick: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxWidth().padding(padding)) {
        item { HeaderSection(header, onWebsiteClick) }
        item { FeesSection(header) }
        item { AboutSection(header.description) }
        item { AssetsSectionHeader(assetsState) }

        when (assetsState) {
            AssetsState.Loading -> item { LoadingSkeleton(itemCount = 5) }

            is AssetsState.Content -> items(assetsState.items) { asset -> AssetRow(asset) }

            AssetsState.Empty ->
                item {
                    Text(
                        text = stringResource(R.string.exchange_detail_assets_empty),
                        modifier = Modifier.fillMaxWidth().padding(Spacing.md).testTag(TestTags.ASSETS_EMPTY),
                    )
                }

            is AssetsState.Error ->
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md).testTag(TestTags.ASSETS_ERROR)) {
                        Text(text = assetsState.error.message.asString())
                        TextButton(onClick = onRetryAssets) {
                            Text(text = stringResource(br.com.edmundo.desafiomb.core.ui.R.string.action_retry))
                        }
                    }
                }
        }
    }
}

@Composable
private fun HeaderSection(
    header: ExchangeDetailUiModel,
    onWebsiteClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().testTag(TestTags.DETAIL_HEADER).padding(Spacing.md)) {
        Row {
            RemoteImage(
                url = header.logoUrl,
                contentDescription = stringResource(R.string.exchange_logo_content_description, header.name),
                size = Spacing.avatarLarge,
                modifier = Modifier.testTag(TestTags.detailField("logo")),
            )
            Column(modifier = Modifier.padding(start = Spacing.md)) {
                Text(
                    text = header.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag(TestTags.detailField("name")),
                )
                Text(
                    text = "#${header.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag(TestTags.detailField("id")),
                )
                Text(
                    text = stringResource(R.string.exchange_launched_at, header.launchedAt),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag(TestTags.detailField("date_launched")),
                )
            }
        }
        if (header.websiteUrl != null) {
            Button(
                onClick = onWebsiteClick,
                modifier = Modifier.padding(top = Spacing.sm).testTag(TestTags.detailField("website")),
            ) {
                Text(text = stringResource(R.string.exchange_detail_visit_website))
            }
        }
    }
}

@Composable
private fun FeesSection(header: ExchangeDetailUiModel) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
        Column(modifier = Modifier.testTag(TestTags.detailField("maker_fee"))) {
            Text(text = stringResource(R.string.exchange_detail_maker_fee_label), style = MaterialTheme.typography.labelMedium)
            Text(text = header.makerFee, style = MaterialTheme.typography.bodyLarge)
        }
        Column(modifier = Modifier.padding(start = Spacing.xxl).testTag(TestTags.detailField("taker_fee"))) {
            Text(text = stringResource(R.string.exchange_detail_taker_fee_label), style = MaterialTheme.typography.labelMedium)
            Text(text = header.takerFee, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun AboutSection(description: String?) {
    if (description.isNullOrBlank()) return
    Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
        Text(text = stringResource(R.string.exchange_detail_about_title), style = MaterialTheme.typography.titleMedium)
        ExpandableText(
            text = description,
            modifier = Modifier.padding(top = Spacing.sm).testTag(TestTags.detailField("description")),
        )
    }
}

@Composable
private fun AssetsSectionHeader(assetsState: AssetsState) {
    val total = (assetsState as? AssetsState.Content)?.total ?: 0
    Text(
        text = stringResource(R.string.exchange_detail_assets_title, total),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm).testTag(TestTags.ASSETS_SECTION),
    )
}

@Composable
private fun AssetRow(asset: ExchangeAssetUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = asset.currencyName, style = MaterialTheme.typography.bodyLarge)
        Text(text = asset.priceUsd, style = MaterialTheme.typography.bodyLarge)
    }
}
