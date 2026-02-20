package com.svp.taskhelpercomposemvi.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.model.TaskPriority
import com.svp.taskhelpercomposemvi.domain.model.TaskStatus
import com.svp.taskhelpercomposemvi.view.ui.theme.MyAppTheme
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Main Task Screen - Composable UI
 * DIP: Depends on ViewModel for state management, not on data layer directly
 * OCP: UI can evolve independently of business logic, allowing for new features or design changes
 * SRP: Only responsible for rendering the Task Screen UI, not handling data fetching or business logic
 * LSP: Can be extended with new UI components or features without breaking existing functionality
 * ISP: Does not force clients to depend on methods they do not use, as it only interacts with the ViewModel for necessary data and actions
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = hiltViewModel()
) {
    // Single state collection point — avoids double collection in child Composables
    val state by viewModel.state.collectAsState()
    val showAddTaskDialog = remember { mutableStateOf(false) }

    // SnackbarHostState lives here so both error and success can use it
    val snackbarHostState = remember { SnackbarHostState() }

    // Side effect: Use error string as key so LaunchedEffect only fires when
    // a NEW error arrives. Show it in a Snackbar and then clear it from state.
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
        }
    }

    // Side effect: Consume successMessage properly — show Snackbar then let
    // ViewModel clear it (ViewModel already auto-clears after 2 s via clearSuccessMessage).
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Task Manager",
                        style = MyAppTheme.typography.titleLarge,
                        color = MyAppTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MyAppTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog.value = true },
                containerColor = MyAppTheme.colorScheme.primary,
                contentColor = MyAppTheme.colorScheme.onPrimary,
                shape = MyAppTheme.shape.button
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        // Side effect FIX: SnackbarHost wired into Scaffold so it respects insets
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MyAppTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Side effect FIX: Pass state and lambda instead of whole ViewModel
            // — avoids independent state collection inside FilterSelection
            FilterSelection(
                selectedStatusFilter = state.selectedStatusFilter,
                onFilterSelected = { status ->
                    viewModel.processIntent(TaskIntent.FilterByStatus(status))
                }
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MyAppTheme.colorScheme.primary
                        )
                    }
                }

                state.tasks.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks found. Click + to add a new task.",
                            style = MyAppTheme.typography.body,
                            color = MyAppTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(MyAppTheme.size.medium),
                        verticalArrangement = Arrangement.spacedBy(MyAppTheme.size.small)
                    ) {
                        items(state.tasks, key = { it.id }) { task ->
                            TaskItem(
                                task = task,
                                onStatusChange = { newStatus ->
                                    viewModel.processIntent(
                                        TaskIntent.UpdateTaskStatus(task.id, newStatus)
                                    )
                                },
                                onDelete = {
                                    viewModel.processIntent(TaskIntent.DeleteTaskById(task.id))
                                }
                            )
                        }
                    }
                }
            }
        }

        // Side effect FIX: Dialog is now INSIDE Scaffold content so it is
        // properly layered within the scaffold's surface and insets
        if (showAddTaskDialog.value) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog.value = false },
                onConfirm = { title, description, priority ->
                    viewModel.processIntent(
                        TaskIntent.CreateTask(
                            title = title,
                            description = description,
                            priority = priority
                        )
                    )
                    showAddTaskDialog.value = false
                }
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Side effect FIX: Create SimpleDateFormat once per composition, not on every recompose
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MyAppTheme.shape.container,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MyAppTheme.colorScheme.onPrimary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MyAppTheme.size.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MyAppTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MyAppTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(MyAppTheme.size.small / 2))
                    Text(
                        text = task.description,
                        style = MyAppTheme.typography.labelNormal,
                        color = MyAppTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete Task",
                        tint = MyAppTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(MyAppTheme.size.small))

            Row(horizontalArrangement = Arrangement.spacedBy(MyAppTheme.size.small)) {

                // Priority Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = when (task.priority) {
                                TaskPriority.LOW -> MyAppTheme.colorScheme.tertiaryContainer
                                TaskPriority.MEDIUM -> MyAppTheme.colorScheme.secondaryContainer
                                TaskPriority.HIGH -> MyAppTheme.colorScheme.primaryContainer
                                TaskPriority.URGENT -> MyAppTheme.colorScheme.errorContainer
                            },
                            shape = MyAppTheme.shape.container
                        )
                        .padding(
                            horizontal = MyAppTheme.size.small,
                            vertical = MyAppTheme.size.small / 2
                        )
                ) {
                    Text(
                        text = task.priority.name,
                        style = MyAppTheme.typography.labelSmall,
                        color = when (task.priority) {
                            TaskPriority.LOW -> MyAppTheme.colorScheme.onTertiaryContainer
                            TaskPriority.MEDIUM -> MyAppTheme.colorScheme.primary
                            TaskPriority.HIGH -> MyAppTheme.colorScheme.onPrimaryContainer
                            TaskPriority.URGENT -> MyAppTheme.colorScheme.onErrorContainer
                        }
                    )
                }

                // Status Dropdown
                Box {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MyAppTheme.colorScheme.secondaryContainer,
                                shape = MyAppTheme.shape.container
                            )
                            .clickable { expanded = true }
                            .padding(
                                horizontal = MyAppTheme.size.small,
                                vertical = MyAppTheme.size.small / 2
                            )
                    ) {
                        Text(
                            text = task.status.name.replace("_", " "),
                            style = MyAppTheme.typography.labelSmall,
                            color = MyAppTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    DropdownMenu(
                        containerColor = MyAppTheme.colorScheme.secondaryContainer,
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                        for ((index, value) in TaskStatus.entries.withIndex()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = value.name.replace("_", " "),
                                        style = MyAppTheme.typography.labelNormal,
                                        color = MyAppTheme.colorScheme.onPrimaryContainer
                                    )
                                },
                                onClick = {
                                    onStatusChange(value)
                                    expanded = false
                                }
                            )
                            if (index < TaskStatus.entries.lastIndex) {
                                HorizontalDivider(
                                    color = MyAppTheme.colorScheme.outLine
                                )
                            }
                        }
                    }
                }
            }

            task.dueDate?.let { dueDate ->
                Spacer(modifier = Modifier.height(MyAppTheme.size.small / 2))
                Text(
                    text = "Due: ${dateFormatter.format(dueDate)}",
                    style = MyAppTheme.typography.labelSmall,
                    color = MyAppTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Side effect FIX: Accept state values and lambdas instead of the whole ViewModel
// — composable is now stateless and only recomposes when selectedStatusFilter changes
@Composable
fun FilterSelection(
    selectedStatusFilter: TaskStatus?,
    onFilterSelected: (TaskStatus?) -> Unit
) {
    // Side effect FIX: Compute the list once, not on every recompose
    val statusList = remember { TaskStatus.entries.toList() }

    val customColor = FilterChipDefaults.filterChipColors(
        containerColor = MyAppTheme.colorScheme.onPrimary,
        labelColor = MyAppTheme.colorScheme.primary,
        selectedLabelColor = MyAppTheme.colorScheme.secondaryContainer,
        selectedContainerColor = MyAppTheme.colorScheme.onPrimaryContainer,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MyAppTheme.size.medium)
    ) {
        Text(
            text = "Filter by Status:",
            style = MyAppTheme.typography.titleNormal,
            color = MyAppTheme.colorScheme.onBackground
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MyAppTheme.size.small),
            contentPadding = PaddingValues(vertical = MyAppTheme.size.small)
        ) {
            item {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { onFilterSelected(null) },
                    colors = customColor,
                    label = {
                        Text(
                            text = "All",
                            style = MyAppTheme.typography.labelNormal
                        )
                    }
                )
            }

            items(statusList) { status ->
                FilterChip(
                    selected = selectedStatusFilter == status,
                    onClick = { onFilterSelected(status) },
                    colors = customColor,
                    label = {
                        Text(
                            text = status.name.replace("_", " "),
                            style = MyAppTheme.typography.labelNormal
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, priority: TaskPriority) -> Unit
) {
    // Side effect FIX: Use rememberSaveable so fields survive config change but are
    // scoped to the dialog's lifecycle — they reset when the dialog leaves composition
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.LOW) }
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableIntStateOf(0) }

    // Side effect FIX: Compute priority list once, not on every recompose
    val priorityList = remember { TaskPriority.entries.toList() }

    val customColor = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor   = MyAppTheme.colorScheme.secondaryContainer,
        focusedBorderColor  = MyAppTheme.colorScheme.primary,
        focusedTextColor = MyAppTheme.colorScheme.primary,
        focusedSupportingTextColor = MyAppTheme.colorScheme.primary,
        focusedLabelColor = MyAppTheme.colorScheme.primary
     )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MyAppTheme.colorScheme.surface,
        title = {
            Text(
                text = "Add New Task",
                style = MyAppTheme.typography.titleNormal,
                color = MyAppTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MyAppTheme.size.normal)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            text = "Title",
                            style = MyAppTheme.typography.labelNormal
                        )
                    },
                    textStyle = MyAppTheme.typography.body,
                    singleLine = true,
                    colors = customColor,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text(
                            text = "Description",
                            style = MyAppTheme.typography.labelNormal
                        )
                    },
                    colors = customColor,
                    textStyle = MyAppTheme.typography.body,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Priority DropDown
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        shape = MyAppTheme.shape.button,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { buttonWidth = it.width }
                    ) {
                        Text(
                            text = "Priority: ${selectedPriority.name}",
                            style = MyAppTheme.typography.labelNormal,
                            color = MyAppTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = MyAppTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.width(with(LocalDensity.current) { buttonWidth.toDp() })
                    ) {
                        priorityList.forEach { priority ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = priority.name,
                                        style = MyAppTheme.typography.labelNormal,
                                        color = MyAppTheme.colorScheme.onPrimaryContainer
                                    )
                                },
                                onClick = {
                                    selectedPriority = priority
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), description.trim(), selectedPriority)
                    }
                }
            ) {
                Text(
                    text = "Create",
                    style = MyAppTheme.typography.labelLarge,
                    color = MyAppTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = MyAppTheme.typography.labelLarge,
                    color = MyAppTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}