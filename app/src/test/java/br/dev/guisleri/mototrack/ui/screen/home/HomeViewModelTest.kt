@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.home

import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.network.HomeApiService
import br.dev.guisleri.mototrack.data.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class HomeViewModelTest {

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshFailureKeepsLoadedHomeAndExposesError() = runTest(dispatcher) {
        val api = FakeHomeApi()
        val viewModel = HomeViewModel(HomeRepository(api))

        api.fail = true
        viewModel.loadHome()

        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(homeResponse(), state.home)
        assertFalse(state.isRefreshing)
        assertEquals(
            "Não foi possível comunicar com o servidor. Tente novamente.",
            state.refreshErrorMessage
        )
    }

    private class FakeHomeApi : HomeApiService {
        var fail = false

        override suspend fun getHome(): HomeResponseDTO {
            if (fail) throw IOException("offline")
            return homeResponse()
        }
    }

    private companion object {
        fun homeResponse() = HomeResponseDTO(
            inProgressTrip = null,
            nextTrip = null,
            lastCompletedTrip = null,
            totalCompletedDistanceKm = 100.0,
            distanceThisYearKm = 50.0,
            completedTrips = 2,
            motorcycleCount = 1
        )
    }
}
