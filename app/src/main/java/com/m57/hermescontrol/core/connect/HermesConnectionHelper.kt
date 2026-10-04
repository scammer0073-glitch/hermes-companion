package com.m57.hermescontrol.core.connect

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m57.hermescontrol.theme.NemasysPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class ProbeTarget(val label: String, val url: String, val hint: String)

val DEFAULT_PROBE_TARGETS =
    listOf(
        ProbeTarget("Tailscale", "http://100.80.15.3:9119", "Tailnet — Google same on PC + phone"),
        ProbeTarget("LAN", "http://192.168.1.3:9119", "Same Wi-Fi — no internet"),
        ProbeTarget("USB", "http://127.0.0.1:9119", "adb reverse tcp:9119 tcp:9119"),
    )

fun isTailscaleInstalled(ctx: Context): Boolean =
    try {
        ctx.packageManager.getPackageInfo("com.tailscale.ipn", 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

suspend fun probeUrl(
    url: String,
    timeoutMs: Int = 1800,
): Boolean =
    withContext(Dispatchers.IO) {
        try {
            val pingUrl = url.trimEnd('/') + "/api/ping"
            val conn = URL(pingUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.connect()
            val ok = conn.responseCode in 200..399
            conn.disconnect()
            ok
        } catch (_: Exception) {
            try {
                val c2 = URL(url).openConnection() as HttpURLConnection
                c2.requestMethod = "GET"
                c2.connectTimeout = timeoutMs
                c2.readTimeout = timeoutMs
                c2.connect()
                val ok2 = c2.responseCode in 200..399
                c2.disconnect()
                ok2
            } catch (_: Exception) {
                false
            }
        }
    }

fun openTailscalePlayStore(ctx: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.tailscale.ipn"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        ctx.startActivity(intent)
    } catch (_: Exception) {
        ctx.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.tailscale.ipn"),
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }
}

fun openTailscaleApp(ctx: Context) {
    try {
        ctx.packageManager.getLaunchIntentForPackage("com.tailscale.ipn")?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(it)
            return
        }
    } catch (_: Exception) {
    }
    openTailscalePlayStore(ctx)
}

@Composable
fun HermesConnectHelperCard(
    onPickUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var installed by remember { mutableStateOf(isTailscaleInstalled(ctx)) }
    var probing by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf(mapOf<String, Boolean?>()) }

    LaunchedEffect(Unit) { installed = isTailscaleInstalled(ctx) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
        border = BorderStroke(1.dp, NemasysPalette.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NemasysPalette.Accent),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "HERMES WRAPPER • ONE-TAP CONNECT",
                    color = NemasysPalette.Muted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.6.sp,
                )
                Spacer(Modifier.weight(1f))
                if (installed) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier.background(
                                NemasysPalette.AccentOverlay,
                                RoundedCornerShape(50),
                            ).padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = NemasysPalette.Accent,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Tailscale on phone",
                            color = NemasysPalette.Accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(
                "Login with same Google on laptop + phone — tailnet joins automatically. " +
                    "Pick green route. Tailscale fallback like Trendloom HOSTING.md; Hub is public tunnel.",
                color = NemasysPalette.Muted,
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )

            DEFAULT_PROBE_TARGETS.forEach { t ->
                val state = results[t.url]
                val dot =
                    when (state) {
                        true -> NemasysPalette.Accent
                        false -> NemasysPalette.Offline
                        null -> NemasysPalette.CardBorder
                    }
                val label =
                    when (state) {
                        true -> "reachable"
                        false -> "offline"
                        null -> "tap Probe"
                    }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NemasysPalette.Card,
                    border = BorderStroke(1.dp, NemasysPalette.CardBorder),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(10.dp).background(dot, CircleShape))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    t.label,
                                    color = NemasysPalette.TextBright,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(t.url, color = NemasysPalette.Muted, fontSize = 10.sp)
                            }
                            Text(t.hint, color = NemasysPalette.Muted, fontSize = 10.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(label, color = dot, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = {
                                    onPickUrl(t.url)
                                },
                                contentPadding =
                                    PaddingValues(
                                        horizontal = 10.dp,
                                        vertical = 0.dp,
                                    ),
                                modifier = Modifier.height(28.dp),
                                border = BorderStroke(1.dp, NemasysPalette.CardBorder),
                            ) {
                                Text("Use", fontSize = 11.sp, color = NemasysPalette.TextBright)
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        probing = true
                        scope.launch {
                            val m = mutableMapOf<String, Boolean?>()
                            for (t in DEFAULT_PROBE_TARGETS) m[t.url] = probeUrl(t.url)
                            results = m
                            probing = false
                            // auto-pick first reachable
                            m.entries.firstOrNull { it.value == true }?.let { onPickUrl(it.key) }
                        }
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = NemasysPalette.Accent,
                            contentColor = NemasysPalette.OnAccent,
                        ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(40.dp),
                    enabled = !probing,
                ) { Text(if (probing) "Probing…" else "Probe all", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                OutlinedButton(
                    onClick = { if (installed) openTailscaleApp(ctx) else openTailscalePlayStore(ctx) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NemasysPalette.CardBorder),
                    modifier = Modifier.weight(1f).height(40.dp),
                ) {
                    Icon(
                        if (installed) Icons.Filled.VpnKey else Icons.Filled.PhoneAndroid,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = NemasysPalette.TextBright,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (installed) "Open Tailscale" else "Get Tailscale",
                        color = NemasysPalette.TextBright,
                        fontSize = 12.sp,
                    )
                }
            }
            if (probing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = NemasysPalette.Accent,
                    trackColor = NemasysPalette.CardBorder,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Language, null, tint = NemasysPalette.Muted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Hub https://<you>.nemasys.in needs no Tailscale — just paste URL.",
                    color = NemasysPalette.Muted,
                    fontSize = 10.sp,
                )
            }
        }
    }
}
