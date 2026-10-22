package br.dev.guisleri.mototrack.ui.components

import androidx.compose.ui.graphics.Color

/** Best-effort swatch for the colour name typed when registering the bike. */
internal fun motorcycleColor(name: String): Color? {
    val n = name.lowercase()

    return when {
        n.contains("verm") || n.contains("red") -> Color(0xFFD93025)
        n.contains("pret") || n.contains("black") -> Color(0xFF111111)
        n.contains("branc") || n.contains("white") -> Color(0xFFF5F5F5)
        n.contains("azul") || n.contains("blue") -> Color(0xFF2F6FD6)
        n.contains("verde") || n.contains("green") -> Color(0xFF2E9B57)
        n.contains("amarel") || n.contains("yellow") -> Color(0xFFF2C21A)
        n.contains("laranj") || n.contains("orange") -> Color(0xFFF28A1A)
        n.contains("cinza") || n.contains("grafite") || n.contains("gray") ||
                n.contains("grey") -> Color(0xFF8A8F98)
        n.contains("prat") || n.contains("silver") -> Color(0xFFC4C9D0)
        n.contains("marrom") || n.contains("brown") -> Color(0xFF7A4B2A)
        n.contains("dourad") || n.contains("gold") -> Color(0xFFD4A537)
        n.contains("vinho") || n.contains("bord") || n.contains("wine") -> Color(0xFF7B1E2B)
        n.contains("rosa") || n.contains("pink") -> Color(0xFFE05A8B)
        n.contains("roxa") || n.contains("lil") || n.contains("purple") -> Color(0xFF6B3FA0)
        n.contains("bege") || n.contains("creme") || n.contains("beige") -> Color(0xFFD9C3A0)
        n.contains("bronze") -> Color(0xFFA8652B)
        else -> null
    }
}

/** Colour names offered when registering a bike; they match [motorcycleColor]. */
internal val MotorcycleColorOptions: List<Pair<String, Color>> = listOf(
    "Vermelha" to Color(0xFFD93025),
    "Preta" to Color(0xFF111111),
    "Branca" to Color(0xFFF5F5F5),
    "Azul" to Color(0xFF2F6FD6),
    "Cinza" to Color(0xFF8A8F98),
    "Prata" to Color(0xFFC4C9D0),
    "Verde" to Color(0xFF2E9B57),
    "Amarela" to Color(0xFFF2C21A),
    "Laranja" to Color(0xFFF28A1A),
    "Marrom" to Color(0xFF7A4B2A),
    "Dourada" to Color(0xFFD4A537)
)
