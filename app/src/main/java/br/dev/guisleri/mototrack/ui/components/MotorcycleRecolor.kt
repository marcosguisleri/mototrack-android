package br.dev.guisleri.mototrack.ui.components

import kotlin.math.abs
import kotlin.math.min

/**
 * Repaints the bodywork of the motorcycle pictures.
 *
 * The pictures are drawn with every painted panel in one saturated red and all
 * the rest (frame, engine, wheels, exhaust) in neutral tones. Only pixels close
 * to that red are touched; their light and shadow are kept, so the volume of the
 * panels survives the new colour. Pixels are non-premultiplied ARGB ints.
 */
internal object MotorcycleRecolor {

    // Saturation and value of the key red used in the artwork (#E10600).
    private const val KEY_SATURATION = 0.97f
    private const val KEY_VALUE = 0.88f

    // Lets dark targets (black) keep visible highlights and shadows.
    private const val SHADE_BOOST = 0.8f

    fun recolor(pixels: IntArray, targetArgb: Int) {
        val targetRed = (targetArgb shr 16) and 0xFF
        val targetGreen = (targetArgb shr 8) and 0xFF
        val targetBlue = targetArgb and 0xFF

        val targetMax = maxOf(targetRed, targetGreen, targetBlue)
        val targetMin = minOf(targetRed, targetGreen, targetBlue)
        val targetValue = targetMax / 255f
        val targetSaturation = if (targetMax == 0) 0f else (targetMax - targetMin).toFloat() / targetMax
        val targetHue = hueOf(targetRed, targetGreen, targetBlue, targetMax, targetMin)

        for (index in pixels.indices) {
            val pixel = pixels[index]
            val alpha = pixel ushr 24
            if (alpha == 0) continue

            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF

            // Red hues always have red as the strongest channel.
            if (red < green || red < blue) continue

            val range = red - min(green, blue)
            if (range == 0) continue

            val saturation = range.toFloat() / red
            if (saturation < 0.30f) continue

            val value = red / 255f
            val hueDistance = abs(green - blue).toFloat() / range / 6f

            val weight =
                smoothstep(0.30f, 0.55f, saturation) *
                        (1f - smoothstep(0.02f, 0.07f, hueDistance)) *
                        smoothstep(0.06f, 0.16f, value)

            if (weight <= 0f) continue

            val shade = value / KEY_VALUE - 1f
            val newValue =
                (targetValue * (1f + shade) + SHADE_BOOST * (1f - targetValue) * shade)
                    .coerceIn(0f, 1f)
            val newSaturation =
                (targetSaturation * min(1f, saturation / KEY_SATURATION))
                    .coerceIn(0f, 1f)

            val painted = hsvToRgb(targetHue, newSaturation, newValue)

            val newRed = blend(red, (painted shr 16) and 0xFF, weight)
            val newGreen = blend(green, (painted shr 8) and 0xFF, weight)
            val newBlue = blend(blue, painted and 0xFF, weight)

            pixels[index] = (alpha shl 24) or (newRed shl 16) or (newGreen shl 8) or newBlue
        }
    }

    private fun blend(from: Int, to: Int, weight: Float): Int =
        (from + (to - from) * weight).toInt().coerceIn(0, 255)

    private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun hueOf(red: Int, green: Int, blue: Int, max: Int, min: Int): Float {
        val range = max - min
        if (range == 0) return 0f

        val sector = when (max) {
            red -> ((green - blue).toFloat() / range).let { if (it < 0f) it + 6f else it }
            green -> (blue - red).toFloat() / range + 2f
            else -> (red - green).toFloat() / range + 4f
        }

        return sector / 6f
    }

    private fun hsvToRgb(hue: Float, saturation: Float, value: Float): Int {
        val scaled = hue * 6f
        val sector = scaled.toInt() % 6
        val fraction = scaled - scaled.toInt()

        val p = value * (1f - saturation)
        val q = value * (1f - saturation * fraction)
        val t = value * (1f - saturation * (1f - fraction))

        val (r, g, b) = when (sector) {
            0 -> Triple(value, t, p)
            1 -> Triple(q, value, p)
            2 -> Triple(p, value, t)
            3 -> Triple(p, q, value)
            4 -> Triple(t, p, value)
            else -> Triple(value, p, q)
        }

        return ((r * 255f).toInt() shl 16) or ((g * 255f).toInt() shl 8) or (b * 255f).toInt()
    }
}
