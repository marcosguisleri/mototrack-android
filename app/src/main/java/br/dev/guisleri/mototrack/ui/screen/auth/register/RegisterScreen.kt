package br.dev.guisleri.mototrack.ui.screen.auth.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import br.dev.guisleri.mototrack.ui.theme.WarmSuccess

private const val MIN_PASSWORD_LENGTH = 8

@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onRegisterClick: (
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable {
        mutableStateOf("")
    }

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var confirmPassword by rememberSaveable {
        mutableStateOf("")
    }

    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    val isLoading = uiState is RegisterUiState.Loading

    val submit: () -> Unit = {
        onRegisterClick(
            name,
            email,
            password,
            confirmPassword
        )
    }

    AuthScaffold(
        headline = "Sua garagem, suas histórias.",
        modifier = modifier,
        heroHeight = 300.dp,
        bikeWidthFraction = 0.58f,
        bikeAlignment = Alignment.BottomEnd
    ) {
        Text(
            text = "Crie sua conta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Comece a registrar suas jornadas.",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkSoft
        )

        Spacer(modifier = Modifier.height(22.dp))

        WarmTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nome",
            leadingIcon = Icons.Outlined.Person,
            error = null,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        WarmTextField(
            value = email,
            onValueChange = { email = it },
            label = "E-mail",
            leadingIcon = Icons.Outlined.Email,
            error = null,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        WarmTextField(
            value = password,
            onValueChange = { password = it },
            label = "Senha",
            leadingIcon = Icons.Outlined.Lock,
            error = null,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            visualTransformation = passwordTransformation(passwordVisible),
            trailingIcon = {
                PasswordVisibilityToggle(
                    visible = passwordVisible,
                    enabled = !isLoading,
                    onToggle = { passwordVisible = !passwordVisible }
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        WarmTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirmar senha",
            leadingIcon = Icons.Outlined.Lock,
            error = null,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { submit() }
            ),
            visualTransformation = passwordTransformation(passwordVisible)
        )

        if (password.isNotEmpty() || confirmPassword.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            PasswordRule(
                text = "Pelo menos $MIN_PASSWORD_LENGTH caracteres",
                satisfied = password.length >= MIN_PASSWORD_LENGTH
            )

            if (confirmPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))

                PasswordRule(
                    text = "As senhas coincidem",
                    satisfied = password == confirmPassword
                )
            }
        }

        (uiState as? RegisterUiState.Error)?.let { error ->
            Spacer(modifier = Modifier.height(14.dp))

            AuthErrorBanner(message = error.message)
        }

        Spacer(modifier = Modifier.height(22.dp))

        AuthPrimaryButton(
            text = "Criar conta",
            isLoading = isLoading,
            onClick = submit
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Já tem uma conta? ",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft
            )

            Box(
                modifier = Modifier
                    .clickable(
                        enabled = !isLoading,
                        role = Role.Button,
                        onClick = onLoginClick
                    )
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Entrar",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmEmber
                )
            }
        }
    }
}

private fun passwordTransformation(visible: Boolean): VisualTransformation =
    if (visible) VisualTransformation.None else PasswordVisualTransformation()

@Composable
private fun PasswordVisibilityToggle(
    visible: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    IconButton(
        onClick = onToggle,
        enabled = enabled
    ) {
        Icon(
            imageVector = if (visible) {
                Icons.Outlined.VisibilityOff
            } else {
                Icons.Outlined.Visibility
            },
            contentDescription = if (visible) "Ocultar senha" else "Mostrar senha",
            tint = WarmMuted
        )
    }
}

@Composable
private fun PasswordRule(
    text: String,
    satisfied: Boolean
) {
    Row(
        modifier = Modifier.padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (satisfied) {
                Icons.Filled.CheckCircle
            } else {
                Icons.Outlined.RadioButtonUnchecked
            },
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (satisfied) WarmSuccess else WarmMuted
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (satisfied) WarmSuccess else WarmMuted
        )
    }
}
