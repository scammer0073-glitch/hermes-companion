# Hosted sandbox computers for Hermes bots

Research date: 2026-10-03. This is an exploratory design, not a deployed service or a promise of Grok feature parity. “Like the Grok bot” is interpreted here as a bot with its own terminal, browser, files and optional visible desktop; the exact Grok feature being compared has not been established. No cloud resources, purchases, deployments or outreach were made.

## Feasible scope

The user prioritizes a **visible desktop they can watch and control**. Start with a single-user pilot on a dedicated Linux VM: **2 vCPU, 4 GiB RAM for one bot computer**, a bounded workspace and a 10-minute idle timeout. Reuse Hermes Bot Screen through its supported Docker/SSH placement. Android should provide Watch, Take control, Hand back, Stop and Reset with artifacts alongside the screen. A 1 vCPU/2 GiB tier is an experimental later optimization or headless tier, not the default desktop promise. Provider provisioning, credentials and billing belong on the server.

Three capabilities must be distinguished:

| Capability | What the user gets | What it does not establish |
| --- | --- | --- |
| Code/terminal sandbox | Shell commands, Python/Node, files, package installs within configured limits | A graphical desktop or tenant isolation merely because an agent calls it a sandbox |
| Browser workspace | A browser session, page automation, screenshots and optional interactive login | A complete Linux computer or shared filesystem with the terminal unless explicitly integrated |
| Bot computer | A Linux display, browser, terminal and human observe/takeover workflow | Strong isolation between customers from display separation alone |

Hermes already provides terminal backends including Docker, SSH, Modal, Daytona and Vercel Sandbox, with resource/persistence controls. Reuse those execution paths before introducing another agent tool. [Terminal backend documentation](https://hermes-agent.nousresearch.com/docs/user-guide/features/tools).

Current **Bot Screen** supports Linux desktops and an authenticated display viewer/control lease. Sandbox placement works with Docker, SSH and Singularity; current Modal, Daytona and Vercel Sandbox placements refuse a display. Separate screens share host access and are not tenant boundaries. The documented default memory headroom check is 1536 MB, so the proposed 2 GiB tier needs benchmarking; start desktop trials at 4 GiB instead of disabling that check to force them into 2 GiB. [Official Bot Screen capabilities and threat model](https://hermes-agent.nousresearch.com/docs/user-guide/features/bot-screen).

These are current documentation findings. Pin and test the actual Hermes release used by the service; a feature on upstream main is not automatically available in the user's installed release.

## Architecture to implement

```text
Android companion / Hermes Desktop
          | HTTPS authenticated Hermes connection
Tenant-specific Hermes gateway / runtime
          | existing terminal environment or explicit provider plugin
Sandbox broker: ownership, quotas, lifecycle, provider adapter
          | short-lived worker credentials
Per-bot worker: isolated Linux VM/microVM or supported managed sandbox
          | bounded files + terminal + browser + optional display
          + encrypted artifact/snapshot storage
```

The diagram is a proposed deployment layout, not a list of existing Hermes HTTP endpoints.

**Tenant runtime:** For the first hosted version, place each customer's gateway in a separate VM/security domain with a separate `HERMES_HOME`, service identity and network policy. Within that tenant, assign a distinct workspace identity to each bot. For independent bots that handle sensitive credentials, use a separate worker VM/microVM per bot. An OS-user split or multiple Hermes profiles on one shared host is insufficient for the promised cross-tenant boundary.

**Execution bridge:** A trusted controller allocates a worker, then configures the tenant gateway's supported backend. Use `ssh` to a private worker carrying the Linux desktop stack, or `docker` inside a tenant-dedicated VM for an initial full-computer trial. Headless managed execution can use the existing Daytona/Modal backend. A provider API advertising desktop support would still need a compatible Hermes display integration; it does not override Hermes's documented placement restriction.

**Broker data model:** Store `tenant_id`, `bot_id`, `workspace_id`, `provider_resource_id`, image digest, generation, resource limits, expiry, last useful activity, desired state and observed state. Resolve ownership from the authenticated identity, not an Android-supplied provider resource ID. Use idempotent create/stop/delete operations and generation checks so a delayed callback cannot attach another bot's recycled worker.

**Mobile bridge:** Keep chat, approvals and normal tools on the existing Hermes authenticated connection. Prefer upstream lifecycle/provider capability surfaces for workspace controls. If those surfaces lack the necessary management operation, add an explicitly documented service API or propose a public Hermes hook; do not invent a built-in RPC name or patch private core functions. Android stores its Hermes session, never a Daytona, VPS, Modal or master cloud API key.

**Viewer:** First expose the existing Hermes Desktop screen workflow to validate the backend. The Android client should observe by default; explicit takeover pauses the agent's input, and hand-back restores it. Do not expose raw VNC or CDP publicly. Viewer reconnects must acquire a fresh ticket, and proxy logs must redact ticket query parameters. [Bot Screen display bridge](https://hermes-agent.nousresearch.com/docs/user-guide/features/bot-screen).

### Exact display contract and mobile integration

The inspected upstream gateway contract exposes these methods over its existing authenticated JSON-RPC connection:

| Action | RPC | Relevant arguments/result |
| --- | --- | --- |
| Inspect availability | `display.status` | Optional `profile`; runtime and lease status |
| Start desktop | `display.start` | Optional `profile`; idempotent startup |
| Watch | `display.observe` | Optional `profile`/`viewer_id`; returns `ticket`, `path`, `profile_key`, server-minted `viewer_id` |
| Take control | `display.lease.acquire` | Required returned `viewer_id`, optional `profile` and `reason` |
| Hand back | `display.lease.release` | Matching `viewer_id` and optional `profile`; `force` is an explicit recovery operation |
| Stop desktop | `display.stop` | Optional `profile`/`force`; active human ownership prevents ordinary stop |

Source: [official OpenRPC gateway contract](https://github.com/NousResearch/hermes-agent/blob/main/apps/shared/src/gateway-contract.openrpc.json). Pin this schema and verify support on the deployed release before using it; these operations are not implemented in Android by this proposal.

Prefer returning a hosted noVNC page over the authenticated gateway as the first mobile viewer, or bundle a reviewed noVNC renderer in a restricted Android WebView. Android's existing authenticated RPC client mints the viewer identity/ticket and controls the lease. The renderer receives only the scoped display connection, never provider keys or a master refresh token. Validate the returned path against the configured gateway origin/prefix, use its profile binding and contract-prescribed query fields, and open the binary RFB WebSocket bridge at `/api/display/ws`. Block arbitrary WebView navigation and broad JavaScript/native bridges; clear display tickets on logout/profile switch. A native RFB renderer is an alternative after proving touch, keyboard, geometry, reconnection and lease correctness, with more implementation/testing work. A plain iframe/link does not solve mobile authentication or takeover coordination.

## Limits, secrets and lifecycle

The following are proposed service policies, not upstream defaults:

- **Quota:** One active workspace per bot; initially one active bot per customer. Desktop quota starts at 2 vCPU/4 GiB, a 5 GiB writable workspace, process/file-descriptor limits and a two-hour hard session lifetime. Set per-bot daily usage and monthly spend caps; stop new work before the cap is exceeded. Enforce limits at the provider/VM runtime, not only in the UI.
- **Network:** Deny access to cloud metadata, control-plane/private networks and other tenants. Permit only needed outbound destinations, with download/egress limits; block public inbound ports. Reach SSH/display/CDP over private authenticated routes. Verify DNS resolution and redirects against the egress policy so URL filters cannot be bypassed through private-address resolution.
- **Secrets:** Keep provisioning keys in a controller secret store with least-privilege scopes. Keep model credentials in the tenant's trusted gateway or a scoped inference proxy; do not forward them wholesale to model-authored shell commands. Use short-lived credentials for approved tools and revoke them on teardown. Browser login cookies are sensitive workspace data, so persistence is opt-in and tenant encrypted.
- **Files:** Mount only that bot's workspace; never the controller home, another bot's home, or a host Docker socket. Pin images by digest, scan them, and separate artifact export from executable machine snapshots. Default exports contain selected work products, not login cookies or environment dumps.
- **Lifecycle:** `requested → provisioning → ready → active → idle → draining → deleted`, with explicit failure states. Terminal work and an authorized human control lease count as activity; passive viewing/heartbeats alone do not buy unlimited uptime. Warn before idle teardown and allow a bounded extension. Do not destroy a worker mid-approved job or active human takeover; the hard limit still provides a visible stop policy.
- **Recovery:** A controller reconciler deletes expired/orphaned resources even after a gateway crash. Reset destroys the old workspace generation and revokes credentials; recreate from a clean image plus approved files. Provider deletion failures remain visible and retryable, with spend alarms and a global provisioning kill switch.

Keep a long-running Hermes gateway separate from ephemeral execution if cron, notifications or messaging must remain available while computers sleep. Stopping a screen, stopping a terminal container and stopping billable compute are different events. Persistence preserves files, not necessarily live shells, browser processes or task execution. [Hermes terminal lifetime/persistence reference](https://hermes-agent.nousresearch.com/docs/reference/environment-variables).

## Cost model from current primary pricing

USD list prices observed on 2026-10-03; these are planning estimates, not vendor quotes. Exclude LLM/API calls, the always-on gateway/controller, managed databases, support, tax, backups, extra storage and network charges. Credits/promotions are excluded. Regional capacity, provider quotas and provisioning latency still need verification.

Assumption for the usage column: 30 sessions/month, one hour of work each plus ten minutes of idle tail = **35 billed hours**. Very short sessions can incur provider minimum charges. Neither table proves that a full Hermes desktop works on every provider.

| Option | Published rate and size | Estimated 35-hour compute | Always allocated | Engineering fit |
| --- | --- | --- | --- | --- |
| DigitalOcean Basic VM | 1 vCPU, 2 GiB: $0.01786/hour, $12 monthly cap | $0.63 | $12/month | Private SSH worker; gateway/desktop image and lifecycle are your responsibility |
| DigitalOcean desktop trial | 2 vCPU, 4 GiB: $0.03571/hour, $24 monthly cap | $1.25 | $24/month | More browser memory; same operational work |
| Daytona desktop-capable provider size | 2 vCPU + 4 GiB = $0.1656/hour | $5.80 | $119.23 compute for 720 hours | Provider has VNC/computer APIs; Hermes Bot Screen integration still requires an adapter |
| Daytona | 1 vCPU at $0.0504/hour + 2 GiB at $0.0162/GiB-hour = $0.0828/hour | $2.90 | $59.62 compute for 720 hours | Existing Hermes headless backend; current in-sandbox Bot Screen unsupported |
| Modal Sandbox, comparison size | 1 physical core (2 vCPU equivalent) + 2 GiB: $0.00003942/core-second + $0.00000667/GiB-second = $0.189936/hour | $6.65 | $136.75 at that full resource-time assumption for 720 hours | Existing headless backend; not the 1-vCPU target, and current screen placement unsupported |

Sources: [DigitalOcean Droplet price table](https://www.digitalocean.com/pricing/droplets), [Daytona pricing](https://www.daytona.io/pricing), [Modal Sandbox pricing](https://modal.com/pricing). Modal's generic function CPU/memory rates differ from its Sandbox section; the estimate uses the Sandbox section, assumes the stated CPU resource-time is billable, and must be checked against an actual usage receipt.

**Recommended desktop baseline:** the dedicated 2-vCPU/4-GiB VM through Docker/SSH, at $24/month if always allocated. The $1.25 intermittent estimate requires actual deletion/recreation after 35 billable hours, and excludes snapshots and the persistent controller. Daytona independently documents VNC and `sandbox.computer_use.start()/stop()/get_status()` with its default desktop image. That makes it a candidate managed computer service, not a working Hermes Bot Screen drop-in: integrating its lifecycle, stream and agent/human lease still requires a reviewed adapter. [Daytona VNC capability](https://www.daytona.io/docs/en/vnc-access/).

DigitalOcean charges powered-off VMs until they are destroyed, with a minimum of 60 seconds or $0.01. Consequently an idle timer that merely powers off a VM does not produce the $0.63 estimate: destroy it after approved export/checkpoint and recreate next time. Its bundled VM monthly billing cap is 672 hours. [DigitalOcean billing rules](https://docs.digitalocean.com/products/droplets/details/pricing/).

Daytona storage lists $0.000108/GiB-hour after the first 5 free GiB. A conservative example charging all 10 GiB for 720 hours adds about **$0.78/month**, making the 35-hour example about **$3.68 before other costs**; actual free storage allowance can reduce it. Stopped container sandboxes continue disk billing; archive/delete semantics matter. [Daytona rates](https://www.daytona.io/pricing), [lifecycle billing](https://www.daytona.io/docs/billing).

A cheap raw VM can therefore beat a managed sandbox's compute rate, but it also requires isolation hardening, image maintenance, orchestration, storage recovery and abuse handling. Start with a personal/BYOC pilot and observed receipts. Do not advertise unlimited bots or a sustainable subscription price from compute-only arithmetic.

## Implementation phases and acceptance evidence

1. **Visible-computer proof after current Android fixes:** On disposable local/Linux infrastructure with 2 vCPU/4 GiB, run one Hermes bot using Docker/SSH with separate workspace files. Verify watching, human takeover/hand-back, terminal work and the browser with the official Desktop viewer. Measure startup, RSS/peak RAM, image size, CPU and artifact export. No public service yet.
2. **BYOC/private pilot:** Build a small controller in a separate repository, initially against one provider. Provide authenticated create/status/stop/reset controls, quotas, idle deletion and explicit persistence. Prove that bot A cannot read bot B's workspace, cookies, credentials or control endpoints. Demonstrate crash cleanup and reconcile provider spend with receipts.
3. **Android workspace UI:** Show capabilities, provisioning progress, expiry, current limits, artifact download and recoverable errors. Keep terminal execution behind Hermes tools/approvals. Add mobile display observation and takeover only after the display contract passes device tests. Keep hosting optional for self-hosted users.
4. **Small paid beta:** Limit accounts/concurrency, publish usage caps and deletion/retention behavior, and test network abuse controls. Expand regions and providers only after lifecycle/error rates and cost per completed task are measured.

Useful demo: ask the bot to build a small file, inspect a page in its own browser, show the screen, take control for a login, hand it back, download the artifact, then reset and demonstrate that prior data is gone. Use test accounts and redact screen/login data. This demonstrates a concrete product benefit that maintainers and prospective users can reproduce.

### Runnable local pilot without buying cloud infrastructure

Use a disposable Linux VM or Linux host with Docker and the chosen Hermes release installed. Enable Browser Automation and Computer Use through `hermes tools`, using the official setup instructions for dependencies. Keep real accounts and production secrets out of the pilot. These are proposed commands to run later, not actions performed by this review:

```bash
hermes config set terminal.backend docker
hermes config set terminal.docker_image nousresearch/hermes-sandbox:desktop
hermes config set terminal.container_cpu 2
hermes config set terminal.container_memory 4096
hermes config set bot_desktop.placement terminal
hermes config set bot_desktop.idle_stop_minutes 10
hermes computer-use screen start
hermes computer-use screen status
```

Connect the official Hermes Desktop to this gateway, open its Screen pane, watch, take control, and hand back. Confirm the bot and browser act inside the Docker desktop rather than on the gateway host. Finish with `hermes computer-use screen stop`; this stops the display, not necessarily the container or any paid VM. Record the tested image digest and pin it before a reproducible release. If the installed release lacks the display RPCs, update the disposable test instance to a verified release before proceeding. A controller needs its own idle reaper for stopping/checkpointing/deleting the full billable worker.

## Upstream and recognition route

Keep the cloud control plane and provider billing in their own project. Reuse a bundled backend as-is; do not submit a renamed fork of an existing provider to the catalog. If a new managed broker genuinely needs a distinct terminal environment, use the official `TerminalEnvironmentProvider` interface, including `create_environment`, `execute`, `cleanup`, secret stripping and session isolation flags. A separate browser provider is appropriate only when its session lifecycle/CDP contract is required. [Terminal provider plugin contract](https://hermes-agent.nousresearch.com/docs/developer-guide/terminal-environment-plugin), [browser provider contract](https://hermes-agent.nousresearch.com/docs/developer-guide/browser-provider-plugin).

The plugin catalog is a concrete recognition path: maintainers review a public loadable plugin at a full 40-character SHA; validation must pass, compatibility metadata and capability disclosures must be truthful, and plugins must use public extension surfaces without runtime core overrides. A listing is not an official hosting endorsement or automatic transfer into Nous Research. [Official catalog submission rules](https://hermes-agent.nousresearch.com/docs/developer-guide/plugins/catalog-submission).

Suggested maintainer proposal, to review before any posting:

> We are prototyping an optional per-bot visible computer and Android viewer. The first pilot reuses Bot Screen on a dedicated Linux VM through Docker/SSH, with Watch, Take control, Hand back and bounded lifecycle/budget controls. We preserve self-hosted support and keep cloud credentials on the backend. Would a mobile viewer consuming the existing display RPC/lease/ticket contract and a separate sandbox lifecycle/provider adapter fit your preferred extension surfaces? We can provide a reproducible demo, pinned plugin, compatibility tests, observed costs and an isolation threat model before proposing a core change or asking for a catalog listing.

Publish the tested demo, compatibility matrix and usage receipts first; contribute general upstream fixes as focused PRs. Seek a reviewed catalog/docs entry before asking for official branding. Maintain M57/Hy4ri's attribution in the Android fork and explain which components are independent. No maintainer message has been sent.
