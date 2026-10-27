package br.dev.guisleri.mototrack.ui.screen.profile.email

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.ui.components.CircleActionButton
import br.dev.guisleri.mototrack.ui.components.FormSection
import br.dev.guisleri.mototrack.ui.components.WarmTextField
import br.dev.guisleri.mototrack.ui.screen.auth.forgot.RESET_CODE_LENGTH
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted

@Composable
fun ChangeEmailScreen(
    uiState: ChangeEmailUiState,
    currentEmail: String?,
    onBackClick: () -> Unit,
    onRequestClick: (newEmail: String, currentPassword: String) -> Unit,
    onConfirmClick: (code: String) -> Unit,
    onResendClick: () -> Unit,
    onEditRequestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }

    val isLoading = uiState.isLoading
    val confirming = uiState.step == ChangeEmailStep.CONFIRM

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            CircleActionButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar",
                onClick = onBackClick
            )

            Column {
                Text(
                    text = "Alterar e-mail",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmInk
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (confirming) {
                        "Enviamos um código de $RESET_CODE_LENGTH dígitos para " +
                            "${uiState.newEmail}. Digite-o para confirmar. Ele vale por 15 minutos."
                    } else {
                        "Para sua segurança, confirmamos o novo endereço com um código e " +
                            "pedimos a sua senha atual." +
                            (currentEmail?.let { " Seu e-mail agora é $it." } ?: "")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkSoft
                )
            }

            if (!confirming) {
                FormSection(title = "NOVO E-MAIL") {
                    WarmTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = "Novo e-mail",
                        leadingIcon = Icons.Outlined.Email,
                        error = null,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    WarmTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Senha atual",
                        leadingIcon = Icons.Outlined.Lock,
                        error = null,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onRequestClick(newEmail, password) }
                        ),
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = if (passwordVisible) {
                                        "Ocultar senha"
                                    } else {
                                        "Mostrar senha"
                                    },
                                    tint = WarmMuted
                                )
                            }
                        }
                    )
                }
            } else {
                FormSection(title = "CÓDIGO") {
                    WarmTextField(
                        value = code,
                        onValueChange = { code = it.filter(Char::isDigit).take(RESET_CODE_LENGTH) },
                        label = "Código de $RESET_CODE_LENGTH dígitos",
                        leadingIcon = Icons.Outlined.Pin,
                        error = null,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onConfirmClick(code) })
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LinkText(
                            text = if (uiState.resendSecondsLeft > 0) {
                                "Reenviar em ${uiState.resendSecondsLeft}s"
                            } else {
                                "Reenviar código"
                            },
                            enabled = !isLoading && uiState.resendSecondsLeft == 0,
                            onClick = onResendClick
                        )

                        LinkText(
                            text = "Corrigir dados",
                            enabled = !isLoading,
                            onClick = onEditRequestClick
                        )
                    }
                }
            }
        }

        Surface(
            color = WarmCard,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = {
                        if (confirming) onConfirmClick(code) else onRequestClick(newEmail, password)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
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
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = when {
                            isLoading -> "Enviando..."
                            confirming -> "Confirmar novo e-mail"
                            else -> "Enviar código"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkText(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) WarmEmber else WarmMuted
        )
    }
}
