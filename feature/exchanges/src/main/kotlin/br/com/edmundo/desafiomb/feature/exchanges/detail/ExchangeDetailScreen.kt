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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import br.com.edmundo.desafiomb.core.ui.component.AppTopBar
import br.com.edmundo.desafiomb.core.ui.component.ErrorState
import br.com.edmundo.desafiomb.core.ui.component.ExpandableText
import br.com.edmundo.desafiomb.core.ui.component.LoadingSkeleton
import br.com.edmundo.desafiomb.core.ui.component.RankBadge
import br.com.edmundo.desafiomb.core.ui.component.RemoteImage
import br.com.edmundo.desafiomb.core.ui.component.SkeletonVariant
import br.com.edmundo.desafiomb.core.ui.component.StatTile
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
            AssetsState.Loading -> item { LoadingSkeleton(itemCount = 5, variant = SkeletonVariant.DividedRow) }

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
    Column(
        modifier = Modifier.fillMaxWidth().testTag(TestTags.DETAIL_HEADER).padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RemoteImage(
            url = header.logoUrl,
            contentDescription = stringResource(R.string.exchange_logo_content_description, header.name),
            size = Spacing.avatarLarge,
            modifier = Modifier.testTag(TestTags.detailField("logo")),
        )
        Text(
            text = header.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = Spacing.md).testTag(TestTags.detailField("name")),
        )
        Row(
            modifier = Modifier.padding(top = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            RankBadge(text = "#${header.id}", modifier = Modifier.testTag(TestTags.detailField("id")))
            Text(
                text = stringResource(R.string.exchange_launched_at, header.launchedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(TestTags.detailField("date_launched")),
            )
        }
        if (header.websiteUrl != null) {
            Button(
                onClick = onWebsiteClick,
                shape = RoundedCornerShape(Spacing.lg),
                modifier = Modifier.padding(top = Spacing.md).testTag(TestTags.detailField("website")),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.padding(end = Spacing.sm),
                )
                Text(text = stringResource(R.string.exchange_detail_visit_website))
            }
        }
    }
}

@Composable
private fun FeesSection(header: ExchangeDetailUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        StatTile(
            label = stringResource(R.string.exchange_detail_maker_fee_label),
            value = header.makerFee,
            modifier = Modifier.weight(1f).testTag(TestTags.detailField("maker_fee")),
        )
        StatTile(
            label = stringResource(R.string.exchange_detail_taker_fee_label),
            value = header.takerFee,
            modifier = Modifier.weight(1f).testTag(TestTags.detailField("taker_fee")),
        )
    }
}

@Composable
private fun AboutSection(description: String?) {
    if (description.isNullOrBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth().padding(Spacing.md),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(text = stringResource(R.string.exchange_detail_about_title), style = MaterialTheme.typography.titleMedium)
            ExpandableText(
                text = description,
                modifier = Modifier.padding(top = Spacing.sm).testTag(TestTags.detailField("description")),
            )
        }
    }
}

@Composable
private fun AssetsSectionHeader(assetsState: AssetsState) {
    val total = (assetsState as? AssetsState.Content)?.total ?: 0
    Text(
        text = stringResource(R.string.exchange_detail_assets_title, total),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm).testTag(TestTags.ASSETS_SECTION),
    )
}

@Composable
private fun AssetRow(asset: ExchangeAssetUiModel) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = asset.currencyName, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = asset.priceUsd,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Spacing.md),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
