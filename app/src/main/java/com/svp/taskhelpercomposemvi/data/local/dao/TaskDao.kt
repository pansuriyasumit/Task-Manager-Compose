package com.svp.taskhelpercomposemvi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.svp.taskhelpercomposemvi.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow


/**
 * Data Access Object for Task Operations
 * Following ISP - specific interface for database operations
 */

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskID")
    suspend fun getTaskById(taskID: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getTaskByStatus(status: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY createdAt DESC")
    fun getTaskByPriority(priority: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskID")
    suspend fun deleteTaskById(taskID: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}