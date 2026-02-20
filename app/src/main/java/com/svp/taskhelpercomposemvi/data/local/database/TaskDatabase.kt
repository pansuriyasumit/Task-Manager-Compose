package com.svp.taskhelpercomposemvi.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.svp.taskhelpercomposemvi.data.local.dao.TaskDao
import com.svp.taskhelpercomposemvi.data.local.entity.TaskEntity

/**
 * Room Database for Task Management
 * Following SRP - only responsible for database configuration and providing DAO instances
 * Following DIP - depends on abstractions (DAOs) rather than concrete implementations
 */
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase: RoomDatabase() {

    abstract fun taskDao(): TaskDao
}