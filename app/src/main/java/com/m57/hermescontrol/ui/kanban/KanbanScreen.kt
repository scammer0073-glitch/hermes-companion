package com.m57.hermescontrol.ui.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import com.m57.hermescontrol.data.model.KanbanTask
import com.m57.hermescontrol.ui.common.ErrorState
import com.m57.hermescontrol.ui.common.HermesScaffold
import com.m57.hermescontrol.ui.common.NavIcon
import com.m57.hermescontrol.ui.common.SearchBar
import com.m57.hermescontrol.ui.common.SkeletonListState
import com.m57.hermescontrol.ui.common.EmptyState
import com.m57.hermescontrol.ui.common.ToastEffect

private const val DEFAULT_COLUMN = "todo"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanScreen(
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    viewModel: KanbanViewModel = viewModel { KanbanViewModel() },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }

    val filteredTasks =
        remember(query, state.tasks) {
            state.tasks.filter { task ->
                task.title.contains(query, ignoreCase = true) ||
                    task.description?.contains(query, ignoreCase = true) == true ||
                    task.status.contains(query, ignoreCase = true) ||
                    task.assignedTo?.contains(query, ignoreCase = true) == true
            }
        }

    val tasksByColumn =
        remember(filteredTasks) {
            filteredTasks.groupBy { it.status.lowercase() }
        }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskForActions by remember { mutableStateOf<KanbanTask?>(null) }
    var confirmTarget by remember { mutableStateOf<Pair<KanbanTask, KanbanTaskAction>?>(null) }
    var summaryTarget by remember { mutableStateOf<Pair<KanbanTask, KanbanTaskAction>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBoards()
    }

    ToastEffect(toastMessage = state.toastMessage, onClearToast = viewModel::clearToast)

    HermesScaffold(
        title = { Text(stringResource(R.string.kanban_board_title)) },
        navigationIcon = onOpenDrawer?.let { NavIcon.Menu(it) },
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.loadBoards() },
    ) { paddingValues ->
        when {
            state.isLoading && state.boards.isEmpty() -> {
                SkeletonListState(modifier = Modifier.padding(paddingValues))
            }

            state.errorMessage != null -> {
                ErrorState(
                    message = state.errorMessage ?: "",
                    onRetry = { viewModel.loadBoards() },
                    modifier = Modifier.padding(paddingValues),
                )
            }

            else -> {
                Box(Modifier.fillMaxSize()) {
                    if (state.isLoading) {
                        CircularProgressIndicator()
                    } else if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(paddingValues),
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            SearchBar(
                                query = query,
                                onQueryChange = { query = it },
                                placeholder = stringResource(R.string.kanban_filter_placeholder),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                            // Board selector tab row
                            if (state.boards.isNotEmpty()) {
                                PrimaryScrollableTabRow(
                                    selectedTabIndex = state.boards.indexOf(state.selectedBoard).coerceAtLeast(0),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    state.boards.forEach { board ->
                                        Tab(
                                            selected = board == state.selectedBoard,
                                            onClick = { viewModel.selectBoard(board) },
                                            text = { Text(board.name) },
                                        )
                                    }
                                }
                            }

                            if (state.selectedBoard == null) {
                                EmptyState(title = "No boards yet", subtitle = "Boards organize your work — create one to start", icon = androidx.compose.material.icons.Icons.Filled.Add)
                            } else if (state.columns.isEmpty()) {
                                EmptyState(title = "No columns", subtitle = "This board has no columns yet — add one to track work", icon = androidx.compose.material.icons.Icons.Filled.Person)
                            } else {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    LiveStatusPill(isLive = state.isLive)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { showAddTaskDialog = true }) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = stringResource(R.string.kanban_add_task),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                LazyRow(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding =
                                        PaddingValues(
                                            start = 16.dp,
                                            top = 16.dp,
                                            end = 16.dp,
                                            bottom = 16.dp,
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    items(state.columns.size, key = { index ->
                                        state.columns[index].name
                                    }) { columnIndex ->
                                        val column = state.columns[columnIndex]
                                        val colName = column.name
                                        val colTasks = tasksByColumn[colName.lowercase()] ?: emptyList()

                                        Column(
                                            modifier =
                                                Modifier
                                                    .width(280.dp)
                                                    .fillMaxSize(),
                                        ) {
                                            Text(
                                                text = "${colName.replaceFirstChar { it.uppercase() }} (${
                                                    colTasks.size
                                                })",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 8.dp),
                                            )

                                            if (colTasks.isEmpty()) {
                                                androidx.compose.material3.Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), color = androidx.compose.ui.graphics.Color(0xFF0D0F12), border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF1E2D44))) { Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Filled.Add, null, tint = androidx.compose.ui.graphics.Color(0xFF2DD4BF), modifier = Modifier.size(14.dp)); androidx.compose.foundation.layout.Spacer(Modifier.width(6.dp)); androidx.compose.material3.Text("Drop tasks here", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = androidx.compose.ui.graphics.Color(0xFF8B9AB0)) } }
                                            } else {
                                                LazyColumn(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    items(colTasks, key = { it.id }) { task ->
                                                        TaskCard(
                                                            task = task,
                                                            onTaskClick = { taskForActions = it },
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showAddTaskDialog) {
                        AddTaskDialog(
                            onDismiss = { showAddTaskDialog = false },
                            onConfirm = { title, desc ->
                                viewModel.createTask(title, desc, state.columns.firstOrNull()?.name ?: DEFAULT_COLUMN)
                                showAddTaskDialog = false
                            },
                        )
                    }

                    taskForActions?.let { task ->
                        val actions = kanbanActionsForStatus(task.status)
                        if (actions.isNotEmpty()) {
                            TaskActionSheet(
                                task = task,
                                actions = actions,
                                onAction = { action ->
                                    taskForActions = null
                                    when {
                                        action.needsSummary -> summaryTarget = task to action
                                        action.needsConfirm -> confirmTarget = task to action
                                        else -> viewModel.moveTask(task, action)
                                    }
                                },
                                onDismiss = { taskForActions = null },
                            )
                        }
                    }

                    confirmTarget?.let { (task, action) ->
                        ConfirmActionDialog(
                            message = stringResource(action.confirmRes()),
                            onConfirm = {
                                confirmTarget = null
                                if (action.needsSummary) {
                                    summaryTarget = task to action
                                } else {
                                    viewModel.moveTask(task, action)
                                }
                            },
                            onDismiss = { confirmTarget = null },
                        )
                    }

                    summaryTarget?.let { (task, action) ->
                        CompleteTaskDialog(
                            onConfirm = { summary -> viewModel.moveTask(task, action, summary) },
                            onDismiss = { summaryTarget = null },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: KanbanTask,
    onTaskClick: (KanbanTask) -> Unit,
) {
    androidx.compose.material3.Card(onClick = { onTaskClick(task) }, modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF0D0F12)), border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF1E2D44)), elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            task.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            task.assignedTo?.let { assignee ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = assignee,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveStatusPill(isLive: Boolean) {
    val color =
        if (isLive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(color = color, shape = CircleShape),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(if (isLive) R.string.kanban_live else R.string.kanban_offline),
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.kanban_add_new_task)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.kanban_task_title)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(stringResource(R.string.kanban_task_desc)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, desc.ifBlank { null }) },
                enabled = title.isNotBlank(),
            ) {
                Text(stringResource(R.string.action_add))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

private fun KanbanTaskAction.labelRes(): Int =
    when (this) {
        KanbanTaskAction.TRIAGE -> R.string.kanban_action_triage
        KanbanTaskAction.READY -> R.string.kanban_action_ready
        KanbanTaskAction.UNBLOCK -> R.string.kanban_action_unblock
        KanbanTaskAction.BLOCK -> R.string.kanban_action_block
        KanbanTaskAction.COMPLETE -> R.string.kanban_action_complete
        KanbanTaskAction.ARCHIVE -> R.string.kanban_action_archive
    }

private fun KanbanTaskAction.confirmRes(): Int =
    when (this) {
        KanbanTaskAction.BLOCK -> R.string.kanban_confirm_blocked
        KanbanTaskAction.COMPLETE -> R.string.kanban_confirm_done
        KanbanTaskAction.ARCHIVE -> R.string.kanban_confirm_archive
        else -> error("Action $this has no confirm message")
    }

@Composable
private fun TaskActionSheet(
    task: KanbanTask,
    actions: List<KanbanTaskAction>,
    onAction: (KanbanTaskAction) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(task.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        text = {
            Column {
                actions.forEach { action ->
                    Button(
                        onClick = { onAction(action) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                    ) {
                        Text(stringResource(action.labelRes()))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun ConfirmActionDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun CompleteTaskDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var summary by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.kanban_complete_title)) },
        text = {
            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text(stringResource(R.string.kanban_complete_summary_label)) },
                supportingText = { Text(stringResource(R.string.kanban_complete_summary_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(summary.trim()) },
                enabled = summary.isNotBlank(),
            ) {
                Text(stringResource(R.string.kanban_action_complete))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
