package com.svp.taskhelpercomposemvi.di

import com.svp.taskhelpercomposemvi.data.repository.TaskRepositoryImpl
import com.svp.taskhelpercomposemvi.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for Repository Dependencies
 * DIP: Binds interface to implementation, allowing high-level modules to depend on abstractions
 * OCP: Easy to swap new repository implementations without modifying existing code
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskTaskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
}