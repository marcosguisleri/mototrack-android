package br.dev.guisleri.mototrack.data.network

import br.dev.guisleri.mototrack.data.model.auth.ForgotPasswordRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.ResetPasswordRequestDTO
import retrofit2.http.Body
import retrofit2.http.POST

/** The password recovery, for someone who is not signed in: both calls are public. */
interface AccountRecoveryApiService {

    /** Always answers 202, whether or not the account exists. */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequestDTO
    )

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequestDTO
    )
}
