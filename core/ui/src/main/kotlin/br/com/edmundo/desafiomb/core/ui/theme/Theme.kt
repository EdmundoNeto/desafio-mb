package br.com.edmundo.desafiomb.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
    lightColorScheme(
        primary = Blue40,
        onPrimary = Neutral99,
        primaryContainer = Blue90,
        onPrimaryContainer = Blue10,
        secondary = Teal40,
        onSecondary = Neutral99,
        secondaryContainer = Teal90,
        onSecondaryContainer = Teal10,
        background = Neutral99,
        onBackground = Neutral10,
        surface = Neutral99,
        onSurface = Neutral10,
        surfaceVariant = Neutral95,
        onSurfaceVariant = Neutral20,
        error = Red40,
        onError = Neutral99,
        errorContainer = Red80,
        onErrorContainer = Red10,
    )

private val DarkColors =
    darkColorScheme(
        primary = Blue80,
        onPrimary = Blue20,
        primaryContainer = Blue30,
        onPrimaryContainer = Blue90,
        secondary = Teal80,
        onSecondary = Teal20,
        secondaryContainer = Teal30,
        onSecondaryContainer = Teal90,
        background = Neutral10,
        onBackground = Neutral90,
        surface = Neutral10,
        onSurface = Neutral90,
        surfaceVariant = Neutral20,
        onSurfaceVariant = Neutral90,
        error = Red80,
        onError = Red10,
        errorContainer = Red30,
        onErrorContainer = Red80,
    )

@Composable
fun CmcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
