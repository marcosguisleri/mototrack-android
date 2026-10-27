package br.dev.guisleri.mototrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Every default Material surface (dialogs, pickers, sheets) takes its colour from
// here, so nothing falls back to the stock lavender or to the old blue/teal.
private val LightColorScheme = lightColorScheme(
    primary = WarmEmber,
    onPrimary = Color.White,
    primaryContainer = WarmEmberTint,
    onPrimaryContainer = WarmEmberDeep,

    secondary = WarmInk,
    onSecondary = Color.White,
    secondaryContainer = WarmHairline,
    onSecondaryContainer = WarmInk,

    tertiary = WarmSuccess,
    onTertiary = Color.White,

    background = WarmCanvas,
    onBackground = WarmInk,

    surface = WarmCard,
    onSurface = WarmInk,
    surfaceVariant = WarmSkeleton,
    onSurfaceVariant = WarmInkSoft,

    surfaceContainerLowest = WarmCard,
    surfaceContainerLow = WarmCanvas,
    surfaceContainer = Color(0xFFF1ECE5),
    surfaceContainerHigh = Color(0xFFEFE9E1),
    surfaceContainerHighest = WarmSkeleton,

    outline = WarmMuted,
    outlineVariant = WarmHairline
)

@Composable
fun MotoTrackTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
