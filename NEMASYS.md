# Nemasys Hub — Hosted Hermes (Open Core)

**Nemasys** = free, open-source Android app (Apache 2.0). **Nemasys Hub** = optional paid hosting for Hermes Agent.

> Like WordPress.org (free) vs WordPress.com (hosted) — you can always self-host. Hub just removes DevOps.

## Why this model
- Hermes requires a 24/7 gateway (PC/VPS, Tailscale, certs, backups). Most users don't want to self-host.
- App stays 100% open — no paywalled features. Monetize **infra, not the app**.
- Aligns with user ask: "subscription to access a hub system in a server where hermes can be hosted".

## Tiers (INR, GST incl.)

| Tier | Price | What you get |
|------|-------|--------------|
| **Self-Hosted** | ₹0 | App + docs, LAN/Tailscale, community support |
| **Hub Starter** | ₹199/mo | 1 instance, HTTPS URL, 5 bots, 50 crons, email support |
| **Hub Pro** | ₹599/mo | 3 instances, unlimited bots/crons, custom domain, daily backups, priority support |

Team/Enterprise: DM for dedicated VPS + SLA.

## How it connects
- Hub provisions a private `https://<you>.nemasys.in` → tunnels to a container running `hermes dashboard`.
- App just needs `Server URL = https://<you>.nemasys.in` + token/password — same flow as self-host.
- No extra SDK. Same REST + `?token=` WS. Tailscale not needed for Hub.

## Tech sketch
- Per-tenant Docker on Hetzner/Contabo VPS (₹500-800/mo for 4 vCPU/8GB hosts; 8-12 tenants/host = healthy margin).
- Caddy reverse proxy + Let's Encrypt wildcard.
- Supabase/Postgres for metering + auth.
- Razorpay + Play Billing (once on Play Store). `billingclient` for in-app purchase → verify on server → issue Hub credential.

## Open-source promise
- `github.com/scammer0073-glitch/hermes-companion` stays Apache 2.0.
- No "open then close". Hub code is separate infra repo (private) — app never checks license for local features.
- F-Droid build stays clean (no billing SDK). Play build includes optional billing.

## Roadmap
- **v0.1** — Rebrand to Nemasys, new site on GitHub Pages, waitlist (now)
- **v0.2** — In-app Hub screen (mock), Razorpay link, manual provisioning
- **v0.3** — Automated provisioning + Play Billing, 10 pilot users
- **v1.0** — Play Store listing, F-Droid, auto-backups

## Dev velocity boosts (done/next)
- Done: `./gradlew assembleDebug --offline` <2m, CI on every push, bot picker, AMOLED polish
- Next: `make install` alias, screenshot automation, issue templates for feature/bug, `fastlane` for Play releases
