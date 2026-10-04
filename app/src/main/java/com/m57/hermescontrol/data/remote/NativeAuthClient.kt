package com.m57.hermescontrol.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import okhttp3.CookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@Serializable
class NativeAuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val provider: String,
    val baseUrl: String,
) {
    override fun toString(): String = "NativeAuthSession(redacted)"
}

class NativeAuthFailure(val statusCode: Int) : Exception("Native authentication failed (HTTP $statusCode).")

class NativeAuthClient(
    private val client: OkHttpClient =
        OkHttpClient.Builder().cookieJar(CookieJar.NO_COOKIES)
            .followRedirects(
                false,
            ).followSslRedirects(false).callTimeout(30, java.util.concurrent.TimeUnit.SECONDS).build(),
) {
    fun cancelRequests() {
        client.dispatcher.cancelAll()
    }

    suspend fun exchange(
        endpoint: ServerEndpoint,
        code: String,
        verifier: String,
    ): NativeAuthSession =
        post(
            endpoint,
            "auth/native/token",
            buildJsonObject {
                put("code", code)
                put("code_verifier", verifier)
            }.toString(),
        )

    suspend fun refresh(
        endpoint: ServerEndpoint,
        session: NativeAuthSession,
    ): NativeAuthSession =
        post(
            endpoint,
            "auth/native/refresh",
            buildJsonObject {
                put("refresh_token", session.refreshToken)
                put("provider", session.provider)
            }.toString(),
        )

    private suspend fun post(
        endpoint: ServerEndpoint,
        path: String,
        body: String,
    ): NativeAuthSession {
        val request =
            Request.Builder().url(endpoint.resolve(path))
                .post(body.toRequestBody("application/json".toMediaType())).build()
        return client.newCall(request).await().use { response ->
            if (!response.isSuccessful) throw NativeAuthFailure(response.code)
            parseSession(response.body.string(), endpoint.baseUrl.toString())
        }
    }

    companion object {
        fun randomSecret(): String =
            Base64.getUrlEncoder().withoutPadding()
                .encodeToString(ByteArray(32).also { SecureRandom().nextBytes(it) })

        fun challenge(verifier: String): String =
            Base64.getUrlEncoder().withoutPadding()
                .encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)))

        fun authorizeUrl(
            endpoint: ServerEndpoint,
            verifier: String,
            state: String,
            redirectUri: String,
        ): String =
            endpoint.resolve("auth/native/authorize").newBuilder()
                .addQueryParameter("code_challenge", challenge(verifier))
                .addQueryParameter("code_challenge_method", "S256")
                .addQueryParameter("state", state).addQueryParameter("redirect_uri", redirectUri).build().toString()

        internal fun parseSession(
            body: String,
            baseUrl: String,
        ): NativeAuthSession {
            val json = OkHttpProvider.json.parseToJsonElement(body).jsonObject

            fun field(name: String): String = json[name]?.jsonPrimitive?.contentOrNull.orEmpty()
            val token = field("access_token")
            val refresh = field("refresh_token")
            val provider = field("provider")
            val expiry = json["expires_at"]?.jsonPrimitive?.long ?: 0L
            require(token.isNotBlank() && provider.isNotBlank() && expiry > 0) { "Invalid native session response." }
            require(field("token_type").equals("Bearer", ignoreCase = true)) { "Unsupported native token type." }
            return NativeAuthSession(token, refresh, expiry, provider, baseUrl)
        }
    }
}
