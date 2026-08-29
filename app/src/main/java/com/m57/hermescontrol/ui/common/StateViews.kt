package com.m57.hermescontrol.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m57.hermescontrol.R
import com.m57.hermescontrol.theme.LocalSpacing

@Composable
fun LoadingState(modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        // Shimmer skeleton list instead of naked spinner - feels like content loading
        repeat(3) { i ->
            ShimmerBar(width = if (i==0) 220.dp else 280.dp, height = 14.dp, modifier = Modifier.padding(vertical = 6.dp))
            ShimmerBar(width = 320.dp, height = 10.dp, modifier = Modifier.padding(bottom = if(i==2) 0.dp else 18.dp))
        }
        if (subtitle != null) {
            Spacer(Modifier.height(16.dp))
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = Color(0xFF8B9AB0))
        }
    }
}

@Composable
private fun ShimmerBar(width: Dp, height: Dp, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "shimmer")
    val x by t.animateFloat(initialValue = 0f, targetValue = 800f, animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Restart), label = "x")
    val brush = Brush.linearGradient(listOf(Color(0xFF13171C), Color(0xFF1A232F), Color(0xFF13171C)), start = Offset(x-300f, 0f), end = Offset(x, 0f))
    Box(modifier.clip(RoundedCornerShape(8.dp)).width(width).height(height).background(brush))
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val s = LocalSpacing.current
    Column(modifier.fillMaxSize().padding(s.lg), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFF2A1212), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3A1A1A))) {
            Icon(Icons.Filled.Refresh, null, tint = Color(0xFFFF4D4D), modifier = Modifier.padding(12.dp).size(22.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(message, color = Color(0xFFE6EDF3), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 16.dp))
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF), contentColor = Color(0xFF001018)), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.Refresh, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Try again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String? = null, icon: ImageVector = Icons.Outlined.Inbox, actionLabel: String? = null, onAction: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val s = LocalSpacing.current
    Column(modifier.fillMaxSize().padding(s.lg), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(Color(0xFF111820)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Color(0xFF8B9AB0), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 12.dp))
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF), contentColor = Color(0xFF001018)), shape = RoundedCornerShape(12.dp)) {
                Text(actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SkeletonListState(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(5) { ShimmerBar(width = 320.dp, height = 72.dp, modifier = Modifier.fillMaxWidth()) }
    }
}

// ── Padding conventions (shared)
val listContentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
val listItemSpacing = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
