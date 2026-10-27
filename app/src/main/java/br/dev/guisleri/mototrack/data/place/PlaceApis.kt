package br.dev.guisleri.mototrack.data.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ---------------------------------------------------------------------------
// IBGE: the list of municipalities and the centre of each one
// ---------------------------------------------------------------------------

interface IbgeApiService {

    @GET("api/v1/localidades/municipios?orderBy=nome")
    suspend fun getMunicipalities(): List<IbgeMunicipalityDTO>

    @GET("api/v3/malhas/municipios/{id}/metadados")
    suspend fun getMunicipalityMetadata(
        @Path("id") id: Long
    ): List<IbgeMetadataDTO>
}

@Serializable
data class IbgeMunicipalityDTO(
    val id: Long,
    val nome: String,
    val microrregiao: IbgeMicroregionDTO? = null,
    @SerialName("regiao-imediata") val immediateRegion: IbgeImmediateRegionDTO? = null
) {
    // A few recent municipalities have no microregion, hence the second path.
    val uf: String?
        get() = microrregiao?.mesorregiao?.uf?.sigla
            ?: immediateRegion?.intermediateRegion?.uf?.sigla
}

@Serializable
data class IbgeMicroregionDTO(val mesorregiao: IbgeMesoregionDTO)

@Serializable
data class IbgeMesoregionDTO(@SerialName("UF") val uf: IbgeUfDTO)

@Serializable
data class IbgeImmediateRegionDTO(
    @SerialName("regiao-intermediaria") val intermediateRegion: IbgeIntermediateRegionDTO
)

@Serializable
data class IbgeIntermediateRegionDTO(@SerialName("UF") val uf: IbgeUfDTO)

@Serializable
data class IbgeUfDTO(val sigla: String)

@Serializable
data class IbgeMetadataDTO(val centroide: IbgeCentroidDTO)

@Serializable
data class IbgeCentroidDTO(val latitude: Double, val longitude: Double)

// ---------------------------------------------------------------------------
// Open-Meteo geocoding: where a city is
// ---------------------------------------------------------------------------

interface GeocodingApiService {

    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 20,
        @Query("language") language: String = "pt",
        @Query("countryCode") countryCode: String = "BR"
    ): GeocodingResponseDTO
}

@Serializable
data class GeocodingResponseDTO(
    val results: List<GeocodingResultDTO>? = null
)

@Serializable
data class GeocodingResultDTO(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val admin1: String? = null,
    val population: Long? = null
)

// ---------------------------------------------------------------------------
// OSRM: the length of the drive between two points
// ---------------------------------------------------------------------------

interface OsrmApiService {

    /** [coordinates] is "lon,lat;lon,lat". */
    @GET("route/v1/driving/{coordinates}?overview=false")
    suspend fun route(
        @Path(value = "coordinates", encoded = true) coordinates: String
    ): OsrmResponseDTO
}

@Serializable
data class OsrmResponseDTO(
    val code: String,
    val routes: List<OsrmRouteDTO> = emptyList()
)

@Serializable
data class OsrmRouteDTO(
    // Metres.
    val distance: Double
)

/** The public services behind the city suggestions and the automatic distance. */
object PublicApis {

    private val json = Json { ignoreUnknownKeys = true }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "MotoTrack-Android/1.0")
                        .build()
                )
            }
            .build()
    }

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    val ibge: IbgeApiService by lazy {
        retrofit("https://servicodados.ibge.gov.br/").create(IbgeApiService::class.java)
    }

    val geocoding: GeocodingApiService by lazy {
        retrofit("https://geocoding-api.open-meteo.com/").create(GeocodingApiService::class.java)
    }

    val osrm: OsrmApiService by lazy {
        retrofit("https://router.project-osrm.org/").create(OsrmApiService::class.java)
    }
}
