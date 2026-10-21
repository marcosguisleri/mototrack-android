package br.dev.guisleri.mototrack.ui.screen.auth.login

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import br.dev.guisleri.mototrack.ui.components.WarmTextField
import br.dev.guisleri.mototrack.ui.screen.auth.AuthErrorBanner
import br.dev.guisleri.mototrack.ui.screen.auth.AuthPrimaryButton
import br.dev.guisleri.mototrack.ui.screen.auth.AuthScaffold
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    modifier: Modifier = Modifier,
    successMessage: String? = null,
    onSuccessMessageConsumed: () -> Unit = {},
    onLoginClick: (
        email: String,
        password: String
    ) -> Unit,
    onCreateAccountClick: () -> Unit
) {
    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val currentOnLoginClick by rememberUpdatedState(
        onLoginClick
    )

    val currentOnSuccessMessageConsumed by rememberUpdatedState(
        onSuccessMessageConsumed
    )

    var permissionError by remember {
        mutableStateOf<String?>(null)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val isLoading = uiState is LoginUiState.Loading

    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            try {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            } finally {
                currentOnSuccessMessageConsumed()
            }
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                permissionError = null

                currentOnLoginClick(
                    email,
                    password
                )
            } else {
                permissionError =
                    "Permita o acesso à rede local para conectar à API. " +
                            "Você pode habilitar em Configurações > Apps > " +
                            "MotoTrack > Permissões."
            }
        }

    val submit: () -> Unit = {
        permissionError = null

        if (
            email.isBlank() ||
            password.isBlank()
        ) {
            onLoginClick(
                email,
                password
            )
        } else if (
            Build.VERSION.SDK_INT >= 37 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(
                Manifest.permission.ACCESS_LOCAL_NETWORK
            )
        } else {
            onLoginClick(
                email,
                password
            )
        }
    }

    AuthScaffold(
        headline = "Sua próxima estrada começa aqui.",
        modifier = modifier,
        overlay = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    ) {
        Text(
            text = "Bem-vindo de volta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Entre para continuar sua jornada.",
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
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { submit() }
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

        val errorMessage =
            permissionError
                ?: (uiState as? LoginUiState.Error)?.message

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(14.dp))

            AuthErrorBanner(message = message)
        }

        Spacer(modifier = Modifier.height(22.dp))

        AuthPrimaryButton(
            text = "Entrar",
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
                text = "Ainda não tem uma conta? ",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft
            )

            Box(
                modifier = Modifier
                    .clickable(
                        enabled = !isLoading,
                        role = Role.Button,
                        onClick = onCreateAccountClick
                    )
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Criar conta",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmEmber
                )
            }
        }
    }
}
