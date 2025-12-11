# Contributing to Smali Translator

Thank you for your interest in contributing to the Smali Translator project! This document provides guidelines and information for contributors.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Development Setup](#development-setup)
4. [Architecture Guidelines](#architecture-guidelines)
5. [Coding Standards](#coding-standards)
6. [Testing Requirements](#testing-requirements)
7. [Submitting Changes](#submitting-changes)
8. [Issue Reporting](#issue-reporting)

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on what is best for the project and community
- Show empathy towards other community members

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 24-34
- Git

### Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/smali-translator.git
   cd smali-translator
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Run the app**
   ```bash
   ./gradlew installDebug
   ```

4. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

## Architecture Guidelines

The project follows Clean Architecture principles with three main layers:

### Data Layer (`data/`)

- **Responsibilities**: Data persistence, caching, external data sources
- **Components**: Database entities, DAOs, repositories, cache
- **Rules**:
  - No direct UI dependencies
  - Use repository pattern for data access
  - Implement cache-first strategy where appropriate

### Domain Layer (`domain/`)

- **Responsibilities**: Business logic, use cases, domain models
- **Components**: Use cases, domain models, business rules
- **Rules**:
  - No Android framework dependencies
  - Pure Kotlin/Java code
  - Testable without Android environment

### UI Layer (`ui/`)

- **Responsibilities**: User interface, user interaction
- **Components**: Activities, Composables, ViewModels
- **Rules**:
  - Observe data through ViewModels
  - Use Jetpack Compose for UI
  - Handle lifecycle appropriately

## Coding Standards

### Kotlin Style Guide

Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html):

#### Naming Conventions

```kotlin
// Classes: PascalCase
class TranslationRepository

// Functions: camelCase
fun translateFile()

// Properties: camelCase
val fileName: String

// Constants: UPPER_SNAKE_CASE
const val MAX_FILE_SIZE = 10_000_000

// Private properties: start with underscore
private val _uiState = MutableLiveData<State>()
```

#### Code Organization

```kotlin
class ExampleClass(
    private val dependency1: Dependency1,
    private val dependency2: Dependency2
) {
    // Companion object (if needed)
    companion object {
        const val CONSTANT = "value"
    }
    
    // Properties
    private val property1 = ""
    
    // Init blocks
    init {
        // Initialization
    }
    
    // Public methods
    fun publicMethod() { }
    
    // Private methods
    private fun privateMethod() { }
}
```

### Documentation

#### Class Documentation

```kotlin
/**
 * Handles translation of Smali files to Java source code.
 *
 * This class performs validation, translation, and caching of results.
 * It uses a cache-first strategy to avoid redundant translations.
 *
 * @property fileValidator Validates Smali file format
 * @property translator Core translation engine
 */
class TranslateFileUseCase(
    private val fileValidator: FileValidator,
    private val translator: SmaliTranslator
)
```

#### Function Documentation

```kotlin
/**
 * Translates a Smali file from the given URI.
 *
 * The function performs the following steps:
 * 1. Reads file content
 * 2. Validates format
 * 3. Checks cache
 * 4. Performs translation
 * 5. Saves result
 *
 * @param uri URI of the Smali file to translate
 * @return Result indicating success or failure with details
 * @throws IOException if file cannot be read
 */
suspend fun execute(uri: Uri): Result
```

### Error Handling

Use sealed classes or Result types for error handling:

```kotlin
sealed class Result {
    data class Success(val data: Data) : Result()
    data class Error(val message: String) : Result()
}

// Usage
when (val result = operation()) {
    is Result.Success -> handleSuccess(result.data)
    is Result.Error -> handleError(result.message)
}
```

### Coroutines

- Use structured concurrency
- Use appropriate dispatchers
- Handle cancellation properly

```kotlin
viewModelScope.launch {
    withContext(Dispatchers.IO) {
        // I/O operations
    }
    // UI updates on Main dispatcher
}
```

## Testing Requirements

### Test Coverage

All new features must include:

1. **Unit Tests** (minimum 80% coverage)
   - Test business logic
   - Test edge cases
   - Test error conditions

2. **Integration Tests**
   - Test component interactions
   - Test database operations
   - Test file I/O operations

3. **End-to-End Tests** (for critical paths)
   - Test complete user flows
   - Test batch processing
   - Test export functionality

### Writing Tests

#### Unit Test Example

```kotlin
class TranslatorTest {
    private lateinit var translator: SmaliTranslator
    
    @Before
    fun setup() {
        translator = SmaliTranslator()
    }
    
    @Test
    fun `test basic class translation`() = runBlocking {
        val smali = ".class public LTest;"
        val java = translator.translateToJava(smali)
        
        assert(java.contains("class Test"))
    }
}
```

#### Integration Test Example

```kotlin
@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {
    private lateinit var database: AppDatabase
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
    }
    
    @Test
    fun testInsertAndRetrieve() = runBlocking {
        // Test implementation
    }
}
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew connectedAndroidTest

# Specific test class
./gradlew test --tests TranslatorTest

# With coverage
./gradlew testDebugUnitTestCoverage
```

## Submitting Changes

### Branch Naming

- `feature/` - New features (e.g., `feature/pdf-export`)
- `bugfix/` - Bug fixes (e.g., `bugfix/cache-invalidation`)
- `refactor/` - Code refactoring (e.g., `refactor/repository-layer`)
- `test/` - Test additions (e.g., `test/batch-processing`)
- `docs/` - Documentation updates (e.g., `docs/api-documentation`)

### Commit Messages

Follow the conventional commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

Example:
```
feat(batch): add progress notifications for batch jobs

- Add notification channel for batch processing
- Show progress in notification
- Add action to cancel batch from notification

Closes #123
```

### Pull Request Process

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Follow coding standards
   - Add tests
   - Update documentation

3. **Run tests**
   ```bash
   ./gradlew test connectedAndroidTest
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: your feature description"
   ```

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**
   - Provide a clear title and description
   - Reference any related issues
   - Include screenshots for UI changes
   - Ensure all checks pass

### Pull Request Checklist

- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] All tests pass
- [ ] No new warnings introduced
- [ ] Backward compatible (or migration provided)

## Issue Reporting

### Bug Reports

Use the bug report template and include:

- **Description**: Clear description of the bug
- **Steps to Reproduce**: Detailed steps to reproduce
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Environment**: Android version, device, app version
- **Logs**: Relevant log output
- **Screenshots**: If applicable

### Feature Requests

Use the feature request template and include:

- **Problem Description**: What problem does this solve?
- **Proposed Solution**: How should it work?
- **Alternatives**: Other solutions considered
- **Additional Context**: Mockups, examples, etc.

## Development Tips

### Debugging

1. **Enable verbose logging**
   ```kotlin
   if (BuildConfig.DEBUG) {
       Log.d(TAG, "Debug information")
   }
   ```

2. **Use Android Studio debugger**
   - Set breakpoints
   - Use evaluate expression
   - Watch variables

3. **Database inspection**
   ```bash
   adb shell
   run-as com.smali.translator
   cd databases
   sqlite3 smali_translator_database
   ```

### Performance

- Profile with Android Studio Profiler
- Watch for memory leaks
- Optimize database queries
- Use appropriate data structures

### Code Review Guidelines

When reviewing code:

- Check for correctness
- Verify test coverage
- Look for potential bugs
- Suggest improvements
- Be constructive and respectful

## Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Developer Guide](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

## Questions?

If you have questions:

1. Check existing documentation
2. Search existing issues
3. Ask in discussions
4. Create a new issue with the question label

Thank you for contributing to Smali Translator!
