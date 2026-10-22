package br.dev.guisleri.mototrack.ui.screen.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleType
import br.dev.guisleri.mototrack.ui.components.MotorcycleHeroImage
import br.dev.guisleri.mototrack.ui.components.RoadDecoration
import br.dev.guisleri.mototrack.ui.theme.EmberHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.theme.WarmInk

private val SheetOverlap = 28.dp
private val CompactHeroHeight = 96.dp

/**
 * Shared frame of the sign in and sign up screens: an ember hero with a
 * motorcycle on top and a white sheet with the form rising over it.
 * The hero folds away while the keyboard is open so the form keeps its room.
 */
@Composable
internal fun AuthScaffold(
    headline: String,
    modifier: Modifier = Modifier,
    heroHeight: Dp = 380.dp,
    bikeWidthFraction: Float = 0.84f,
    bikeAlignment: Alignment = Alignment.BottomCenter,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    LightStatusBarIcons()

    val density = LocalDensity.current
    val keyboardOpen = WindowInsets.ime.getBottom(density) > 0
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val currentHeroHeight by animateDpAsState(
        targetValue = if (keyboardOpen) CompactHeroHeight else heroHeight,
        label = "auth-hero-height"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmCanvas)
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBar + currentHeroHeight)
                .background(EmberHeroBrush)
        ) {
            RoadDecoration(
                modifier = Modifier.matchParentSize(),
                color = Color.White
            )

            AnimatedVisibility(
                visible = !keyboardOpen,
                modifier = Modifier.align(bikeAlignment),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                MotorcycleHeroImage(
                    type = MotorcycleType.NAKED,
                    colorName = "Preta",
                    modifier = Modifier
                        .fillMaxWidth(bikeWidthFraction)
                        .padding(bottom = SheetOverlap)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 28.dp,
                        end = 28.dp,
                        top = statusBar + 20.dp
                    )
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) {
                            append("MOTO")
                        }
                        withStyle(SpanStyle(color = WarmInk)) {
                            append("TRACK")
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp
                )

                AnimatedVisibility(
                    visible = !keyboardOpen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = headline,
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .padding(top = 16.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBar + currentHeroHeight - SheetOverlap)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(WarmCard)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 28.dp,
                    bottom = 24.dp
                )
                .animateContentSize(),
            content = content
        )

        overlay()
    }
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = WarmInk,
            contentColor = Color.White,
            disabledContainerColor = WarmInk.copy(alpha = 0.7f),
            disabledContentColor = Color.White
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun AuthErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    val error = MaterialTheme.colorScheme.error

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(error.copy(alpha = 0.08f))
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = error
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = error
        )
    }
}

/** The hero is dark, so the status bar icons must be light while a screen of this family shows. */
@Composable
private fun LightStatusBarIcons() {
    val view = LocalView.current

    if (view.isInEditMode) return

    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars

        controller?.isAppearanceLightStatusBars = false

        onDispose {
            if (controller != null && previous != null) {
                controller.isAppearanceLightStatusBars = previous
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
