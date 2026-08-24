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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun RemoteImage(url: String?, contentDescription: String, modifier: Modifier = Modifier) {
    if (url == null) {
        Surface(modifier = modifier.size(48.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Icon(imageVector = Icons.Default.CurrencyExchange, contentDescription = contentDescription)
        }
        return
    }
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier.size(48.dp),
        contentScale = ContentScale.Fit,
    )
}
