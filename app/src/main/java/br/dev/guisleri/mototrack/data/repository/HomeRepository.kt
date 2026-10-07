package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.network.HomeApiService

class HomeRepository(
    private val homeApiService: HomeApiService
) {

    suspend fun getHome(): HomeResponseDTO {
        return homeApiService.getHome()
    }
}