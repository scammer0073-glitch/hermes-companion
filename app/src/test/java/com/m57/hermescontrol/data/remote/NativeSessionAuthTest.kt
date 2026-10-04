package com.m57.hermescontrol.data.remote

import com.m57.hermescontrol.data.local.AuthManager
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class NativeSessionAuthTest {
    private lateinit var server: MockWebServer
    private val stored = AtomicReference<NativeAuthSession?>()

    @Before
    fun setup() {
        server = MockWebServer().apply { start() }
        stored.set(NativeAuthSession("expired", "refresh-secret", 1, "nous", server.url("/").toString()))
        mockkObject(AuthManager)
        every { AuthManager.getSelectedProfileId() } returns "profile"
        every { AuthManager.getNativeAuthRevision() } returns 0
        every { AuthManager.getNativeSession() } answers { stored.get() }
        every { AuthManager.endpointForBuild() } returns
            ServerEndpoint.parse(
                server.url("/").toString(),
                CleartextPolicy.ALLOW_WITH_WARNING,
            )
        every { AuthManager.replaceNativeSession(any(), any(), any(), any()) } answers {
            stored.compareAndSet(secondArg(), thirdArg())
        }
    }

    @After
    fun teardown() {
        NativeSessionAuth.cancelRefresh()
        server.shutdown()
        unmockkAll()
    }

    @Test
    fun concurrentRefreshesRotateOnlyOnce() {
        server.enqueue(
            MockResponse().setBody(
                """{"access_token":"fresh","refresh_token":"rotated","expires_at":2000000000,""" +
                    """"provider":"nous","token_type":"Bearer"}""",
            ),
        )
        val pool = Executors.newFixedThreadPool(6)
        try {
            val results = pool.invokeAll(List(6) { Callable { NativeSessionAuth.accessToken() } })
            assertEquals(List(6) { "fresh" }, results.map { it.get(5, TimeUnit.SECONDS) })
            assertEquals(1, server.requestCount)
            assertEquals("rotated", stored.get()!!.refreshToken)
        } finally {
            pool.shutdownNow()
        }
    }

    @Test
    fun rejectedRefreshClearsSessionButTransientFailurePreservesIt() {
        server.enqueue(MockResponse().setResponseCode(503))
        assertTrue(runCatching { NativeSessionAuth.accessToken() }.isFailure)
        assertEquals("refresh-secret", stored.get()!!.refreshToken)
        server.enqueue(MockResponse().setResponseCode(401))
        assertNull(NativeSessionAuth.accessToken())
        assertNull(stored.get())
    }

    @Test
    fun refreshCannotRestoreLoggedOutOrSwitchedSession() {
        server.enqueue(
            MockResponse().setBody(
                """{"access_token":"fresh","refresh_token":"rotated","expires_at":2000000000,""" +
                    """"provider":"nous","token_type":"Bearer"}""",
            ),
        )
        every { AuthManager.replaceNativeSession(any(), any(), any(), any()) } answers {
            stored.set(null)
            false
        }
        assertNull(NativeSessionAuth.accessToken())
        assertNull(stored.get())
    }

    @Test
    fun rest401RotatesSessionAndRetriesWithFreshBearer() {
        stored.set(NativeAuthSession("live", "refresh-secret", 2000000000, "nous", server.url("/").toString()))
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(
            MockResponse().setBody(
                """{"access_token":"fresh","refresh_token":"rotated","expires_at":2000000000,""" +
                    """"provider":"nous","token_type":"Bearer"}""",
            ),
        )
        server.enqueue(MockResponse().setBody("ok"))
        val client = OkHttpClient.Builder().addInterceptor(NativeBearerInterceptor).followRedirects(false).build()
        client.newCall(Request.Builder().url(server.url("/api/sessions")).build()).execute().use {
            assertEquals(200, it.code)
        }
        assertEquals("Bearer live", server.takeRequest().getHeader("Authorization"))
        val rotation = server.takeRequest()
        assertEquals("/auth/native/refresh", rotation.path)
        assertNull(rotation.getHeader("Authorization"))
        assertEquals("Bearer fresh", server.takeRequest().getHeader("Authorization"))
        assertEquals(3, server.requestCount)
    }
}
