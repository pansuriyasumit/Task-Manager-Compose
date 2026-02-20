package com.svp.taskhelpercomposemvi.domain.usecase

import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case for deleting a task by its ID
 *
 * SRP: This class has a single responsibility - to handle the deletion of tasks
 * DIP: Depends on the TaskRepository interface, allowing for flexibility in implementation
 */
class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long): DeleteTaskResult {
        val result = repository.deleteTaskById(taskID = taskId)
        return if (result.isSuccess) {
            DeleteTaskResult.Success
        } else {
            DeleteTaskResult.Error(
                result.exceptionOrNull()?.message
                    ?: "An unknown error occurred while deleting the task"
            )
        }
    }

    suspend operator fun invoke(): DeleteTaskResult {
        val result = repository.deleteAllTasks()
        return if (result.isSuccess) {
            DeleteTaskResult.Success
        } else {
            DeleteTaskResult.Error(
                result.exceptionOrNull()?.message
                    ?: "An unknown error occurred while deleting all tasks"
            )
        }
    }

    suspend operator fun invoke(task: Task): DeleteTaskResult {
        val result = repository.deleteTask(task)
        return if (result.isSuccess) {
            DeleteTaskResult.Success
        } else {
            DeleteTaskResult.Error(
                result.exceptionOrNull()?.message
                    ?: "An unknown error occurred while deleting the task"
            )
        }
    }
}

/**
 * OCP: Sealed class allows for easy extension of result types without modifying existing code
 */
sealed class DeleteTaskResult {
    object Success : DeleteTaskResult()
    data class Error(val message: String) : DeleteTaskResult()
}