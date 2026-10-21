package br.dev.guisleri.mototrack.ui.components

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatTripDate(
    date: String
): String {
    return runCatching {
        LocalDate
            .parse(date)
            .format(
                DateTimeFormatter.ofPattern(
                    "d MMM yyyy",
                    Locale.forLanguageTag("pt-BR")
                )
            )
            .replace(".", "")
    }.getOrDefault(date)
}

internal fun formatNumber(
    value: Double
): String {
    return NumberFormat
        .getIntegerInstance(Locale.forLanguageTag("pt-BR"))
        .format(value)
}

internal fun formatDistance(
    distanceKm: Double
): String {
    val formatter = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("pt-BR")
    ).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 1
    }

    return "${formatter.format(distanceKm)} km"
}

internal fun formatTerrain(
    terrain: String
): String {
    return when (terrain) {
        "ASPHALT" -> "Asfalto"
        "MIXED" -> "Misto"
        "OFF_ROAD" -> "Off-road"
        else -> terrain
    }
}
