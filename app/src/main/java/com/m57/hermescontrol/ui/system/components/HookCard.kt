package com.m57.hermescontrol.ui.system.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m57.hermescontrol.R
import com.m57.hermescontrol.data.model.HookEntry
import com.m57.hermescontrol.theme.NemasysPalette
import com.m57.hermescontrol.ui.common.StatusBadge
import com.m57.hermescontrol.ui.common.StatusBadgeType

@Composable
internal fun HookCard(
    hook: HookEntry,
    spacing: com.m57.hermescontrol.theme.Spacing,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
        border = BorderStroke(1.dp, NemasysPalette.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NemasysPalette.Accent),
                    )
                    Spacer(modifier = Modifier.width(spacing.xs))
                    Text(
                        text = (hook.command ?: "?").uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = NemasysPalette.Muted,
                    )
                }
                Spacer(modifier = Modifier.height(spacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text(
                        text = (hook.event ?: "EVENT UNSPECIFIED").uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = NemasysPalette.Muted,
                    )
                    hook.matcher?.let { matcher ->
                        Text(
                            text = stringResource(R.string.system_hooks_matcher, matcher),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    StatusBadge(
                        text =
                            if (hook.executable != false) {
                                stringResource(R.string.system_hooks_allowed)
                            } else {
                                stringResource(R.string.system_hooks_not_executable)
                            },
                        status =
                            if (hook.executable != false) StatusBadgeType.SUCCESS else StatusBadgeType.ERROR,
                    )
                    StatusBadge(
                        text =
                            if (hook.allowed == true) {
                                stringResource(R.string.system_hooks_allowed)
                            } else {
                                stringResource(R.string.system_hooks_not_approved)
                            },
                        status =
                            if (hook.allowed == true) StatusBadgeType.SUCCESS else StatusBadgeType.WARNING,
                    )
                    hook.timeout?.let { t ->
                        StatusBadge(
                            text = "${t}s",
                            status = StatusBadgeType.NEUTRAL,
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
