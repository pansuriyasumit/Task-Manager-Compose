# 🛠️ Tech Stack Documentation

**Project:** Task Manager — Compose MVI  
**Package:** `com.svp.taskhelpercomposemvi`  
**Date:** February 20, 2026  
**Platform:** Android (minSdk 28 · targetSdk 36 · compileSdk 36)

---

## Table of Contents

1. [Language & Core](#1-language--core)
2. [Build System](#2-build-system)
3. [UI Layer](#3-ui-layer)
4. [Architecture Pattern](#4-architecture-pattern)
5. [Dependency Injection](#5-dependency-injection)
6. [Database](#6-database)
7. [Async & Reactive](#7-async--reactive)
8. [Navigation](#8-navigation)
9. [Design System](#9-design-system)
10. [Testing](#10-testing)
11. [Dependency Version Table](#11-dependency-version-table)
12. [Project Structure](#12-project-structure)

---

## 1. Language & Core

| Technology     | Version  | Details                                          |
|----------------|----------|--------------------------------------------------|
| **Kotlin**     | `2.0.21` | Primary language for all source files            |
| **JVM Target** | `17`     | `sourceCompatibility` + `targetCompatibility`    |
| **JDK Toolchain** | `17`  | Set via `kotlin { jvmToolchain(17) }`            |

- Full Kotlin — **no Java source files**
- Uses **Kotlin Coroutines** and **Kotlin Flow** throughout
- Follows **idiomatic Kotlin** (data classes, sealed classes, extension functions)

---

## 2. Build System

| Technology             | Version    | Details                                       |
|------------------------|------------|-----------------------------------------------|
| **Gradle**             | Wrapper    | `gradlew` / `gradlew.bat`                     |
| **Android Gradle Plugin (AGP)** | `8.13.2` | `com.android.application`              |
| **Gradle Version Catalog** | —     | `gradle/libs.versions.toml`                   |
| **KSP** (Kotlin Symbol Processing) | `2.0.21-1.0.28` | Replaces kapt for Hilt + Room |
| **Build Scripts**      | `.kts`     | All Gradle files use Kotlin DSL                |

### Key Build Features
```kotlin
buildFeatures {
    compose = true       // Jetpack Compose enabled
    viewBinding = true   // View Binding enabled
}
```

---

## 3. UI Layer

| Technology                | Version        | Details                                           |
|---------------------------|----------------|---------------------------------------------------|
| **Jetpack Compose**       | BOM `2024.09.00` | 100% declarative UI — zero XML layouts          |
| **Compose BOM**           | `2024.09.00`   | Manages all Compose library versions together     |
| **Material 3**            | BOM-managed    | `Scaffold`, `FilterChip`, `Card`, `Snackbar`, etc.|
| **Activity Compose**      | `1.12.3`       | `setContent {}` + `ComponentActivity`            |
| **UI Tooling**            | BOM-managed    | `@Preview` support in Android Studio             |
| **Compose UI Test**       | BOM-managed    | UI testing via `ComposeTestRule`                 |

### Compose Components Used
- `Scaffold` — top app bar + snackbar host
- `LazyColumn` — task list
- `FilterChip` — status filter bar
- `AlertDialog` — create/update task dialogs
- `DropdownMenu` — status selection
- `SnackbarHost` — error & success messages

---

## 4. Architecture Pattern

### Clean Architecture + MVI

```
┌──────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                     │
│  TaskScreen.kt ←→ TaskViewModel.kt                       │
│  TaskIntent.kt    TaskState.kt                           │
└───────────────────────┬──────────────────────────────────┘
                        │  uses
┌───────────────────────▼──────────────────────────────────┐
│                    DOMAIN LAYER                          │
│  Task.kt (model)   TaskRepository.kt (interface)         │
│  GetTaskUseCase    CreateTaskUseCase                     │
│  UpdateTaskUseCase DeleteTaskUseCase                     │
│  TaskValidator.kt                                        │
└───────────────────────┬──────────────────────────────────┘
                        │  implements
┌───────────────────────▼──────────────────────────────────┐
│                     DATA LAYER                           │
│  TaskRepositoryImpl → TaskDao (Room)                     │
│  TaskEntity   TaskMapper   TaskDatabase                  │
└──────────────────────────────────────────────────────────┘
```

### MVI (Model-View-Intent) Flow

```
User Action → TaskIntent → TaskViewModel → TaskState → Compose UI (recompose)
```

| MVI Component | File             | Role                                               |
|---------------|------------------|----------------------------------------------------|
| **Model**     | `TaskState.kt`   | Single immutable state object rendered by the UI  |
| **View**      | `TaskScreen.kt`  | Composable that observes state and emits intents  |
| **Intent**    | `TaskIntent.kt`  | Sealed class — all user actions                   |
| **ViewModel** | `TaskViewModel.kt` | Processes intents, calls use cases, emits state |

### Use Cases (Domain)

| Use Case               | Responsibility                            |
|------------------------|-------------------------------------------|
| `GetTaskUseCase`       | Fetch all tasks as `Flow<List<Task>>`     |
| `CreateTaskUseCase`    | Validate & insert a new task              |
| `UpdateTaskUseCase`    | Update task status                        |
| `DeleteTaskUseCase`    | Delete a task by ID                       |

---

## 5. Dependency Injection

| Technology         | Version | Details                                          |
|--------------------|---------|--------------------------------------------------|
| **Hilt (Dagger)**  | `2.52`  | `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel` |
| **Hilt Navigation Compose** | `1.3.0` | `hiltViewModel()` in Composables       |
| **KSP**            | `2.0.21-1.0.28` | Code generation for Hilt modules       |

### DI Modules

| Module               | File                  | Provides                                      |
|----------------------|-----------------------|-----------------------------------------------|
| `DatabaseModule`     | `di/DatabaseModule.kt` | `TaskDatabase`, `TaskDao`                    |
| `RepositoryModule`   | `di/RepositoryModule.kt` | `TaskRepository` → `TaskRepositoryImpl`    |
| `ValidatorModule`    | `di/ValidatorModule.kt` | `TaskValidator` instances                   |

---

## 6. Database

| Technology        | Version  | Details                                              |
|-------------------|----------|------------------------------------------------------|
| **Room**          | `2.8.4`  | SQLite ORM — local persistent storage                |
| **Room Runtime**  | `2.8.4`  | Core Room library                                    |
| **Room KTX**      | `2.8.4`  | Kotlin Coroutines + Flow support for Room            |
| **Room Compiler** | `2.8.4`  | KSP-based annotation processor                      |

### Room Components

| Component           | File                                 | Role                              |
|---------------------|--------------------------------------|-----------------------------------|
| `TaskDatabase`      | `data/local/database/TaskDatabase.kt` | `RoomDatabase` — single instance |
| `TaskDao`           | `data/local/dao/TaskDao.kt`          | DAO — CRUD queries with Flow      |
| `TaskEntity`        | `data/local/entity/TaskEntity.kt`    | `@Entity` — DB table definition  |
| `TaskMapper`        | `data/mapper/TaskMapper.kt`          | Maps `TaskEntity` ↔ `Task` model |
| `TaskRepositoryImpl`| `data/repository/TaskRepositoryImpl.kt` | Implements `TaskRepository`  |

---

## 7. Async & Reactive

| Technology                  | Version        | Details                                      |
|-----------------------------|----------------|----------------------------------------------|
| **Kotlin Coroutines**       | Lifecycle-managed | `viewModelScope`, `suspend` functions      |
| **Kotlin Flow**             | Lifecycle-managed | Reactive streams from Room DAO             |
| **StateFlow**               | Kotlin stdlib  | `MutableStateFlow` for MVI state in ViewModel|
| **Lifecycle Runtime KTX**   | `2.10.0`       | `collectAsStateWithLifecycle()`              |
| **ViewModel KTX**           | `2.10.0`       | `viewModelScope` coroutine scope             |
| **ViewModel Compose**       | `2.10.0`       | `viewModel()` / `hiltViewModel()` in Compose |

---

## 8. Navigation

| Technology                   | Version | Details                                        |
|------------------------------|---------|------------------------------------------------|
| **Hilt Navigation Compose**  | `1.3.0` | `hiltViewModel()` — ViewModel scoped to nav graph |
| **Fragment KTX**             | `1.8.9` | `androidx.fragment:fragment-ktx`               |

> Navigation is currently single-screen. The navigation compose dependency is in place for future multi-screen expansion.

---

## 9. Design System

| Technology          | Details                                                         |
|---------------------|-----------------------------------------------------------------|
| **Material 3**      | Base design system — tokens, color scheme, typography           |
| **Custom Theme**    | `MyAppTheme` — wraps M3 via `CompositionLocalProvider`          |
| **Custom Colors**   | `Color.kt` — Light & Dark palette definitions                   |
| **Custom Typography** | `Type.kt` + `AppFont.kt` — Lato font family (Regular, Bold, Light, Thin) |
| **AppDesignSystem** | `AppDesignSystem.kt` — exposes `AppTheme.colors`, `AppTheme.typography` |
| **Dark Mode**       | Automatic via `isSystemInDarkTheme()`                           |

### Theme Files

| File                | Role                                              |
|---------------------|---------------------------------------------------|
| `Theme.kt`          | `MyAppTheme {}` composable entry point            |
| `AppTheme.kt`       | `CompositionLocal` providers for colors & typography |
| `AppDesignSystem.kt`| `object AppTheme` — access point for design tokens|
| `AppFont.kt`        | `AppFontFamily` — Lato font definitions           |
| `Color.kt`          | Light & Dark color palettes                       |
| `Type.kt`           | Typography scale                                  |

---

## 10. Testing

| Technology                | Version        | Details                                   |
|---------------------------|----------------|-------------------------------------------|
| **JUnit 4**               | `4.13.2`       | Unit testing framework                    |
| **AndroidX JUnit**        | `1.3.0`        | `@RunWith(AndroidJUnit4::class)`          |
| **Espresso Core**         | `3.7.0`        | UI instrumentation testing                |
| **Compose UI Test JUnit4**| BOM-managed    | Compose-specific UI tests                 |
| **Compose UI Test Manifest** | BOM-managed | Test manifest for Compose                |

---

## 11. Dependency Version Table

| Dependency                        | Version          |
|-----------------------------------|------------------|
| `kotlin`                          | `2.0.21`         |
| `android-gradle-plugin`           | `8.13.2`         |
| `androidx.core:core-ktx`          | `1.17.0`         |
| `androidx.lifecycle:*`            | `2.10.0`         |
| `androidx.activity:activity-compose` | `1.12.3`      |
| `androidx.compose:compose-bom`    | `2024.09.00`     |
| `com.google.dagger:hilt-android`  | `2.52`           |
| `com.google.devtools.ksp`         | `2.0.21-1.0.28`  |
| `androidx.room:room-*`            | `2.8.4`          |
| `androidx.hilt:hilt-navigation-compose` | `1.3.0`   |
| `androidx.fragment:fragment-ktx`  | `1.8.9`          |
| `junit:junit`                     | `4.13.2`         |
| `androidx.test.ext:junit`         | `1.3.0`          |
| `androidx.test.espresso:espresso-core` | `3.7.0`    |

---

## 12. Project Structure

```
app/src/main/java/com/svp/taskhelpercomposemvi/
│
├── MainActivity.kt                        # Entry point — hosts TaskScreen
├── TaskManagerApplication.kt             # @HiltAndroidApp Application class
│
├── data/
│   ├── local/
│   │   ├── dao/        TaskDao.kt         # Room DAO — CRUD + Flow queries
│   │   ├── database/   TaskDatabase.kt    # RoomDatabase singleton
│   │   └── entity/     TaskEntity.kt      # DB table entity
│   ├── mapper/         TaskMapper.kt      # Entity ↔ Domain model mapping
│   └── repository/     TaskRepositoryImpl.kt  # Implements TaskRepository
│
├── di/
│   ├── DatabaseModule.kt                  # Provides Room DB & DAO
│   ├── RepositoryModule.kt                # Binds TaskRepository interface
│   └── ValidatorModule.kt                 # Provides Validator instances
│
├── domain/
│   ├── model/          Task.kt            # Domain model (pure Kotlin)
│   ├── repository/     TaskRepository.kt  # Abstract repository interface
│   ├── usecase/
│   │   ├── CreateTaskUseCase.kt
│   │   ├── DeleteTaskUseCase.kt
│   │   ├── GetTaskUseCase.kt
│   │   └── UpdateTaskUseCase.kt
│   └── validator/      TaskValidator.kt   # Input validation logic
│
├── presentation/
│   ├── TaskIntent.kt                      # MVI — sealed user actions
│   ├── TaskState.kt                       # MVI — immutable UI state
│   ├── TaskScreen.kt                      # Compose UI screen
│   └── TaskViewModel.kt                   # MVI ViewModel
│
└── view/ui/theme/
    ├── Theme.kt                           # MyAppTheme entry composable
    ├── AppTheme.kt                        # CompositionLocal providers
    ├── AppDesignSystem.kt                 # AppTheme object — design tokens
    ├── AppFont.kt                         # Lato font family
    ├── Color.kt                           # Light & Dark color palettes
    └── Type.kt                            # Typography scale
```

---

## Summary

> This project is a **production-ready architecture showcase** for Android interviews, demonstrating Clean Architecture, MVI, Jetpack Compose, Hilt DI, Room DB, and a custom Material 3 design system — all written in modern Kotlin with KSP-based code generation.
