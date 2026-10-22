package br.dev.guisleri.mototrack.ui.screen.trips.details.photos

import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import br.dev.guisleri.mototrack.data.photo.MAX_TRIP_PHOTOS

/**
 * Opens the system photo picker for as many photos as [remaining] allows and hands
 * the chosen ones to [onPicked] as uris in text form. No storage permission is needed.
 */
@Composable
internal fun rememberPhotoPicker(
    remaining: Int,
    onPicked: (List<String>) -> Unit
): () -> Unit {
    val pickOne = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPicked(listOf(uri.toString()))
    }

    // The multiple picker needs at least two slots, and the system caps how many it allows.
    val pickMany = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = remaining.coerceIn(2, pickerLimit())
        )
    ) { uris ->
        onPicked(uris.take(remaining).map { it.toString() })
    }

    return {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

        if (remaining == 1) pickOne.launch(request) else pickMany.launch(request)
    }
}

private fun pickerLimit(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        MediaStore.getPickImagesMaxLimit()
    } else {
        MAX_TRIP_PHOTOS
    }
