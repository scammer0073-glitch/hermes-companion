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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m57.hermescontrol.theme.LocalSpacing
import com.m57.hermescontrol.theme.NemasysCard
import com.m57.hermescontrol.theme.NemasysCardBorder
import com.m57.hermescontrol.theme.NemasysPalette

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier.fillMaxSize().padding(24.dp).testTag("loading_state"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Shimmer skeleton list instead of naked spinner - feels like content loading
        repeat(3) { i ->
            ShimmerBar(
                width = if (i == 0) 220.dp else 280.dp,
                height = 14.dp,
                modifier = Modifier.padding(vertical = 6.dp).testTag("loading_skeleton"),
            )
            ShimmerBar(
                width = 320.dp,
                height = 10.dp,
                modifier = Modifier.padding(bottom = if (i == 2) 0.dp else 18.dp).testTag("loading_skeleton"),
            )
        }
        if (subtitle != null) {
            Spacer(Modifier.height(16.dp))
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = NemasysPalette.Muted)
        }
    }
}

@Composable
private fun ShimmerBar(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val t = rememberInfiniteTransition(label = "shimmer")
    val x by t.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "x",
    )
    val brush =
        Brush.linearGradient(
            listOf(NemasysPalette.ShimmerBase, NemasysPalette.ShimmerHighlight, NemasysPalette.ShimmerBase),
            start = Offset(x - 300f, 0f),
            end = Offset(x, 0f),
        )
    Box(modifier.clip(RoundedCornerShape(8.dp)).width(width).height(height).background(brush))
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val s = LocalSpacing.current
    Column(
        modifier.fillMaxSize().padding(s.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = NemasysPalette.ErrorContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.ErrorBorder),
        ) {
            Icon(
                Icons.Filled.Refresh,
                null,
                tint = NemasysPalette.Error,
                modifier = Modifier.padding(12.dp).size(22.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            color = NemasysPalette.Text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.testTag("error_retry_button"),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = NemasysPalette.Accent,
                        contentColor = NemasysPalette.OnAccent,
                    ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Filled.Refresh, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Try again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val s = LocalSpacing.current
    Column(
        modifier.fillMaxSize().padding(s.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
            border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = NemasysCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NemasysCardBorder),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                    contentColor = NemasysPalette.Text,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = NemasysPalette.Text, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(NemasysPalette.Accent))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = title.uppercase(),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = NemasysPalette.Text,
                        textAlign = TextAlign.Center,
                    )
                }
                if (subtitle != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        subtitle,
                        color = NemasysPalette.Muted,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
                if (actionLabel != null && onAction != null) {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onAction,
                        modifier = Modifier.testTag("empty_state_action"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = NemasysPalette.Accent,
                                contentColor = NemasysPalette.OnAccent,
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(actionLabel, fontWeight = FontWeight.Bold)
                    }
                }
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
