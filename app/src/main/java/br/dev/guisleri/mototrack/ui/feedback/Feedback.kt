package br.dev.guisleri.mototrack.ui.feedback

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import br.dev.guisleri.mototrack.ui.theme.WarmInk

enum class FeedbackKind {
    SUCCESS,
    INFO,
    ERROR
}

/** A short confirmation (or failure) message shown after something the rider did. */
data class Feedback(
    val message: String,
    val kind: FeedbackKind = FeedbackKind.SUCCESS
)

private class FeedbackVisuals(
    val feedback: Feedback
) : SnackbarVisuals {
    override val message: String get() = feedback.message
    override val actionLabel: String? get() = null
    override val withDismissAction: Boolean get() = false
    override val duration: SnackbarDuration
        get() = if (feedback.kind == FeedbackKind.ERROR) {
            SnackbarDuration.Long
        } else {
            SnackbarDuration.Short
        }
}

/** Replaces the message on screen, if any, so quick actions never queue up. */
suspend fun SnackbarHostState.showFeedback(feedback: Feedback) {
    currentSnackbarData?.dismiss()
    showSnackbar(FeedbackVisuals(feedback))
}

private val SuccessGreen = Color(0xFF7BD39A)
private val ErrorRed = Color(0xFFFF8A80)

@Composable
fun FeedbackHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        val kind = (data.visuals as? FeedbackVisuals)?.feedback?.kind ?: FeedbackKind.INFO

        LaunchedEffect(data) {
            when (kind) {
                FeedbackKind.SUCCESS -> haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                FeedbackKind.ERROR -> haptics.performHapticFeedback(HapticFeedbackType.Reject)
                FeedbackKind.INFO -> Unit
            }
        }

        Snackbar(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            containerColor = WarmInk,
            contentColor = Color.White
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (kind) {
                        FeedbackKind.SUCCESS -> Icons.Filled.CheckCircle
                        FeedbackKind.INFO -> Icons.Filled.Info
                        FeedbackKind.ERROR -> Icons.Filled.ErrorOutline
                    },
                    contentDescription = null,
                    tint = when (kind) {
                        FeedbackKind.SUCCESS -> SuccessGreen
                        FeedbackKind.INFO -> WarmEmberOnDark
                        FeedbackKind.ERROR -> ErrorRed
                    },
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
