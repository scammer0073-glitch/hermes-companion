package com.m57.hermescontrol.ui.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m57.hermescontrol.R

@Composable
fun LandingScreen(
    onAuthLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0E14), Color(0xFF111A26), Color(0xFF0A0E14))
    )
    val uriHandler = LocalUriHandler.current
    val accent = Brush.linearGradient(listOf(Color(0xFF2DD4BF), Color(0xFF38BDF8)))
    Box(
        modifier = modifier.fillMaxSize().background(bg)
    ) {
        // subtle glow
        Box(modifier = Modifier.fillMaxWidth().height(280.dp).background(
            Brush.radialGradient(
                colors = listOf(Color(0x332DD4BF), Color.Transparent),
                radius = 600f
            )
        ))
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("N", color = Color(0xFF001018), fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.landing_brand), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp, color = Color.White)
                    Text(stringResource(R.string.landing_brand_caption), style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9AB0), letterSpacing = 0.8.sp)
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = Color(0xFF16202E), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))) {
                    Text(stringResource(R.string.landing_private_badge), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2DD4BF), fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
                }
            }
            Spacer(Modifier.height(36.dp))
            Text("Your Hermes,", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, color = Color.White, lineHeight = 38.sp), fontSize = 38.sp)
            Text("everywhere.", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF2DD4BF)), fontSize = 38.sp)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.landing_subtitle), style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF8B9AB0), lineHeight = 24.sp))
            Spacer(Modifier.height(22.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF111820), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Nemasys Hub", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Managed hosting from ₹199/mo — no PC needed", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9AB0))
                    }
                    Badge()
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LandingCapability(Icons.Filled.SmartToy, stringResource(R.string.landing_capability_bots), "Bots = Profiles", Modifier.weight(1f))
                LandingCapability(Icons.Filled.Memory, stringResource(R.string.landing_capability_memory), "Memory & Skills", Modifier.weight(1f))
                LandingCapability(Icons.Filled.VerifiedUser, stringResource(R.string.landing_capability_tools), "Tools & Cron", Modifier.weight(1f))
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onAuthLogin,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF), contentColor = Color(0xFF001018))
            ) {
                Text(stringResource(R.string.landing_action_auth_login), fontWeight = FontWeight.Black, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { uriHandler.openUri("https://scammer0073-glitch.github.io/hermes-companion/#pricing") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFACC15))
                Spacer(Modifier.width(8.dp))
                Text("Explore Nemasys Hub", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.landing_connection_hint), modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9AB0), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Badge() {
    Surface(shape = RoundedCornerShape(50), color = Color(0x332DD4BF)) {
        Text("COMING SOON", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2DD4BF), fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.8.sp)
    }
}

@Composable
private fun LandingCapability(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    sub: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(18.dp)).background(Color(0xFF111820), RoundedCornerShape(18.dp)).padding(horizontal = 10.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF16202E)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(18.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Text(sub, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9AB0), textAlign = TextAlign.Center, lineHeight = 12.sp, fontSize = 11.sp)
    }
}
