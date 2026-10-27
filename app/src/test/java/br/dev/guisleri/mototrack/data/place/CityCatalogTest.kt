package br.dev.guisleri.mototrack.data.place

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class CityCatalogTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun cacheFile() = File(folder.root, "places/cities.json")

    @Test
    fun downloadsOnceAndThenReadsTheFile() = runTest {
        val api = FakeIbgeApi()

        val first = CityCatalog(api, cacheFile()).load()
        // A new catalog, as after restarting the app: the network is not needed.
        val second = CityCatalog(api, cacheFile()).load()

        assertEquals(1, api.downloads)
        assertEquals(SAMPLE_CITIES.size, first.size)
        assertEquals(SAMPLE_CITIES.size, second.size)
        assertTrue(cacheFile().exists())
    }

    @Test
    fun keepsTheListInMemoryOnceLoaded() = runTest {
        val api = FakeIbgeApi()
        val catalog = CityCatalog(api, cacheFile())

        catalog.load()
        cacheFile().delete()
        catalog.load()

        assertEquals(1, api.downloads)
    }

    @Test
    fun skipsMunicipalitiesWithoutAState() = runTest {
        val api = object : IbgeApiService {
            override suspend fun getMunicipalities() = listOf(
                IbgeMunicipalityDTO(1, "Sem Estado"),
                IbgeMunicipalityDTO(
                    2, "Com Região Imediata",
                    immediateRegion = IbgeImmediateRegionDTO(
                        IbgeIntermediateRegionDTO(IbgeUfDTO("BA"))
                    )
                )
            )

            override suspend fun getMunicipalityMetadata(id: Long) = emptyList<IbgeMetadataDTO>()
        }

        val index = CityCatalog(api, cacheFile()).load()

        assertEquals(1, index.size)
        assertEquals("Com Região Imediata", index.search("com").single().name)
    }

    @Test
    fun failsWhenThereIsNeitherAFileNorAConnection() = runTest {
        val api = FakeIbgeApi().apply { failure = IOException("offline") }

        val failed = runCatching { CityCatalog(api, cacheFile()).load() }

        assertTrue(failed.exceptionOrNull() is IOException)
    }

    @Test
    fun aBrokenFileIsReplacedByANewDownload() = runTest {
        cacheFile().parentFile!!.mkdirs()
        cacheFile().writeText("not json")
        val api = FakeIbgeApi()

        val index = CityCatalog(api, cacheFile()).load()

        assertEquals(1, api.downloads)
        assertEquals(SAMPLE_CITIES.size, index.size)
    }
}
