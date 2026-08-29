# Easy Install — Nemasys (Hermes + Phone) in 1 tap

> Like Trendloom's `HOSTING.md`: app stays on this PC, Caddy fronts LAN, tunnel gives HTTPS. Nemasys mirrors that with one double-click.

## One double-click (Windows)

```bat
scripts\EasyInstall.bat        :: or
powershell -ExecutionPolicy Bypass -File scripts\easy-install.ps1
```

What it does (idempotent):

1. **Tailscale** — installs `1.102.3` if missing (`pkgs.tailscale.com`), tells you `tailscale up` with **same Google** on PC + phone (tailnet auto-joins). Like `HOSTING.md#Fallback: Tailscale`.
2. **Firewall** — creates `Hermes Dashboard 9119 LAN` + `Hermes Dashboard 9119 Tailscale (100.64.0.0/10)` (needs Admin; re-run as Admin if prompted).
3. **Hermes** — starts `hermes dashboard --host 0.0.0.0 --port 9119 --no-open` detached (like a systemd unit) if not already listening.
4. **QR** — probes all three routes and writes `Desktop\Nemasys-Connect-QR.html` + `Nemasys-QR.png` (via `api.qrserver.com`, no local dep). Opens it. Scan on phone.

## URLs (phone picks green)

| Route | URL | When |
|---|---|---|
| **Tailscale** | `http://100.80.15.3:9119` | Zero-config, same Google tailnet |
| **LAN** | `http://192.168.1.3:9119` | Same Wi-Fi |
| **USB** | `http://127.0.0.1:9119` | `adb reverse tcp:9119 tcp:9119` |

## Phone: 1 scan

1. Install **Nemasys APK** (`app/build/outputs/apk/debug/app-debug.apk` or Releases) + **Tailscale** from Play Store — login **same Google** on both.
2. Open Nemasys → login screen → **Hermes wrapper** card → `Probe all` → green row → `Use` (auto-fills Server URL). Or scan `Desktop\Nemasys-Connect-QR.html` QR → long-press → paste.
3. Enter Hermes `Username/Password` (or token) → `Connect` → `Probe` → `Connect`.

Tips:

- `adb reverse` path never needs Wi-Fi (like T3's `0.0.0.0` + Caddy).
- Hub path (`https://<you>.nemasys.in`) needs no Tailscale — just paste Hub URL.
- T3 share-page pattern (`share.html` with skeletons + `safe-area-inset-bottom`) inspired Personal Apps stats: same bottom-done feel, 400/800px image fallback.
