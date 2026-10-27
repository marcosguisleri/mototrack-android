package br.dev.guisleri.mototrack.data.place

import java.io.IOException

internal val ARARAS = City(3503307, "Araras", "SP")
internal val LEME = City(3526704, "Leme", "SP")

internal val SAMPLE_CITIES = listOf(
    ARARAS,
    LEME,
    City(1, "Araras", "PA"),
    City(2, "Arapongas", "PR"),
    City(3, "São José do Rio Preto", "SP"),
    City(4, "São José dos Pinhais", "PR"),
    City(5, "Rio Claro", "SP"),
    City(6, "Santa Rita do Passa Quatro", "SP")
)

internal class FakeIbgeApi(
    private val cities: List<City> = SAMPLE_CITIES,
    private val centres: Map<Long, IbgeCentroidDTO> = emptyMap()
) : IbgeApiService {

    var downloads = 0
    var failure: Exception? = null

    override suspend fun getMunicipalities(): List<IbgeMunicipalityDTO> {
        downloads++
        failure?.let { throw it }

        return cities.map { city ->
            IbgeMunicipalityDTO(
                id = city.id,
                nome = city.name,
                microrregiao = IbgeMicroregionDTO(IbgeMesoregionDTO(IbgeUfDTO(city.uf)))
            )
        }
    }

    override suspend fun getMunicipalityMetadata(id: Long): List<IbgeMetadataDTO> =
        centres[id]?.let { listOf(IbgeMetadataDTO(it)) } ?: throw IOException("no centre")
}

internal class FakeGeocodingApi(
    private val results: Map<String, List<GeocodingResultDTO>> = defaultResults()
) : GeocodingApiService {

    var failure: Exception? = null
    var searches = 0

    override suspend fun search(
        name: String,
        count: Int,
        language: String,
        countryCode: String
    ): GeocodingResponseDTO {
        searches++
        failure?.let { throw it }

        return GeocodingResponseDTO(results[name])
    }

    companion object {
        fun defaultResults() = mapOf(
            "Araras" to listOf(
                GeocodingResultDTO("Araras", -3.78333, -51.78333, "Pará", 100),
                GeocodingResultDTO("Araras", -22.35694, -47.38417, "São Paulo", 130_000),
                GeocodingResultDTO("Araras", -7.33333, -38.53333, "Paraíba", 50)
            ),
            "Leme" to listOf(
                GeocodingResultDTO("Leme", -22.18111, -47.38972, "São Paulo", 100_000)
            )
        )
    }
}

internal class FakeOsrmApi(
    private val kilometres: Double = 21.9
) : OsrmApiService {

    var failure: Exception? = null
    val requests = mutableListOf<String>()

    override suspend fun route(coordinates: String): OsrmResponseDTO {
        requests += coordinates
        failure?.let { throw it }

        return OsrmResponseDTO("Ok", listOf(OsrmRouteDTO(kilometres * 1_000)))
    }
}
