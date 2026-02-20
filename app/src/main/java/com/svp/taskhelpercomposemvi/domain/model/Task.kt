package com.svp.taskhelpercomposemvi.domain.model

import java.util.Date

/**
 * Domain Model - Clean representation of a Task
 * Independent of any framework or database, following Clean Architecture principles
 * Following SRP - only responsible for representing task data
 */
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val dueDate: Date? = null
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class TaskStatus {
    TODO, IN_PROGRESS, PENDING, COMPLETED, CANCELLED, ARCHIVED, DELETED, ON_HOLD, REJECTED
}