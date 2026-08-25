package br.com.edmundo.desafiomb.core.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import br.com.edmundo.desafiomb.core.ui.theme.Spacing
import coil3.compose.AsyncImage

@Composable
fun RemoteImage(
    url: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = Spacing.avatarMedium,
) {
    if (url == null) {
        Surface(modifier = modifier.size(size), color = MaterialTheme.colorScheme.surfaceVariant) {
            Icon(imageVector = Icons.Default.CurrencyExchange, contentDescription = contentDescription)
        }
        return
    }
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
    )
}
