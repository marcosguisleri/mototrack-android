@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.home

import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO
import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.network.HomeApiService
import br.dev.guisleri.mototrack.data.remote.user.UserApiService
import br.dev.guisleri.mototrack.data.repository.HomeRepository
import br.dev.guisleri.mototrack.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        val viewModel = viewModel(api, FakeUserApi())

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

    @Test
    fun homeCarriesTheNameOfTheSignedInUser() = runTest(dispatcher) {
        val viewModel = viewModel(FakeHomeApi(), FakeUserApi())

        assertEquals("Marcos Guisleri", viewModel.success().userName)
    }

    @Test
    fun nameIsFetchedOnlyOnceAcrossRefreshes() = runTest(dispatcher) {
        val userApi = FakeUserApi()
        val viewModel = viewModel(FakeHomeApi(), userApi)

        viewModel.loadHome()
        viewModel.loadHome()

        assertEquals(1, userApi.calls)
        assertEquals("Marcos Guisleri", viewModel.success().userName)
    }

    @Test
    fun failingToLoadTheNameDoesNotBreakHome() = runTest(dispatcher) {
        val userApi = FakeUserApi().apply { failure = IOException("offline") }
        val viewModel = viewModel(FakeHomeApi(), userApi)

        assertNull(viewModel.success().userName)
    }

    @Test
    fun nameIsRetriedOnTheNextRefreshAfterAFailure() = runTest(dispatcher) {
        val userApi = FakeUserApi().apply { failure = IOException("offline") }
        val viewModel = viewModel(FakeHomeApi(), userApi)

        userApi.failure = null
        viewModel.loadHome()

        assertEquals("Marcos Guisleri", viewModel.success().userName)
        assertEquals(2, userApi.calls)
    }

    private fun viewModel(homeApi: FakeHomeApi, userApi: FakeUserApi) = HomeViewModel(
        homeRepository = HomeRepository(homeApi),
        userRepository = UserRepository(userApi)
    )

    private fun HomeViewModel.success() = uiState.value as HomeUiState.Success

    private class FakeHomeApi : HomeApiService {
        var fail = false

        override suspend fun getHome(): HomeResponseDTO {
            if (fail) throw IOException("offline")
            return homeResponse()
        }
    }

    private class FakeUserApi : UserApiService {
        var calls = 0
        var failure: Exception? = null

        override suspend fun getCurrentUser(): UserResponseDTO {
            calls++
            failure?.let { throw it }
            return UserResponseDTO(1, "Marcos Guisleri", "marcos@example.com")
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
