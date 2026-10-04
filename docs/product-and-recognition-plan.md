# Product quality and Hermes recognition plan

Reviewed on 2026-10-03. This is a proposal for the `scammer0073-glitch/hermes-companion` fork of [Hy4ri/hermes-mobile](https://github.com/Hy4ri/hermes-mobile), not evidence of Nous Research endorsement. Findings below describe the inspected checkout; some may be addressed by the accompanying repair branch. No outreach has been sent.

Implemented in this review branch: sleep logging now resolves validated 24-hour input into actual local timestamps, including overnight intervals, and the sleep chart aggregates seven calendar days rather than seven records. Invalid or incomplete sleep notes remain editable instead of becoming fabricated seven-hour records. Focused JVM tests cover time validation, overnight/daytime intervals, previous completed intervals and calendar-day aggregation. The table preserves the original findings for context; see the branch's validation results before treating these fixes as release-tested. Other proposals remain outstanding unless separately noted by the repair work.

The repair branch also updates fork README/distribution links and the release updater, repairs the server URL migration, recognizes current OAuth provider names, aligns stale instrumented tests with the current UI, and removes doubled scaffold padding from the personal-app list/detail screens. It implements gateway-brokered native OAuth through an external browser, loopback callback, PKCE/state validation, encrypted endpoint/profile-bound tokens, rotating refresh and bearer-authenticated REST/WebSocket tickets. Native sign-in requires HTTPS and a gateway advertising `native_pkce`; unsupported gateways explain that an update is needed. These changes still require a real Android/Portal sign-in test before a release-level OAuth claim. They do not establish an official endorsement. Remaining limitations include independent release signing/distribution verification, hardcoded private connection suggestions, personal-data persistence/privacy, ignored LLM parser output, and unverified hosting claims.

## What would make this useful to more people?

The strongest positioning is a dependable Android companion for an existing Hermes installation: quick connection, resilient chat, useful notifications, and safe administration. Those workflows deserve priority over additional screens or hosted-service marketing.

| Priority | Observed problem | Proposed improvement | Acceptance evidence |
| --- | --- | --- | --- |
| P0 | The reported Android workflow fails style and instrumented tests, so downstream builds are skipped. | Repair the actual failures; retain the checks and diagnose device failures from reports/logcat. | A fresh workflow run passes style, lint, unit tests, emulator tests, debug APK and release compilation. |
| P0 | README download badges, clone instructions, About link, and `AppUpdateChecker` target Hy4ri's repository, while the fork's installed app name is Nemasys. | Choose a clear fork identity; explain the upstream relationship; make download, support, update and signing ownership consistent. Label upstream installation links explicitly until fork distribution exists. | A new user can tell which app they install, who maintains it, where bugs belong, and where updates come from. |
| P0 | `HermesConnectionHelper` suggests private addresses `100.80.15.3` and `192.168.1.3`. Those hosts belong to one environment. | Use the user's saved profiles or explicit hostname entry. Make example hosts explanatory placeholders rather than runnable default targets. | A clean install never probes another person's hardcoded private host. |
| P0 | OAuth is explicitly unsupported in `AuthLoginViewModel`; provider detection originally checked `oauth`, while current Hermes docs show `nous`. | Correct provider classification and explain unsupported modes immediately. Design an approved browser-to-native session handoff with upstream before implementing OAuth. | `nous`, legacy `oauth`, `basic`, no-auth and unknown providers have tested outcomes; hosted sign-in is never advertised before an end-to-end test passes. |
| P1 | `PersonalAppsScreen` stores every parsed sleep entry as the previous seven hours, ignoring the parsed bed/wake values. Its seven-day chart takes seven records rather than seven calendar days. | Parse and validate actual local timestamps, including overnight ranges and dates; aggregate by calendar day; allow correction/deletion. | An entry of 23:30–06:45 records 7 h 15 min, survives restart, and appears on the correct day. |
| P1 | `PersonalAppParser.parseWithLLM` calls an LLM but discards its answer. Keyword calories are fixed guesses. | Either keep a clearly labelled local parser or validate structured LLM output and show an editable preview. Label estimated calories and preserve the original note. | Multiple quantities and unfamiliar foods do not silently receive an authoritative calorie value; unsupported input is editable. |
| P1 | Personal data is stored in ordinary SharedPreferences; persistence errors become an empty list, and mutation updates use read/copy/write. | Make data ownership, export/deletion and backup behavior explicit; use transactional persistence and show recoverable load errors. | Concurrent additions cannot lose entries; malformed storage does not look like a genuinely empty account. |
| P1 | New personal screens reapply `HermesScaffold` padding and use many literal colors; the repo documents both as recurring regressions. | Follow the scaffold contract and theme tokens; check dark/light themes, large fonts, TalkBack, and long translated labels. | No doubled top offset, invisible controls or clipped primary actions on supported devices. |
| P1 | The hub screen claims private provisioning in about 60 seconds and a hosted connection path, despite the native OAuth gap. | Publish hosted claims only after verifying provisioning, auth, support and data handling. Keep optional commercial hosting separate from self-hosted setup. | A fresh account can complete the advertised path; unavailable hosting is described honestly. |
| P2 | A large control surface can overwhelm first-time users. | Put Chat, Sessions and Connection in the primary path; group administration behind an advanced area; add server diagnostics with redacted export. | New users can connect and send a message without reading the developer guide. |

Evidence locations: `README.md`, `app/build.gradle.kts`, `ui/settings/components/AboutSection.kt`, `data/update/AppUpdateChecker.kt`, `core/connect/HermesConnectionHelper.kt`, `ui/authlogin/AuthLoginViewModel.kt`, `ui/personal/PersonalAppsScreen.kt`, `ui/personal/PersonalAppParser.kt`, `data/local/PersonalAppStore.kt`, `ui/hub/NemasysHubScreen.kt`, and `AGENTS.md` under `app/src/main/java/com/m57/hermescontrol/` where applicable. See `app/src/main/res/values/strings.xml` for the Nemasys app label.

## Compatibility claim and release discipline

“Works with latest Hermes” requires a running-backend check. Source comparison and mocked tests alone establish only part of the contract. Publish the exact Hermes tag and commit, dashboard launch mode, auth provider, Android version and test date for each verified release. Test the current stable release and a pinned main commit; retain the previous supported release as a regression target.

Recommended end-to-end paths are login, session list/resume, send/stream/cancel, reconnection after a network switch, server restart, expired authentication, profile switching, attachment retrieval, notification reply and logout. Disable unavailable features based on server capabilities and explain why. Keep a small changelog of compatibility fixes and unsupported modes.

Current official documentation distinguishes trusted-network password access from public hosting and describes cookie-backed authentication plus WebSocket tickets. [Hermes dashboard documentation](https://hermes-agent.nousresearch.com/docs/user-guide/features/web-dashboard). Hermes also documents a native PKCE broker that returns bearer sessions rather than browser cookies. The repair branch implements that contract with an ephemeral loopback listener; a completed device/Portal login remains the acceptance evidence for hosted support. [Official native sign-in contract](https://github.com/NousResearch/hermes-agent/blob/main/website/docs/guides/desktop-native-signin.md).

## Can this become official?

It is possible to propose it, but repository ownership, popularity and a working APK do not grant official status. No public automatic acceptance route or mobile-client endorsement criteria were established by this review. Nous Research maintainers would need to decide whether they want a community listing, an endorsed client, or a repository maintained under their organization. These are different requests.

Preserve M57/Hy4ri's existing attribution, license text and history. The mobile project uses Apache-2.0; Hermes core's contribution guide states MIT terms. Before proposing direct incorporation of mobile code, obtain maintainer guidance on attribution and licensing rather than silently relabeling inherited code. Request an explicit branding decision before describing the fork as an official Nous/Hermes product.

The official contribution guide prioritizes bug fixes, platform support, security and reliability. It asks for focused PRs with testing evidence and points design proposals to GitHub Discussions and the Nous Research Discord. Follow those published contribution practices when submitting upstream work. [Official contributing guide](https://hermes-agent.nousresearch.com/docs/developer-guide/contributing).

Suggested sequence, not official requirements:

1. Repair the fork's CI and prepare an installable, consistently branded release with reproducible compatibility evidence.
2. Send broadly useful Android fixes to Hy4ri/hermes-mobile in small PRs, following that repository's `CONTRIBUTING.md`. Keep personal hosting and tracker changes separate so upstream can assess them independently.
3. Ask Hermes maintainers which native Android authentication contract they support and whether a community-client listing belongs in their docs. Start with a design discussion rather than a large code-import PR.
4. If maintainers express interest, agree on ownership, maintainers, signing/distribution, compatibility policy, support boundaries and branding. Then submit the specific change they request.

## Recognition through evidence

Suggested first month:

- **Week 1:** Green CI, coherent README/install/update paths, a troubleshooting page, exact tested backend versions, and two-minute self-hosted onboarding.
- **Week 2:** A 60–90 second demo of connection, streamed chat, a network interruption and recovery; recruit a small beta group through channels where project sharing is welcome.
- **Week 3:** Turn reproducible beta failures into focused fixes; contribute the general ones upstream; publish a concise release note with contributor credit.
- **Week 4:** Request a community listing or maintainer review with the release, demo and compatibility matrix. Seek official endorsement only if they invite that discussion.

Track successful first connection, repeat use, unresolved reproducible bugs, and time to fix regressions. Begin with opt-in feedback and anonymized issue templates; avoid collecting prompts, credentials or personal health entries. Stars can show awareness but do not prove usability. Use searchable repository topics and a clear description of the Android use case, without implying endorsement.

Official community destinations documented by Hermes are [Nous Research Discord](https://discord.gg/NousResearch) and [Hermes GitHub Discussions](https://github.com/NousResearch/hermes-agent/discussions). Check the channel's current sharing rules before posting. No message or issue has been posted as part of this review.

## Outreach draft for review

> Hello Hermes maintainers — I maintain `scammer0073-glitch/hermes-companion`, an Android fork of M57/Hy4ri's Hermes Mobile, with original attribution preserved. I am working on CI reliability, self-hosted onboarding and documented compatibility with current Hermes. The repair branch implements the documented native PKCE broker; real Android/Portal validation is still pending, and I am not presenting the app as official. Would an Android compatibility review and a community-client documentation proposal be useful? I can provide a tested release, a brief demo and an exact version matrix once verified. I would also appreciate your guidance on branding and maintenance expectations before discussing endorsement.

Before sending, replace future promises with links to the actual green run, APK, demo and version matrix. Do not claim tests or OAuth support that have not been completed.
