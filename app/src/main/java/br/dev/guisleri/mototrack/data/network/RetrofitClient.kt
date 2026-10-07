package br.dev.guisleri.mototrack.data.network

import br.dev.guisleri.mototrack.data.local.TokenStorage
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val publicRetrofit: Retrofit by lazy {
        createRetrofit()
    }

    val authApiService: AuthApiService by lazy {
        publicRetrofit.create(AuthApiService::class.java)
    }

    fun createAuthenticatedRetrofit(
        tokenStorage: TokenStorage
    ): Retrofit {

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor(tokenStorage)
            )
            .authenticator(
                TokenAuthenticator(
                    authApiService = authApiService,
                    tokenStorage = tokenStorage
                )
            )
            .build()

        return createRetrofit(
            okHttpClient = okHttpClient
        )
    }

    private fun createRetrofit(
        okHttpClient: OkHttpClient? = null
    ): Retrofit {

        val builder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )

        if (okHttpClient != null) {
            builder.client(okHttpClient)
        }

        return builder.build()
    }
}