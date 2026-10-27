package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.auth.ChangeEmailRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.ConfirmEmailChangeRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.ForgotPasswordRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.ResetPasswordRequestDTO
import br.dev.guisleri.mototrack.data.network.AccountRecoveryApiService
import br.dev.guisleri.mototrack.data.remote.user.UserApiService

/**
 * What the rider does about the credentials of the account: recovering a forgotten
 * password (signed out) and changing the e-mail (signed in), both with a code sent by
 * e-mail.
 */
class AccountRepository(
    private val recoveryApi: AccountRecoveryApiService,
    private val userApi: UserApiService,
    // Where the session goes when the e-mail changes: it is the subject of the tokens.
    private val saveTokens: suspend (accessToken: String, refreshToken: String) -> Unit
) {

    suspend fun forgotPassword(email: String) {
        recoveryApi.forgotPassword(ForgotPasswordRequestDTO(email = email))
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String) {
        recoveryApi.resetPassword(
            ResetPasswordRequestDTO(email = email, code = code, newPassword = newPassword)
        )
    }

    suspend fun requestEmailChange(newEmail: String, currentPassword: String) {
        userApi.requestEmailChange(
            ChangeEmailRequestDTO(newEmail = newEmail, currentPassword = currentPassword)
        )
    }

    /**
     * Confirms the new e-mail. The server closes every session and opens a new one, so
     * the tokens it returns replace the ones on the device and the app stays signed in.
     */
    suspend fun confirmEmailChange(code: String) {
        val tokens = userApi.confirmEmailChange(ConfirmEmailChangeRequestDTO(code = code))

        saveTokens(tokens.accessToken, tokens.refreshToken)
    }
}
