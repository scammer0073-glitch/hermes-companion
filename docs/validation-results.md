# Validation results

Updated 2026-10-04.

## Android validation

[PR #13](https://github.com/scammer0073-glitch/hermes-companion/pull/13) was merged after all checks passed on commit `5dc954ac1a6064692e12820a0c3aa7806a7aa6c0`:
ktlint/color guard, Android Lint, unit/integration tests, instrumented tests, debug APK, release Kotlin compilation and CodeQL.
Evidence: [Android CI run](https://github.com/scammer0073-glitch/hermes-companion/actions/runs/37200273075),
[CodeQL run](https://github.com/scammer0073-glitch/hermes-companion/actions/runs/37200273082).
Local Windows `ktlintCheck` and `checkColorLiterals` also passed after resolving the MCP text-color merge conflict.

Dependency refreshes require compileSdk 37 for OkHttp 5.5 and CodeQL 2.27.1 for Kotlin 2.4.20.
The changes preserve minSdk 26 and targetSdk 36. Each dependency PR is checked before merge;
[PR #5](https://github.com/scammer0073-glitch/hermes-companion/pull/5) also combines the updates to verify their integration.

These automated checks do not establish live Android-to-Portal sign-in or a hosted desktop service.
See [upstream contributions](upstream-contributions.md) for submitted work and unverified gates.

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
