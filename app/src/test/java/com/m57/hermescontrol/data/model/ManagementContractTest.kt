package com.m57.hermescontrol.data.model

import com.m57.hermescontrol.data.remote.OkHttpProvider
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManagementContractTest {
    @Test
    fun mcpEnvRemovalPreservesOtherServersUnknownFieldsAndCredentialReferences() {
        val config =
            OkHttpProvider.json.parseToJsonElement(
                """
                {"mcp_servers":{
                  "alpha":{"command":"python","args":["server.py"],"enabled":false,
                    "headers":{"Authorization":"Bearer TOKEN_REF"},"unknown":{"keep":1},
                    "env":{"REMOVE":"old","KEEP":"ENV_REF"}},
                  "beta":{"url":"https://example.test","extra":true}
                }}
                """.trimIndent(),
            ) as JsonObject
        val replacement = replaceMcpEnvValue(config, "alpha", "REMOVE", null, "work")
        val alpha = replacement.servers["alpha"] as JsonObject
        assertEquals("work", replacement.profile)
        assertEquals(config.getValue("mcp_servers").let { it as JsonObject }["beta"], replacement.servers["beta"])
        val original = (config["mcp_servers"] as JsonObject)["alpha"] as JsonObject
        for (key in listOf("command", "args", "enabled", "headers", "unknown")) assertEquals(original[key], alpha[key])
        val env = alpha["env"] as JsonObject
        assertFalse(env.containsKey("REMOVE"))
        assertEquals(JsonPrimitive("ENV_REF"), env["KEEP"])
        val added = replaceMcpEnvValue(config, "alpha", "NEW", "value")
        assertEquals(JsonPrimitive("value"), ((added.servers["alpha"] as JsonObject)["env"] as JsonObject)["NEW"])
    }

    @Test(expected = IllegalStateException::class)
    fun missingSavedMcpServerFailsRatherThanReplacingTheMap() {
        replaceMcpEnvValue(mapOf("mcp_servers" to JsonObject(emptyMap())), "plugin-server", "KEY", "value")
    }

    @Test
    fun disconnectClearsOnlyThePlatformsDeclaredCredentialKeys() {
        val platform =
            MessagingPlatform(
                id = "telegram",
                name = "Telegram",
                enabled = true,
                configured = true,
                envVars =
                    listOf(
                        EnvVarField("TELEGRAM_BOT_TOKEN", true, true, isPassword = true),
                        EnvVarField("TELEGRAM_ALLOWED_USERS", false, true, isPassword = false),
                    ),
            )
        val request = platform.disconnectRequest("work")
        assertEquals(false, request.enabled)
        assertEquals(listOf("TELEGRAM_BOT_TOKEN", "TELEGRAM_ALLOWED_USERS"), request.clearEnv)
        assertTrue(request.env.isEmpty())
        assertEquals("work", request.profile)
    }
}
