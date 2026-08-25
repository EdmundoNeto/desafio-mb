package br.com.edmundo.desafiomb.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.edmundo.desafiomb.core.ui.theme.Spacing

@Composable
fun RankBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(percent = 50))
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
    )
}
