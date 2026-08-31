package com.m57.hermescontrol.ui.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m57.hermescontrol.R
import com.m57.hermescontrol.data.config.resolveBaseUrl
import com.m57.hermescontrol.ui.settings.SectionCard
import com.m57.hermescontrol.ui.settings.SettingsUiState
import com.m57.hermescontrol.ui.settings.SettingsViewModel

@Composable
internal fun ConnectionSection(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    passwordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
) {
    SectionCard {
        // ── Saved profiles list ──────────────────────────────────
        if (state.profiles.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2DD4BF)),
                )
                Text(
                    text = stringResource(R.string.settings_saved_profiles, state.profiles.size).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = Color(0xFF8B9AB0),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            state.profiles.forEach { profile ->
                val isActive = profile.id == state.selectedProfileId
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable(role = Role.Button) { viewModel.selectProfile(profile.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0F12)),
                    border =
                        if (isActive) {
                            BorderStroke(1.dp, Color(0xFF2DD4BF))
                        } else {
                            BorderStroke(1.dp, Color(0xFF1E2D44))
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                color = Color.White,
                            )
                            Text(
                                text = profile.resolveBaseUrl(state.baseUrl),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8B9AB0),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                            IconButton(
                                onClick = { viewModel.openEditProfile(profile.id) },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = stringResource(R.string.settings_action_edit_profile),
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFF8B9AB0),
                                )
                            }
                            IconButton(
                                onClick = { viewModel.requestDeleteProfile(profile.id) },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.content_desc_delete_profile),
                                    tint = Color(0xFF8B9AB0),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Add profile button ───────────────────────────────────────
        OutlinedButton(
            onClick = viewModel::openAddProfile,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2DD4BF)),
            border = BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.5f)),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(stringResource(R.string.settings_action_add_profile))
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────

    if (state.showProfileDialog) {
        ProfileEditorDialog(
            isEditing = state.editingProfileId != null,
            name = state.dialogProfileName,
            baseUrl = state.dialogProfileBaseUrl,
            token = state.dialogProfileToken,
            error = state.dialogProfileError,
            onNameChange = viewModel::onDialogProfileNameChange,
            onBaseUrlChange = viewModel::onDialogProfileBaseUrlChange,
            onTokenChange = viewModel::onDialogProfileTokenChange,
            onSave = viewModel::saveProfileFromDialog,
            onDismiss = viewModel::closeProfileDialog,
        )
    }

    if (state.showDeleteConfirm) {
        DeleteProfileConfirmDialog(
            profileName = state.profileToDeleteName,
            onConfirm = viewModel::confirmDeleteProfile,
            onDismiss = viewModel::cancelDeleteProfile,
        )
    }
}

@Composable
internal fun TestResultCard(testResult: String?) {
    AnimatedVisibility(
        visible = testResult != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        testResult?.let { result ->
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            if (result.startsWith("✅")) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            },
                    ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = result,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
internal fun TestConnectionButton(
    isTesting: Boolean,
    onTest: () -> Unit,
) {
    OutlinedButton(
        onClick = onTest,
        enabled = !isTesting,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isTesting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(stringResource(R.string.settings_action_test_connection))
        }
    }
}
