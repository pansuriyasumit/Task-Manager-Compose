package com.svp.taskhelpercomposemvi.di

import android.content.Context
import androidx.room.Room
import com.svp.taskhelpercomposemvi.data.local.dao.TaskDao
import com.svp.taskhelpercomposemvi.data.local.database.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for providing database-related dependencies
 * Following SRP - only responsible for providing database and DAO instances
 * Following DIP - provides abstractions (DAOs) rather than concrete implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTaskDatabase(
        @ApplicationContext context: Context
    ): TaskDatabase {
        val database = Room.databaseBuilder(
            context,
            TaskDatabase::class.java,
            "task_database"
        ).fallbackToDestructiveMigration(true)
            .build()
        return database
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: TaskDatabase): TaskDao {
        return database.taskDao()
    }
}