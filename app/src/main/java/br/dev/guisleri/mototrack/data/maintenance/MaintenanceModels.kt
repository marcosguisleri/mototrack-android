package br.dev.guisleri.mototrack.data.maintenance

import kotlinx.serialization.Serializable

/** What a rider keeps an eye on, with the interval most motorcycles follow. */
enum class MaintenanceItem(
    val key: String,
    val label: String,
    val hint: String,
    val defaultKm: Int?,
    val defaultMonths: Int?
) {
    OIL("oil", "Óleo do motor", "Troca do óleo e do filtro de óleo", 3_000, 6),
    CHAIN("chain", "Corrente", "Limpeza e lubrificação do kit de transmissão", 500, null),
    TIRES("tires", "Pneus", "Troca dos pneus dianteiro e traseiro", 15_000, null),
    BRAKES("brakes", "Freios", "Pastilhas, discos e fluido de freio", 10_000, 24),
    AIR_FILTER("air_filter", "Filtro de ar", "Limpeza ou troca do filtro de ar", 6_000, 12),
    SERVICE("service", "Revisão geral", "Revisão completa na oficina", 6_000, 6)
}

/**
 * The km on the panel, as the rider told it, and the km of completed trips the
 * app had counted at that moment: from then on the app adds only the trips since.
 */
@Serializable
data class OdometerReading(
    val km: Int,
    val tripKmAtReading: Double,
    val date: String
)

/** The last time an item was done; [doneAtKm] is null when only the date is known. */
@Serializable
data class ServiceRecord(
    val doneAtKm: Int? = null,
    val date: String
)

/** A rider's own interval for an item; a null side means "not by that measure". */
@Serializable
data class ItemInterval(
    val km: Int? = null,
    val months: Int? = null
)

/** Everything kept about one motorcycle's maintenance, by [MaintenanceItem.key]. */
@Serializable
data class MotorcycleMaintenance(
    val odometer: OdometerReading? = null,
    val services: Map<String, ServiceRecord> = emptyMap(),
    val intervals: Map<String, ItemInterval> = emptyMap(),
    // The last km of completed trips the app saw, and the motorcycle's name: what a
    // notification needs when the app is closed and cannot ask the server.
    val tripKmSnapshot: Double? = null,
    val motorcycleName: String? = null,
    // The level (SOON or OVERDUE) each item was last notified at, by item key.
    val alerted: Map<String, String> = emptyMap()
)
