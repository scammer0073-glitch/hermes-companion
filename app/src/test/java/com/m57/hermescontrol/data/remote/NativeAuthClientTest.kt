package com.m57.hermescontrol.data.remote

import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeAuthClientTest {
    @Test
    fun pkceUsesKnownS256VectorAndPreservesProxyPrefix() {
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM", NativeAuthClient.challenge(verifier))
        val url =
            NativeAuthClient.authorizeUrl(
                ServerEndpoint.parse("https://gateway.example/hermes/"),
                verifier,
                "state",
                "http://127.0.0.1:1234/cb",
            ).toHttpUrl()
        assertEquals("/hermes/auth/native/authorize", url.encodedPath)
        assertEquals("S256", url.queryParameter("code_challenge_method"))
        assertEquals("http://127.0.0.1:1234/cb", url.queryParameter("redirect_uri"))
        assertFalse(url.toString().contains(verifier))
    }

    @Test
    fun callbackRejectsWrongStateHostPathMethodAndDuplicateParameters() {
        val redirect = "http://127.0.0.1:1234/callback"

        fun request(
            target: String,
            host: String = "127.0.0.1:1234",
            method: String = "GET",
        ) = "$method $target HTTP/1.1\r\nHost: $host\r\n\r\n"
        val valid = "/callback?state=expected&code=one-time"
        assertEquals("one-time", NativeLoopbackListener.validateRequest(request(valid), redirect, "expected"))
        assertNull(NativeLoopbackListener.validateRequest(request(valid), redirect, "wrong"))
        assertNull(NativeLoopbackListener.validateRequest(request(valid, "evil.example"), redirect, "expected"))
        assertNull(NativeLoopbackListener.validateRequest(request(valid, method = "POST"), redirect, "expected"))
        assertNull(
            NativeLoopbackListener.validateRequest(request(valid.replace("/callback", "/other")), redirect, "expected"),
        )
        assertNull(NativeLoopbackListener.validateRequest(request("$valid&state=expected"), redirect, "expected"))
        assertNull(NativeLoopbackListener.validateRequest(request("$valid&code=other"), redirect, "expected"))
        assertNull(NativeLoopbackListener.validateRequest(request("$valid&error=denied"), redirect, "expected"))
    }

    @Test
    fun realLoopbackListenerReceivesCodeAndCancellationClosesPort() =
        runBlocking {
            val listener = NativeLoopbackListener("expected")
            val result = async { listener.awaitCode(3000) }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                OkHttpClient().newCall(
                    Request.Builder().url("${listener.redirectUri}?state=expected&code=code").build(),
                )
                    .execute().use { assertEquals(200, it.code) }
            }
            assertEquals("code", withTimeout(5000) { result.await() })
            val cancelled = NativeLoopbackListener("expected")
            val pending = async { cancelled.awaitCode() }
            kotlinx.coroutines.yield()
            pending.cancelAndJoin()
            val failure =
                runCatching {
                    java.net.Socket("127.0.0.1", cancelled.redirectUri.toHttpUrl().port).close()
                }.isFailure
            assertTrue(failure)
        }

    @Test
    fun incompleteLocalRequestCannotAbortSubsequentBrowserCallback() =
        runBlocking {
            NativeLoopbackListener("expected").use { listener ->
                val result = async { listener.awaitCode(8000) }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    java.net.Socket("127.0.0.1", listener.redirectUri.toHttpUrl().port).use { partial ->
                        partial.getOutputStream().write("GET /unfinished HTTP/1.1\r\n".toByteArray())
                        partial.soTimeout = 4000
                        // The listener times out this connection after two seconds and keeps accepting.
                        assertEquals(-1, partial.getInputStream().read())
                    }
                    OkHttpClient().newCall(
                        Request.Builder().url("${listener.redirectUri}?state=expected&code=valid-code").build(),
                    ).execute().use { assertEquals(200, it.code) }
                }
                assertEquals("valid-code", withTimeout(5000) { result.await() })
            }
        }

    @Test
    fun exchangeAndRefreshFollowBearerContractWithoutCookiesOrRedirects() =
        runBlocking {
            MockWebServer().use { server ->
                server.start()
                val endpoint =
                    ServerEndpoint.parse(
                        server.url("/prefix/").toString(),
                        CleartextPolicy.ALLOW_WITH_WARNING,
                    )
                val body =
                    """{"access_token":"access","refresh_token":"rotate","expires_at":2000000000,""" +
                        """"provider":"nous","token_type":"Bearer"}"""
                server.enqueue(MockResponse().setBody(body))
                server.enqueue(MockResponse().setBody(body.replace("\"access\"", "\"new-access\"")))
                val client = NativeAuthClient()
                val session = client.exchange(endpoint, "one-time", "verifier")
                assertEquals("access", session.accessToken)
                assertFalse(session.toString().contains("rotate"))
                val exchange = server.takeRequest()
                assertEquals("/prefix/auth/native/token", exchange.path)
                assertEquals("""{"code":"one-time","code_verifier":"verifier"}""", exchange.body.readUtf8())
                assertNull(exchange.getHeader("Cookie"))
                val refreshed = client.refresh(endpoint, session)
                assertEquals("new-access", refreshed.accessToken)
                val refresh = server.takeRequest()
                assertEquals("/prefix/auth/native/refresh", refresh.path)
                assertEquals("""{"refresh_token":"rotate","provider":"nous"}""", refresh.body.readUtf8())
                server.enqueue(MockResponse().setResponseCode(302).addHeader("Location", server.url("/leak")))
                val failure =
                    runCatching {
                        client.exchange(
                            endpoint,
                            "secret-code",
                            "secret-verifier",
                        )
                    }.exceptionOrNull()
                assertTrue(failure is NativeAuthFailure)
                assertEquals(3, server.requestCount)
            }
        }
}
