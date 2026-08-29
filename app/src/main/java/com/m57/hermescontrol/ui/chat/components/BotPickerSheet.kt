package com.m57.hermescontrol.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m57.hermescontrol.R
import com.m57.hermescontrol.data.model.ProfileInfo
import com.m57.hermescontrol.ui.common.ErrorState
import com.m57.hermescontrol.ui.common.LoadingState
import com.m57.hermescontrol.ui.profiles.ProfilesViewModel

/**
 * Chat-header bot picker. Same idea as a desktop bot menu: search, tap to
 * switch, then stay in chat. Backed by Hermes server profiles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotPickerSheet(
    onDismiss: () -> Unit,
    onManageBots: () -> Unit,
    onCreateBot: () -> Unit,
    viewModel: ProfilesViewModel = viewModel { ProfilesViewModel() },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.loadProfiles()
    }

    val filtered =
        remember(state.profiles, query) {
            val q = query.trim()
            if (q.isEmpty()) {
                state.profiles
            } else {
                state.profiles.filter { profile ->
                    profile.name.contains(q, ignoreCase = true) ||
                        (profile.description?.contains(q, ignoreCase = true) == true) ||
                        (profile.model?.contains(q, ignoreCase = true) == true)
                }
            }
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp),
        ) {
            Text(
                text = stringResource(R.string.bots_picker_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.bots_picker_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                    )
                },
                placeholder = { Text(stringResource(R.string.bots_picker_search)) },
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            when {
                state.isLoading && state.profiles.isEmpty() -> {
                    LoadingState(modifier = Modifier.height(180.dp))
                }
                state.errorMessage != null && state.profiles.isEmpty() -> {
                    ErrorState(
                        message = state.errorMessage ?: "",
                        onRetry = { viewModel.loadProfiles() },
                        modifier = Modifier.height(180.dp),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.height(420.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(filtered, key = { it.name }) { profile ->
                            BotPickerRow(
                                profile = profile,
                                isActive = profile.name == state.activeProfileName,
                                onClick = {
                                    if (profile.name != state.activeProfileName) {
                                        viewModel.selectActiveProfile(profile.name)
                                    }
                                    onDismiss()
                                },
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = androidx.compose.ui.graphics.Color(0xFF1E2D44))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onManageBots) {
                    Text(stringResource(R.string.bots_picker_manage))
                }
                TextButton(onClick = onCreateBot) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.profiles_builder_action_create))
                }
            }
        }
    }
}

@Composable
internal fun BotPickerRow(
    profile: ProfileInfo,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color =
            if (isActive) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surface
            },
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color =
                    if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                contentColor =
                    if (isActive) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle =
                    profile.description?.takeIf { it.isNotBlank() }
                        ?: listOfNotNull(profile.provider, profile.model).joinToString(" / ")
                            .ifBlank { stringResource(R.string.profiles_description_placeholder) }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isActive) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.profiles_content_desc_active),
                    tint = androidx.compose.ui.graphics.Color(0xFF2DD4BF),
                )
            }
        }
    }
}
