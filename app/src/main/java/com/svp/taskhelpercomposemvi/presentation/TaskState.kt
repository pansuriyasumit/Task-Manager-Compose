package com.svp.taskhelpercomposemvi.presentation

import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.model.TaskStatus

/**
 * MVI State - UI State Representation
 * Immutable data class representing the state of the Task UI
 */
data class TaskState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedStatusFilter: TaskStatus? = null // null means "All" is selected
)