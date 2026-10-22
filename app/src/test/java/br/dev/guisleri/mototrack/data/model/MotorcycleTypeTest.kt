package br.dev.guisleri.mototrack.data.model

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleType
import br.dev.guisleri.mototrack.data.model.motorcycle.motorcycleType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MotorcycleTypeTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun everyApiValueMapsBackToItsType() {
        MotorcycleType.entries.forEach { type ->
            assertEquals(type, MotorcycleType.fromApi(type.apiValue))
        }
    }

    @Test
    fun unknownOrMissingValueIsNull() {
        assertNull(MotorcycleType.fromApi(null))
        assertNull(MotorcycleType.fromApi("SPACESHIP"))
    }

    @Test
    fun responseWithTypeIsParsed() {
        val motorcycle = json.decodeFromString<MotorcycleResponseDTO>(
            """{"id":1,"brand":"Honda","model":"CB 500X","color":"Red","year":2024,
               "engineCapacity":471,"type":"BIG_TRAIL",
               "owner":{"id":1,"name":"M","email":"m@x.com"}}"""
        )

        assertEquals(MotorcycleType.BIG_TRAIL, motorcycle.motorcycleType)
    }

    @Test
    fun responseWithoutTypeIsStillParsed() {
        val motorcycle = json.decodeFromString<MotorcycleResponseDTO>(
            """{"id":1,"brand":"Honda","model":"CG 160","color":"Red","year":2020,
               "engineCapacity":160,
               "owner":{"id":1,"name":"M","email":"m@x.com"}}"""
        )

        assertNull(motorcycle.motorcycleType)
    }

    @Test
    fun responseWithNullTypeIsStillParsed() {
        val motorcycle = json.decodeFromString<MotorcycleResponseDTO>(
            """{"id":1,"brand":"Honda","model":"CG 160","color":"Red","year":2020,
               "engineCapacity":160,"type":null,
               "owner":{"id":1,"name":"M","email":"m@x.com"}}"""
        )

        assertNull(motorcycle.motorcycleType)
    }
}
