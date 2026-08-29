package com.m57.hermescontrol.ui.chat.fullbleed

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m57.hermescontrol.data.ws.CommandCatalog
import com.m57.hermescontrol.theme.HermesControlTheme
import com.m57.hermescontrol.theme.ThemePreference
import com.m57.hermescontrol.theme.ThemePreset
import com.m57.hermescontrol.ui.chat.ChatBubble
import com.m57.hermescontrol.ui.chat.ChatMessage
import com.m57.hermescontrol.ui.chat.MessageRole
import com.m57.hermescontrol.ui.chat.components.ChatInputBar
import com.m57.hermescontrol.ui.landing.LandingScreen

private const val PREVIEW_TIMESTAMP = 1_725_600_000_000L

@Preview(
    name = "Hermes • Landing",
    device = Devices.PIXEL_4,
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun GrokInspiredLandingPreview() {
    HermesControlTheme(
        themePreference = ThemePreference.DARK,
        themePreset = ThemePreset.AMOLED,
        useDynamicColors = false,
    ) {
        LandingScreen(onAuthLogin = {})
    }
}

@Preview(
    name = "Hermes • Research chat",
    device = Devices.PIXEL_4,
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun GrokInspiredStaticChatPreview() {
    HermesControlTheme(
        themePreference = ThemePreference.DARK,
        themePreset = ThemePreset.AMOLED,
        useDynamicColors = false,
    ) {
        StaticResearchChat()
    }
}

@Composable
private fun StaticResearchChat() {
    var input by remember { mutableStateOf(TextFieldValue()) }
    val userMessage =
        remember {
            ChatMessage(
                id = "preview-user",
                role = MessageRole.USER,
                content = "Help me organize a research project on trustworthy autonomous agents.",
                timestamp = PREVIEW_TIMESTAMP,
            )
        }
    val assistantMessage =
        remember {
            ChatMessage(
                id = "preview-assistant",
                role = MessageRole.ASSISTANT,
                content =
                    """
                    **A clean way to start:**

                    1. Define one testable research question.
                    2. Build a source map around safety, evaluation, and oversight.
                    3. Track claims, evidence, and open questions in one review table.

                    I can turn this into a milestone plan next.
                    """.trimIndent(),
                timestamp = PREVIEW_TIMESTAMP + 60_000L,
            )
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HermesPreviewHeader()
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
            )

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = "A focused workspace for thinking, researching, and building.",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(32.dp))
                ChatBubble(message = userMessage)
                Spacer(modifier = Modifier.height(18.dp))
                FullBleedAgentMessage(
                    message = assistantMessage,
                    showTurnHeader = true,
                    isDarkTheme = true,
                )
            }

            ChatInputBar(
                inputFieldValue = input,
                onInputChange = { input = it },
                onSend = {},
                onMicTap = {},
                isListening = false,
                isAgentTyping = false,
                isConnected = true,
                commandCatalog = CommandCatalog(),
            )
        }
    }
}

@Composable
private fun HermesPreviewHeader() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "HERMES",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "RESEARCH MODE",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
