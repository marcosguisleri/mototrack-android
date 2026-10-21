@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.motorcycles.create

import br.dev.guisleri.mototrack.data.model.motorcycle.CreateMotorcycleRequestDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleOwnerDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleType
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import br.dev.guisleri.mototrack.data.remote.motorcycle.MotorcycleApiService
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class MotorcycleCreateViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emptyFormShowsEveryErrorAndDoesNotCallTheApi() = runTest(dispatcher) {
        val api = FakeApi()
        val viewModel = viewModel(api)

        viewModel.submit()

        val state = viewModel.uiState.value
        assertNotNull(state.typeError)
        assertNotNull(state.brandError)
        assertNotNull(state.modelError)
        assertNotNull(state.yearError)
        assertNotNull(state.engineCapacityError)
        assertNotNull(state.colorError)
        assertTrue(api.requests.isEmpty())
    }

    @Test
    fun missingTypeBlocksTheSubmission() = runTest(dispatcher) {
        val api = FakeApi()
        val untyped = viewModel(api).also {
            it.onBrandChange("Honda")
            it.onModelChange("CB 500F")
            it.onYearChange("2022")
            it.onEngineCapacityChange("500")
            it.onColorSelected("Vermelha")
        }

        untyped.submit()

        assertEquals("Escolha o tipo da moto.", untyped.uiState.value.typeError)
        assertTrue(api.requests.isEmpty())
    }

    @Test
    fun choosingATypeClearsItsError() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.submit()
        assertNotNull(viewModel.uiState.value.typeError)

        viewModel.onTypeSelected(MotorcycleType.TRAIL)

        assertNull(viewModel.uiState.value.typeError)
        assertEquals(MotorcycleType.TRAIL, viewModel.uiState.value.type)
    }

    @Test
    fun yearAndEngineKeepOnlyDigits() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onYearChange("20a2b4x9")
        viewModel.onEngineCapacityChange("5,00cc99999")

        assertEquals("2024", viewModel.uiState.value.year)
        assertEquals("5009", viewModel.uiState.value.engineCapacity)
    }

    @Test
    fun yearBefore1900IsRejected() = runTest(dispatcher) {
        val viewModel = filledViewModel()
        viewModel.onYearChange("1850")

        viewModel.submit()

        assertNotNull(viewModel.uiState.value.yearError)
    }

    @Test
    fun validFormSendsTrimmedValues() = runTest(dispatcher) {
        val api = FakeApi()
        val viewModel = filledViewModel(api)
        viewModel.onBrandChange("  Honda ")

        viewModel.submit()

        assertEquals(
            CreateMotorcycleRequestDTO(
                brand = "Honda",
                model = "CB 500F",
                color = "Vermelha",
                year = 2022,
                engineCapacity = 500,
                type = "BIG_TRAIL"
            ),
            api.requests.single()
        )
        assertTrue(viewModel.uiState.value.isCreated)
    }

    @Test
    fun customColorStartsEmptyAndIsSentAsTyped() = runTest(dispatcher) {
        val api = FakeApi()
        val viewModel = filledViewModel(api)

        viewModel.onCustomColorSelected()
        assertEquals("", viewModel.uiState.value.color)

        viewModel.submit()
        assertNotNull(viewModel.uiState.value.colorError)

        viewModel.onCustomColorChange("Vinho metalizado")
        viewModel.submit()

        assertEquals("Vinho metalizado", api.requests.single().color)
    }

    @Test
    fun pickingAnotherColorLeavesCustomMode() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onCustomColorSelected()
        viewModel.onColorSelected("Azul")

        assertFalse(viewModel.uiState.value.isCustomColor)
        assertEquals("Azul", viewModel.uiState.value.color)
    }

    @Test
    fun networkFailureKeepsTheFormAndShowsMessage() = runTest(dispatcher) {
        val api = FakeApi().apply { failure = IOException("offline") }
        val viewModel = filledViewModel(api)

        viewModel.submit()

        val state = viewModel.uiState.value
        assertFalse(state.isCreated)
        assertFalse(state.isSubmitting)
        assertEquals("CB 500F", state.model)
        assertEquals("Não foi possível comunicar com o servidor.", state.submitErrorMessage)
    }

    @Test
    fun editingAFieldClearsItsError() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.submit()
        assertNotNull(viewModel.uiState.value.brandError)

        viewModel.onBrandChange("Honda")

        assertNull(viewModel.uiState.value.brandError)
    }

    private fun viewModel(api: FakeApi = FakeApi()) =
        MotorcycleCreateViewModel(MotorcycleRepository(api))

    private fun filledViewModel(api: FakeApi = FakeApi()): MotorcycleCreateViewModel =
        viewModel(api).also {
            it.onTypeSelected(MotorcycleType.BIG_TRAIL)
            it.onBrandChange("Honda")
            it.onModelChange("CB 500F")
            it.onYearChange("2022")
            it.onEngineCapacityChange("500")
            it.onColorSelected("Vermelha")
        }

    private class FakeApi : MotorcycleApiService {
        val requests = mutableListOf<CreateMotorcycleRequestDTO>()
        var failure: Exception? = null

        override suspend fun getMotorcycles(): List<MotorcycleResponseDTO> =
            throw AssertionError("Unexpected list request")

        override suspend fun createMotorcycle(
            request: CreateMotorcycleRequestDTO
        ): MotorcycleResponseDTO {
            failure?.let { throw it }
            requests += request

            return MotorcycleResponseDTO(
                id = 1,
                brand = request.brand,
                model = request.model,
                color = request.color,
                year = request.year,
                engineCapacity = request.engineCapacity,
                type = request.type,
                owner = MotorcycleOwnerDTO(1, "Rider", "rider@example.com")
            )
        }

        override suspend fun getMotorcycleStatistics(
            motorcycleId: Long
        ): MotorcycleStatisticsResponseDTO =
            throw AssertionError("Unexpected statistics request")
    }
}
