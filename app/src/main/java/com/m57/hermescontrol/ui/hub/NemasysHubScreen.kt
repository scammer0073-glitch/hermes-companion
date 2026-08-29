package com.m57.hermescontrol.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NemasysHubScreen(modifier: Modifier = Modifier) {
    val uri = LocalUriHandler.current
    Column(
        modifier = modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0E14), Color(0xFF111A26))))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Cloud, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Nemasys Hub", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                Text("Managed Hermes — no PC, no Tailscale, just a URL", style = MaterialTheme.typography.labelMedium, color = Color(0xFF8B9AB0))
            }
        }
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111820)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("How it works", fontWeight = FontWeight.Bold, color = Color.White)
                HubStep("1", "Subscribe — ₹199/mo Starter or ₹599 Pro")
                HubStep("2", "We spin your private https://you.nemasys.in in ~60s")
                HubStep("3", "Paste URL in Nemasys → Connect. Same token/password flow.")
                Text("Self-host stays free forever. Hub is just hosted infra — like WordPress.com. Apache 2.0, no paywall on app features.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9AB0))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PriceCard("Self-Hosted", "₹0", "Forever free", listOf("All app features", "LAN + Tailscale", "Community help"), Modifier.weight(1f))
            PriceCard("Hub Starter", "₹199/mo", "Most popular", listOf("1 instance, HTTPS", "5 bots, 50 crons", "Email support"), Modifier.weight(1f), featured = true)
        }
        PriceCard("Hub Pro", "₹599/mo", "For teams", listOf("3 instances", "Unlimited bots/crons", "Custom domain + backups"), Modifier.fillMaxWidth(), featured = false)
        Button(
            onClick = { uri.openUri("https://scammer0073-glitch.github.io/hermes-companion/#pricing") },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF), contentColor = Color(0xFF001018))
        ) {
            Icon(Icons.Filled.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Join waitlist — get early price", fontWeight = FontWeight.Black)
        }
        OutlinedButton(
            onClick = { uri.openUri("https://github.com/scammer0073-glitch/hermes-companion/blob/main/NEMASYS.md") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))
        ) { Text("Read business doc", color = Color.White) }
        Text("Open source promise: app stays Apache 2.0. Payments via Razorpay/UPI + Play Billing soon.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9AB0))
    }
}

@Composable private fun HubStep(n: String, t: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(50), color = Color(0x332DD4BF)) { Text(n, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color(0xFF2DD4BF), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        Spacer(Modifier.width(10.dp))
        Text(t, color = Color.White, style = MaterialTheme.typography.bodySmall)
    }
}
@Composable private fun PriceCard(title: String, price: String, badge: String, feats: List<String>, mod: Modifier, featured: Boolean = false) {
    Card(mod, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (featured) Color(0xFF16202E) else Color(0xFF111820)), border = androidx.compose.foundation.BorderStroke(1.dp, if (featured) Color(0xFF2DD4BF) else Color(0xFF1E2D44))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text(title, fontWeight = FontWeight.Bold, color = Color.White); Spacer(Modifier.weight(1f)); Surface(shape = RoundedCornerShape(50), color = if (featured) Color(0xFF2DD4BF) else Color(0xFF1E2D44)) { Text(badge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (featured) Color(0xFF001018) else Color(0xFF8B9AB0)) } }
            Text(price, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
            feats.forEach { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(14.dp)); Spacer(Modifier.width(6.dp)); Text(it, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9AB0)) } }
        }
    }
}
