package br.dev.guisleri.mototrack.data.remote.trip

import br.dev.guisleri.mototrack.data.model.trip.FuelEntryRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.FuelEntryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseResponseDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TripCostApiService {

    @GET("trips/{tripId}/cost-summary")
    suspend fun getCostSummary(
        @Path("tripId") tripId: Long
    ): TripCostSummaryResponseDTO

    @GET("trips/{tripId}/fuel-entries")
    suspend fun getFuelEntries(
        @Path("tripId") tripId: Long
    ): List<FuelEntryResponseDTO>

    @POST("trips/{tripId}/fuel-entries")
    suspend fun addFuelEntry(
        @Path("tripId") tripId: Long,
        @Body request: FuelEntryRequestDTO
    ): FuelEntryResponseDTO

    @PUT("trips/{tripId}/fuel-entries/{fuelEntryId}")
    suspend fun updateFuelEntry(
        @Path("tripId") tripId: Long,
        @Path("fuelEntryId") fuelEntryId: Long,
        @Body request: FuelEntryRequestDTO
    ): FuelEntryResponseDTO

    @DELETE("trips/{tripId}/fuel-entries/{fuelEntryId}")
    suspend fun deleteFuelEntry(
        @Path("tripId") tripId: Long,
        @Path("fuelEntryId") fuelEntryId: Long
    )

    @GET("trips/{tripId}/expenses")
    suspend fun getExpenses(
        @Path("tripId") tripId: Long
    ): List<TripExpenseResponseDTO>

    @POST("trips/{tripId}/expenses")
    suspend fun addExpense(
        @Path("tripId") tripId: Long,
        @Body request: TripExpenseRequestDTO
    ): TripExpenseResponseDTO

    @PUT("trips/{tripId}/expenses/{expenseId}")
    suspend fun updateExpense(
        @Path("tripId") tripId: Long,
        @Path("expenseId") expenseId: Long,
        @Body request: TripExpenseRequestDTO
    ): TripExpenseResponseDTO

    @DELETE("trips/{tripId}/expenses/{expenseId}")
    suspend fun deleteExpense(
        @Path("tripId") tripId: Long,
        @Path("expenseId") expenseId: Long
    )

}
