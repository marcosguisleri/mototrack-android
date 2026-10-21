package br.dev.guisleri.mototrack.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import br.dev.guisleri.mototrack.R
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Used when the colour typed for the bike is not one we know how to paint.
private val NeutralPaint = Color(0xFF8A8F98)

private const val DISPLAY_WIDTH_PX = 1100
private const val CACHE_BYTES = 20 * 1024 * 1024

/**
 * Picture of the bike for its type, repainted in the bike's own colour.
 * Bikes registered before the type existed keep the original picture as is.
 */
@Composable
internal fun MotorcycleHeroImage(
    type: MotorcycleType?,
    colorName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext
    val resource = motorcycleHeroResource(type)
    val paint = if (type == null) null else (motorcycleColor(colorName) ?: NeutralPaint).toArgb()

    val bitmap by produceState<ImageBitmap?>(initialValue = null, resource, paint) {
        value = MotorcycleHeroBitmaps.load(context, resource, paint)
    }

    Crossfade(
        targetState = bitmap,
        modifier = modifier,
        label = "motorcycle-hero"
    ) { current ->
        if (current != null) {
            Image(
                bitmap = current,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }
    }
}

@DrawableRes
internal fun motorcycleHeroResource(type: MotorcycleType?): Int =
    when (type) {
        MotorcycleType.STREET -> R.drawable.motorcycle_street_hero
        MotorcycleType.NAKED -> R.drawable.motorcycle_naked_hero
        MotorcycleType.SPORT -> R.drawable.motorcycle_esportiva_hero
        MotorcycleType.CUSTOM -> R.drawable.motorcycle_custom_hero
        MotorcycleType.TRAIL -> R.drawable.motorcycle_trail_hero
        MotorcycleType.BIG_TRAIL -> R.drawable.motorcycle_bigtrail_hero
        null -> R.drawable.motorcycle_hero
    }

/** Decodes, scales and repaints the pictures off the main thread, keeping the results. */
private object MotorcycleHeroBitmaps {

    private val originals = bitmapCache()
    private val painted = bitmapCache()

    suspend fun load(
        context: Context,
        @DrawableRes resource: Int,
        paintArgb: Int?
    ): ImageBitmap = withContext(Dispatchers.Default) {
        val original = originals.get("$resource") ?: decode(context, resource)
            .also { originals.put("$resource", it) }

        if (paintArgb == null) {
            return@withContext original.asImageBitmap()
        }

        val key = "$resource-$paintArgb"

        painted.get(key)?.let { return@withContext it.asImageBitmap() }

        val pixels = IntArray(original.width * original.height)
        original.getPixels(pixels, 0, original.width, 0, 0, original.width, original.height)
        MotorcycleRecolor.recolor(pixels, paintArgb)

        Bitmap.createBitmap(pixels, original.width, original.height, Bitmap.Config.ARGB_8888)
            .also { painted.put(key, it) }
            .asImageBitmap()
    }

    private fun decode(context: Context, @DrawableRes resource: Int): Bitmap {
        val options = BitmapFactory.Options().apply { inScaled = false }
        val source = BitmapFactory.decodeResource(context.resources, resource, options)

        if (source.width <= DISPLAY_WIDTH_PX) return source

        val height = source.height * DISPLAY_WIDTH_PX / source.width

        return Bitmap.createScaledBitmap(source, DISPLAY_WIDTH_PX, height, true)
            .also { source.recycle() }
    }

    private fun bitmapCache() =
        object : LruCache<String, Bitmap>(CACHE_BYTES) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
        }
}
