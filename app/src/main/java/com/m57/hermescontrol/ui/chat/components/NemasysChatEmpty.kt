package com.m57.hermescontrol.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NemasysChatEmpty(
    botName: String,
    onPrompt: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF111820), border = BorderStroke(1.dp, Color(0xFF1E2D44))) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF2DD4BF)) {
                    androidx.compose.foundation.layout.Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Text("N", color = Color(0xFF001018), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(botName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("Ask anything — tools, memory, cron, all here", color = Color(0xFF8B9AB0), fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Start a conversation", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White, textAlign = TextAlign.Center)
        Text("Try one of these — or just type", color = Color(0xFF8B9AB0), fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        val prompts = listOf(
            Triple(Icons.Filled.Code, "Explain this repo", "Walk me through the architecture"),
            Triple(Icons.Filled.Schedule, "Make a cron", "Run a daily standup at 9am"),
            Triple(Icons.Filled.AutoAwesome, "Create a bot", "A bras-measurement assistant"),
            Triple(Icons.Filled.Bolt, "Check health", "What's running, what's failing?"),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            prompts.forEach { (icon, title, sub) ->
                Surface(
                    onClick = { onPrompt(sub) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0D0F12),
                    border = BorderStroke(1.dp, Color(0xFF1E2D44)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF16202E)) {
                            Icon(icon, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.padding(8.dp).size(16.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                            Text(sub, color = Color(0xFF8B9AB0), fontSize = 11.sp)
                        }
                        Text("→", color = Color(0xFF2DD4BF), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Tip: / for commands • @ to mention files • self-host free, Hub from ₹199", color = Color(0xFF5A6B84), fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}
