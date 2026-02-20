package com.svp.taskhelpercomposemvi.domain.usecase

import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.model.TaskPriority
import com.svp.taskhelpercomposemvi.domain.model.TaskStatus
import com.svp.taskhelpercomposemvi.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case for fetching tasks based on different filters
 * Following SRP - only responsible for fetching tasks based on specified types
 * Following OCP - can be extended with new filter types without modifying existing code
 */
class GetTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(filter: TaskFilter = TaskFilter.GetAllTask): Flow<List<Task>> {
        return when (filter) {
            is TaskFilter.GetAllTask -> repository.getAllTAsks()
            is TaskFilter.GetTaskByStatus -> repository.getTaskByStatus(filter.status)
            is TaskFilter.GetTaskByPriority -> repository.getTaskByPriority(filter.priority)
        }
    }
}

/**
 * OCP: Sealed class for filters - easy to extend with new filter types without modifying existing code
 */
sealed class TaskFilter {
    object GetAllTask : TaskFilter()
    data class GetTaskByStatus(val status: TaskStatus) : TaskFilter()
    data class GetTaskByPriority(val priority: TaskPriority) : TaskFilter()
}