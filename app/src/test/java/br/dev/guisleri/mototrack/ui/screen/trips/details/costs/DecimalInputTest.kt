package br.dev.guisleri.mototrack.ui.screen.trips.details.costs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecimalInputTest {

    @Test
    fun sanitizeKeepsDigitsAndOneSeparatorShownAsComma() {
        assertEquals("12,5", sanitizeDecimalInput("12.5", 4, 3))
        assertEquals("12,5", sanitizeDecimalInput("1a2,5", 4, 3))
        assertEquals("12,53", sanitizeDecimalInput("12,5,3", 4, 3))
    }

    @Test
    fun sanitizeLimitsTheDigitsTheBackendAccepts() {
        assertEquals("1234", sanitizeDecimalInput("123456", 4, 3))
        assertEquals("1234,567", sanitizeDecimalInput("12345,56789", 4, 3))
        assertEquals("99999999,99", sanitizeDecimalInput("999999999,999", 8, 2))
    }

    @Test
    fun parseReadsCommaAndDotAndRejectsEmptyText() {
        assertEquals(12.5, parseDecimalInput("12,5")!!, 0.0)
        assertEquals(12.5, parseDecimalInput(" 12.5 ")!!, 0.0)
        assertEquals(12.0, parseDecimalInput("12,")!!, 0.0)
        assertEquals(0.0, parseDecimalInput("0")!!, 0.0)
        assertNull(parseDecimalInput(""))
        assertNull(parseDecimalInput(","))
    }

    @Test
    fun formatGivesBackWhatTheInputAccepts() {
        assertEquals("6,19", formatDecimalInput(6.19, 2))
        assertEquals("45", formatDecimalInput(45.0, 2))
        assertEquals("12,345", formatDecimalInput(12.345, 3))
        assertEquals("0", formatDecimalInput(0.0, 2))
    }
}
