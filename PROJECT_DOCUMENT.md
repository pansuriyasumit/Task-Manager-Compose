# Task Manager — Summary & Implementation Guide

**Project:** TaskHelperComposeMVI  
**Package:** `com.svp.taskhelpercomposemvi`  
**Date:** February 20, 2026  
**Platform:** Android (minSdk 28 · targetSdk 36 · compileSdk 36)  
**Language:** Kotlin · JVM 17  

---

## Table of Contents

1. [Project Summary](#1-project-summary)
2. [Architecture Overview](#2-architecture-overview)
3. [Tech Stack](#3-tech-stack)
4. [Project Structure](#4-project-structure)
5. [Layer-by-Layer Implementation Guide](#5-layer-by-layer-implementation-guide)
   - 5.1 [Data Layer](#51-data-layer)
   - 5.2 [Domain Layer](#52-domain-layer)
   - 5.3 [Presentation Layer (MVI)](#53-presentation-layer-mvi)
   - 5.4 [Design System & Theme](#54-design-system--theme)
   - 5.5 [Dependency Injection (Hilt)](#55-dependency-injection-hilt)
6. [MVI Pattern Explained](#6-mvi-pattern-explained)
7. [SOLID Principles Applied](#7-solid-principles-applied)
8. [Data Flow Diagram](#8-data-flow-diagram)
9. [Key Design Decisions](#9-key-design-decisions)
10. [Known Limitations & Future Improvements](#10-known-limitations--future-improvements)

---

## 1. Project Summary

**Task Manager** is a single-screen Android application that allows users to **create, view, update, filter, and delete tasks**. It is built as a learning/interview-preparation project demonstrating:

- **Clean Architecture** — strict separation of Data, Domain, and Presentation layers.
- **MVI (Model-View-Intent)** — a unidirectional data flow pattern for predictable state management.
- **Jetpack Compose** — fully declarative UI with no XML layouts.
- **SOLID Principles** — every class has a single reason to change and depends on abstractions.
- **Custom Design System** — a hand-crafted theme (`MyAppTheme`) built on top of Material 3's `CompositionLocalProvider`, supporting automatic Light / Dark mode.

### Core Features

| Feature | Description |
|---------|-------------|
| Create Task | Add a task with title, description, and priority via a dialog |
| View Tasks | Scrollable card list ordered by creation date (newest first) |
| Update Status | Tap a task's status badge to open a dropdown and change it |
| Delete Task | Swipe the delete icon to remove a single task |
| Filter by Status | Horizontally scrollable FilterChips — "All" is selected by default |
| Error Feedback | Errors and success messages surfaced via `SnackbarHost` |
| Dark / Light Mode | Automatic based on system setting via `isSystemInDarkTheme()` |

---

## 2. Architecture Overview

The project follows **Clean Architecture** divided into three layers:

```
┌──────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│  TaskScreen (Compose UI)  ←→  TaskViewModel (MVI)           │
│  TaskIntent · TaskState · TaskScreen composables             │
└───────────────────────┬──────────────────────────────────────┘
                        │  uses (via constructor injection)
┌───────────────────────▼──────────────────────────────────────┐
│                       DOMAIN LAYER                           │
│  Task (model) · TaskRepository (interface)                   │
│  GetTaskUseCase · CreateTaskUseCase                          │
│  UpdateTaskUseCase · DeleteTaskUseCase                       │
│  TaskTitleValidator · TaskDescriptionValidator               │
└───────────────────────┬──────────────────────────────────────┘
                        │  implements
┌───────────────────────▼──────────────────────────────────────┐
│                        DATA LAYER                            │
│  TaskRepositoryImpl  →  TaskDao (Room)                       │
│  TaskEntity · TaskMapper · TaskDatabase                      │
└──────────────────────────────────────────────────────────────┘
```

**Rule:** Arrows only point inward. The domain layer has **zero** knowledge of Room, Compose, or Hilt.

---

## 3. Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.1.x | Primary language |
| Jetpack Compose | BOM-managed | Declarative UI |
| Material 3 | BOM-managed | UI components (FilterChip, Scaffold, etc.) |
| Hilt (Dagger) | 2.56.1 | Dependency Injection |
| KSP | 2.1.21-2.0.20 | Annotation processing for Hilt + Room |
| Room | Latest | Local SQLite database with Kotlin Flow support |
| Kotlin Coroutines / Flow | Lifecycle-managed | Async operations & reactive streams |
| ViewModel (Lifecycle) | Latest | Survive configuration changes |
| Lato Font | Custom | `AppFontFamily` — Regular, Light, Thin, Bold |
| WindowCompat | AndroidX Core | Edge-to-edge & status/navigation bar styling |

---

## 4. Project Structure

```
app/src/main/java/com/svp/taskhelpercomposemvi/
│
├── TaskManagerApplication.kt          ← @HiltAndroidApp entry point
├── MainActivity.kt                    ← Single Activity, hosts TaskScreen
│
├── data/
│   ├── local/
│   │   ├── dao/        TaskDao.kt     ← Room DAO (CRUD + Flow queries)
│   │   ├── database/   TaskDatabase.kt← @Database definition
│   │   └── entity/     TaskEntity.kt  ← Room @Entity (stores enums as Strings)
│   ├── mapper/         TaskMapper.kt  ← Bidirectional Entity ↔ Domain model mapping
│   └── repository/     TaskRepositoryImpl.kt ← Implements TaskRepository
│
├── di/
│   ├── DatabaseModule.kt              ← Provides TaskDatabase & TaskDao (@Singleton)
│   └── RepositoryModule.kt            ← Binds TaskRepository → TaskRepositoryImpl
│
├── domain/
│   ├── model/          Task.kt        ← Domain model + TaskPriority + TaskStatus enums
│   ├── repository/     TaskRepository.kt ← Abstract contract (interface)
│   ├── usecase/
│   │   ├── GetTaskUseCase.kt          ← Returns Flow<List<Task>> for a given filter
│   │   ├── CreateTaskUseCase.kt       ← Validates then creates a task
│   │   ├── UpdateTaskUseCase.kt       ← Validates then updates a task
│   │   ├── DeleteTaskUseCase.kt       ← Deletes by id / by object / delete all
│   │   └── TaskFilter.kt (inline)     ← Sealed class: GetAllTask | ByStatus | ByPriority
│   └── validator/
│       ├── TaskTitleValidator.kt      ← Title must be non-blank, max 100 chars
│       └── TaskDescriptionValidator.kt← Description max 500 chars
│
├── presentation/
│   ├── TaskIntent.kt                  ← Sealed class: all user actions
│   ├── TaskState.kt                   ← Immutable UI state snapshot
│   ├── TaskViewModel.kt               ← @HiltViewModel, processes intents, emits state
│   └── TaskScreen.kt                  ← All Compose UI composables
│
└── view/ui/theme/
    ├── AppDesignSystem.kt             ← Data classes + CompositionLocals (colors, type, shape, size)
    ├── AppTheme.kt                    ← TaskHelperComposeMVITheme + MyAppTheme object
    ├── AppFont.kt                     ← Lato FontFamily definition
    └── Color.kt                       ← Full light/dark/medium/high-contrast color palettes
```

---

## 5. Layer-by-Layer Implementation Guide

---

### 5.1 Data Layer

#### TaskEntity (`data/local/entity/TaskEntity.kt`)
Room database row. Enums are stored as `String` to avoid fragile integer ordinals.

```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val priority: String,    // TaskPriority.name
    val status: String,      // TaskStatus.name
    val createdAt: Long,     // Date.time (epoch ms)
    val updatedAt: Long,
    val dueData: Long?       // nullable
)
```

#### TaskDao (`data/local/dao/TaskDao.kt`)
All read operations return `Flow<List<TaskEntity>>` so Room emits a new list automatically whenever the table changes — no manual refresh needed.

```
getAllTasks()            → Flow<List<TaskEntity>>   (ordered by createdAt DESC)
getTaskById(id)          → suspend TaskEntity?
getTaskByStatus(status)  → Flow<List<TaskEntity>>
getTaskByPriority(prio)  → Flow<List<TaskEntity>>
insertTask(task)         → suspend Long  (returns new rowId)
updateTask(task)         → suspend Unit
deleteTask(task)         → suspend Unit
deleteTaskById(id)       → suspend Unit
deleteAllTasks()         → suspend Unit
```

#### TaskMapper (`data/mapper/TaskMapper.kt`)
Static object with four conversion functions:

| Function | From | To |
|----------|------|----|
| `toDomain(entity)` | `TaskEntity` | `Task` |
| `toEntity(task)` | `Task` | `TaskEntity` |
| `toDomainList(entities)` | `List<TaskEntity>` | `List<Task>` |
| `toEntityList(tasks)` | `List<Task>` | `List<TaskEntity>` |

Enums are converted via `TaskPriority.valueOf(entity.priority)` — safe because we always write `priority.name`.

#### TaskRepositoryImpl (`data/repository/TaskRepositoryImpl.kt`)
Bridges Room and the domain layer. Read operations use `.map { TaskMapper.toDomainList(it) }` on the Flow. Write operations are wrapped in `runCatching { }` to return `Result<T>` instead of throwing.

---

### 5.2 Domain Layer

#### Task Model (`domain/model/Task.kt`)
Pure Kotlin data class. No Room annotations, no Compose dependencies.

```kotlin
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String,
    val priority: TaskPriority,   // LOW | MEDIUM | HIGH | URGENT
    val status: TaskStatus,       // TODO | IN_PROGRESS | PENDING | COMPLETED |
                                  // CANCELLED | ARCHIVED | DELETED | ON_HOLD | REJECTED
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val dueDate: Date? = null
)
```

#### TaskRepository Interface (`domain/repository/TaskRepository.kt`)
The domain layer **only sees this interface** — it never imports Room or `TaskRepositoryImpl`. This is the DIP boundary.

#### Use Cases

Each use case has **one public `invoke` operator** (or overloaded variants for delete), making them callable as functions.

| Use Case | Input | Output | Validation |
|----------|-------|--------|------------|
| `GetTaskUseCase` | `TaskFilter` | `Flow<List<Task>>` | None |
| `CreateTaskUseCase` | `Task` | `CreateTaskResult` | Title + Description |
| `UpdateTaskUseCase` | `Task` | `UpdateTaskResult` | Title + Description |
| `DeleteTaskUseCase` | `Long` / `Task` / `Unit` | `DeleteTaskResult` | None |

**TaskFilter** (sealed class inside `GetTaskUseCase.kt`):
```
GetAllTask            → repository.getAllTasks()
GetTaskByStatus(s)    → repository.getTaskByStatus(s)
GetTaskByPriority(p)  → repository.getTaskByPriority(p)
```

**Result types** are sealed classes, e.g.:
```kotlin
sealed class CreateTaskResult {
    data class Success(val taskId: Long) : CreateTaskResult()
    data class ValidationError(val message: String) : CreateTaskResult()
    data class Error(val message: String) : CreateTaskResult()
}
```
This forces the caller (`ViewModel`) to handle every case explicitly.

#### Validators
- `TaskTitleValidator` — must not be blank, max 100 characters.
- `TaskDescriptionValidator` — max 500 characters.
- Both return `ValidationResult.Valid` or `ValidationResult.Invalid(message)`.

---

### 5.3 Presentation Layer (MVI)

#### TaskIntent (`presentation/TaskIntent.kt`)
Sealed class. Every possible user action is modelled as an intent object.

```kotlin
sealed class TaskIntent {
    object LoadTasks : TaskIntent()
    data class CreateTask(val title, val description, val priority, val dueDate?) : TaskIntent()
    data class UpdateTaskStatus(val taskId, val newStatus) : TaskIntent()
    data class DeleteTaskById(val taskId) : TaskIntent()
    object DeleteAllTasks : TaskIntent()
    data class DeleteTask(val task) : TaskIntent()
    data class FilterByStatus(val status?) : TaskIntent()
    data class FilterByPriority(val priority?) : TaskIntent()
}
```

#### TaskState (`presentation/TaskState.kt`)
Single immutable snapshot of everything the UI needs:

```kotlin
data class TaskState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedStatusFilter: TaskStatus? = null  // null = "All"
)
```

#### TaskViewModel (`presentation/TaskViewModel.kt`)
`@HiltViewModel` — survives configuration changes, injected automatically.

```
_state: MutableStateFlow<TaskState>   (private)
 state: StateFlow<TaskState>          (public, read-only)
```

**Flow of execution when an intent arrives:**

```
UI calls processIntent(intent)
    └─ when(intent) → private fun (loadTasks / createTask / ...)
           └─ viewModelScope.launch { ... }
                  └─ _state.update { it.copy(...) }   ← immutable state update
                         └─ UI recomposes automatically via collectAsState()
```

Key implementation details:
- `loadTasks()` uses `.catch {}` on the Flow to handle errors without crashing.
- `filterByStatus()` stores `selectedStatusFilter` in state so the UI can highlight the active chip.
- `clearSuccessMessage()` auto-resets the success message after 2 seconds using `delay(2000)`.
- `currentFilter` is a private `var` in the ViewModel — a simple, effective way to track the active filter between `loadTasks()` calls.

#### TaskScreen (`presentation/TaskScreen.kt`)
Four composables, each with a single responsibility:

| Composable | Responsibility |
|------------|---------------|
| `TaskScreen` | Root. Collects state, owns `SnackbarHostState`, wires `Scaffold` |
| `FilterSelection` | Stateless. Renders filter chips. Accepts `selectedStatusFilter` + lambda |
| `TaskItem` | Stateless. Renders one task card with priority badge, status dropdown, delete icon |
| `AddTaskDialog` | Local state only. Title/description text fields + priority dropdown |

**Side effects handled correctly:**
- `LaunchedEffect(state.error)` — fires only when a new error value arrives, then clears it.
- `LaunchedEffect(state.successMessage)` — shows Snackbar then ViewModel auto-clears after 2 s.
- `SimpleDateFormat` is `remember`'d to avoid re-allocation on every recompose.
- `TaskStatus.entries.toList()` and `TaskPriority.entries.toList()` are `remember`'d.
- `AddTaskDialog` dialog state resets naturally when `showAddTaskDialog.value = false` removes it from composition.

---

### 5.4 Design System & Theme

The project uses a **custom design system** built on Compose's `CompositionLocalProvider` instead of relying solely on Material 3's `MaterialTheme`. This gives full control over every token.

#### Design Tokens

| Token Class | Fields |
|-------------|--------|
| `AppColorScheme` | 19 semantic color slots (primary, surface, error, etc.) |
| `AppTypography` | 6 text styles (titleLarge, titleNormal, body, labelLarge, labelNormal, labelSmall) |
| `AppShape` | `container` (RoundedCornerShape 12 dp), `button` (pill — 50%) |
| `AppSize` | `large` 24 dp · `medium` 16 dp · `normal` 12 dp · `small` 8 dp |

#### CompositionLocals
Four `staticCompositionLocalOf` locals are declared in `AppDesignSystem.kt`. They default to `Color.Unspecified` / `TextStyle.Default` / `RectangleShape` / `Dp.Unspecified` — so the app will render visually broken if the theme wrapper is missing (a deliberate fail-fast signal).

#### TaskHelperComposeMVITheme (`AppTheme.kt`)
```
TaskHelperComposeMVITheme(darkTheme, dynamicColor, content)
    ├─ Selects lightSchemeTheme or darkSchemeTheme
    ├─ CompositionLocalProvider(
    │       LocalAppColorScheme  provides colorScheme,
    │       LocalAppTypography   provides typography,
    │       LocalAppShape        provides shape,
    │       LocalAppSize         provides size,
    │       LocalIndication      provides ripple()    ← Material 3 ripple
    │  )
    └─ SideEffect {
           statusBarColor      = colorScheme.primary
           navigationBarColor  = primary 8% over surface
           isAppearanceLightStatusBars = !darkTheme
       }
```

#### MyAppTheme Object
Convenience accessor — reads each CompositionLocal at the call site:
```kotlin
object MyAppTheme {
    val colorScheme: AppColorScheme  @Composable get() = LocalAppColorScheme.current
    val typography: AppTypography    @Composable get() = LocalAppTypography.current
    val shape: AppShape              @Composable get() = LocalAppShape.current
    val size: AppSize                @Composable get() = LocalAppSize.current
}
```
Usage in any composable: `MyAppTheme.colorScheme.primary`, `MyAppTheme.typography.titleLarge`, etc.

#### Font
**Lato** font family loaded from `res/font/`:
- `lato_regular` → `FontWeight.Normal`
- `lato_light` → `FontWeight.Light`
- `lato_thin` → `FontWeight.Thin`
- `lato_bold` → `FontWeight.Bold`

---

### 5.5 Dependency Injection (Hilt)

#### Setup
`TaskManagerApplication` is annotated with `@HiltAndroidApp` — the root Hilt component.  
`MainActivity` is annotated with `@AndroidEntryPoint` — enables injection into the activity.

#### Modules

**DatabaseModule** (`di/DatabaseModule.kt`) — `@InstallIn(SingletonComponent::class)`

```
provideTaskDatabase(Context) → TaskDatabase   (@Singleton)
provideTaskDao(TaskDatabase) → TaskDao        (@Singleton)
```
Uses `fallbackToDestructiveMigration(true)` for development convenience.

**RepositoryModule** (`di/RepositoryModule.kt`) — `@InstallIn(SingletonComponent::class)`

```kotlin
@Binds @Singleton
abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
```
Binds the interface to the implementation — the domain layer only ever sees `TaskRepository`.

#### ViewModel Injection
```kotlin
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTaskUseCase: GetTaskUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel()
```
Use cases themselves are not scoped — they are created fresh per ViewModel, which is correct since they hold no state.

---

## 6. MVI Pattern Explained

```
         ┌──────────────────────────────────────────────┐
         │                    VIEW                       │
         │   TaskScreen — collectAsState() → recompose  │
         └──────┬───────────────────────────────────────┘
                │ User taps (Intent)
                ▼
         ┌──────────────────┐
         │   TaskViewModel  │
         │  processIntent() │
         └──────┬───────────┘
                │ calls use case / updates _state
                ▼
         ┌──────────────────────────────────────────────┐
         │              MODEL (TaskState)                │
         │   MutableStateFlow — emits new snapshot      │
         └──────────────────────────────────────────────┘
                │ StateFlow collected by View
                └──────────────────────────────────────▶ (cycle repeats)
```

**Why MVI over MVVM?**
- **Single state object** — impossible to have UI in a partially-updated state.
- **Intents are logged/testable** — every user action is a data class that can be replayed.
- **No two-way data binding** — state flows in one direction, easier to reason about and debug.

---

## 7. SOLID Principles Applied

| Principle | Where Applied |
|-----------|---------------|
| **S** — Single Responsibility | Each use case does exactly one thing. `TaskMapper` only maps. `TaskScreen` only renders. |
| **O** — Open/Closed | `TaskFilter` sealed class: add a new filter type without touching `GetTaskUseCase`. `TaskIntent` sealed class: add new intents without touching existing `when` arms. Result sealed classes (`CreateTaskResult`, `DeleteTaskResult`, etc.) extend the same way. |
| **L** — Liskov Substitution | `TaskRepositoryImpl` fully satisfies the `TaskRepository` interface contract. Can be replaced with a remote or in-memory implementation without changing any use case. |
| **I** — Interface Segregation | `TaskRepository` exposes only task-related methods. `TaskDao` exposes only database operations. No fat interfaces. |
| **D** — Dependency Inversion | `TaskViewModel` depends on use case interfaces/classes, not on `TaskRepositoryImpl` or `TaskDao`. `TaskRepositoryImpl` depends on `TaskDao` (abstract interface), not on a concrete Room generated class. Hilt wires all concrete types at the DI boundary. |

---

## 8. Data Flow Diagram

### Example: User Creates a Task

```
1. User fills dialog and taps "Create"
   └─ TaskScreen calls viewModel.processIntent(TaskIntent.CreateTask(...))

2. TaskViewModel.processIntent()
   └─ createTask(intent) launches coroutine on Dispatchers.Main

3. createTask() builds Task domain model
   └─ calls createTaskUseCase(task)

4. CreateTaskUseCase.invoke(task)
   ├─ TaskTitleValidator.validate(title)     → Valid ✓
   ├─ TaskDescriptionValidator.validate(desc)→ Valid ✓
   └─ repository.createTask(task)            → Result<Long>

5. TaskRepositoryImpl.createTask(task)
   ├─ TaskMapper.toEntity(task)              → TaskEntity
   └─ taskDao.insertTask(entity)             → Long (new id)

6. Result bubbles back to ViewModel
   └─ _state.update { copy(successMessage = "Task created successfully") }

7. Room emits new list automatically via Flow
   └─ taskDao.getAllTasks() Flow emits
      └─ TaskMapper.toDomainList(entities)
         └─ _state.update { copy(tasks = tasks) }

8. StateFlow emits new TaskState
   └─ TaskScreen recomposes — new task appears in LazyColumn
   └─ LaunchedEffect(state.successMessage) → Snackbar shown → auto-cleared after 2 s
```

---

## 9. Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Room Flow** for reads | Auto-emits on table change — no manual `loadTasks()` needed after write |
| **Sealed result classes** per use case | Forces exhaustive `when` in ViewModel, no forgotten error cases |
| **`staticCompositionLocalOf`** for theme | Throws at composition time if theme is missing — fail-fast is safer than silent fallback |
| **`TaskStatus` stored as `String`** in Room | Adding new enum values won't corrupt existing data (vs integer ordinals) |
| **Stateless child composables** | `FilterSelection`, `TaskItem` receive state + lambdas — no ViewModel reference, easy to test/preview |
| **`SnackbarHostState` in root** | Single source of truth for messages; both error and success share the same host |
| **`remember { ... .entries.toList() }`** | Avoid allocating a new list on every recompose for static enum lists |
| **`fallbackToDestructiveMigration(true)`** | Acceptable during development; must be replaced with proper migration scripts before production release |
| **`currentFilter` as ViewModel `var`** | Simpler than putting it in `TaskState` since the filter value is already mirrored as `selectedStatusFilter` (for UI highlight) |

---

## 10. Known Limitations & Future Improvements

| Area | Current Limitation | Suggested Fix |
|------|--------------------|---------------|
| **`deleteAllTasks` / `deleteTask`** | Stub functions — body is empty | Implement using `DeleteTaskUseCase` |
| **Filter by Priority** | `filterByPriority()` works but no UI for it | Add a second `FilterSelection` row for priority |
| **`selectedStatusFilter` not in state for priority** | `filterByPriority` doesn't update state | Mirror `selectedPriorityFilter` in `TaskState` |
| **`fallbackToDestructiveMigration`** | Deletes all data on schema change | Write proper Room `Migration` objects |
| **No due date picker** | `AddTaskDialog` has no date input | Add `DatePickerDialog` and wire to `TaskIntent.CreateTask.dueDate` |
| **No search** | Cannot search tasks by keyword | Add `TaskIntent.SearchTasks(query)` + DAO `LIKE` query |
| **No unit tests** | No test files present | Add `TaskViewModelTest`, `CreateTaskUseCaseTest`, `TaskMapperTest` |
| **`java.sql.Date`** in ViewModel | `import java.sql.Date` used instead of `java.util.Date` | Replace with `java.util.Date` to avoid ambiguity |
| **Loading state on filter** | `isLoading = true` flickers briefly on every filter change | Use a debounce or skip loading state for in-memory re-filter |
| **Single Activity / No Navigation** | Navigation component not wired | Add `NavHost` + task detail / edit screen |
