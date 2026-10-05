package br.dev.guisleri.mototrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = MotoTrackTeal,
    onPrimary = MotoTrackSurface,

    secondary = MotoTrackAccent,
    onSecondary = MotoTrackDark,

    background = MotoTrackBackground,
    onBackground = MotoTrackTextPrimary,

    surface = MotoTrackSurface,
    onSurface = MotoTrackTextPrimary,

    onSurfaceVariant = MotoTrackTextSecondary
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