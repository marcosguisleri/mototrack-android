package br.dev.guisleri.mototrack.data.place

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CityIndexTest {

    private val index = CityIndex(SAMPLE_CITIES)

    @Test
    fun normalizingIgnoresAccentsCaseAndExtraSpaces() {
        assertEquals("sao jose", normalizePlace("  São   José "))
        assertEquals("sao jose sp", normalizePlace("São José, SP"))
    }

    @Test
    fun suggestsTheCitiesThatStartWithWhatWasTyped() {
        val result = index.search("ara")

        assertEquals(listOf("Araras", "Araras", "Arapongas"), result.map { it.name })
    }

    @Test
    fun ignoresAccentsAndCaseWhileSearching() {
        assertEquals(
            setOf("São José do Rio Preto", "São José dos Pinhais"),
            index.search("SAO jose").map { it.name }.toSet()
        )
    }

    @Test
    fun aTrailingStateNarrowsTheSearch() {
        assertEquals(listOf(ARARAS), index.search("araras sp"))
        assertEquals(listOf(ARARAS), index.search("Araras, SP"))
        assertEquals(listOf("São José dos Pinhais"), index.search("são josé pr").map { it.name })
    }

    @Test
    fun aWordInTheMiddleOfTheNameAlsoMatchesButAfterTheOnesThatStartWithIt() {
        val result = index.search("rio")

        assertEquals("Rio Claro", result.first().name)
        assertTrue(result.any { it.name == "São José do Rio Preto" })
    }

    @Test
    fun tooShortOrEmptyQueriesSuggestNothing() {
        assertTrue(index.search("").isEmpty())
        assertTrue(index.search("a").isEmpty())
        assertTrue(index.search("zzz").isEmpty())
    }

    @Test
    fun theNumberOfSuggestionsIsLimited() {
        assertEquals(2, index.search("sa", limit = 2).size)
        assertEquals(3, index.search("sa", limit = 10).size)
    }

    @Test
    fun resolvesExactlyWrittenCitiesAndNothingElse() {
        assertEquals(ARARAS, index.resolve("Araras, SP"))
        assertEquals(ARARAS, index.resolve("araras,sp"))
        assertNull(index.resolve("Araras"))
        assertNull(index.resolve("Araras, MG"))
        assertNull(index.resolve("Praia do Rosa, SC"))
    }
}
