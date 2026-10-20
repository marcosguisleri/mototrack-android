package br.dev.guisleri.mototrack.ui.screen.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false
) {
    Column(
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = "Perfil"
        )

        Button(
            onClick = onLogoutClick,
            enabled = !isLoggingOut,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator()
            } else {
                Text("Sair")
            }
        }
    }
}