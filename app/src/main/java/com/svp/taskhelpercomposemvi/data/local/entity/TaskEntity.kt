package com.svp.taskhelpercomposemvi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity
 * Separated from domain model to follow SRP and DIP principles and to avoid coupling between layers
 */

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val dueData: Long?
)