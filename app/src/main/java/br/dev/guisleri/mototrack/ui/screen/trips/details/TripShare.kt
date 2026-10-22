package br.dev.guisleri.mototrack.ui.screen.trips.details

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.formatDistance
import br.dev.guisleri.mototrack.ui.components.formatMoney
import br.dev.guisleri.mototrack.ui.components.formatTerrain
import br.dev.guisleri.mototrack.ui.components.formatTripDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.Instant

private const val SHARE_DIRECTORY = "shared_trips"

/** The money and the ride's figures a finished trip may show on its share card. */
internal data class TripShareCosts(
    val totalCost: Double,
    val costPerKm: Double?,
    val kmPerLiter: Double?,
    val duration: Duration?
)

/** Null while the trip is not finished or nothing was recorded: there is nothing to show. */
internal fun TripLiveStats.toShareCosts(trip: TripResponseDTO): TripShareCosts? {
    val total = totalCost ?: return null
    if (trip.status != "COMPLETED") return null

    return TripShareCosts(
        totalCost = total,
        costPerKm = costPerKm,
        kmPerLiter = kmPerLiter,
        duration = tripElapsed(trip, Instant.now())
    )
}

/** The caption that travels with the shared picture. */
internal fun tripShareText(trip: TripResponseDTO, costs: TripShareCosts? = null): String {
    val verb = when (trip.status) {
        "COMPLETED" -> "Fiz esta viagem"
        "IN_PROGRESS" -> "Estou nesta viagem"
        else -> "Vou fazer esta viagem"
    }

    return buildString {
        appendLine("🏍️ $verb: ${trip.origin} → ${trip.destination}")
        appendLine(
            "${formatDistance(trip.distanceKm)} · ${formatTerrain(trip.terrain)} · " +
                    formatTripDate(trip.tripDate)
        )
        appendLine("${trip.motorcycle.brand} ${trip.motorcycle.model}")
        costs?.let {
            appendLine(
                buildList {
                    add("${formatMoney(it.totalCost)} gastos")
                    it.costPerKm?.let { perKm -> add("${formatMoney(perKm)}/km") }
                    it.kmPerLiter?.let { kmL -> add("${formatDecimal(kmL, 1)} km/L") }
                }.joinToString(" · ")
            )
        }
        appendLine()
        append("Registrado no MotoTrack")
    }
}

/** Short label of the status shown on the share card. */
internal fun tripShareStatusLabel(status: String): String =
    when (status) {
        "COMPLETED" -> "VIAGEM CONCLUÍDA"
        "IN_PROGRESS" -> "EM VIAGEM"
        else -> "PRÓXIMA VIAGEM"
    }

/** Writes [bitmap] to the cache folder the file provider exposes and returns its uri. */
internal suspend fun saveShareImage(
    context: Context,
    bitmap: Bitmap,
    tripId: Long
): Uri = withContext(Dispatchers.IO) {
    val directory = File(context.cacheDir, SHARE_DIRECTORY).apply { mkdirs() }
    val file = File(directory, "trip-$tripId.png")

    file.outputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }

    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

internal fun shareTripImage(
    context: Context,
    uri: Uri,
    text: String
) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        clipData = ClipData.newRawUri("", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(send, "Compartilhar viagem"))
}
