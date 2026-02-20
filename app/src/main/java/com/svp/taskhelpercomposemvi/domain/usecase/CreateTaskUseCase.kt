package com.svp.taskhelpercomposemvi.domain.usecase

import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.repository.TaskRepository
import com.svp.taskhelpercomposemvi.domain.usecase.CreateTaskResult.*
import com.svp.taskhelpercomposemvi.domain.validator.TaskDescriptionValidator
import com.svp.taskhelpercomposemvi.domain.validator.TaskTitleValidator
import com.svp.taskhelpercomposemvi.domain.validator.ValidationResult
import javax.inject.Inject

/**
 * Use Case for creating a new task
 * SRP: Only responsible for the business logic of creating a task
 * DIP: Depends on abstractions (TaskRepository, Validators) rather than concrete implementations
 * OCP: Can be extended with additional validation rules or result types without modifying existing code
 */
class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val titleValidator: TaskTitleValidator,
    private val descriptionValidator: TaskDescriptionValidator
) {
    suspend operator fun invoke(task: Task): CreateTaskResult {
        when (val titleValidator = titleValidator.validate(task.title)) {
            is ValidationResult.Invalid -> {
                return ValidationError(titleValidator.message)
            }

            ValidationResult.Valid -> {
                /* Continue */
            }
        }

        when (val descValidation = descriptionValidator.validate(task.description)) {
            is ValidationResult.Invalid -> {
                return ValidationError(descValidation.message)
            }

            ValidationResult.Valid -> {
                /* Continue */
            }
        }

        val result = repository.createTask(task)
        return if (result.isSuccess) {
            Success(result.getOrNull()!!)
        } else {
            Error(
                result.exceptionOrNull()?.message
                    ?: "An unknown error occurred while creating the task"
            )
        }
    }
}


/**
 * OCP: Sealed class allows for easy extension of result types without modifying existing code
 */
sealed class CreateTaskResult {
    data class Success(val taskId: Long) : CreateTaskResult()
    data class ValidationError(val message: String) : CreateTaskResult()
    data class Error(val message: String) : CreateTaskResult()
}