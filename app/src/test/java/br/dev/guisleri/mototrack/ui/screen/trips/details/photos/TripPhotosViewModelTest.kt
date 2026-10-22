@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.trips.details.photos

import br.dev.guisleri.mototrack.data.photo.AddPhotosResult
import br.dev.guisleri.mototrack.data.photo.MAX_TRIP_PHOTOS
import br.dev.guisleri.mototrack.data.photo.TripPhoto
import br.dev.guisleri.mototrack.data.photo.TripPhotoStorage
import br.dev.guisleri.mototrack.ui.feedback.Feedback
import br.dev.guisleri.mototrack.ui.feedback.FeedbackKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
import java.io.File

class TripPhotosViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeStorage(initial: List<String> = emptyList()) : TripPhotoStorage {
        val ids = initial.toMutableList()
        var failed = 0
        var error: Exception? = null

        private fun photo(id: String) = TripPhoto(id, File(id))

        override suspend fun photos(tripId: Long) = ids.map(::photo)

        override suspend fun add(tripId: Long, sources: List<String>): AddPhotosResult {
            error?.let { throw it }
            val room = MAX_TRIP_PHOTOS - ids.size
            val taken = sources.take(room)
            ids += taken
            return AddPhotosResult(taken.size, sources.size - taken.size, failed)
        }

        override suspend fun remove(tripId: Long, photoId: String) {
            ids.remove(photoId)
        }

        override suspend fun makeCover(tripId: Long, photoId: String) {
            if (ids.remove(photoId)) ids.add(0, photoId)
        }

        override suspend fun clear(tripId: Long) = ids.clear()
    }

    @Test
    fun loadsTheTripsPhotosOnCreation() = runTest(dispatcher) {
        val viewModel = TripPhotosViewModel(1, FakeStorage(listOf("a", "b")))

        val state = viewModel.uiState.value
        assertEquals(listOf("a", "b"), state.photos.map { it.id })
        assertFalse(state.isLoading)
        assertEquals(MAX_TRIP_PHOTOS - 2, state.remaining)
    }

    @Test
    fun addingPhotosShowsThemAndConfirms() = runTest(dispatcher) {
        val viewModel = TripPhotosViewModel(1, FakeStorage())

        viewModel.addPhotos(listOf("a", "b", "c"))

        assertEquals(3, viewModel.uiState.value.photos.size)
        assertFalse(viewModel.uiState.value.isAdding)
        assertEquals(Feedback("3 fotos adicionadas"), viewModel.feedback.first())
    }

    @Test
    fun aSinglePhotoIsConfirmedInTheSingular() = runTest(dispatcher) {
        val viewModel = TripPhotosViewModel(1, FakeStorage())

        viewModel.addPhotos(listOf("a"))

        assertEquals(Feedback("Foto adicionada"), viewModel.feedback.first())
    }

    @Test
    fun beyondTheLimitTheRiderIsToldWhy() = runTest(dispatcher) {
        val viewModel = TripPhotosViewModel(1, FakeStorage((1..9).map { "p$it" }))

        viewModel.addPhotos(listOf("x", "y", "z"))

        assertEquals(MAX_TRIP_PHOTOS, viewModel.uiState.value.photos.size)
        assertEquals(
            Feedback("Foto adicionada. O limite é de 10 fotos por viagem", FeedbackKind.INFO),
            viewModel.feedback.first()
        )
    }

    @Test
    fun nothingReadableIsAnError() {
        val feedback = addFeedback(AddPhotosResult(added = 0, skippedForLimit = 0, failed = 2))

        assertEquals(FeedbackKind.ERROR, feedback.kind)
        assertEquals("2 fotos não puderam ser lidas", feedback.message)
    }

    @Test
    fun aStorageFailureIsReportedAndTheScreenKeepsWorking() = runTest(dispatcher) {
        val storage = FakeStorage(listOf("a")).apply { error = RuntimeException("disk full") }
        val viewModel = TripPhotosViewModel(1, storage)

        viewModel.addPhotos(listOf("b"))

        assertEquals(
            Feedback("Não foi possível adicionar as fotos.", FeedbackKind.ERROR),
            viewModel.feedback.first()
        )
        assertEquals(listOf("a"), viewModel.uiState.value.photos.map { it.id })
        assertFalse(viewModel.uiState.value.isAdding)
    }

    @Test
    fun emptyPickDoesNothing() = runTest(dispatcher) {
        val storage = FakeStorage()
        val viewModel = TripPhotosViewModel(1, storage)

        viewModel.addPhotos(emptyList())

        assertTrue(storage.ids.isEmpty())
        assertFalse(viewModel.uiState.value.isAdding)
    }

    @Test
    fun removingAPhotoUpdatesTheListAndConfirms() = runTest(dispatcher) {
        val viewModel = TripPhotosViewModel(1, FakeStorage(listOf("a", "b")))

        viewModel.removePhoto("a")

        assertEquals(listOf("b"), viewModel.uiState.value.photos.map { it.id })
        assertEquals(Feedback("Foto excluída"), viewModel.feedback.first())
    }

    @Test
    fun makingACoverPutsItFirst() = runTest(dispatcher) {
        val viewModel = TripPhotosViewModel(1, FakeStorage(listOf("a", "b", "c")))

        viewModel.makeCover("c")

        assertEquals(listOf("c", "a", "b"), viewModel.uiState.value.photos.map { it.id })
        assertEquals(Feedback("Foto definida como capa"), viewModel.feedback.first())
    }
}
