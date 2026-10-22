package br.dev.guisleri.mototrack.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MotorcycleRecolorTest {

    private val keyRed = 0xFFE10600.toInt()
    private val blue = 0xFF2F6FD6.toInt()
    private val black = 0xFF111111.toInt()
    private val white = 0xFFF5F5F5.toInt()

    private fun recolored(pixel: Int, target: Int): Int {
        val pixels = intArrayOf(pixel)
        MotorcycleRecolor.recolor(pixels, target)
        return pixels[0]
    }

    private fun red(argb: Int) = (argb shr 16) and 0xFF
    private fun green(argb: Int) = (argb shr 8) and 0xFF
    private fun blue(argb: Int) = argb and 0xFF
    private fun brightest(argb: Int) = maxOf(red(argb), green(argb), blue(argb))
    private fun dimmest(argb: Int) = minOf(red(argb), green(argb), blue(argb))

    @Test
    fun transparentPixelsAreLeftAlone() {
        assertEquals(0x00E10600, recolored(0x00E10600, blue))
    }

    @Test
    fun neutralPartsAreNeverRepainted() {
        listOf(
            0xFF000000.toInt(), // tyre
            0xFF202020.toInt(), // frame
            0xFF808080.toInt(), // engine
            0xFFC0C0C0.toInt(), // exhaust
            0xFFFFFFFF.toInt()  // highlight
        ).forEach { neutral ->
            assertEquals(neutral, recolored(neutral, blue))
        }
    }

    @Test
    fun keyRedTakesTheHueOfTheTarget() {
        val result = recolored(keyRed, blue)

        assertTrue("blue should dominate", blue(result) > red(result) && blue(result) > green(result))
    }

    @Test
    fun alphaIsPreserved() {
        assertEquals(0x80, recolored(0x80E10600.toInt(), blue) ushr 24)
    }

    @Test
    fun shadowStaysDarkerThanLightOfTheSamePanel() {
        val light = recolored(0xFFE10600.toInt(), blue)
        val shadow = recolored(0xFF7A0300.toInt(), blue)

        assertTrue(brightest(shadow) < brightest(light))
    }

    @Test
    fun blackTargetKeepsPanelsDark() {
        assertTrue(brightest(recolored(keyRed, black)) < 60)
    }

    @Test
    fun blackTargetStillShowsHighlights() {
        val base = recolored(keyRed, black)
        val highlight = recolored(0xFFFF2A1F.toInt(), black)

        assertTrue(brightest(highlight) > brightest(base))
    }

    @Test
    fun whiteTargetKeepsPanelsBright() {
        assertTrue(dimmest(recolored(keyRed, white)) > 200)
    }

    @Test
    fun pinkishPixelsAreNotMistakenForThePaint() {
        val skinLike = 0xFFE8C8C8.toInt()

        assertEquals(skinLike, recolored(skinLike, blue))
    }

    @Test
    fun orangePixelsAreNotMistakenForThePaint() {
        val orange = 0xFFE08A00.toInt()

        assertEquals(orange, recolored(orange, blue))
    }

    @Test
    fun everyColorOfTheRegistrationPaletteRecoloursTheKey() {
        MotorcycleColorOptions.forEach { (name, color) ->
            val result = recolored(keyRed, color.toArgb())

            assertTrue("$name should change the key red", result != keyRed || name == "Vermelha")
        }
    }

    @Test
    fun colourNamesTypedByHandAreUnderstood() {
        assertNotNull(motorcycleColor("Vinho metalizado"))
        assertNotNull(motorcycleColor("azul petróleo"))
        assertNotNull(motorcycleColor("VERMELHA"))
        assertNull(motorcycleColor("Camuflada"))
    }
}
