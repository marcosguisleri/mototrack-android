package br.dev.guisleri.mototrack.data.network

import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import retrofit2.http.GET

interface HomeApiService {

    @GET("home")
    suspend fun getHome(): HomeResponseDTO
}