package com.svp.taskhelpercomposemvi.data.repository

import com.svp.taskhelpercomposemvi.data.local.dao.TaskDao
import com.svp.taskhelpercomposemvi.data.mapper.TaskMapper
import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.model.TaskPriority
import com.svp.taskhelpercomposemvi.domain.model.TaskStatus
import com.svp.taskhelpercomposemvi.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository Implementation for Task Operations
 * Following DIP - depends on abstractions (TaskDao) rather than concrete implementations
 * Following SRP - only responsible for implementing data operations related to tasks
 * Following OCP - can be extended with new data sources (e.g., remote API) without modifying existing code
 */
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    override fun getAllTAsks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            TaskMapper.toDomainList(entities)
        }
    }

    override suspend fun getTaskById(taskID: Long): Task? {
        return taskDao.getTaskById(taskID)?.let { entity ->
            TaskMapper.toDomain(entity)
        }
    }

    override fun getTaskByStatus(status: TaskStatus): Flow<List<Task>> {
        return taskDao.getTaskByStatus(status.name).map { entities ->
            TaskMapper.toDomainList(entities)
        }
    }

    override fun getTaskByPriority(priority: TaskPriority): Flow<List<Task>> {
        return taskDao.getTaskByPriority(priority.name).map { entities ->
            TaskMapper.toDomainList(entities)
        }
    }

    override suspend fun createTask(task: Task): Result<Long> {
        return try {
            val entity = TaskMapper.toEntity(task = task)
            val id = taskDao.insertTask(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            val entity = TaskMapper.toEntity(task = task)
            taskDao.updateTask(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(task: Task): Result<Unit> {
        return try {
            val entity = TaskMapper.toEntity(task = task)
            taskDao.deleteTask(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTaskById(taskID: Long): Result<Unit> {
        return try {
            taskDao.deleteTaskById(taskID = taskID)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllTasks(): Result<Unit> {

        return try {
            taskDao.deleteAllTasks()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}