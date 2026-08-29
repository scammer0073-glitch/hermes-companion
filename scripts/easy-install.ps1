# Nemasys — Easy Install (one double-click)

# T3-inspired: Trendloom keeps DB/photos on this PC, tunnel gives https.
# Nemasys mirrors it: Hermes stays on this PC (0.0.0.0:9119), Tailscale = fallback,
# Hub = public tunnel. This script is the "T3 easy phone connect" equivalent.

param([switch]$NoPause)

$ErrorActionPreference = "Stop"
function Write-Step($m) { Write-Host "`n== $m ==" -ForegroundColor Cyan }
function Write-Ok($m)   { Write-Host "  OK $m" -ForegroundColor Green }
function Write-Warn($m) { Write-Host "  !! $m" -ForegroundColor Yellow }

$PC_TAILSCALE = "100.80.15.3"
$PC_LAN = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -like "192.168.*" } | Select-Object -First 1 -ExpandProperty IPAddress)
if (-not $PC_LAN) { $PC_LAN = "192.168.1.3" }
$PORT = 9119

Write-Step "Nemasys Easy Install — Hermes -> Phone in 1 tap"
Write-Host "PC tailnet $PC_TAILSCALE  LAN $PC_LAN  port $PORT  $(Get-Date -Format 'yyyy-MM-dd HH:mm')" -ForegroundColor Gray

# 1. Tailscale check
Write-Step "1/4 Tailscale"
$tsexists = Get-Command tailscale -ErrorAction SilentlyContinue
if ($tsexists) { Write-Ok "tailscale $($(& tailscale version)) at $($tsexists.Source)" }
else {
  Write-Warn "Tailscale not found — installing 1.102.3 (like HOSTING.md fallback)"
  $msi = "$env:LOCALAPPDATA\Temp\tailscale-setup-1.102.3-amd64.msi"
  if (-not (Test-Path $msi)) {
    # fallback URL also in E:/t3-code style
    Invoke-WebRequest -Uri "https://pkgs.tailscale.com/stable/tailscale-setup-1.102.3-amd64.msi" -OutFile $msi -UseBasicParsing
  }
  Start-Process msiexec.exe -ArgumentList "/i `"$msi`" /quiet /norestart" -Wait -Verb RunAs
  Write-Ok "Tailscale installed — login with same Google on PC + phone"
  Write-Host "      Run: tailscale up  and pick Google SSO (same email on both)" -ForegroundColor Yellow
}

# 2. Firewall (like Caddy front door, but for Hermes)
Write-Step "2/4 Firewall — allow :9119 on LAN + tailnet"
try {
  $r1 = Get-NetFirewallRule -DisplayName "Hermes Dashboard 9119 LAN" -ErrorAction SilentlyContinue
  if (-not $r1) { New-NetFirewallRule -DisplayName "Hermes Dashboard 9119 LAN" -Direction Inbound -LocalPort $PORT -Protocol TCP -Action Allow -Profile Private | Out-Null; Write-Ok "LAN rule created" } else { Write-Ok "LAN rule exists" }
  $r2 = Get-NetFirewallRule -DisplayName "Hermes Dashboard 9119 Tailscale" -ErrorAction SilentlyContinue
  if (-not $r2) { New-NetFirewallRule -DisplayName "Hermes Dashboard 9119 Tailscale" -Direction Inbound -LocalPort $PORT -Protocol TCP -Action Allow -RemoteAddress 100.64.0.0/10 | Out-Null; Write-Ok "Tailscale rule created" } else { Write-Ok "Tailscale rule exists" }
} catch { Write-Warn "Firewall needs Admin — right-click -> Run as Admin. $_" }

# 3. Hermes dashboard
Write-Step "3/4 Hermes dashboard :9119"
$hermes = "$env:LOCALAPPDATA\hermes\hermes-agent\venv\Scripts\hermes.exe"
if (-not (Test-Path $hermes)) { $hermes = (Get-Command hermes -ErrorAction SilentlyContinue).Source }
if ($hermes) {
  $running = Get-NetTCPConnection -LocalPort $PORT -ErrorAction SilentlyContinue | Where-Object State -eq Listen
  if ($running) { Write-Ok "already LISTENING :$PORT (PID $($running.OwningProcess))" }
  else {
    # start detached like Trendloom's systemd unit
    Start-Process -FilePath $hermes -ArgumentList "dashboard --host 0.0.0.0 --port $PORT --no-open" -WindowStyle Hidden
    Start-Sleep 2
    Write-Ok "started hermes dashboard --host 0.0.0.0 --port $PORT"
  }
} else { Write-Warn "hermes.exe not found — install Hermes Agent first" }

# 4. Connection URLs (T3-style: give all three, phone picks green)
Write-Step "4/4 Connect URLs — scan QR on phone"
$TAIL_URL = "http://${PC_TAILSCALE}:$PORT"
$LAN_URL  = "http://${PC_LAN}:$PORT"
$USB_URL  = "http://127.0.0.1:$PORT"

# Probe each
function Test-Url($u) { try { $r = Invoke-WebRequest -Uri $u -UseBasicParsing -TimeoutSec 2; return $r.StatusCode -in 200,302 } catch { return $false } }
$okTail = Test-Url $TAIL_URL; $okLan = Test-Url $LAN_URL

Write-Host "`n  Tailscale : $TAIL_URL  $(if($okTail){'reachable'}else{'offline'})" -ForegroundColor $(if($okTail){"Green"}else{"Yellow"})
Write-Host "  LAN       : $LAN_URL  $(if($okLan){'reachable'}else{'offline'})" -ForegroundColor $(if($okLan){"Green"}else{"Yellow"})
Write-Host "  USB       : $USB_URL  (adb reverse tcp:$PORT tcp:$PORT)" -ForegroundColor Gray
Write-Host "  Tip: same Google on PC + phone -> tailnet auto-joins (Trendloom HOSTING fallback)." -ForegroundColor DarkGray

# Generate QR html (like share.html token link — no login, just paste URL)
$qrText = if ($okTail) { $TAIL_URL } elseif ($okLan) { $LAN_URL } else { $TAIL_URL }
# simple QR via api.qrserver.com (no local dep) + local html
$qrApi = "https://api.qrserver.com/v1/create-qr-code/?size=320x320&data=" + [Uri]::EscapeDataString($qrText)
$html = @"
<!doctype html><meta charset=utf-8><meta name=viewport content="width=device-width,initial-scale=1">
<title>Nemasys — scan to connect</title>
<style>body{font-family:system-ui;background:#0A0E14;color:#E6EDF3;display:grid;place-items:center;min-height:100vh;margin:0;padding:24px}
.card{background:#0D0F12;border:1px solid #1E2D44;border-radius:16px;padding:24px;max-width:420px;text-align:center}
h1{margin:0 0 6px;font-size:20px} p{color:#8B9AB0;font-size:13px} code{background:#111820;padding:2px 6px;border-radius:6px}
img{border-radius:12px;border:1px solid #1E2D44;margin:16px 0} a{color:#2DD4BF}
</style>
<div class=card>
<h1>Nemasys — scan on phone</h1>
<p>Open <b>Nemasys</b> → paste this URL → Connect</p>
<img src="$qrApi" width=320 height=320 alt="QR">
<p><code>$qrText</code></p>
<p>Fallbacks: <code>$LAN_URL</code> · <code>$USB_URL</code></p>
<p style="font-size:11px">Tailscale: login same Google on PC + phone → tailnet green. Hub is <code>https://&lt;you&gt;.nemasys.in</code> (no Tailscale).</p>
<p><a href="$qrText">Open dashboard</a> · <a href="https://scammer0073-glitch.github.io/hermes-companion/">Docs</a></p>
</div>
"@
$outHtml = "$env:USERPROFILE\Desktop\Nemasys-Connect-QR.html"
$html | Out-File -Encoding utf8 $outHtml
Write-Ok "QR saved to $outHtml"
try { Start-Process $outHtml | Out-Null } catch {}
try { Write-Host "`nQR API: $qrApi" -ForegroundColor DarkGray; Invoke-WebRequest $qrApi -OutFile "$env:USERPROFILE\Desktop\Nemasys-QR.png" -UseBasicParsing; Write-Ok "PNG saved to Desktop\Nemasys-QR.png" } catch {}

Write-Host "`nONE-SCAN FLOW:" -ForegroundColor Cyan
Write-Host "  1. Phone: install Nemasys APK + Tailscale (same Google)" -ForegroundColor White
Write-Host "  2. Phone: open Nemasys → Server URL auto-fills via Probe all → Use" -ForegroundColor White
Write-Host "  3. Or scan Desktop\Nemasys-Connect-QR.html → long-press URL → paste in Nemasys" -ForegroundColor White
Write-Host "  USB fallback: adb reverse tcp:$PORT tcp:$PORT  then use $USB_URL" -ForegroundColor Gray

if (-not $NoPause) { Write-Host "`nPress Enter to close…"; Read-Host | Out-Null }
