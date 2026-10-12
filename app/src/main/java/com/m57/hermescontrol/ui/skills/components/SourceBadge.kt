package com.m57.hermescontrol.ui.skills.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m57.hermescontrol.theme.NemasysPalette

@Composable
internal fun SourceBadge(source: String) {
    val (color, label) =
        when (source) {
            "hub" -> MaterialTheme.colorScheme.tertiary to "hub"
            "built-in" -> MaterialTheme.colorScheme.secondary to "built-in"
            "optional" -> MaterialTheme.colorScheme.outline to "opt"
            else -> MaterialTheme.colorScheme.outline to source
        }
    Surface(
        color = NemasysPalette.Card,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, NemasysPalette.CardBorder),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(5.dp)
                    .background(NemasysPalette.Accent, CircleShape),
            )
            Text(
                text = label.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = color,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}
