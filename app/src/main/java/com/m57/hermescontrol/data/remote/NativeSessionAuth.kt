package com.m57.hermescontrol.data.remote

import com.m57.hermescontrol.data.local.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/** Native tokens are separate from the single-use WebSocket ticket and legacy cookie session. */
object NativeSessionAuth {
    private val client = NativeAuthClient()

    @Volatile var cancelPendingLogin: (() -> Unit)? = null

    fun hasSession(): Boolean = runCatching { AuthManager.getNativeSession() != null }.getOrDefault(false)

    fun cancelRefresh() {
        client.cancelRequests()
        cancelPendingLogin?.invoke()
    }

    @Synchronized
    fun accessToken(
        forceRefresh: Boolean = false,
        rejectedToken: String? = null,
    ): String? {
        val profileId = AuthManager.getSelectedProfileId()
        val revision = AuthManager.getNativeAuthRevision()
        val session = AuthManager.getNativeSession() ?: return null
        if (rejectedToken != null && session.accessToken != rejectedToken) return session.accessToken
        if (!forceRefresh && session.expiresAt > System.currentTimeMillis() / 1000 + 60) return session.accessToken
        if (session.refreshToken.isBlank()) {
            if (!forceRefresh && session.expiresAt > System.currentTimeMillis() / 1000) return session.accessToken
            AuthManager.replaceNativeSession(profileId, session, null, revision)
            return null
        }
        return try {
            val next =
                runBlocking(Dispatchers.IO) {
                    client.refresh(ServerEndpoint.parseForBuild(session.baseUrl), session)
                }
            if (AuthManager.replaceNativeSession(profileId, session, next, revision)) next.accessToken else null
        } catch (e: NativeAuthFailure) {
            if (e.statusCode == 400 || e.statusCode == 401) {
                AuthManager.replaceNativeSession(profileId, session, null, revision)
                return null
            }
            throw IOException("Native session refresh failed (HTTP ${e.statusCode}).")
        } catch (_: Exception) {
            throw IOException("Native session refresh unavailable. Retry the connection.")
        }
    }
}

object NativeBearerInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!NativeSessionAuth.hasSession()) return chain.proceed(chain.request())
        val endpoint = AuthManager.endpointForBuild().baseUrl
        val profileId = AuthManager.getSelectedProfileId()

        fun unchanged(): Boolean =
            profileId == AuthManager.getSelectedProfileId() &&
                endpoint == AuthManager.endpointForBuild().baseUrl
        val request = chain.request()
        if (request.url.scheme != endpoint.scheme || request.url.host != endpoint.host ||
            request.url.port != endpoint.port ||
            !request.url.encodedPath.startsWith(endpoint.encodedPath)
        ) {
            return chain.proceed(request)
        }
        val token = NativeSessionAuth.accessToken() ?: return chain.proceed(request)
        if (!unchanged()) throw IOException("Connection profile changed during authentication.")
        val response =
            chain.proceed(
                request.newBuilder().header("Authorization", "Bearer $token").removeHeader("Cookie").build(),
            )
        if (response.code != 401) return response
        val refreshed =
            try {
                NativeSessionAuth.accessToken(true, token)
            } catch (
                e: IOException,
            ) {
                response.close()
                throw e
            }
        if (refreshed == null) return response
        if (!unchanged()) {
            response.close()
            throw IOException("Connection profile changed during authentication.")
        }
        response.close()
        return chain.proceed(
            request.newBuilder().header("Authorization", "Bearer $refreshed").removeHeader("Cookie").build(),
        )
    }
}
