package br.dev.guisleri.mototrack.data.photo

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files

class FileTripPhotoStorageTest {

    private lateinit var root: File
    private val failing = mutableSetOf<String>()

    // Writes the source name as the "image", so a test can tell the photos apart.
    private val encoder = PhotoEncoder { source, target ->
        if (source in failing) throw IOException("cannot read $source")
        target.writeText(source)
    }

    private lateinit var storage: FileTripPhotoStorage

    @Before
    fun setUp() {
        root = Files.createTempDirectory("trip-photos").toFile()
        storage = FileTripPhotoStorage(root, encoder)
    }

    @After
    fun tearDown() {
        root.deleteRecursively()
    }

    private suspend fun names(tripId: Long) =
        storage.photos(tripId).map { it.file.readText() }

    @Test
    fun aTripStartsWithoutPhotos() = runTest {
        assertTrue(storage.photos(1).isEmpty())
    }

    @Test
    fun addedPhotosKeepTheirOrder() = runTest {
        val result = storage.add(1, listOf("a", "b", "c"))

        assertEquals(AddPhotosResult(added = 3, skippedForLimit = 0, failed = 0), result)
        assertEquals(listOf("a", "b", "c"), names(1))
    }

    @Test
    fun photosOfDifferentTripsStayApart() = runTest {
        storage.add(1, listOf("a"))
        storage.add(2, listOf("b", "c"))

        assertEquals(listOf("a"), names(1))
        assertEquals(listOf("b", "c"), names(2))
    }

    @Test
    fun theLimitIsEnforcedAndTheExtraPhotosAreCounted() = runTest {
        storage.add(1, (1..8).map { "p$it" })

        val result = storage.add(1, listOf("x", "y", "z", "w"))

        assertEquals(AddPhotosResult(added = 2, skippedForLimit = 2, failed = 0), result)
        assertEquals(MAX_TRIP_PHOTOS, storage.photos(1).size)
        assertEquals(listOf("x", "y"), names(1).takeLast(2))
    }

    @Test
    fun aPhotoThatCannotBeReadIsSkippedWithoutLeavingAFileBehind() = runTest {
        failing += "bad"

        val result = storage.add(1, listOf("a", "bad", "c"))

        assertEquals(AddPhotosResult(added = 2, skippedForLimit = 0, failed = 1), result)
        assertEquals(listOf("a", "c"), names(1))
        assertEquals(2, File(root, "1").listFiles { file -> file.extension == "jpg" }!!.size)
    }

    @Test
    fun removingAPhotoDeletesItsFile() = runTest {
        storage.add(1, listOf("a", "b"))
        val first = storage.photos(1).first()

        storage.remove(1, first.id)

        assertEquals(listOf("b"), names(1))
        assertFalse(first.file.exists())
    }

    @Test
    fun removingAnUnknownPhotoChangesNothing() = runTest {
        storage.add(1, listOf("a"))

        storage.remove(1, "nope")

        assertEquals(listOf("a"), names(1))
    }

    @Test
    fun theCoverMovesToTheFront() = runTest {
        storage.add(1, listOf("a", "b", "c"))
        val third = storage.photos(1)[2]

        storage.makeCover(1, third.id)

        assertEquals(listOf("c", "a", "b"), names(1))
    }

    @Test
    fun clearingATripRemovesAllItsPhotosAndOnlyThose() = runTest {
        storage.add(1, listOf("a", "b"))
        storage.add(2, listOf("c"))

        storage.clear(1)

        assertTrue(storage.photos(1).isEmpty())
        assertFalse(File(root, "1").exists())
        assertEquals(listOf("c"), names(2))
    }

    @Test
    fun aPhotoWhoseFileWasLostIsLeftOutOfTheList() = runTest {
        storage.add(1, listOf("a", "b"))
        storage.photos(1).first().file.delete()

        assertEquals(listOf("b"), names(1))
    }
}
