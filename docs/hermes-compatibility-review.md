# Hermes compatibility review

Reviewed on 2026-10-03 against Hermes upstream main commit `bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1`. The release identified during this review is `v2026.9.24`; main and a release are separate compatibility targets. These findings come from source inspection, not a live Android-to-Hermes integration run. They do not establish full compatibility with either target.

## Fixed: current browser authentication provider names

The app checked only `oauth` when interpreting `/api/status.auth_providers`. Current provider implementations identify themselves as [`nous`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/plugins/dashboard_auth/nous/__init__.py#L43) and [`self-hosted`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/plugins/dashboard_auth/self_hosted/__init__.py#L79). Both therefore fell through to a username/password screen and a request with `provider=basic`.

`AuthLoginViewModel.deriveAuthMode` now recognizes both current names and retains the legacy `oauth` name. Tests cover each current provider alone and with `basic`. Browser providers use the newly implemented native authorization flow when the gateway advertises it; unsupported gateways receive an explicit error. Unknown provider names still use the existing basic fallback, which should eventually be replaced by provider discovery or an explicit unsupported-provider state.

## Fixed: server-to-client JSON-RPC requests

Current upstream [`server_requests.py`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/tui_gateway/server_requests.py) sends request frames containing `id`, `method` and `params`. The app's `EventParser.parse` classifies every frame with an `id` as an RPC result/error before inspecting its method. A request such as `{"jsonrpc":"2.0","id":"srq-example","method":"sudo","params":{"session_id":"s","command":"..."}}` would be interpreted as a result with a null value, so no password prompt appears.

The app also sends `clarify.respond`, `sudo.respond` and `secret.respond`. These names are absent from the inspected generated contract and are replaced in the inspected upstream request implementation by response frames using the original request ID. [`methods_prompt.py`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/tui_gateway/methods_prompt.py#L1108) offers `clarify.lock` for individual question locks and `request.answer` for answering a request whose original frame was not received.

Implemented changes:

1. Classify `method + id` as a server request before classifying RPC results.
2. Send response frames preserving the server's ID, bound to the current connection generation. Responses are never queued or payload-logged; duplicate and stale IDs are rejected. Parser exception logs omit input-bearing throwables.
3. Handle `clarify`, `approval`, `sudo`, `secret` and `request.cancel`. Batch clarify questions are shown sequentially, with answers indexed by `qid`.
4. Clear server prompt state on disconnect, replay authoritative `open_requests` from session resume, and reconcile cancellations by request ID. Approval buttons bind to the exact card, with command context visible.
5. Advertise `client.capabilities {server_requests:true}` on `gateway.ready`. Unsupported desktop window requests receive the upstream non-owner decline code `4404`, so Android cannot consume a desktop's pending preview/terminal request. Other unsupported methods receive an explicit error. Requests arriving without an attached chat consumer are declined.

Response shapes are defined in the pinned [`server request contracts`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/tui_gateway/contracts/server_requests.py): sudo/secret use `{value:...}`, approval uses `{choice:...}`, and clarify uses `{answers:{qid:answer}}`.

Remaining clarify limitations: the existing UI does not offer native multiple-selection controls for `multi_select` questions, and it does not issue `clarify.lock` for each intermediate answer. It collects a batch locally and submits the final answers together. Intermediate local answers are cleared on disconnect; upstream-provided locked answers are honored on replay. Tests exercise frame classification, exact response IDs, credential logging/queue protection, cancellation, batch answers, approval identity, and disconnect/replay. These are local automated checks, not live upstream integration certification.

## Implemented: native browser login and token lifecycle

The previous app explicitly blocked OAuth. Upstream provides a gateway-brokered native authorization flow: browser authorization with PKCE/state, a loopback redirect, code exchange, then rotating refresh tokens. Its current redirect validation accepts canonical HTTP loopback IP URLs, so an Android custom URI scheme cannot simply be substituted. See pinned [`auth routes`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/dashboard_auth/routes.py#L216).

The native implementation adds a loopback listener, system-browser authorization, state/PKCE verification, encrypted access/refresh-token storage, serialized refresh, expiry handling, and bearer-authenticated WS ticket minting. Current [`gated middleware`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/dashboard_auth/middleware.py#L154) accepts valid provider-issued bearer tokens. Session bearer tokens and legacy dashboard tokens are handled separately. Automated protocol tests do not establish that browser callbacks and Android lifecycle behavior work against a deployed Nous or self-hosted gateway; that device integration matrix remains necessary.

Existing cookie-authenticated basic login and per-reconnect WS ticket minting are conceptually aligned with the current [`ticket contract`](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/hermes_cli/dashboard_auth/ws_tickets.py): tickets are single use and expire after 30 seconds. This is source alignment, not runtime verification.

## Compatibility gate before claiming support

Pin the release tag and a main SHA separately in CI. Run an integration matrix covering token-only, basic-cookie, Nous and self-hosted OIDC login; reverse-proxy base paths; session create/resume/history; streaming and interrupted generation; attachments; approvals; multi-question clarification; secret/sudo requests; token rotation; WS reconnect and open-request replay. Compare called RPC names and parameters with the pinned [generated OpenRPC contract](https://github.com/NousResearch/hermes-agent/blob/bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1/apps/shared/src/gateway-contract.openrpc.json), then retain captured sanitized fixtures for Android parser tests.

The follow-up REST audit fixed profile cloning, MCP environment edits, messaging disconnection and stored-session prompt access, and disabled unsupported MCP restart controls. See `docs/rest-compatibility-audit.md` for pinned source evidence and remaining route coverage. Passing app unit tests backed by app-authored mock responses cannot substitute for upstream-backed integration checks.
