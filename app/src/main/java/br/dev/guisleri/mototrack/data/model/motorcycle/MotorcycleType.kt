package br.dev.guisleri.mototrack.data.model.motorcycle

enum class MotorcycleType(
    val apiValue: String,
    val label: String,
    val hint: String
) {
    STREET("STREET", "Street", "Urbana"),
    NAKED("NAKED", "Naked", "Sem carenagem"),
    SPORT("SPORT", "Esportiva", "Carenada"),
    CUSTOM("CUSTOM", "Custom", "Estilo cruiser"),
    TRAIL("TRAIL", "Trail", "Estrada e terra"),
    BIG_TRAIL("BIG_TRAIL", "Big Trail", "Aventura");

    companion object {
        fun fromApi(value: String?): MotorcycleType? =
            entries.firstOrNull { it.apiValue == value }
    }
}
