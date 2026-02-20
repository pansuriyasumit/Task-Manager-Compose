package com.svp.taskhelpercomposemvi.domain.repository

import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.model.TaskPriority
import com.svp.taskhelpercomposemvi.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository Interface for Task Operations
 * Following DIP - High-level module (domain) depends on abstractions (repository interface) rather than concrete implementations
 * Following SRP - only responsible for defining data operations related to tasks
 * Following ISP - specific interface for task-related data operations, not forcing clients to depend on methods they don't use
 */
interface TaskRepository {

    fun getAllTAsks(): Flow<List<Task>>
    suspend fun getTaskById(taskID: Long): Task?
    fun getTaskByStatus(status: TaskStatus): Flow<List<Task>>
    fun getTaskByPriority(priority: TaskPriority): Flow<List<Task>>

    suspend fun createTask(task: Task): Result<Long>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun deleteTask(task: Task): Result<Unit>
    suspend fun deleteTaskById(taskID: Long): Result<Unit>
    suspend fun deleteAllTasks(): Result<Unit>
}