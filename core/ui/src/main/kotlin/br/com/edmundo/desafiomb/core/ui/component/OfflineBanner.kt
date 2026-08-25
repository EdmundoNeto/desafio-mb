package br.com.edmundo.desafiomb.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import br.com.edmundo.desafiomb.core.ui.testing.TestTags
import br.com.edmundo.desafiomb.core.ui.theme.Spacing

@Composable
fun OfflineBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(TestTags.OFFLINE_BANNER)
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    )
}
