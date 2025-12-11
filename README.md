# Android Multi-Module Project (Kotlin)

A modern, scalable Android application with multi-module architecture built with Kotlin, Gradle DSL, and Jetpack libraries.

## Project Overview

This is a production-ready Android project showcasing best practices for multi-module architecture with clear separation of concerns, dependency injection, and MVVM pattern implementation.

### Technologies & Libraries

- **Kotlin 1.9.20** - Modern Android development language
- **Gradle 8.2.0** with Kotlin DSL - Build system
- **Jetpack Components:**
  - AndroidX Core KTX
  - ViewModel & LiveData
  - Jetpack Compose for UI
  - Navigation Compose
- **Dependency Injection:** Hilt (Dagger 2 extension)
- **Serialization:** Kotlinx Serialization
- **Networking:** Retrofit 2 & OkHttp
- **Coroutines:** Kotlinx Coroutines for async operations
- **Target:** SDK 34 (Android 14)
- **Min SDK:** 24 (Android 7.0)

## Module Structure

```
android-multimodule/
├── app/                              # Main application module
│   ├── src/main/java/com/example/androidmultimodule/
│   │   ├── MainApplication.kt        # Hilt entry point
│   │   └── ui/
│   │       ├── MainActivity.kt        # Main activity with navigation
│   │       ├── home/
│   │       │   ├── HomeScreen.kt
│   │       │   └── HomeViewModel.kt
│   │       └── theme/
│   │           ├── Theme.kt
│   │           └── Type.kt
│   └── build.gradle.kts              # App dependencies
│
├── libraries/                         # Reusable library modules
│   ├── shared-models/                # Shared data models & DTOs
│   │   ├── src/main/java/com/example/shared_models/
│   │   │   └── Result.kt
│   │   └── build.gradle.kts
│   │
│   ├── smali-translator/             # Smali code translation
│   │   ├── src/main/java/com/example/smali_translator/
│   │   │   ├── data/
│   │   │   │   └── repository/       # Data layer
│   │   │   │       └── SmaliRepository.kt
│   │   │   ├── domain/
│   │   │   │   └── usecase/          # Business logic
│   │   │   │       └── TranslateSmaliUseCase.kt
│   │   │   ├── presentation/
│   │   │   │   └── viewmodel/        # Presentation layer
│   │   │   │       └── SmaliViewModel.kt
│   │   │   └── di/
│   │   │       └── SmaliModule.kt    # Hilt dependency injection
│   │   └── build.gradle.kts
│   │
│   └── arabic-explainer/             # Arabic language explanations
│       ├── src/main/java/com/example/arabic_explainer/
│       │   ├── data/
│       │   │   ├── model/            # Data models
│       │   │   │   └── ArabicWord.kt
│       │   │   └── repository/       # Data layer
│       │   │       └── ArabicRepository.kt
│       │   ├── domain/
│       │   │   └── usecase/          # Business logic
│       │   │       └── ExplainArabicWordUseCase.kt
│       │   ├── presentation/
│       │   │   └── viewmodel/        # Presentation layer
│       │   │       └── ArabicViewModel.kt
│       │   └── di/
│       │       └── ArabicModule.kt   # Hilt dependency injection
│       └── build.gradle.kts
│
├── build.gradle.kts                  # Root build configuration
├── settings.gradle.kts               # Project module definitions
└── gradle/libs.versions.toml         # Centralized dependency versions
```

## Module Responsibilities

### App Module (`:app`)
- **Purpose:** Main application entry point
- **Responsibilities:**
  - MainActivity and activity-related UI
  - Navigation setup and routing
  - Application theming and styling
  - Jetpack Compose UI implementation
  - Aggregates and uses features from library modules
- **Dependencies:** All library modules
- **UI Framework:** Jetpack Compose

### Shared Models Module (`:libraries:shared-models`)
- **Purpose:** Shared data structures across modules
- **Responsibilities:**
  - Common DTOs and data models
  - Result wrapper for API responses
  - Shared serialization models
- **Dependencies:** Kotlinx Serialization only
- **No Android dependencies** - Pure Kotlin library

### Smali Translator Module (`:libraries:smali-translator`)
- **Purpose:** Smali bytecode translation functionality
- **Responsibilities:**
  - Translating Smali code to human-readable format
  - Managing translation state and caching
  - Providing translation services via UseCase pattern
- **Architecture Layers:**
  - **Data Layer:** SmaliRepository (data access)
  - **Domain Layer:** TranslateSmaliUseCase (business logic)
  - **Presentation Layer:** SmaliViewModel (UI state management)
- **DI:** Hilt modules in `di/SmaliModule.kt`
- **Key Features:** Coroutine-based async translation

### Arabic Explainer Module (`:libraries:arabic-explainer`)
- **Purpose:** Generate detailed Arabic technical explanations for Smali instructions/methods.
- **Responsibilities:**
  - Consuming translator output/IR or raw Smali text
  - Producing per-instruction and full-method step-by-step narratives in Arabic
  - Handling malformed/unsupported Smali with localized Arabic issue descriptions
  - Ensuring correct RTL formatting while embedding LTR tokens (e.g., `v0`, `p0`, method refs)
- **Core API:** `ArabicSmaliExplainer` (see `libraries/arabic-explainer/README.md`)

## MVVM Architecture Pattern

All modules follow the MVVM (Model-View-ViewModel) pattern:

```
View (UI)
  ↓
ViewModel (State Management & UseCase orchestration)
  ↓
UseCase (Business Logic)
  ↓
Repository (Data Access)
  ↓
Data Source (API, Database, etc.)
```

### Layers Explanation

- **Presentation:** ViewModels, Composables, UI state management
- **Domain:** UseCases containing business logic
- **Data:** Repositories implementing data access abstraction

## Dependency Injection with Hilt

All modules use Hilt for compile-time safe dependency injection:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: MyUseCase
) : ViewModel()

@Module
@InstallIn(SingletonComponent::class)
abstract class MyModule {
    @Binds
    abstract fun bindRepository(impl: MyRepositoryImpl): MyRepository
}
```

### Entry Points

- **Application Level:** `@HiltAndroidApp` on `MainApplication`
- **Activity Level:** `@AndroidEntryPoint` on `MainActivity`
- **ViewModel Level:** `@HiltViewModel` for ViewModels
- **Module Level:** `@Module @InstallIn(SingletonComponent::class)` for DI modules

## Building & Running

### Prerequisites

- Android SDK 34 (Android 14)
- Kotlin 1.9.20
- Gradle 8.2.0

### Build Commands

```bash
# Assemble all modules (APK/AAB creation)
./gradlew assemble

# Build all modules
./gradlew build

# Run debug build
./gradlew assembleDebug

# Run release build
./gradlew assembleRelease

# Build specific module
./gradlew :app:assemble
./gradlew :libraries:smali-translator:assemble

# Run tests
./gradlew test

# Clean build
./gradlew clean assemble
```

### Gradle Configuration Highlights

- **Kotlin DSL:** All build scripts use Kotlin for type-safe configurations
- **Centralized Versions:** `gradle/libs.versions.toml` manages all dependency versions
- **Plugin Management:** Centralized in settings.gradle.kts
- **Common Configuration:** Root build.gradle.kts applies common settings to all modules

## Jetpack Compose Setup

The project is configured with Jetpack Compose for modern declarative UI:

- **Compose BOM Version:** 2024.01.00
- **Compose Compiler Version:** 1.5.8
- **Material3 Design System** for modern Material Design 3

### Example Composable

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Multi-Module Android App")
        Button(onClick = { }) {
            Text("Navigate")
        }
    }
}
```

## Coroutines & Async Operations

All async operations use Kotlin Coroutines:

```kotlin
viewModelScope.launch {
    val result = useCase(params)
    _uiState.value = result
}
```

### Flow for Continuous Data Streams

```kotlin
override fun getWords(): Flow<List<Word>> = flow {
    emit(repository.fetchWords())
}
```

## Serialization

The project uses Kotlinx Serialization for type-safe JSON handling:

```kotlin
@Serializable
data class ArabicWord(
    val word: String,
    val translation: String
)

// Serialization
val json = Json.encodeToString(arabicWord)

// Deserialization
val word = Json.decodeFromString<ArabicWord>(json)
```

## Project Sync & Gradle Sync

To sync the project with Gradle:

```bash
# Sync Gradle dependencies
./gradlew --refresh-dependencies

# Full clean and sync
./gradlew clean sync
```

## Testing

Each module includes test configurations:

- **Unit Tests:** JUnit 4
- **Android Tests:** AndroidX Test with Espresso
- **Compose Tests:** Compose UI Test framework

```kotlin
class SmaliViewModelTest {
    @Test
    fun testTranslation() {
        // Test implementation
    }
}
```

## Package Naming Convention

```
com.example.<module-name>.<layer>.<feature>

Examples:
- com.example.androidmultimodule.ui.home (app module UI)
- com.example.smali_translator.data.repository (smali module data)
- com.example.arabic_explainer.domain.usecase (arabic module domain)
- com.example.shared_models (shared models)
```

## Important Configuration Files

### `build.gradle.kts` (Root)
- Common Android SDK configuration
- Plugin versions
- Shared extension functions for module configuration

### `settings.gradle.kts`
- Project structure definition
- Module inclusion
- Repository configuration

### `gradle/libs.versions.toml`
- Centralized dependency version management
- Library bundles for common dependencies
- Plugin definitions

## Best Practices Implemented

✅ **Modular Architecture** - Clear separation of concerns
✅ **Type Safety** - Kotlin DSL for build scripts
✅ **Dependency Injection** - Hilt for compile-time safe DI
✅ **MVVM Pattern** - Proper layer separation
✅ **Jetpack Libraries** - Latest Android components
✅ **Coroutines** - Non-blocking async operations
✅ **Compose UI** - Modern declarative UI framework
✅ **Centralized Configuration** - Single source of truth for versions
✅ **Naming Conventions** - Clear, consistent package structure
✅ **Resource Isolation** - Per-module resource management

## Future Enhancements

- Database integration (Room)
- Remote API integration (Retrofit + OkHttp)
- Unit and UI testing implementations
- CI/CD pipeline setup
- Performance monitoring
- Feature flags and A/B testing

## Troubleshooting

### Gradle Sync Fails
```bash
./gradlew --refresh-dependencies
./gradlew clean build
```

### Hilt Compilation Issues
Ensure all ViewModels and Services have `@Inject` constructors or use `@Provides` in modules.

### Kotlin Compiler Issues
Ensure Kotlin version (1.9.20) matches across all modules. Check `libs.versions.toml`.

## Contributing

When adding new modules:
1. Create module directory structure
2. Add module-specific `build.gradle.kts`
3. Include module in `settings.gradle.kts`
4. Create DI module for feature-specific injection
5. Follow MVVM architecture pattern

## License

This project is provided as-is for educational and development purposes.
