package br.dev.guisleri.mototrack.data.remote.motorcycle

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import retrofit2.http.GET
import retrofit2.http.Path

interface MotorcycleApiService {

    @GET("motorcycles")
    suspend fun getMotorcycles(): List<MotorcycleResponseDTO>

    @GET("motorcycles/{id}/statistics")
    suspend fun getMotorcycleStatistics(
        @Path("id") motorcycleId: Long
    ): MotorcycleStatisticsResponseDTO

}