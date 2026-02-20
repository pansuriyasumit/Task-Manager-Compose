package com.svp.taskhelpercomposemvi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svp.taskhelpercomposemvi.domain.model.Task
import com.svp.taskhelpercomposemvi.domain.model.TaskPriority
import com.svp.taskhelpercomposemvi.domain.model.TaskStatus
import com.svp.taskhelpercomposemvi.domain.usecase.CreateTaskResult
import com.svp.taskhelpercomposemvi.domain.usecase.CreateTaskUseCase
import com.svp.taskhelpercomposemvi.domain.usecase.DeleteTaskResult
import com.svp.taskhelpercomposemvi.domain.usecase.DeleteTaskUseCase
import com.svp.taskhelpercomposemvi.domain.usecase.GetTaskUseCase
import com.svp.taskhelpercomposemvi.domain.usecase.TaskFilter
import com.svp.taskhelpercomposemvi.domain.usecase.UpdateTaskResult
import com.svp.taskhelpercomposemvi.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.sql.Date
import javax.inject.Inject

/**
 * ViewModel for Task Management
 * SRP: Only responsible for UI Logic and state management related to Tasks
 * DIP: Depends on use case abstractions, not on concrete implementations
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTaskUseCase: GetTaskUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TaskState())
    val state: StateFlow<TaskState> = _state.asStateFlow()

    private var currentFilter: TaskFilter = TaskFilter.GetAllTask

    init {
        loadTasks()
    }

    /**
     * Process use intents from the UI and trigger corresponding actions
     * OCP: Adding new intents doesn't require modifying existing code, just adding new cases
     */
    fun processIntent(intent: TaskIntent) {
        when (intent) {
            is TaskIntent.LoadTasks -> loadTasks()
            is TaskIntent.CreateTask -> createTask(intent)
            is TaskIntent.UpdateTaskStatus -> updateTaskStatus(intent)
            is TaskIntent.DeleteTaskById -> deleteTaskById(intent)
            is TaskIntent.DeleteAllTasks -> deleteAllTasks()
            is TaskIntent.DeleteTask -> deleteTask(intent)
            is TaskIntent.FilterByStatus -> filterByStatus(status = intent.status)
            is TaskIntent.FilterByPriority -> filterByPriority(priority = intent.priority)
        }
    }

    /**
     * In viewModelScope.launch {}, the default dispatcher is Dispatchers.Main.
     */
    private fun loadTasks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) } // Set loading state

            getTaskUseCase(currentFilter)
                .catch { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load tasks"
                        )
                    }
                }
                .collect { tasks ->
                    _state.update {
                        it.copy(
                            tasks = tasks,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun createTask(intent: TaskIntent.CreateTask) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val task = Task(
                title = intent.title,
                description = intent.description,
                priority = intent.priority,
                status = TaskStatus.TODO,
                dueDate = intent.dueDate?.let { Date(it) }
            )

            when (val result = createTaskUseCase(task)) {
                is CreateTaskResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Task created successfully"
                        )
                    }
                    clearSuccessMessage()
                }

                is CreateTaskResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                is CreateTaskResult.ValidationError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    private fun updateTaskStatus(intent: TaskIntent.UpdateTaskStatus) {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, error = null)
            }

            val task = _state.value.tasks.find { it.id == intent.taskId } ?: return@launch
            val updatedTask = task.copy(status = intent.newStatus)

            when (val result = updateTaskUseCase(updatedTask)) {
                is UpdateTaskResult.Success -> {
                    _state.update {
                        it.copy(successMessage = "Task updated successfully", isLoading = false)
                    }
                }

                is UpdateTaskResult.ValidationError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                is UpdateTaskResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    private fun deleteTaskById(intent: TaskIntent.DeleteTaskById) {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, error = null)
            }
            when (val result = deleteTaskUseCase(intent.taskId)) {
                is DeleteTaskResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Task deleted successfully"
                        )
                    }
                }

                is DeleteTaskResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    private fun deleteAllTasks() {}

    private fun deleteTask(intent: TaskIntent.DeleteTask) {}

    private fun filterByStatus(status: TaskStatus?) {
        currentFilter = status?.let {
            TaskFilter.GetTaskByStatus(it)
        } ?: TaskFilter.GetAllTask
        _state.update { it.copy(selectedStatusFilter = status) }
        loadTasks()
    }

    private fun filterByPriority(priority: TaskPriority?) {
        currentFilter = priority?.let {
            TaskFilter.GetTaskByPriority(it)
        } ?: TaskFilter.GetAllTask
        loadTasks()
    }

    private fun clearSuccessMessage() {
        viewModelScope.launch {
            delay(2000)
            _state.update { it.copy(successMessage = null) }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}