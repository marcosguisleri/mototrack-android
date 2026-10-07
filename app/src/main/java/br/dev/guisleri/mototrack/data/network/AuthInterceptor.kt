package br.dev.guisleri.mototrack.data.network

import br.dev.guisleri.mototrack.data.local.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val accessToken = runBlocking {
            tokenStorage.getAccessToken()
        }

        val requestBuilder = chain
            .request()
            .newBuilder()

        if (!accessToken.isNullOrBlank()) {
            requestBuilder.addHeader(
                "Authorization",
                "Bearer $accessToken"
            )
        }

        return chain.proceed(
            requestBuilder.build()
        )
    }
}
