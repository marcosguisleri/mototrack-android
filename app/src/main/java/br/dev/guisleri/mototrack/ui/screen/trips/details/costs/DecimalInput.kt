package br.dev.guisleri.mototrack.ui.screen.trips.details.costs

/**
 * Keeps only digits and one decimal separator, shown as a comma, within the
 * digits the backend accepts (e.g. "1234,567" for liters).
 */
fun sanitizeDecimalInput(
    raw: String,
    maxIntegerDigits: Int,
    maxFractionDigits: Int
): String {
    val filtered = raw.filter { it.isDigit() || it == ',' || it == '.' }

    val separatorIndex = filtered.indexOfFirst { it == ',' || it == '.' }

    if (separatorIndex == -1) {
        return filtered.take(maxIntegerDigits)
    }

    val integerPart = filtered.substring(0, separatorIndex).take(maxIntegerDigits)

    val fractionPart = filtered
        .substring(separatorIndex + 1)
        .filter { it.isDigit() }
        .take(maxFractionDigits)

    return "$integerPart,$fractionPart"
}

/** "12,5", "12.5" and "12," all read as numbers; empty or invalid text is null. */
fun parseDecimalInput(text: String): Double? =
    text.trim()
        .replace(',', '.')
        .trimEnd('.')
        .toDoubleOrNull()
        ?.takeIf { it.isFinite() }

/** Value in the shape the inputs accept, for prefilling an edit. */
fun formatDecimalInput(value: Double, fractionDigits: Int): String {
    val text = java.math.BigDecimal.valueOf(value)
        .setScale(fractionDigits, java.math.RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()

    return text.replace('.', ',')
}
