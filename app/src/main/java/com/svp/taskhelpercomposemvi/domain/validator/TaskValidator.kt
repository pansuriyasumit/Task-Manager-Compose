package com.svp.taskhelpercomposemvi.domain.validator

/**
 * ISP: Segregated interfaces for different validation rules, allowing for flexible and reusable
 * validators for task properties.
 * - Each validator has a single responsibility, making it easier to maintain and extend validation
 * logic without affecting other parts of the codebase.
 *
 * OCP: New validation rules can be added by creating new validator classes without modifying
 * existing ones, adhering to the Open/Closed Principle.
 */
interface Validator<T> {
    fun validate(value: T): ValidationResult
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

/**
 * SRP: Only validates the task title
 */
class TaskTitleValidator : Validator<String> {
    override fun validate(value: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult.Invalid("Title cannot be empty")
            value.length < 3 -> ValidationResult.Invalid("Title must be at least 3 characters")
            value.length > 50 -> ValidationResult.Invalid("Title cannot exceed 50 characters")
            else -> ValidationResult.Valid
        }
    }
}

/**
 * SRP: Only validates the task description
 */
class TaskDescriptionValidator : Validator<String> {
    override fun validate(value: String): ValidationResult {
        return when {
            value.length > 500 -> ValidationResult.Invalid("Description cannot exceed 500 characters")
            else -> ValidationResult.Valid
        }
    }
}

/**
 * SRP: Only validates the task due date
 */
class DueDateValidator : Validator<Long?> {
    override fun validate(value: Long?): ValidationResult {
        if (value == null) return ValidationResult.Valid

        val currentTime = System.currentTimeMillis()
        return when {
            value < currentTime -> ValidationResult.Invalid("Due date cannot be in the past")
            else -> ValidationResult.Valid
        }
    }
}