package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.motorcycle.CreateMotorcycleRequestDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.UpdateMotorcycleRequestDTO
import br.dev.guisleri.mototrack.data.remote.motorcycle.MotorcycleApiService

class MotorcycleRepository(
    private val motorcycleApiService: MotorcycleApiService
) {

    suspend fun getMotorcycles(): List<MotorcycleResponseDTO> {
        return motorcycleApiService.getMotorcycles()
    }

    suspend fun createMotorcycle(
        request: CreateMotorcycleRequestDTO
    ): MotorcycleResponseDTO {
        return motorcycleApiService.createMotorcycle(request)
    }

    suspend fun updateMotorcycle(
        motorcycleId: Long,
        request: UpdateMotorcycleRequestDTO
    ): MotorcycleResponseDTO {
        return motorcycleApiService.updateMotorcycle(
            motorcycleId = motorcycleId,
            request = request
        )
    }

    suspend fun deleteMotorcycle(
        motorcycleId: Long
    ) {
        motorcycleApiService.deleteMotorcycle(motorcycleId)
    }

    suspend fun getMotorcycleStatistics(
        motorcycleId: Long
    ): MotorcycleStatisticsResponseDTO {
        return motorcycleApiService.getMotorcycleStatistics(
            motorcycleId = motorcycleId
        )
    }

}