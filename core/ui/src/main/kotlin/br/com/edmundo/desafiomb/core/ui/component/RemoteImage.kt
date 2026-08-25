package br.com.edmundo.desafiomb.core.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        if (url == null) {
            Icon(
                imageVector = Icons.Default.CurrencyExchange,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
