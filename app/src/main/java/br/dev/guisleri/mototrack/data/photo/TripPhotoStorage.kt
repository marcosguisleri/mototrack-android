package br.dev.guisleri.mototrack.data.photo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

const val MAX_TRIP_PHOTOS = 10

data class TripPhoto(
    val id: String,
    val file: File
)

data class AddPhotosResult(
    val added: Int,
    val skippedForLimit: Int,
    val failed: Int
)

/** Turns a picked image into the (smaller) file the app keeps. */
fun interface PhotoEncoder {
    suspend fun encode(source: String, target: File)
}

/**
 * The best photos of each trip, kept on the device.
 *
 * The first photo is the cover. Trip ids are unique across accounts, so a trip's
 * photos never mix with another rider's.
 */
interface TripPhotoStorage {

    suspend fun photos(tripId: Long): List<TripPhoto>

    /** Adds as many of [sources] as fit under the limit; the rest are counted, not lost silently. */
    suspend fun add(tripId: Long, sources: List<String>): AddPhotosResult

    suspend fun remove(tripId: Long, photoId: String)

    suspend fun makeCover(tripId: Long, photoId: String)

    /** Removes every photo of the trip, e.g. when the trip is deleted. */
    suspend fun clear(tripId: Long)
}

class FileTripPhotoStorage(
    private val root: File,
    private val encoder: PhotoEncoder
) : TripPhotoStorage {

    private val mutex = Mutex()

    private fun directory(tripId: Long) = File(root, tripId.toString())

    private fun orderFile(tripId: Long) = File(directory(tripId), ORDER_FILE)

    private fun photoFile(tripId: Long, id: String) = File(directory(tripId), "$id.jpg")

    // The order file is the source of truth; ids whose file is gone are dropped.
    private fun readOrder(tripId: Long): List<String> {
        val order = orderFile(tripId)

        if (!order.exists()) return emptyList()

        return order.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && photoFile(tripId, it).exists() }
    }

    private fun writeOrder(tripId: Long, ids: List<String>) {
        directory(tripId).mkdirs()
        orderFile(tripId).writeText(ids.joinToString("\n"))
    }

    override suspend fun photos(tripId: Long): List<TripPhoto> =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                readOrder(tripId).map { TripPhoto(it, photoFile(tripId, it)) }
            }
        }

    override suspend fun add(tripId: Long, sources: List<String>): AddPhotosResult =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val order = readOrder(tripId).toMutableList()
                val room = (MAX_TRIP_PHOTOS - order.size).coerceAtLeast(0)
                val accepted = sources.take(room)
                var failed = 0
                var added = 0

                directory(tripId).mkdirs()

                accepted.forEach { source ->
                    val id = UUID.randomUUID().toString()
                    val target = photoFile(tripId, id)

                    try {
                        encoder.encode(source, target)
                        order += id
                        added++
                    } catch (exception: CancellationException) {
                        target.delete()
                        throw exception
                    } catch (exception: Exception) {
                        target.delete()
                        failed++
                    }
                }

                if (added > 0) writeOrder(tripId, order)

                AddPhotosResult(
                    added = added,
                    skippedForLimit = sources.size - accepted.size,
                    failed = failed
                )
            }
        }

    override suspend fun remove(tripId: Long, photoId: String) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val order = readOrder(tripId)

                if (photoId in order) {
                    photoFile(tripId, photoId).delete()
                    writeOrder(tripId, order - photoId)
                }
            }
        }
    }

    override suspend fun makeCover(tripId: Long, photoId: String) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val order = readOrder(tripId)

                if (photoId in order) {
                    writeOrder(tripId, listOf(photoId) + (order - photoId))
                }
            }
        }
    }

    override suspend fun clear(tripId: Long) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                directory(tripId).deleteRecursively()
            }
        }
    }

    private companion object {
        const val ORDER_FILE = "order.txt"
    }
}
