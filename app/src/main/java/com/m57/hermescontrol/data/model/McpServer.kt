package com.m57.hermescontrol.data.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class McpServersReplaceRequest(
    val servers: Map<String, JsonElement>,
    val profile: String? = null,
)

/** Preserve complete saved definitions, including unknown fields and credential references. */
fun replaceMcpEnvValue(
    config: Map<String, JsonElement>,
    serverName: String,
    key: String,
    value: String?,
    profile: String? = null,
): McpServersReplaceRequest {
    val servers = config["mcp_servers"] as? JsonObject ?: error("Saved MCP configuration is unavailable")
    val server =
        servers[serverName] as? JsonObject ?: error(
            "Server is not editable in this profile's saved configuration",
        )
    val envValue = server["env"]
    val env =
        if (envValue == null) {
            emptyMap()
        } else {
            (
                envValue as? JsonObject ?: error(
                    "Saved MCP environment is invalid",
                )
            )
        }
    val updatedEnv = env.toMutableMap()
    if (value == null) updatedEnv.remove(key) else updatedEnv[key] = JsonPrimitive(value)
    val updatedServer = JsonObject(server + ("env" to JsonObject(updatedEnv)))
    return McpServersReplaceRequest(servers + (serverName to updatedServer), profile)
}

@Serializable
data class McpServersResponse(
    val servers: List<McpServer>,
)

@Serializable
data class McpServer(
    val name: String,
    val transport: String? = null,
    val url: String? = null,
    val command: String? = null,
    val args: List<String>? = null,
    val env: Map<String, String>? = null,
    val enabled: Boolean,
    val status: String? = null,
    val error: String? = null,
    val auth: String? = null,
)

@Serializable
data class McpServerToggleRequest(
    val enabled: Boolean,
)

@Serializable
data class AddMcpServerRequest(
    val name: String,
    val url: String? = null,
    val command: String? = null,
    val args: List<String>? = null,
    val env: Map<String, String>? = null,
    val auth: String? = null, // "none" | "header" | "oauth"
    val bearerToken: String? = null, // sent only when auth == "header"
)

@Serializable
data class McpCatalogResponse(
    val entries: List<McpCatalogEntry>,
)

@Serializable
data class McpCatalogEntry(
    val name: String,
    val description: String? = null,
    val source: String? = null,
    val url: String? = null,
    val command: String? = null,
    val args: List<String>? = null,
    val env: List<McpCatalogEnvVar>? = null,
)

@Serializable
data class McpCatalogEnvVar(
    val key: String,
    val label: String? = null,
    val description: String? = null,
    val required: Boolean = false,
)

@Serializable
data class McpCatalogInstallRequest(
    val name: String,
    val env: Map<String, String>? = null,
)

@Serializable
data class McpOAuthFlowResponse(
    val flowId: String,
    val serverName: String,
    val status: String,
    val authorizationUrl: String? = null,
    val error: String? = null,
)
