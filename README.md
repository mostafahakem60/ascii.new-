# Smali Translator

A comprehensive Android application for translating Smali bytecode files to Java source code with support for batch processing, caching, and file I/O operations using Storage Access Framework (SAF).

## Features

### Core Functionality
- **Smali to Java Translation**: Convert Android DEX bytecode (Smali format) to readable Java source code
- **File Validation**: Robust validation of Smali files with detailed error messages
- **Explanations**: Generate detailed explanations of the translation process
- **Caching**: Intelligent caching layer for faster repeated translations

### File I/O
- **Storage Access Framework (SAF)**: Full integration with Android's SAF for secure file access
- **Import**: Import single or multiple Smali files
- **Export**: Save translated Java files to device storage
- **Share**: Share translation results via Android's share menu
- **File Size Limits**: Configurable file size limits (default: 10 MB)

### Batch Processing
- **Queue Management**: Process multiple files in sequence
- **Progress Tracking**: Real-time progress updates for batch jobs
- **Job Status**: Track pending, processing, completed, and failed jobs
- **Cancellation**: Cancel batch jobs in progress
- **Error Handling**: Graceful handling of individual file failures in batch operations

### Persistence
- **Room Database**: Store translation history and batch job information
- **DataStore**: Manage app preferences and settings
- **Cache Layer**: In-memory cache with TTL and size limits
- **Hash-based Deduplication**: Avoid re-translating identical files

## Architecture

The application follows clean architecture principles with clear separation of concerns:

```
app/
├── data/
│   ├── cache/          # In-memory caching layer
│   ├── database/       # Room database entities and DAOs
│   └── repository/     # Data repositories
├── domain/
│   ├── model/          # Domain models
│   └── usecase/        # Business logic use cases
├── ui/                 # User interface layer
└── util/               # Utility classes
```

### Key Components

#### Data Layer
- **TranslationCache**: LRU cache with time-based expiration
- **AppDatabase**: Room database for persistent storage
- **TranslationRepository**: Manages translation data with cache-first strategy
- **BatchJobRepository**: Manages batch job lifecycle

#### Domain Layer
- **SmaliTranslator**: Core translation engine
- **TranslateFileUseCase**: Handles single file translation with validation
- **BatchTranslateUseCase**: Manages batch processing with progress tracking
- **ExportResultUseCase**: Handles file export and sharing

#### Utilities
- **FileIOHelper**: SAF integration for file operations
- **FileValidator**: Comprehensive Smali file validation
- **HashUtils**: File content hashing for deduplication

## Supported Smali Format

The translator supports standard Smali format files with the following directives:

### Required Elements
- `.class` directive defining the class
- `.super` directive defining the superclass

### Supported Elements
- `.field` - Field declarations
- `.method` / `.end method` - Method definitions
- `.annotation` - Annotations
- Access modifiers (public, private, protected, final, abstract)

### Example Smali File
```smali
.class public Lcom/example/MyClass;
.super Ljava/lang/Object;

.field private value:I

.method public constructor <init>()V
    .locals 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public getValue()I
    .locals 1
    iget v0, p0, Lcom/example/MyClass;->value:I
    return v0
.end method
```

## File Validation

The validator checks for:

1. **Empty Files**: Files must contain content
2. **Smali Format**: Files must contain Smali directives (`.class` or `.super`)
3. **Class Definition**: Files must have a `.class` directive
4. **Syntax Validation**: Basic syntax checking (matched `.method`/`.end method` pairs)
5. **File Size**: Files must not exceed the maximum size limit
6. **Encoding**: Files must use supported text encoding

### Validation Errors

| Error | Description |
|-------|-------------|
| `EMPTY_FILE` | File contains no content |
| `NOT_SMALI_FORMAT` | File doesn't appear to be Smali format |
| `MISSING_CLASS_DEFINITION` | No `.class` directive found |
| `MALFORMED_SYNTAX` | Syntax errors detected |
| `FILE_TOO_LARGE` | File exceeds size limit |
| `INVALID_ENCODING` | Unsupported file encoding |

## Cache Behavior

### Cache Configuration
- **Max Size**: 50 entries (LRU eviction)
- **TTL**: 1 hour
- **Strategy**: Cache-first with database fallback

### Cache Operations
1. Check cache for file hash
2. If miss, check database
3. If miss, perform translation
4. Store in both cache and database

## Batch Processing

### Batch Job Lifecycle

1. **Creation**: Create batch job with list of file URIs
2. **Queuing**: Files added to processing queue
3. **Processing**: Sequential processing with progress updates
4. **Completion**: Final status with success/failure counts

### Progress Updates

Progress information includes:
- Current file being processed
- Current index / total files
- Completed file count
- Failed file count
- Overall progress percentage

### Error Handling

- Individual file failures don't stop batch processing
- Failed files are counted and reported
- Validation errors are caught and recorded
- I/O errors are handled gracefully

## Testing

### Unit Tests
- `FileValidatorTest`: File validation logic
- `TranslationCacheTest`: Cache behavior and eviction
- `SmaliTranslatorTest`: Translation correctness
- `HashUtilsTest`: Hash generation

### Integration Tests
- `FileIOIntegrationTest`: SAF file operations
- `DatabaseIntegrationTest`: Room database operations
- `BatchProcessingIntegrationTest`: End-to-end batch processing

### Running Tests

```bash
# Unit tests
./gradlew test

# Integration tests
./gradlew connectedAndroidTest

# All tests
./gradlew testDebug connectedAndroidTest
```

## Usage Examples

### Single File Translation

```kotlin
val translateFileUseCase = TranslateFileUseCase(
    fileIOHelper,
    fileValidator,
    translator,
    repository
)

val result = translateFileUseCase.execute(fileUri)
when (result) {
    is TranslateFileUseCase.Result.Success -> {
        // Handle successful translation
        val translation = result.translation
    }
    is TranslateFileUseCase.Result.ValidationError -> {
        // Handle validation error
    }
    is TranslateFileUseCase.Result.IOError -> {
        // Handle I/O error
    }
}
```

### Batch Processing

```kotlin
val batchTranslateUseCase = BatchTranslateUseCase(
    fileIOHelper,
    fileValidator,
    translator,
    translationRepository,
    batchJobRepository
)

val jobId = batchTranslateUseCase.createBatchJob(uris)

batchTranslateUseCase.executeBatch(jobId, uris)
    .collect { progress ->
        // Update UI with progress
        updateProgressUI(
            progress.currentFile,
            progress.progress,
            progress.completedCount,
            progress.failedCount
        )
    }
```

### Export Results

```kotlin
val exportResultUseCase = ExportResultUseCase(fileIOHelper)

// Export to file
val result = exportResultUseCase.exportToFile(
    uri = outputUri,
    translation = translation,
    includeExplanation = true
)

// Or create share intent
val shareIntent = exportResultUseCase.createShareIntent(
    translation = translation,
    includeExplanation = true
)
startActivity(shareIntent)
```

## Dependencies

- **AndroidX Core**: Core Android libraries
- **Kotlin Coroutines**: Asynchronous programming
- **Room**: Local database persistence
- **DataStore**: Preferences storage
- **WorkManager**: Background job scheduling
- **Jetpack Compose**: Modern UI toolkit
- **Material3**: Material Design components

## Configuration

### File Size Limit

Modify in `FileIOHelper`:
```kotlin
companion object {
    const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10 MB
}
```

### Cache Configuration

Modify in `TranslationCache`:
```kotlin
private val maxSize = 50
private val maxAgeMillis = 3600000L // 1 hour
```

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on device
./gradlew installDebug
```

## Requirements

- Android SDK 24 (Android 7.0) or higher
- Kotlin 1.9.0
- Gradle 8.1.0

## License

This project is provided as-is for educational and development purposes.

## Contributing

When contributing, please:
1. Follow the existing architecture patterns
2. Add tests for new functionality
3. Update documentation
4. Ensure all tests pass before submitting

## Future Enhancements

- [ ] Parallel batch processing
- [ ] Export to multiple formats (PDF, HTML)
- [ ] Dark mode support
- [ ] Advanced Smali analysis
- [ ] Bytecode optimization detection
- [ ] Class dependency graphs
