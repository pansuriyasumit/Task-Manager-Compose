package com.svp.taskhelpercomposemvi.domain.usecase

import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.repository.TaskRepository
import com.svp.taskhelpercomposemvi.domain.validator.TaskDescriptionValidator
import com.svp.taskhelpercomposemvi.domain.validator.TaskTitleValidator
import com.svp.taskhelpercomposemvi.domain.validator.ValidationResult
import java.util.Date
import javax.inject.Inject

/**
 * Use Case for updating an existing task
 * SRP: Only responsible for the business logic of updating a task
 * DIP: Depends on abstractions (TaskRepository, Validators) rather than concrete implementations
 * OCP: Can be extended with additional validation rules or result types without modifying existing code
 */
class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val titleValidator: TaskTitleValidator,
    private val descriptionValidator: TaskDescriptionValidator
) {

    suspend operator fun invoke(task: Task): UpdateTaskResult {
        when (val titleValidation = titleValidator.validate(task.title)) {
            is ValidationResult.Invalid -> {
                return UpdateTaskResult.ValidationError(titleValidation.message)
            }

            ValidationResult.Valid -> {
                /* Continue */
            }
        }

        when (val descValidation = descriptionValidator.validate(task.description)) {
            is ValidationResult.Invalid -> {
                return UpdateTaskResult.ValidationError(descValidation.message)
            }

            ValidationResult.Valid -> {
                /* Continue */
            }
        }

        val updatedTask = task.copy(updatedAt = Date())

        val result = repository.updateTask(updatedTask)
        return if (result.isSuccess) {
            UpdateTaskResult.Success
        } else {
            UpdateTaskResult.Error(
                result.exceptionOrNull()?.message
                    ?: "An unknown error occurred while updating the task"
            )
        }
    }

}


/**
 * OCP: Sealed class allows for easy extension of result types without modifying existing code
 */
sealed class UpdateTaskResult {
    object Success : UpdateTaskResult()
    data class ValidationError(val message: String) : UpdateTaskResult()
    data class Error(val message: String) : UpdateTaskResult()
}