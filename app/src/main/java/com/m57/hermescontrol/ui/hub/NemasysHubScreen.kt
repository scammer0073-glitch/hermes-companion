package com.m57.hermescontrol.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m57.hermescontrol.theme.NemasysPalette

@Composable
fun NemasysHubScreen(modifier: Modifier = Modifier) {
    val uri = LocalUriHandler.current
    Column(
        modifier =
            modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(NemasysPalette.Background, NemasysPalette.SecondarySurface)))
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Cloud,
                contentDescription = null,
                tint = NemasysPalette.Accent,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "Nemasys Hub",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = NemasysPalette.TextBright,
                )
                Text(
                    "Managed Hermes — no PC, no Tailscale, just a URL",
                    style = MaterialTheme.typography.labelMedium,
                    color = NemasysPalette.Muted,
                )
            }
        }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
            border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(6.dp).clip(CircleShape).background(NemasysPalette.Accent),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "HOW IT WORKS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = NemasysPalette.Muted,
                    )
                }
                HubStep("1", "Subscribe — ₹199/mo Starter or ₹599 Pro")
                HubStep("2", "We spin your private https://you.nemasys.in in ~60s")
                HubStep("3", "Paste URL in Nemasys → Connect. Same token/password flow.")
                Text(
                    "Self-host stays free forever. Hub is just hosted infra — like " +
                        "WordPress.com. Apache 2.0, no paywall on app features.",
                    style = MaterialTheme.typography.labelSmall,
                    color = NemasysPalette.Muted,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PriceCard(
                "Self-Hosted",
                "₹0",
                "Forever free",
                listOf("All app features", "LAN + Tailscale", "Community help"),
                Modifier.weight(1f),
                nemasysBlack = true,
            )
            PriceCard(
                "Hub Starter",
                "₹199/mo",
                "Most popular",
                listOf("1 instance, HTTPS", "5 bots, 50 crons", "Email support"),
                Modifier.weight(1f),
                featured = true,
                nemasysBlack = true,
            )
        }
        PriceCard(
            "Hub Pro",
            "₹599/mo",
            "For teams",
            listOf("3 instances", "Unlimited bots/crons", "Custom domain + backups"),
            Modifier.fillMaxWidth(),
            featured = false,
            nemasysBlack = true,
        )
        Button(
            onClick = { uri.openUri("https://scammer0073-glitch.github.io/hermes-companion/#pricing") },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = NemasysPalette.Accent,
                    contentColor = NemasysPalette.OnAccent,
                ),
        ) {
            Icon(Icons.Filled.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Join waitlist — get early price", fontWeight = FontWeight.Black)
        }
        OutlinedButton(
            onClick = { uri.openUri("https://github.com/scammer0073-glitch/hermes-companion/blob/main/NEMASYS.md") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.CardBorder),
        ) { Text("Read business doc", color = NemasysPalette.TextBright) }
        Text(
            "Open source promise: app stays Apache 2.0. Payments via Razorpay/UPI + Play Billing soon.",
            style = MaterialTheme.typography.labelSmall,
            color = NemasysPalette.Muted,
        )
    }
}

@Composable private fun HubStep(
    n: String,
    t: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(50), color = NemasysPalette.AccentOverlay) {
            Text(
                n,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                color = NemasysPalette.Accent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(t, color = NemasysPalette.TextBright, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun PriceCard(
    title: String,
    price: String,
    badge: String,
    feats: List<String>,
    mod: Modifier,
    featured: Boolean = false,
    nemasysBlack: Boolean = false,
) {
    val cardColor =
        if (nemasysBlack) {
            NemasysPalette.Card
        } else if (featured) {
            // Keep the featured plan on the Nemasys Black card surface; the accent border carries emphasis.
            NemasysPalette.Card
        } else {
            NemasysPalette.Card
        }
    val cardBorder = if (featured && !nemasysBlack) NemasysPalette.Accent else NemasysPalette.CardBorder
    Card(
        mod,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NemasysPalette.Accent),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = NemasysPalette.TextBright,
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (featured) NemasysPalette.Accent else NemasysPalette.CardBorder,
                ) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (featured) NemasysPalette.OnAccent else NemasysPalette.Muted,
                    )
                }
            }
            Text(price, fontWeight = FontWeight.Black, fontSize = 22.sp, color = NemasysPalette.TextBright)
            feats.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = NemasysPalette.Accent,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(it, style = MaterialTheme.typography.labelSmall, color = NemasysPalette.Muted)
                }
            }
        }
    }
}
