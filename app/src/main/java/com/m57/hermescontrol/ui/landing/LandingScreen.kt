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
import com.m57.hermescontrol.theme.NemasysPalette

@Composable
fun LandingScreen(
    onAuthLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg =
        Brush.verticalGradient(
            colors = listOf(NemasysPalette.Background, NemasysPalette.SecondarySurface, NemasysPalette.Background),
        )
    val uriHandler = LocalUriHandler.current
    val accent = Brush.linearGradient(listOf(NemasysPalette.Accent, NemasysPalette.Info))
    Box(
        modifier = modifier.fillMaxSize().background(bg),
    ) {
        // subtle glow
        Box(
            modifier =
                Modifier.fillMaxWidth().height(280.dp).background(
                    Brush.radialGradient(
                        colors = listOf(NemasysPalette.AccentOverlay, Color.Transparent),
                        radius = 600f,
                    ),
                ),
        )
        Column(
            modifier =
                Modifier.fillMaxSize().statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 18.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("N", color = NemasysPalette.OnAccent, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        stringResource(R.string.landing_brand),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = NemasysPalette.TextBright,
                    )
                    Text(
                        stringResource(R.string.landing_brand_caption),
                        style = MaterialTheme.typography.labelSmall,
                        color = NemasysPalette.Muted,
                        letterSpacing = 0.8.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = NemasysPalette.SelectedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.CardBorder),
                ) {
                    Text(
                        stringResource(R.string.landing_private_badge),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = NemasysPalette.Accent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    )
                }
            }
            Spacer(Modifier.height(36.dp))
            Text(
                "Your Hermes,",
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        color = NemasysPalette.TextBright,
                        lineHeight = 38.sp,
                    ),
                fontSize = 38.sp,
            )
            Text(
                "everywhere.",
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        color = NemasysPalette.Accent,
                    ),
                fontSize = 38.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.landing_subtitle),
                style = MaterialTheme.typography.bodyLarge.copy(color = NemasysPalette.Muted, lineHeight = 24.sp),
            )
            Spacer(Modifier.height(22.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NemasysPalette.Card,
                border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.CardBorder),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Nemasys Hub",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NemasysPalette.TextBright,
                        )
                        Text(
                            "Managed hosting from ₹199/mo — no PC needed",
                            style = MaterialTheme.typography.labelSmall,
                            color = NemasysPalette.Muted,
                        )
                    }
                    Badge()
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LandingCapability(
                    Icons.Filled.SmartToy,
                    stringResource(R.string.landing_capability_bots),
                    "Bots = Profiles",
                    Modifier.weight(1f),
                )
                LandingCapability(
                    Icons.Filled.Memory,
                    stringResource(R.string.landing_capability_memory),
                    "Memory & Skills",
                    Modifier.weight(1f),
                )
                LandingCapability(
                    Icons.Filled.VerifiedUser,
                    stringResource(R.string.landing_capability_tools),
                    "Tools & Cron",
                    Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onAuthLogin,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = NemasysPalette.Accent,
                        contentColor = NemasysPalette.OnAccent,
                    ),
            ) {
                Text(
                    stringResource(R.string.landing_action_auth_login),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { uriHandler.openUri("https://scammer0073-glitch.github.io/hermes-companion/#pricing") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.CardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NemasysPalette.TextBright),
            ) {
                Icon(
                    Icons.Filled.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = NemasysPalette.Warning,
                )
                Spacer(Modifier.width(8.dp))
                Text("Explore Nemasys Hub", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.landing_connection_hint),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = NemasysPalette.Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Badge() {
    Surface(shape = RoundedCornerShape(50), color = NemasysPalette.AccentOverlay) {
        Text(
            "COMING SOON",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = NemasysPalette.Accent,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.8.sp,
        )
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
        modifier =
            modifier.border(
                1.dp,
                NemasysPalette.CardBorder,
                RoundedCornerShape(16.dp),
            ).background(NemasysPalette.Card, RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(NemasysPalette.SelectedSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = NemasysPalette.Accent, modifier = Modifier.size(18.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = NemasysPalette.TextBright,
            fontWeight = FontWeight.Bold,
        )
        Text(
            sub,
            style = MaterialTheme.typography.labelSmall,
            color = NemasysPalette.Muted,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            fontSize = 11.sp,
        )
    }
}
