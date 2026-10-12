package br.dev.guisleri.mototrack.ui.screen.auth.login

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationSuccessSnackbarTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun shownMessageIsConsumedWhenLoginLeavesAndDoesNotReappear() {
        val successMessage = mutableStateOf<String?>(MESSAGE)
        val showLogin = mutableStateOf(true)
        var consumedCount = 0

        compose.setContent {
            MotoTrackTheme {
                if (showLogin.value) {
                    LoginScreen(
                        uiState = LoginUiState.Idle,
                        successMessage = successMessage.value,
                        onSuccessMessageConsumed = {
                            consumedCount++
                            successMessage.value = null
                        },
                        onLoginClick = { _, _ -> },
                        onCreateAccountClick = {}
                    )
                } else {
                    Text("Main")
                }
            }
        }

        compose.onNodeWithText(MESSAGE).assertIsDisplayed()

        compose.runOnIdle {
            showLogin.value = false
        }
        compose.onNodeWithText("Main").assertIsDisplayed()
        compose.waitUntil(timeoutMillis = 5_000) {
            consumedCount == 1
        }

        compose.runOnIdle {
            assertNull(successMessage.value)
            assertEquals(1, consumedCount)
            showLogin.value = true
        }

        compose.onNodeWithText(MESSAGE).assertDoesNotExist()
    }

    private companion object {
        const val MESSAGE = "Conta criada com sucesso! Entre para continuar."
    }
}
