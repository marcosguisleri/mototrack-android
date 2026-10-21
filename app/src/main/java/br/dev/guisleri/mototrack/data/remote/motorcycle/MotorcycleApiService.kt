package br.dev.guisleri.mototrack.data.remote.motorcycle

import br.dev.guisleri.mototrack.data.model.motorcycle.CreateMotorcycleRequestDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MotorcycleApiService {

    @GET("motorcycles")
    suspend fun getMotorcycles(): List<MotorcycleResponseDTO>

    @POST("motorcycles")
    suspend fun createMotorcycle(
        @Body request: CreateMotorcycleRequestDTO
    ): MotorcycleResponseDTO

    @DELETE("motorcycles/{id}")
    suspend fun deleteMotorcycle(
        @Path("id") motorcycleId: Long
    )

    @GET("motorcycles/{id}/statistics")
    suspend fun getMotorcycleStatistics(
        @Path("id") motorcycleId: Long
    ): MotorcycleStatisticsResponseDTO

}