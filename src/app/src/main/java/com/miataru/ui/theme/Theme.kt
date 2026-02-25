package com.miataru.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MiataruPrimary,
    onPrimary = MiataruOnPrimary,
    primaryContainer = MiataruPrimaryContainer,
    onPrimaryContainer = MiataruOnPrimaryContainer,
    secondary = MiataruSecondary,
    onSecondary = MiataruOnSecondary,
    secondaryContainer = MiataruSecondaryContainer,
    onSecondaryContainer = MiataruOnSecondaryContainer,
    surface = MiataruSurface,
    onSurface = MiataruOnSurface,
    surfaceVariant = MiataruSurfaceVariant,
    onSurfaceVariant = MiataruOnSurfaceVariant,
    error = MiataruError,
    onError = MiataruOnError,
)

private val DarkColors = darkColorScheme(
    primary = MiataruPrimaryContainer,
    onPrimary = MiataruOnPrimaryContainer,
    secondary = MiataruSecondaryContainer,
    onSecondary = MiataruOnSecondaryContainer,
)

@Composable
fun MiataruTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
