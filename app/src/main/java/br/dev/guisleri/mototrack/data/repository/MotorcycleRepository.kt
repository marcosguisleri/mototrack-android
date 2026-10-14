package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import br.dev.guisleri.mototrack.data.remote.motorcycle.MotorcycleApiService

class MotorcycleRepository(
    private val motorcycleApiService: MotorcycleApiService
) {

    suspend fun getMotorcycles(): List<MotorcycleResponseDTO> {
        return motorcycleApiService.getMotorcycles()
    }

    suspend fun getMotorcycleStatistics(
        motorcycleId: Long
    ): MotorcycleStatisticsResponseDTO {
        return motorcycleApiService.getMotorcycleStatistics(
            motorcycleId = motorcycleId
        )
    }

}