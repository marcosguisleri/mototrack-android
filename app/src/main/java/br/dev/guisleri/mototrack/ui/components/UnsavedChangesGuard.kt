package br.dev.guisleri.mototrack.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Asks before a form is left with filled-in data. [content] gets [requestExit] for its back
 * button; the system back gesture goes through the same check. Without changes both leave at once.
 */
@Composable
internal fun UnsavedChangesGuard(
    hasChanges: Boolean,
    onExit: () -> Unit,
    content: @Composable (requestExit: () -> Unit) -> Unit
) {
    var confirming by rememberSaveable { mutableStateOf(false) }

    val requestExit: () -> Unit = {
        if (hasChanges) {
            confirming = true
        } else {
            onExit()
        }
    }

    BackHandler(enabled = hasChanges, onBack = requestExit)

    content(requestExit)

    if (confirming) {
        WarmDialog(
            icon = Icons.Outlined.WarningAmber,
            title = "Sair sem salvar?",
            message = "Se você sair agora, o que já preencheu será perdido.",
            confirmText = "Sair",
            onConfirm = {
                confirming = false
                onExit()
            },
            dismissText = "Continuar editando",
            onDismissRequest = { confirming = false },
            destructive = true
        )
    }
}

/** Tapping an empty area of a form drops the focus of the field being typed in (and the keyboard). */
@Composable
internal fun Modifier.clearFocusOnTap(): Modifier {
    val focusManager = LocalFocusManager.current

    return pointerInput(focusManager) {
        detectTapGestures { focusManager.clearFocus() }
    }
}
