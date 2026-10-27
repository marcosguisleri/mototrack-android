package br.dev.guisleri.mototrack.ui.screen.trips.details.photos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import br.dev.guisleri.mototrack.ui.theme.WarmSkeleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val CACHE_BYTES = 24 * 1024 * 1024

/** Decodes the kept photos at the size they are shown, off the main thread, keeping the results. */
private object TripPhotoBitmaps {

    private val cache = object : LruCache<String, Bitmap>(CACHE_BYTES) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    private val ratios = java.util.concurrent.ConcurrentHashMap<String, Float>()

    /** Width over height, read from the file header without decoding the picture. */
    suspend fun aspectRatio(file: File): Float =
        withContext(Dispatchers.IO) {
            ratios.getOrPut(file.absolutePath) {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)

                if (bounds.outWidth > 0 && bounds.outHeight > 0) {
                    bounds.outWidth.toFloat() / bounds.outHeight
                } else {
                    1f
                }
            }
        }

    suspend fun load(file: File, targetPx: Int): ImageBitmap? =
        withContext(Dispatchers.IO) {
            val key = "${file.absolutePath}@$targetPx"

            cache.get(key)?.let { return@withContext it.asImageBitmap() }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)

            if (bounds.outWidth <= 0) return@withContext null

            var sample = 1
            while (minOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= targetPx) {
                sample *= 2
            }

            BitmapFactory.decodeFile(
                file.absolutePath,
                BitmapFactory.Options().apply { inSampleSize = sample }
            )?.also { cache.put(key, it) }?.asImageBitmap()
        }
}

/** A kept photo, filling the space it is given; a soft block shows until it is decoded. */
@Composable
internal fun TripPhotoImage(
    file: File,
    targetPx: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Color = WarmSkeleton
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, file, targetPx) {
        value = TripPhotoBitmaps.load(file, targetPx)
    }

    Box(modifier = modifier.background(placeholder)) {
        bitmap?.let { image ->
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = contentScale
            )
        }
    }
}

/** Width over height of a kept photo, or 1 until it is known. */
@Composable
internal fun rememberPhotoAspectRatio(file: File): Float {
    val ratio by produceState(initialValue = 1f, file) {
        value = TripPhotoBitmaps.aspectRatio(file)
    }

    return ratio
}
