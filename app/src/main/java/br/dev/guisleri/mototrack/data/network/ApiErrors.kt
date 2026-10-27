package br.dev.guisleri.mototrack.data.network

import br.dev.guisleri.mototrack.data.model.common.ApiErrorResponseDTO
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.HttpException

private val apiErrorJson = Json {
    ignoreUnknownKeys = true
}

/** The message the backend put in the error body, when there is one. */
internal fun HttpException.apiErrorMessage(): String? {
    val body = response()
        ?.errorBody()
        ?.string()
        ?: return null

    return runCatching {
        apiErrorJson.decodeFromString<ApiErrorResponseDTO>(body).message
    }.getOrNull()
}
