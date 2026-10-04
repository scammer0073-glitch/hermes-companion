package com.m57.hermescontrol.data.remote

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.Closeable
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketTimeoutException
import java.security.MessageDigest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** A temporary RFC 8252 listener. Never binds a LAN interface or emits credentials to the browser. */
class NativeLoopbackListener(private val state: String) : Closeable {
    private val server = ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"))
    private val path = "/native-callback/${NativeAuthClient.randomSecret()}"
    val redirectUri: String = "http://127.0.0.1:${server.localPort}$path"

    suspend fun awaitCode(timeoutMillis: Long = 600000): String =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { close() }
            Thread({
                val deadline = System.nanoTime() + timeoutMillis * 1000000
                try {
                    server.soTimeout = 1000
                    while (continuation.isActive && System.nanoTime() < deadline) {
                        val socket =
                            try {
                                server.accept()
                            } catch (_: SocketTimeoutException) {
                                continue
                            }
                        try {
                            socket.use {
                                it.soTimeout = 2000
                                val input = it.getInputStream()
                                val bytes = java.io.ByteArrayOutputStream()
                                while (bytes.size() < 8192) {
                                    val next = input.read()
                                    if (next == -1) break
                                    bytes.write(next)
                                    if (bytes.size() >= 4 && bytes.toString("US-ASCII").endsWith("\r\n\r\n")) break
                                }
                                val request = bytes.toString("US-ASCII")
                                val code = validateRequest(request, redirectUri, state)
                                val status = if (code != null) "200 OK" else "400 Bad Request"
                                val message =
                                    if (code != null) {
                                        "Sign-in received. Return to Hermes Companion."
                                    } else {
                                        "Invalid callback."
                                    }
                                try {
                                    it.getOutputStream().write(
                                        (
                                            "HTTP/1.1 $status\r\nContent-Type: text/plain\r\n" +
                                                "Cache-Control: no-store\r\n" +
                                                "Connection: close\r\nContent-Length: ${message.length}\r\n\r\n$message"
                                        )
                                            .toByteArray(Charsets.US_ASCII),
                                    )
                                } catch (_: IOException) {
                                    // A browser closing its response must not discard a validated callback.
                                }
                                if (code != null && continuation.isActive) {
                                    continuation.resume(code)
                                    return@Thread
                                }
                            }
                        } catch (_: IOException) {
                            // Ignore incomplete/slow local requests; keep waiting for the real browser callback.
                            continue
                        }
                    }
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            SocketTimeoutException("Sign-in timed out."),
                        )
                    }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                } finally {
                    close()
                }
            }, "hermes-native-callback").apply {
                isDaemon = true
                start()
            }
        }

    override fun close() {
        server.close()
    }

    companion object {
        internal fun validateRequest(
            request: String,
            redirectUri: String,
            expectedState: String,
        ): String? {
            if (!request.endsWith("\r\n\r\n") || request.length > 8192) return null
            val lines = request.split("\r\n")
            val first = lines.first().split(' ')
            if (first.size != 3 || first[0] != "GET" || first[2] !in listOf("HTTP/1.1", "HTTP/1.0")) return null
            if (!first[1].startsWith('/') || first[1].startsWith("//")) return null
            val expected = redirectUri.toHttpUrlOrNull() ?: return null
            val host = lines.filter { it.startsWith("Host:", true) }.map { it.substringAfter(':').trim() }
            if (host != listOf("127.0.0.1:${expected.port}")) return null
            val received = ("http://127.0.0.1:${expected.port}" + first[1]).toHttpUrlOrNull() ?: return null
            if (received.encodedPath != expected.encodedPath || received.fragment != null) return null
            if (received.queryParameterValues(
                    "state",
                ).size != 1 || received.queryParameterValues("code").size != 1
            ) {
                return null
            }
            if (received.queryParameter("error") != null) return null
            val actualState = received.queryParameter("state") ?: return null
            if (!MessageDigest.isEqual(actualState.toByteArray(), expectedState.toByteArray())) return null
            return received.queryParameter("code")?.takeIf { it.isNotBlank() && it.length <= 1024 }
        }
    }
}
