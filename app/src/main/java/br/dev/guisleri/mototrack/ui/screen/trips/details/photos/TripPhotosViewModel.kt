package br.dev.guisleri.mototrack.ui.screen.trips.details.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.dev.guisleri.mototrack.data.photo.AddPhotosResult
import br.dev.guisleri.mototrack.data.photo.MAX_TRIP_PHOTOS
import br.dev.guisleri.mototrack.data.photo.TripPhoto
import br.dev.guisleri.mototrack.data.photo.TripPhotoStorage
import br.dev.guisleri.mototrack.ui.feedback.Feedback
import br.dev.guisleri.mototrack.ui.feedback.FeedbackKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class TripPhotosUiState(
    val photos: List<TripPhoto> = emptyList(),
    val isLoading: Boolean = true,
    val isAdding: Boolean = false
) {
    /** How many more photos the trip has room for. */
    val remaining: Int get() = (MAX_TRIP_PHOTOS - photos.size).coerceAtLeast(0)
}

class TripPhotosViewModel(
    private val tripId: Long,
    private val storage: TripPhotoStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripPhotosUiState())

    val uiState: StateFlow<TripPhotosUiState> = _uiState.asStateFlow()

    private val _feedback = Channel<Feedback>(Channel.BUFFERED)

    val feedback: Flow<Feedback> = _feedback.receiveAsFlow()

    init {
        viewModelScope.launch { refresh() }
    }

    /** [sources] are the uris of the picked images, as text. */
    fun addPhotos(sources: List<String>) {
        if (sources.isEmpty() || _uiState.value.isAdding) return

        _uiState.value = _uiState.value.copy(isAdding = true)

        viewModelScope.launch {
            try {
                val result = storage.add(tripId, sources)

                _feedback.trySend(addFeedback(result))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _feedback.trySend(Feedback("Não foi possível adicionar as fotos.", FeedbackKind.ERROR))
            } finally {
                refresh()
                _uiState.value = _uiState.value.copy(isAdding = false)
            }
        }
    }

    fun removePhoto(photoId: String) {
        viewModelScope.launch {
            try {
                storage.remove(tripId, photoId)
                _feedback.trySend(Feedback("Foto excluída"))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _feedback.trySend(Feedback("Não foi possível excluir a foto.", FeedbackKind.ERROR))
            }

            refresh()
        }
    }

    fun makeCover(photoId: String) {
        viewModelScope.launch {
            try {
                storage.makeCover(tripId, photoId)
                _feedback.trySend(Feedback("Foto definida como capa"))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _feedback.trySend(Feedback("Não foi possível definir a capa.", FeedbackKind.ERROR))
            }

            refresh()
        }
    }

    private suspend fun refresh() {
        val photos = try {
            storage.photos(tripId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            _uiState.value.photos
        }

        _uiState.value = _uiState.value.copy(photos = photos, isLoading = false)
    }
}

internal fun addFeedback(result: AddPhotosResult): Feedback {
    val parts = buildList {
        when {
            result.added == 1 -> add("Foto adicionada")
            result.added > 1 -> add("${result.added} fotos adicionadas")
        }

        if (result.skippedForLimit > 0) {
            add("o limite é de $MAX_TRIP_PHOTOS fotos por viagem")
        }

        if (result.failed > 0) {
            add(
                if (result.failed == 1) "1 foto não pôde ser lida"
                else "${result.failed} fotos não puderam ser lidas"
            )
        }
    }

    val kind = when {
        result.failed > 0 && result.added == 0 -> FeedbackKind.ERROR
        result.skippedForLimit > 0 || result.failed > 0 -> FeedbackKind.INFO
        else -> FeedbackKind.SUCCESS
    }

    val message = parts.joinToString(". ") { it.replaceFirstChar { first -> first.uppercase() } }

    return Feedback(message.ifEmpty { "Nenhuma foto adicionada" }, kind)
}

class TripPhotosViewModelFactory(
    private val tripId: Long,
    private val storage: TripPhotoStorage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripPhotosViewModel::class.java)) {
            return TripPhotosViewModel(tripId, storage) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
