# REST compatibility audit

Audit date: 2026-10-03. Upstream source: NousResearch/hermes-agent at
[`bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1`](https://github.com/NousResearch/hermes-agent/tree/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1).
This records the Android route declarations before the route corrections in this review.
It is a source comparison, not a live dashboard integration test.

## Scope and method

Compared all 169 Retrofit HTTP annotations (168 unique method/path pairs) in
`HermesApiService.kt`, including PATCH and `@HTTP`, against every Python module in
`hermes_cli/web_routers/` at the pinned commit. Parsed Python decorators with the AST,
normalized path parameter names/converters, and checked the actual router mounting
in [`web_server.py`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_server.py#L981).
Comments were excluded from the Android annotation scan.

Ten initially unmatched plugin annotations were then verified against the bundled
Kanban and achievements plugin routers and their dynamic mounting prefix.
**164 declarations / 163 unique pairs have matching source routes. Five unique core
method/path pairs have no matching route in the inspected mounted router set.**
The unmatched routes were checked against complete owning router files rather than
treated as findings solely because a regex did not match.

## Confirmed core mismatches

| Android declaration | Pinned upstream evidence | Impact and correction direction |
| --- | --- | --- |
| `GET /api/sessions/{id}/prompt` | The complete [sessions router](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/sessions.py) has no prompt endpoint. [Session detail](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/sessions.py#L524) returns the database session row. | `getSessionPrompt` has no main-source caller in this checkout, so this is a dormant declaration. Remove it or deliberately map the detail row's `system_prompt` into the existing response model; do not advertise a prompt route. |
| `POST /api/profiles/{name}/clone` | [Profile creation](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/profiles.py#L803) uses `POST /api/profiles`; [ProfileCreate](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_models.py#L429) accepts `name`, `clone_from`, `clone_all`, and opt-in `clone_channels`. No per-name clone route exists. | `ProfilesViewModel.cloneProfile` calls the absent route from the clone dialog. Send the new name and source via profile creation, preserving clone depth semantics. Keep messaging credential cloning opt-in as upstream defines it. |
| `PUT /api/mcp/servers/{name}` | The complete [MCP router](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/mcp.py) has no per-server PUT. [PUT /api/mcp/servers](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/mcp.py#L160) **replaces the entire server map**, using `{servers, profile}`. | `McpServersViewModel` calls the missing endpoint for environment updates and removal. Do not substitute the collection replacement endpoint with a partial map: that can remove unrelated servers. Assess a scoped config merge for updates; explicit deletion needs a contract that actually removes a key. |
| `POST /api/mcp/servers/{name}/restart` | No restart endpoint exists in the complete [MCP router](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/mcp.py); test, auth, enable, delete, catalog and collection operations are explicit registrations. | `McpServersViewModel.restartServer` calls the absent route. Use a verified gateway RPC if one offers the intended operation, or mark the action unsupported. Do not present a connectivity test as a server restart. |
| `DELETE /api/messaging/platforms/{platform_id}` | The [messaging router](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/messaging.py#L876) registers PUT and test POST, but no platform DELETE. PUT accepts `enabled`, `env`, `clear_env`, and `profile`; it validates each environment key against the platform catalog. | `ChannelsViewModel.removePlatform` calls the absent route. Implement deliberate disable/credential-clear behavior through PUT using the selected platform's allowed keys and profile. Report partial failures; an omitted/blank environment value means keep, not delete. |

These are endpoint mismatches, not claims about an observed HTTP status. Auth
middleware, proxies and SPA handling can affect the response for an unmatched path.

## Corrections implemented in this review

After the route corrections, an independent re-scan found **166 HTTP annotations /
164 unique method/path pairs, all matching the pinned source registrations**.

- Removed the unused session-prompt endpoint and response DTO; session detail now
  exposes nullable `system_prompt` from the existing detail contract.
- Profile cloning uses `POST /api/profiles` with `name` and `clone_from`. The profile
  builder also aligns `no_skills` with the upstream boolean and `keep_skills` with
  the upstream list of names.
- MCP environment mutations read saved config with `include_defaults=false`, keep
  the complete server map and unknown JSON fields, and use the actual collection
  PUT endpoint. A local mutex serializes these mutations, profile changes abort
  the write, and missing or plugin-managed servers are refused. This handles
  actual environment key removal, which a deep merge cannot guarantee.
- MCP restart is disabled with an explanation. The absent route and misleading
  restart-success path are removed.
- Channel disconnection fetches current catalog metadata, then sends scoped PUT
  with `enabled=false` and the platform's declared `clear_env` keys. The catalog
  reloads after success; the UI calls this operation Disconnect rather than
  claiming to delete a built-in catalog entry.

The pinned [`_normalize_config_for_web`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_server_config.py#L529)
only normalizes model fields; it does not redact or transform `mcp_servers`.
Saved-config reads therefore preserve raw credential references such as `${VAR}`
when round-tripping the map. The collection replacement still has a **cross-client
read/write race**: a desktop or another client can change the map between Android's
read and write. The Android mutex cannot prevent that; upstream would need a
version/conditional write or atomic per-server mutation to eliminate it. Plugin
entries remain immutable through this operation.

## Critical route families with matching registrations

Counts below refer to the initial Android declarations, including the two PATCH
functions that share the session edit endpoint.

| Family | Matching declarations | Evidence and scope |
| --- | --- | --- |
| Sessions | 13 of 14 | [sessions.py](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/sessions.py) registers list/search/stats, paged messages, detail, PATCH rename/pin, delete, bulk delete, prune, empty cleanup and latest descendant. The unmatched declaration is the unused prompt endpoint above. |
| Config | 6 of 6 | [config_env.py](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/config_env.py) registers config read/update/defaults/schema; [analytics.py](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/analytics.py#L33) registers raw config read/write. |
| Model | 6 of 6 | [models.py](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/models.py) registers info, options, auxiliary models, MoA read/update and assignment. |
| Profiles | 12 of 13 | [profiles.py](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_routers/profiles.py) registers create/list/active, soul read/write, model/description writes, describe-auto, rename/delete and setup/open-terminal. The clone declaration differs as above. |
| MCP | 9 of 11 | The per-server update and restart paths differ as above. |
| Messaging | 7 of 8 | Platform list/config/test and Telegram onboarding routes match; platform removal differs as above. |

Chat generation, interrupt, session runtime resume and interactive requests are
primarily WebSocket JSON-RPC operations in this app. A successful REST route scan
does not validate those operations. See the [protocol compatibility review](hermes-compatibility-review.md).

## Plugin verification

All five Android Kanban annotations match the bundled
[Kanban router](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/plugins/kanban/dashboard/plugin_api.py):
board/boards GET, board switch POST, task PATCH and task POST. All five achievements
annotations match the bundled
[achievements router](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/plugins/hermes-achievements/dashboard/plugin_api.py).

The mount uses `/api/plugins/{plugin.name}` in
[`_mount_plugin_api_routes`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/web_server_dashboard.py#L878).
Availability remains conditional on discovery, loading and configuration of that
plugin on the actual gateway. Optional-plugin absence is not a core route regression.

## Limits and next verification

Matching a method/path does not prove request bodies, required fields, query
parameters, response DTOs, profile scoping, authorization, or destructive operation
semantics are compatible. Only the five mismatches' relevant handlers/models were
examined beyond registration. No authenticated dashboard was available for a live
REST test, and response payloads were not exhaustively compared.

The corrected annotation inventory has been re-scanned as recorded above.
The implementation adds request-contract tests for profile cloning, MCP mutations
and channel disable/credential removal; their execution belongs to the coordinated
Gradle validation for this review. A live
matrix should use both the pinned main commit and a separately pinned stable
release, with basic and OAuth auth, profile selection, reverse proxy base paths,
and optional plugins. Record exact tested versions before claiming full compatibility.
