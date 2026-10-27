package br.dev.guisleri.mototrack.data.place

import kotlinx.serialization.Serializable
import java.text.Normalizer

@Serializable
data class City(
    val id: Long,
    val name: String,
    val uf: String
) {
    /** How the city is written in a trip: "Araras, SP". */
    val label: String get() = "$name, $uf"
}

private val ACCENTS = Regex("\\p{M}+")
private val SEPARATORS = Regex("[,\\s]+")

/** Lower case, no accents, single spaces: "São José" and "sao  jose" compare equal. */
internal fun normalizePlace(text: String): String =
    Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
        .replace(ACCENTS, "")
        .lowercase()
        .replace(SEPARATORS, " ")
        .trim()

/**
 * The cities of Brazil, searched on the device: instant, and it works offline
 * once the list has been downloaded.
 */
class CityIndex(cities: List<City>) {

    private class Entry(val city: City, val name: String, val uf: String)

    private val entries = cities.map { Entry(it, normalizePlace(it.name), it.uf.lowercase()) }

    private val ufs = entries.mapTo(HashSet()) { it.uf }

    val size: Int get() = entries.size

    /**
     * The cities to suggest for what was typed so far. Names that start with it come
     * first, then names with a word that starts with it; a trailing state ("araras sp",
     * "Araras, SP") narrows the search to that state.
     */
    fun search(query: String, limit: Int = 5): List<City> {
        val tokens = normalizePlace(query).split(' ').filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return emptyList()

        val uf = tokens.last().takeIf { tokens.size > 1 && it.length == 2 && it in ufs }
        val text = (if (uf != null) tokens.dropLast(1) else tokens).joinToString(" ")
        if (text.length < MIN_QUERY_LENGTH) return emptyList()

        return entries
            .asSequence()
            .filter { uf == null || it.uf == uf }
            .mapNotNull { entry ->
                val rank = when {
                    entry.name.startsWith(text) -> 0
                    entry.name.split(' ').any { it.startsWith(text) } -> 1
                    else -> return@mapNotNull null
                }
                rank to entry
            }
            .sortedWith(compareBy({ it.first }, { it.second.name.length }, { it.second.name }))
            .map { it.second.city }
            .take(limit)
            .toList()
    }

    /** The city a text names exactly ("Araras, SP"), or null: free text is not a city. */
    fun resolve(text: String): City? {
        val comma = text.lastIndexOf(',')
        if (comma < 0) return null

        val name = normalizePlace(text.substring(0, comma))
        val uf = normalizePlace(text.substring(comma + 1))

        return entries.firstOrNull { it.name == name && it.uf == uf }?.city
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}
