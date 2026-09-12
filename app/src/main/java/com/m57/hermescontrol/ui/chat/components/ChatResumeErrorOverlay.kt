package com.m57.hermescontrol.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m57.hermescontrol.R

/**
 * Full-screen overlay shown when a session resume has exhausted its bounded
 * auto-retries (desktop parity). Gives the user an explicit error + manual
 * Retry instead of an infinite spinner.
 */
@Composable
fun ChatResumeErrorOverlay(
    errorMessage: String?,
    onRetry: () -> Unit,
) {
    AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF1E2D44)),
                colors =
                    CardDefaults.cardColors(
                        containerColor = Color(0xFF0D0F12),
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // mono 11sp uppercase header with teal dot
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2DD4BF)))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.chat_resume_error_title).uppercase(),
                            color = Color(0xFF8B9AB0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF2A1212)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF4D4D),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B9AB0),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF), contentColor = Color(0xFF001018)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(stringResource(R.string.action_retry), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
