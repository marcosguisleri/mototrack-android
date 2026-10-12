package br.dev.guisleri.mototrack.data.network

import br.dev.guisleri.mototrack.data.model.auth.RegisterRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.Body
import retrofit2.http.POST

class AuthApiServiceRegisterContractTest {

    @Test
    fun registerUsesUsersPostEndpointWithRequestBody() {
        val method = AuthApiService::class.java.declaredMethods
            .single { it.name == "register" }
        val post = requireNotNull(method.getAnnotation(POST::class.java))

        assertEquals("users", post.value)
        assertTrue(method.parameterAnnotations.first().any { it is Body })
    }

    @Test
    fun registerRequestSerializesOnlyBackendFields() {
        val request = RegisterRequestDTO(
            name = "Rider",
            email = "rider@example.com",
            password = "password"
        )

        val encoded = Json.encodeToJsonElement(
            RegisterRequestDTO.serializer(),
            request
        ).jsonObject

        assertEquals(setOf("name", "email", "password"), encoded.keys)
    }

    @Test
    fun userResponseDeserializesCreatedUserContract() {
        val response = Json.decodeFromString(
            UserResponseDTO.serializer(),
            """{"id":1,"name":"Rider","email":"rider@example.com"}"""
        )

        assertEquals(UserResponseDTO(1, "Rider", "rider@example.com"), response)
    }
}
