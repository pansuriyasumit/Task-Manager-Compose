package com.svp.taskhelpercomposemvi.di

import com.svp.taskhelpercomposemvi.domain.validator.DueDateValidator
import com.svp.taskhelpercomposemvi.domain.validator.TaskDescriptionValidator
import com.svp.taskhelpercomposemvi.domain.validator.TaskTitleValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for Validator Dependencies
 * DIP: Provides concrete validator implementations, allowing high-level modules to depend on abstractions
 * OCP: Easy to add new validators without modifying existing code
 * ISP: Each validator has a single responsibility
 * SRP: Only Responsible for validator provision
 */
@Module
@InstallIn(SingletonComponent::class)
object ValidatorModule {

    @Provides
    @Singleton
    fun provideTaskTitleValidator(): TaskTitleValidator {
        return TaskTitleValidator()
    }

    @Provides
    @Singleton
    fun provideTaskDescriptionValidator(): TaskDescriptionValidator {
        return TaskDescriptionValidator()
    }

    @Provides
    @Singleton
    fun provideTaskDueDateValidator(): DueDateValidator {
        return DueDateValidator()
    }
}