# Validation results

Reviewed 2026-10-03. Android build/test results are pending the final coordinated run.

## Live self-hosted transport smoke

A dedicated Hermes backend was started with a temporary HERMES_HOME, generated test-only password,
and no model generation or changes to the user's agent configuration. Runtime commit:
`10c6188de188871f64a88dd95bc6b262adb0c307` (installed Hermes reports v0.21.5, release date 2026.9.24).
This is a separate target from the source audit at `bed0d535556b5b0a2bdd6fd3a74162ad5f860ca1`.

Passed: public status/provider discovery, unauthorized protected config returning 401,
password login and session cookie, authenticated config, single-use WebSocket ticket,
gateway.ready, client.capabilities, session.list, and commands.catalog.

This test used a Python HTTP/WebSocket client against the real backend. It does not establish
Android-to-backend end-to-end behavior, hosted identity-provider login, or model/tool execution.
Real Nous Portal and self-hosted OIDC sign-in on a physical Android device remain unverified.
