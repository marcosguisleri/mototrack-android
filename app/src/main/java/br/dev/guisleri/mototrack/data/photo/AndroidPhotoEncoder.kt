package br.dev.guisleri.mototrack.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt

private const val LONGEST_SIDE_PX = 1600
private const val JPEG_QUALITY = 88

/**
 * Saves a picked image as a JPEG no bigger than 1600 px on its longest side.
 * Re-encoding also drops the metadata, such as where the photo was taken.
 */
class AndroidPhotoEncoder(context: Context) : PhotoEncoder {

    private val resolver = context.applicationContext.contentResolver

    override suspend fun encode(source: String, target: File) {
        withContext(Dispatchers.IO) {
            val bitmap = decode(Uri.parse(source))

            try {
                target.outputStream().use { output ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                        throw IOException("Could not write the photo")
                    }
                }
            } finally {
                bitmap.recycle()
            }
        }
    }

    private fun decode(uri: Uri): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Honors the photo's orientation by itself.
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE

                val longest = max(info.size.width, info.size.height)

                if (longest > LONGEST_SIDE_PX) {
                    val scale = LONGEST_SIDE_PX.toFloat() / longest
                    decoder.setTargetSize(
                        (info.size.width * scale).roundToInt(),
                        (info.size.height * scale).roundToInt()
                    )
                }
            }
        } else {
            decodeLegacy(uri)
        }

    // Android 8.x: no orientation handling, a rotated photo is rare enough here.
    private fun decodeLegacy(uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }

        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= LONGEST_SIDE_PX) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sample }

        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IOException("Could not read the photo")
    }
}
