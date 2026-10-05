<h1 align="center">Hermes Companion</h1>
<p align="center"><strong>Community Android companion for Hermes Agent.</strong></p>

This repository is an independently maintained community fork of
[Hy4ri/hermes-mobile](https://github.com/Hy4ri/hermes-mobile), originally created by M57 (Hy4ri).
It is not an official Nous Research or Hermes Agent app. Upstream license and copyright credits are preserved.

<div align="center">
  <br>
  <img src="https://img.shields.io/badge/Android-34DDDD?style=for-the-badge&logo=android&logoColor=black" alt="Android"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Material%20You-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material You"/>
  <br><br>
</div>

<p align="center">
  <a href="https://github.com/scammer0073-glitch/hermes-companion/releases"><img src="https://img.shields.io/github/v/release/scammer0073-glitch/hermes-companion?color=6750A4&label=Fork%20Release&logo=github" alt="Fork Release"></a>
  <img src="https://img.shields.io/github/actions/workflow/status/scammer0073-glitch/hermes-companion/android.yml?branch=main&label=CI&logo=githubactions" alt="CI">
  <img src="https://img.shields.io/badge/minSdk-26-brightgreen" alt="minSdk 26">
  <img src="https://img.shields.io/badge/targetSdk-36-brightgreen" alt="targetSdk 36">
</p>

This fork has no published releases yet. Build from source using the instructions below;
future binaries will be listed on [this fork's releases page](https://github.com/scammer0073-glitch/hermes-companion/releases).
The inherited F-Droid listing and Obtainium source distribute the upstream app, not this fork.

---

## Overview

**Hermes Companion** is a community Android client for [Hermes Agent](https://hermes-agent.nousresearch.com). It connects to your local Hermes gateway (REST API and WebSocket TUI Gateway) over LAN, giving you pocket control over your AI assistant.

---

## Screenshots

<p align="center">
  <img src="docs/screenshots/chat.png" width="180" alt="Hermes Mobile chat screen" />
  <img src="docs/screenshots/cron.png" width="180" alt="Hermes Mobile cron jobs screen" />
  <img src="docs/screenshots/kanban.png" width="180" alt="Hermes Mobile Kanban screen" />
  <img src="docs/screenshots/skills.png" width="180" alt="Hermes Mobile skills screen" />
</p>

<p align="center"><em>Chat, automation, productivity, and agent configuration — from your phone.</em></p>

---

## Features

- **Real-Time Chat:** Message your agent with Room-backed local database history and inline reply notifications.
- **System Config:** Manage active profiles, installed skills, plugins, toolsets, and LLM model/provider selections.
- **Operations:** Stream and filter live logs, manage cron jobs, edit environment keys, test webhooks, and monitor processes.
- **Gateway Status:** Monitor WebSocket connection, MCP servers, messaging channels, and OAuth providers.
- **Productivity:** View and manage tasks via integrated Kanban boards, track agent milestones, and browse session history.
- **Analytics & Billing:** Usage analytics dashboard and billing/subscription management.
- **Theming:** 6 built-in color presets (Default, Monochrome, Gruvbox, Catppuccin, AMOLED, Neon Noir) plus Material You dynamic colors on supported devices.
- **Modern UX:** Native Material 3 design with pull-to-refresh, scroll-aware TopBar, and customizable bottom navigation.

---

## Quick Start

### Prerequisites

- **JDK 21+** (required for Kotlin compilation and the Gradle toolchain)
- **Android Studio** (Ladybug+) or a **Nix** development environment

### Build & Deploy

1. **Clone the repository:**
   ```bash
   git clone https://github.com/scammer0073-glitch/hermes-companion.git
   cd hermes-companion
   ```
2. **Build the debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```
3. **Install on your emulator/device:**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

_Note: For release builds, ensure keystore environment variables (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) are configured, or let the GitHub Actions release workflow handle it on tag push (`v*`)._

---

## Authentication

Once the app is installed, you need to point it at your Hermes gateway. The app auto-detects which auth mode the dashboard is using — just fill in the fields it shows.

### 1. Start the dashboard

On your host machine, start the dashboard:

```bash
hermes dashboard                          # loopback (127.0.0.1:9119) — no auth needed
hermes dashboard --host 0.0.0.0           # LAN — requires auth
```

For LAN access, configure credentials in `~/.hermes/config.yaml`:

```yaml
dashboard:
  basic_auth:
    username: admin # pick your own
    password: hermes # pick your own
```

### 2. Connect the app

Tap **Sign in** on the landing screen and enter the dashboard host and port. The app probes the dashboard and reveals the fields you need:

| Auth mode      | When                                 | What you fill                                                                                                                                                          |
| -------------- | ------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Token only** | Dashboard on same machine (loopback) | **Token** — grab from `~/.hermes/dashboard-token.txt` or `~/.hermes/.env` (`HERMES_DASHBOARD_SESSION_TOKEN`). The app can also auto-extract it from the dashboard page |
| **Basic auth** | Dashboard on LAN with password gate  | **Username** + **Password** (default `admin` / `hermes`). The app logs in, gets a session cookie, and mints a WebSocket ticket automatically                           |

> Cleartext HTTP is available for trusted local/VPN deployments. Use HTTPS and the appropriate OAuth provider for a public or hosted dashboard.

### Hosted and OAuth gateways

For a gateway using Nous Portal or Hermes's self-hosted OIDC provider, enter its **HTTPS dashboard URL**
(including any reverse-proxy path prefix), probe the connection, and choose **Sign in with browser**.
Complete authentication in your system browser and return to the app. The gateway must advertise
`native_pkce` in `/api/status`; update Hermes if this capability is unavailable. You can cancel the
pending sign-in, and it expires after ten minutes.

The native flow uses PKCE and state validation with a temporary callback bound only to
`127.0.0.1`. Gateway bearer sessions and rotating refresh tokens are stored in encrypted preferences
for the specific connection profile and dashboard URL. REST calls and freshly minted WebSocket
tickets use that session; browser cookies are not copied into the app.

Self-hosted password login remains available on a trusted LAN/VPN. Hosted OAuth requires HTTPS.
The native broker implementation has contract/security tests; a complete Android device + real
Nous Portal sign-in still needs verification before claiming release-tested hosted support.
See the [official native sign-in contract](https://github.com/NousResearch/hermes-agent/blob/main/website/docs/guides/desktop-native-signin.md).

### Connection profiles

Have multiple gateways? Switch between them in **Settings → Connection profiles**. Each profile stores its own host, port, and token — just tap to swap.

### Pairing (admin)

The **Pairing** screen lets you approve or revoke agents and services that are trying to connect to your gateway, such as Telegram or Discord sessions.

---

## Project Structure

```
app/src/main/java/com/m57/hermescontrol/
├── data/          # Local (Room, AuthManager), Remote (Retrofit, OkHttp), WS (WebSocket), Models
├── notification/  # Foreground service + inline reply for chat notifications
├── theme/         # Preset-based design system (6 themes), status colors, spacing, typography
├── ui/            # 28 Compose feature screens + common components (HermesScaffold, StateViews)
├── util/          # CronExpressionFormatter, LocaleContextWrapper
└── Navigation*.kt # Navigation3 wiring, keys, screen registry, controller
```

---

## Tech Stack

- **Language:** Kotlin 2.4.10 with KSP 2.3.10 compiler plugin
- **UI & Layout:** Jetpack Compose (BOM 2026.03.01) & Material 3 / Material You
- **Navigation:** Navigation3 (Compose-first Routing)
- **Networking:** Retrofit 3.0.0, OkHttp 5.4.0, Kotlinx Serialization 1.11.0
- **Database:** Room 2.7.1 with SQLCipher 4.17.0 encryption
- **Security:** `EncryptedSharedPreferences` (AES256-GCM), DataStore
- **Theming:** 6 built-in presets (Default, Monochrome, Gruvbox, Catppuccin, AMOLED, Neon Noir) + Material You dynamic colors
- **Image Loading:** Coil 2.7.0
- **Testing:** JUnit 5, MockK, Turbine, Espresso, Compose UI testing
- **Formatting:** `ktlint` 1.2.1 style rules (checked automatically in CI)

---

## Release versioning

Signed releases use stable tags `vMAJOR.MINOR.PATCH`, without leading zeros or prerelease suffixes.
Minor and patch components must be between 0 and 999. The release workflow validates the tag
before reading signing credentials and sets `versionCode = MAJOR * 1000000 + MINOR * 1000 + PATCH`.
Codes must be positive and at most 2100000000. For example, `v1.99.0` produces 1099000 and
`v2.0.0` produces 2000000, so a major upgrade keeps the Android version code increasing.
Maintain semantic version order when publishing; this mapping does not authorize a rollback.

Check the mapping locally with `python -m unittest discover -s scripts/tests -p test_release_version.py`.
CI runs those tests on pull requests. A signed release still requires the configured keystore secrets.

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for our branch workflow, code style guidelines, and PR checklist.

For developer-specific details, code conventions, and project architecture notes, refer to [AGENTS.md](AGENTS.md).

See the [Hermes compatibility review](docs/hermes-compatibility-review.md) for current protocol findings
and the [product and recognition plan](docs/product-and-recognition-plan.md) for improvement priorities
and the route toward upstream collaboration.

---

## License

Copyright © 2026 M57 (Hy4ri).

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
