package com.svp.taskhelpercomposemvi.presentation

import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.model.TaskPriority
import com.svp.taskhelpercomposemvi.domain.model.TaskStatus

sealed class TaskIntent {
    object LoadTasks : TaskIntent()

    data class CreateTask(
        val title: String,
        val description: String,
        val priority: TaskPriority,
        val dueDate: Long? = null
    ) : TaskIntent()

    data class UpdateTaskStatus(
        val taskId: Long,
        val newStatus: TaskStatus
    ) : TaskIntent()

    data class DeleteTaskById(val taskId: Long) : TaskIntent()

    object DeleteAllTasks : TaskIntent()

    data class DeleteTask(val task: Task) : TaskIntent()

    data class FilterByStatus(val status: TaskStatus?) : TaskIntent()

    data class FilterByPriority(val priority: TaskPriority?) : TaskIntent()

}