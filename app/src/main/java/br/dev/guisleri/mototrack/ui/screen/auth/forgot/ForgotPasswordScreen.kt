package br.dev.guisleri.mototrack.ui.screen.auth.forgot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.ui.components.WarmTextField
import br.dev.guisleri.mototrack.ui.screen.auth.AuthErrorBanner
import br.dev.guisleri.mototrack.ui.screen.auth.AuthPrimaryButton
import br.dev.guisleri.mototrack.ui.screen.auth.AuthScaffold
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted

@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onSendCodeClick: (email: String) -> Unit,
    onResendCodeClick: () -> Unit,
    onChangeEmailClick: () -> Unit,
    onResetClick: (code: String, newPassword: String, confirmPassword: String) -> Unit,
    onBackToLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable { mutableStateOf(uiState.email) }
    var code by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val isLoading = uiState.isLoading

    AuthScaffold(
        headline = "Vamos recuperar o seu acesso.",
        modifier = modifier,
        heroHeight = 300.dp,
        bikeWidthFraction = 0.58f,
        bikeAlignment = Alignment.BottomEnd
    ) {
        when (uiState.step) {
            ForgotPasswordStep.EMAIL -> {
                Text(
                    text = "Esqueceu a senha?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmInk
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Informe o e-mail da sua conta e enviaremos um código para " +
                        "você criar uma senha nova.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkSoft
                )

                Spacer(modifier = Modifier.height(22.dp))

                WarmTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail",
                    leadingIcon = Icons.Outlined.Email,
                    error = null,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSendCodeClick(email) })
                )

                uiState.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(14.dp))
                    AuthErrorBanner(message = message)
                }

                Spacer(modifier = Modifier.height(22.dp))

                AuthPrimaryButton(
                    text = "Enviar código",
                    isLoading = isLoading,
                    onClick = { onSendCodeClick(email) }
                )
            }

            ForgotPasswordStep.CODE -> {
                Text(
                    text = "Confira o seu e-mail",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmInk
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Se existe uma conta com ${uiState.email}, enviamos um código de " +
                        "$RESET_CODE_LENGTH dígitos. Ele vale por 15 minutos. Não achou? " +
                        "Olhe também o spam.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkSoft
                )

                Spacer(modifier = Modifier.height(22.dp))

                WarmTextField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit).take(RESET_CODE_LENGTH) },
                    label = "Código",
                    leadingIcon = Icons.Outlined.Pin,
                    error = null,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                WarmTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Nova senha",
                    leadingIcon = Icons.Outlined.Lock,
                    error = null,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
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

                Spacer(modifier = Modifier.height(12.dp))

                WarmTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirmar nova senha",
                    leadingIcon = Icons.Outlined.Lock,
                    error = null,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onResetClick(code, password, confirmPassword) }
                    ),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    }
                )

                uiState.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(14.dp))
                    AuthErrorBanner(message = message)
                }

                Spacer(modifier = Modifier.height(22.dp))

                AuthPrimaryButton(
                    text = "Redefinir senha",
                    isLoading = isLoading,
                    onClick = { onResetClick(code, password, confirmPassword) }
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                        onClick = onResendCodeClick
                    )

                    LinkText(
                        text = "Trocar e-mail",
                        enabled = !isLoading,
                        onClick = onChangeEmailClick
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LinkText(
                text = "Voltar para o login",
                enabled = !isLoading,
                onClick = onBackToLoginClick,
                emphasized = true
            )
        }
    }
}

@Composable
private fun LinkText(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    emphasized: Boolean = false
) {
    Box(
        modifier = Modifier
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (enabled) WarmEmber else WarmMuted
        )
    }
}
