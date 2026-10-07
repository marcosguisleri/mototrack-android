package br.dev.guisleri.mototrack.data.network

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import br.dev.guisleri.mototrack.data.local.TokenStorage
import br.dev.guisleri.mototrack.data.model.auth.AuthTokenResponseDTO
import br.dev.guisleri.mototrack.data.model.auth.LoginRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.RefreshTokenRequestDTO
import br.dev.guisleri.mototrack.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Protocol
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.HttpException
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class AuthSessionConcurrencyTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dataStoreJob = SupervisorJob()
    private val workers = Executors.newFixedThreadPool(2)
    private val api = FakeAuthApi()
    private lateinit var storage: TokenStorage

    @Before
    fun setUp() = runBlocking {
        storage = TokenStorage(
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(dataStoreJob + Dispatchers.IO),
                produceFile = { temporaryFolder.root.resolve("auth.preferences_pb") }
            )
        )
        storage.saveTokens("access-old", "refresh-old")
    }

    @After
    fun tearDown() = runBlocking {
        workers.shutdownNow()
        assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS))
        dataStoreJob.cancelAndJoin()
    }

    @Test
    fun concurrentAuthenticatorsRefreshOnceAndReuseRotatedToken() = runBlocking {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        api.onRefresh = { request ->
            assertEquals("refresh-old", request.refreshToken)
            entered.countDown()
            await(release)
            AuthTokenResponseDTO("access-rotated", "refresh-rotated")
        }

        val first = workers.submit<Request?> {
            authenticator().authenticate(null, unauthorized())
        }
        await(entered)
        val secondStarted = CountDownLatch(1)
        val second = workers.submit<Request?> {
            secondStarted.countDown()
            authenticator().authenticate(null, unauthorized())
        }
        await(secondStarted)
        assertFalse(second.isDone)
        release.countDown()

        assertEquals("Bearer access-rotated", first.get(5, TimeUnit.SECONDS)?.header("Authorization"))
        assertEquals("Bearer access-rotated", second.get(5, TimeUnit.SECONDS)?.header("Authorization"))
        assertEquals(1, api.refreshCalls.get())
        assertEquals("access-rotated" to "refresh-rotated", storage.getTokens())
    }

    @Test
    fun alreadyRenewedAccessTokenDoesNotRefreshAgain() = runBlocking {
        storage.saveTokens("access-rotated", "refresh-rotated")

        val retry = authenticator().authenticate(null, unauthorized())

        assertEquals("Bearer access-rotated", retry?.header("Authorization"))
        assertEquals(0, api.refreshCalls.get())
    }

    @Test
    fun refreshesForDifferentSessionsNeverOverlap() = runBlocking {
        val firstEntered = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val secondEntered = CountDownLatch(1)
        api.onRefresh = { request ->
            when (request.refreshToken) {
                "refresh-old" -> {
                    firstEntered.countDown()
                    await(releaseFirst)
                    AuthTokenResponseDTO("access-stale", "refresh-stale")
                }
                "refresh-login" -> {
                    secondEntered.countDown()
                    AuthTokenResponseDTO("access-login-rotated", "refresh-login-rotated")
                }
                else -> throw AssertionError("Unexpected refresh token")
            }
        }

        val first = workers.submit<Request?> {
            authenticator().authenticate(null, unauthorized())
        }
        await(firstEntered)
        AuthRepository(api, storage).login("new@example.test", "test-password")
        val secondStarted = CountDownLatch(1)
        val second = workers.submit<Request?> {
            secondStarted.countDown()
            authenticator().authenticate(null, unauthorized("access-login"))
        }
        await(secondStarted)
        assertFalse(secondEntered.await(200, TimeUnit.MILLISECONDS))
        releaseFirst.countDown()

        assertNull(first.get(5, TimeUnit.SECONDS))
        assertEquals("Bearer access-login-rotated", second.get(5, TimeUnit.SECONDS)?.header("Authorization"))
        assertEquals(0L, secondEntered.count)
        assertEquals(2, api.refreshCalls.get())
        assertEquals("access-login-rotated" to "refresh-login-rotated", storage.getTokens())
    }

    @Test
    fun successiveRefreshesUseTheRotatedRefreshToken() = runBlocking {
        api.onRefresh = { request ->
            when (request.refreshToken) {
                "refresh-old" -> AuthTokenResponseDTO("access-first", "refresh-first")
                "refresh-first" -> AuthTokenResponseDTO("access-second", "refresh-second")
                else -> throw AssertionError("Unexpected refresh token")
            }
        }

        authenticator().authenticate(null, unauthorized())
        val retry = authenticator().authenticate(null, unauthorized("access-first"))

        assertEquals("Bearer access-second", retry?.header("Authorization"))
        assertEquals("access-second" to "refresh-second", storage.getTokens())
        assertEquals(2, api.refreshCalls.get())
    }

    @Test
    fun refreshFinishingAfterLogoutCannotRestoreSession() = runBlocking {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        api.onRefresh = {
            entered.countDown()
            await(release)
            AuthTokenResponseDTO("access-rotated", "refresh-rotated")
        }
        api.onLogout = { request ->
            assertEquals("refresh-old", request.refreshToken)
            assertEquals(null to null, storage.getTokens())
        }

        val refresh = workers.submit<Request?> {
            authenticator().authenticate(null, unauthorized())
        }
        await(entered)
        AuthRepository(api, storage).logout()
        release.countDown()

        assertNull(refresh.get(5, TimeUnit.SECONDS))
        assertEquals(null to null, storage.getTokens())
        assertFalse(storage.hasStoredSession())
    }

    @Test
    fun refreshFinishingAfterNewLoginCannotOverwriteSession() = runBlocking {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        api.onRefresh = {
            entered.countDown()
            await(release)
            AuthTokenResponseDTO("access-rotated", "refresh-rotated")
        }

        val refresh = workers.submit<Request?> {
            authenticator().authenticate(null, unauthorized())
        }
        await(entered)
        AuthRepository(api, storage).login("new@example.test", "test-password")
        release.countDown()

        assertNull(refresh.get(5, TimeUnit.SECONDS))
        assertEquals("access-login" to "refresh-login", storage.getTokens())
    }

    @Test
    fun oldRefreshRejectionCannotClearNewLogin() = runBlocking {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        api.onRefresh = {
            entered.countDown()
            await(release)
            throw unauthorizedException()
        }

        val refresh = workers.submit<Request?> {
            authenticator().authenticate(null, unauthorized())
        }
        await(entered)
        AuthRepository(api, storage).login("new@example.test", "test-password")
        release.countDown()

        assertNull(refresh.get(5, TimeUnit.SECONDS))
        assertEquals("access-login" to "refresh-login", storage.getTokens())
    }

    @Test
    fun failedLogoutRevocationCannotClearLoginStartedAfterLocalLogout() = runBlocking {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        api.onLogout = {
            assertEquals(null to null, storage.getTokens())
            entered.countDown()
            await(release)
            throw IOException("Backend unavailable")
        }

        val logout = workers.submit<Boolean> {
            runBlocking {
                try {
                    AuthRepository(api, storage).logout()
                    false
                } catch (exception: IOException) {
                    true
                }
            }
        }
        await(entered)
        AuthRepository(api, storage).login("new@example.test", "test-password")
        release.countDown()

        assertTrue(logout.get(5, TimeUnit.SECONDS))
        assertEquals("access-login" to "refresh-login", storage.getTokens())
    }

    @Test
    fun conditionalStorageOperationsRequireCurrentRefreshToken() = runBlocking {
        assertFalse(storage.saveTokensIfRefreshTokenMatches("stale", "access-stale", "refresh-stale"))
        assertFalse(storage.clearTokensIfRefreshTokenMatches("stale"))
        assertEquals("access-old" to "refresh-old", storage.getTokens())

        assertTrue(storage.saveTokensIfRefreshTokenMatches("refresh-old", "access-rotated", "refresh-rotated"))
        assertFalse(storage.clearTokensIfRefreshTokenMatches("refresh-old"))
        assertEquals("access-rotated" to "refresh-rotated", storage.getTokens())

        assertTrue(storage.clearTokensIfRefreshTokenMatches("refresh-rotated"))
        assertEquals(null to null, storage.getTokens())
    }

    @Test
    fun rejectedCurrentRefreshClearsSessionAndPreventsFurtherRefresh() = runBlocking {
        api.onRefresh = { throw unauthorizedException() }

        assertNull(authenticator().authenticate(null, unauthorized()))
        assertNull(authenticator().authenticate(null, unauthorized()))
        assertEquals(1, api.refreshCalls.get())
        assertEquals(null to null, storage.getTokens())
    }

    @Test
    fun concurrentConditionalSavesAcceptOnlyOneRotation() = runBlocking {
        val ready = CountDownLatch(2)
        fun save(accessToken: String, refreshToken: String) = workers.submit<Boolean> {
            ready.countDown()
            await(ready)
            runBlocking {
                storage.saveTokensIfRefreshTokenMatches("refresh-old", accessToken, refreshToken)
            }
        }

        val first = save("access-first", "refresh-first")
        val second = save("access-second", "refresh-second")
        val firstSaved = first.get(5, TimeUnit.SECONDS)
        val secondSaved = second.get(5, TimeUnit.SECONDS)

        assertTrue(firstSaved != secondSaved)
        val expectedTokens = if (firstSaved) {
            "access-first" to "refresh-first"
        } else {
            "access-second" to "refresh-second"
        }
        assertEquals(expectedTokens, storage.getTokens())
    }

    @Test
    fun refreshNetworkFailurePreservesTokens() = runBlocking {
        api.onRefresh = { throw IOException("Backend unavailable") }

        assertThrows(IOException::class.java) {
            authenticator().authenticate(null, unauthorized())
        }
        assertEquals("access-old" to "refresh-old", storage.getTokens())
    }

    @Test
    fun loginPersistsSessionThatCanBeRestoredFromDisk() = runBlocking {
        AuthRepository(api, storage).login("test@example.test", "test-password")
        dataStoreJob.cancelAndJoin()
        val restoredJob = SupervisorJob()
        try {
            val restored = TokenStorage(
                PreferenceDataStoreFactory.create(
                    scope = CoroutineScope(restoredJob + Dispatchers.IO),
                    produceFile = { temporaryFolder.root.resolve("auth.preferences_pb") }
                )
            )
            assertTrue(restored.hasStoredSession())
            assertEquals("access-login" to "refresh-login", restored.getTokens())
        } finally {
            restoredJob.cancelAndJoin()
        }
    }

    @Test
    fun protectedHttpRequestSendsBearerAndRetriesWithRotatedToken() = runBlocking {
        api.onRefresh = { AuthTokenResponseDTO("access-rotated", "refresh-rotated") }

        val status = protectedRequest(
            statusCodes = listOf(401, 200),
            expectedAuthorization = listOf("Authorization: Bearer access-old", "Authorization: Bearer access-rotated")
        )

        assertEquals(200, status)
        assertEquals(1, api.refreshCalls.get())
        assertEquals("access-rotated" to "refresh-rotated", storage.getTokens())
    }

    @Test
    fun asynchronousProtectedRequestReportsRefreshFailureInsteadOfOriginal401() = runBlocking {
        api.onRefresh = {
            throw HttpException(retrofit2.Response.error<Any>(503, "Unavailable".toResponseBody()))
        }

        val exception = assertThrows(ExecutionException::class.java) {
            protectedRequest(
                statusCodes = listOf(401),
                expectedAuthorization = listOf("Authorization: Bearer access-old")
            )
        }

        assertTrue(exception.cause is IOException)
        assertEquals(503, (exception.cause?.cause as HttpException).code())
        assertEquals("access-old" to "refresh-old", storage.getTokens())
    }

    @Test
    fun refreshServerFailurePreservesTokensAndPropagatesCommunicationError() = runBlocking {
        api.onRefresh = {
            throw HttpException(retrofit2.Response.error<Any>(503, "Unavailable".toResponseBody()))
        }

        val exception = assertThrows(IOException::class.java) {
            authenticator().authenticate(null, unauthorized())
        }
        assertEquals(503, (exception.cause as HttpException).code())
        assertEquals("access-old" to "refresh-old", storage.getTokens())
    }

    @Test
    fun refreshCancellationIsPropagatedWithoutClearingTokens() = runBlocking {
        api.onRefresh = { throw CancellationException("Cancelled") }

        assertThrows(CancellationException::class.java) {
            authenticator().authenticate(null, unauthorized())
        }
        assertEquals("access-old" to "refresh-old", storage.getTokens())
    }

    @Test
    fun logoutClearsLocalSessionEvenWhenCallerIsAlreadyCancelled() = runBlocking {
        api.onLogout = { throw AssertionError("Cancelled logout must not start remote revocation") }
        var cancellationPropagated = false
        val logout = launch {
            currentCoroutineContext().cancel()
            try {
                AuthRepository(api, storage).logout()
            } catch (exception: CancellationException) {
                cancellationPropagated = true
                throw exception
            }
        }
        logout.join()

        assertTrue(cancellationPropagated)
        assertEquals(null to null, storage.getTokens())
        assertFalse(storage.hasStoredSession())
    }

    @Test
    fun secondUnauthorizedResponseDoesNotRefreshAgain() {
        val response = unauthorized().newBuilder()
            .priorResponse(unauthorized())
            .build()

        assertNull(authenticator().authenticate(null, response))
        assertEquals(0, api.refreshCalls.get())
    }

    private fun authenticator() = TokenAuthenticator(api, storage)

    private fun protectedRequest(statusCodes: List<Int>, expectedAuthorization: List<String>): Int {
        ServerSocket(0).use { server ->
            server.soTimeout = 5_000
            val incoming = workers.submit<List<String>> {
                statusCodes.map { status ->
                    server.accept().use { socket ->
                        socket.soTimeout = 5_000
                        val reader = socket.getInputStream().bufferedReader()
                        val headers = generateSequence { reader.readLine() }
                            .takeWhile { it.isNotEmpty() }.toList()
                        socket.getOutputStream().write(
                            ("HTTP/1.1 $status Test\r\n" +
                                "Content-Type: application/json\r\n" +
                                "WWW-Authenticate: Bearer\r\n" +
                                "Content-Length: 2\r\n" +
                                "Connection: close\r\n\r\n{}").toByteArray()
                        )
                        headers.single { it.startsWith("Authorization:") }
                    }
                }
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(storage))
                .authenticator(authenticator())
                .build()
            try {
                val result = CompletableFuture<Int>()
                val request = Request.Builder().url("http://127.0.0.1:${server.localPort}/home").build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        result.completeExceptionally(e)
                    }
                    override fun onResponse(call: Call, response: Response) {
                        response.use { result.complete(it.code) }
                    }
                })
                return result.get(5, TimeUnit.SECONDS)
            } finally {
                try {
                    assertEquals(expectedAuthorization, incoming.get(5, TimeUnit.SECONDS))
                } finally {
                    client.dispatcher.executorService.shutdown()
                    client.connectionPool.evictAll()
                }
            }
        }
    }

    private fun unauthorized(accessToken: String = "access-old"): Response =
        Response.Builder()
            .request(
                Request.Builder()
                    .url("http://localhost/home")
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            )
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

    private fun unauthorizedException() = HttpException(
        retrofit2.Response.error<Any>(401, "Rejected".toResponseBody())
    )

    private fun await(latch: CountDownLatch) {
        assertTrue("Timed out waiting for authentication operation", latch.await(5, TimeUnit.SECONDS))
    }

    private class FakeAuthApi : AuthApiService {
        val refreshCalls = AtomicInteger()
        var onRefresh: suspend (RefreshTokenRequestDTO) -> AuthTokenResponseDTO = {
            throw AssertionError("Unexpected refresh")
        }
        var onLogout: suspend (RefreshTokenRequestDTO) -> Unit = {}

        override suspend fun login(request: LoginRequestDTO) =
            AuthTokenResponseDTO("access-login", "refresh-login")

        override suspend fun refresh(request: RefreshTokenRequestDTO): AuthTokenResponseDTO {
            refreshCalls.incrementAndGet()
            return onRefresh(request)
        }

        override suspend fun logout(request: RefreshTokenRequestDTO) {
            onLogout(request)
        }
    }
}
