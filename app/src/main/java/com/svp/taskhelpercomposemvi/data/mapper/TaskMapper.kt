package com.svp.taskhelpercomposemvi.data.mapper

import com.svp.taskhelpercomposemvi.data.local.entity.TaskEntity
import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.model.TaskPriority
import com.svp.taskhelpercomposemvi.domain.model.TaskStatus
import java.util.Date

object TaskMapper {

    fun toDomain(entity: TaskEntity): Task {
        return Task(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            priority = TaskPriority.valueOf(entity.priority),
            status = TaskStatus.valueOf(entity.status),
            createdAt = Date(entity.createdAt),
            updatedAt = Date(entity.updatedAt),
            dueDate = entity.dueData?.let { Date(it) }
        )
    }

    fun toEntity(task: Task): TaskEntity {
        return TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            priority = task.priority.name,
            status = task.status.name,
            createdAt = task.createdAt.time,
            updatedAt = task.updatedAt.time,
            dueData = task.dueDate?.time
        )
    }

    fun toDomainList(entities: List<TaskEntity>): List<Task> {
        return entities.map {
            toDomain(it)
        }
    }

    fun toEntityList(tasks: List<Task>): List<TaskEntity> {
        return tasks.map { toEntity(it) }
    }
}