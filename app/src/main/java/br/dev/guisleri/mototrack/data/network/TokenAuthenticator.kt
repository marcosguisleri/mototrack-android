package br.dev.guisleri.mototrack.data.network

import br.dev.guisleri.mototrack.data.local.TokenStorage
import br.dev.guisleri.mototrack.data.model.auth.RefreshTokenRequestDTO
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import java.io.IOException

class TokenAuthenticator(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage
) : Authenticator {

    private companion object {
        val refreshMutex = Mutex()
    }

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {
        if (responseCount(response) >= 2) {
            return null
        }

        val newAccessToken = runBlocking {
            refreshMutex.withLock {
                val session = tokenStorage.getSessionSnapshot()
                    ?: return@withLock null
                val requestSessionId = response.request
                    .tag(AuthSessionTag::class.java)
                    ?.sessionId

                if (requestSessionId != null && requestSessionId != session.sessionId) {
                    return@withLock null
                }

                if (response.request.header("Authorization") != "Bearer ${session.accessToken}") {
                    return@withLock session.accessToken
                }

                try {
                    val tokens = authApiService.refresh(
                        RefreshTokenRequestDTO(
                            refreshToken = session.refreshToken
                        )
                    )
                    val saved = tokenStorage.saveTokensIfRefreshTokenMatches(
                        expectedRefreshToken = session.refreshToken,
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken
                    )
                    if (saved) tokens.accessToken else null
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: HttpException) {
                    if (exception.code() == 401) {
                        tokenStorage.clearTokensIfRefreshTokenMatches(session.refreshToken)
                        null
                    } else {
                        throw IOException("Não foi possível renovar a sessão.", exception)
                    }
                } catch (exception: IOException) {
                    throw exception
                } catch (exception: Exception) {
                    throw IOException("Não foi possível renovar a sessão.", exception)
                }
            }
        } ?: return null

        return response.request
            .newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var currentResponse: Response? = response
        var count = 1
        while (currentResponse?.priorResponse != null) {
            count++
            currentResponse = currentResponse.priorResponse
        }
        return count
    }
}
