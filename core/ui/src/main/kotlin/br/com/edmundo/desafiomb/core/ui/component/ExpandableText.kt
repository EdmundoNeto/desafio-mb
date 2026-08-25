package br.com.edmundo.desafiomb.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import br.com.edmundo.desafiomb.core.ui.R

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 4,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var canExpand by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            maxLines = if (expanded) Int.MAX_VALUE else maxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result -> if (!expanded) canExpand = result.hasVisualOverflow },
            style = MaterialTheme.typography.bodyMedium,
        )
        if (canExpand || expanded) {
            Text(
                text = stringResource(if (expanded) R.string.action_show_less else R.string.action_show_more),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { expanded = !expanded },
            )
        }
    }
}
