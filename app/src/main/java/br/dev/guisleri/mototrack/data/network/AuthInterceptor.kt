package br.dev.guisleri.mototrack.data.network

import br.dev.guisleri.mototrack.data.local.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

internal data class AuthSessionTag(
    val sessionId: String
)

class AuthInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val session = runBlocking {
            tokenStorage.getSessionSnapshot()
        }

        val requestBuilder = chain.request().newBuilder()
        if (session != null) {
            requestBuilder
                .header("Authorization", "Bearer ${session.accessToken}")
                .tag(AuthSessionTag::class.java, AuthSessionTag(session.sessionId))
        }

        return chain.proceed(requestBuilder.build())
    }
}
