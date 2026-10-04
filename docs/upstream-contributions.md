# Upstream contributions

Submission record, 2026-10-04. The repositories receive separate, focused contributions:

| Project | Pull request | Change |
| --- | --- | --- |
| Hermes Mobile (`dev`) | [#1470](https://github.com/Hy4ri/hermes-mobile/pull/1470) | Disconnect channels through the supported scoped PUT; clear declared fields and preserve the catalog row |
| Hermes Mobile (`dev`) | [#1471](https://github.com/Hy4ri/hermes-mobile/pull/1471) | Update MCP env through the collection route while preserving raw configuration and checking connection/profile scope |
| Hermes Mobile (`dev`) | [#1472](https://github.com/Hy4ri/hermes-mobile/pull/1472) | Normalize Windows paths in the color guard; reproduced failure and corrected check pass |
| Hermes Agent (`main`) | [#132744](https://github.com/NousResearch/hermes-agent/pull/132744) | Private Docker Bot Screen pilot and explicit worker/data cleanup boundaries |

GitHub is the source of current review and CI status. Submission does not imply maintainer acceptance.
The channel and MCP PRs include regression and Retrofit wire tests; local formatting passes.
Their draft descriptions record incomplete device/live-gateway verification and test status.
The documentation PR is a draft: configuration keys, CLI commands, links and persistence behavior were checked against source,
but local website dependency installation stalled and no live Linux desktop pilot was run.

## Review findings

Upstream mobile already contains profile cloning and removal of unsupported MCP restart/session prompt routes (#926),
and server-to-client WebSocket request support (#1125/#1198). These were not resubmitted.
Native OAuth is absent, but [issue #639](https://github.com/Hy4ri/hermes-mobile/issues/639) was explicitly closed as `not_planned`.
The fork retains its PKCE implementation and automated security/protocol tests; real Android/Portal login remains unverified.

The dashboard/gateway review checked messaging, saved configuration, MCP and display lifecycle routes.
An existing [display profile-binding PR #131762](https://github.com/NousResearch/hermes-agent/pull/131762) covers the related scope issue;
no duplicate code patch was opened. The Bot Screen guide's idle-stop wording was improved because screen shutdown can leave
persistent Docker containers running and never establishes VM deletion or billing cessation.

The hosted bot computer remains a private pilot design, not a deployed multi-tenant service.
Cloud provisioning, strong tenant isolation, quotas, idle worker deletion, artifact retention and measured cost still require implementation and validation.
