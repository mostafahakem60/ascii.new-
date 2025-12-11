# API Documentation

This document provides detailed API documentation for the Smali Translator application components.

## Table of Contents

1. [Core Use Cases](#core-use-cases)
2. [Repositories](#repositories)
3. [Utilities](#utilities)
4. [Data Models](#data-models)
5. [ViewModels](#viewmodels)

---

## Core Use Cases

### TranslateFileUseCase

Handles the translation of a single Smali file to Java.

#### Constructor Parameters
- `fileIOHelper: FileIOHelper` - File I/O operations handler
- `fileValidator: FileValidator` - File validation handler
- `translator: SmaliTranslator` - Translation engine
- `repository: TranslationRepository` - Data persistence handler

#### Methods

##### execute(uri: Uri): Result

Translates a Smali file from the given URI.

**Parameters:**
- `uri: Uri` - URI of the Smali file to translate

**Returns:** `TranslateFileUseCase.Result`
- `Success(translation: TranslationResult)` - Successful translation
- `ValidationError(error: ValidationError)` - File validation failed
- `IOError(message: String)` - File I/O error
- `TranslationError(message: String)` - Translation process error

**Process:**
1. Read file content from URI
2. Calculate file hash
3. Check cache/database for existing translation
4. Validate file format
5. Perform translation
6. Save to database
7. Return result

**Example:**
```kotlin
val result = translateFileUseCase.execute(fileUri)
when (result) {
    is TranslateFileUseCase.Result.Success -> {
        val translation = result.translation
        println("Translated: ${translation.fileName}")
    }
    is TranslateFileUseCase.Result.ValidationError -> {
        println("Validation error: ${result.error.message}")
    }
    is TranslateFileUseCase.Result.IOError -> {
        println("I/O error: ${result.message}")
    }
    is TranslateFileUseCase.Result.TranslationError -> {
        println("Translation error: ${result.message}")
    }
}
```

---

### BatchTranslateUseCase

Manages batch processing of multiple Smali files.

#### Constructor Parameters
- `fileIOHelper: FileIOHelper`
- `fileValidator: FileValidator`
- `translator: SmaliTranslator`
- `translationRepository: TranslationRepository`
- `batchJobRepository: BatchJobRepository`

#### Methods

##### createBatchJob(uris: List<Uri>): Long

Creates a new batch job for the given file URIs.

**Parameters:**
- `uris: List<Uri>` - List of file URIs to process

**Returns:** `Long` - Job ID

**Example:**
```kotlin
val jobId = batchTranslateUseCase.createBatchJob(fileUris)
```

##### executeBatch(jobId: Long, uris: List<Uri>): Flow<BatchProgress>

Executes a batch job with progress updates.

**Parameters:**
- `jobId: Long` - ID of the batch job
- `uris: List<Uri>` - List of file URIs to process

**Returns:** `Flow<BatchProgress>` - Flow of progress updates

**Example:**
```kotlin
batchTranslateUseCase.executeBatch(jobId, fileUris)
    .collect { progress ->
        println("Progress: ${progress.completedCount}/${progress.totalFiles}")
        println("Current file: ${progress.currentFile}")
        println("Failed: ${progress.failedCount}")
    }
```

##### cancelBatchJob(jobId: Long)

Cancels a running batch job.

**Parameters:**
- `jobId: Long` - ID of the job to cancel

**Example:**
```kotlin
batchTranslateUseCase.cancelBatchJob(jobId)
```

##### getBatchJobProgress(jobId: Long): Flow<BatchJob?>

Gets continuous progress updates for a batch job.

**Parameters:**
- `jobId: Long` - ID of the job to monitor

**Returns:** `Flow<BatchJob?>` - Flow of job status updates

---

### ExportResultUseCase

Handles exporting translation results to files or sharing.

#### Constructor Parameters
- `fileIOHelper: FileIOHelper`

#### Methods

##### exportToFile(uri: Uri, translation: TranslationResult, includeExplanation: Boolean): ExportResult

Exports a translation to a file.

**Parameters:**
- `uri: Uri` - Destination file URI
- `translation: TranslationResult` - Translation to export
- `includeExplanation: Boolean` - Whether to include explanation in output

**Returns:** `ExportResult`
- `Success(fileName: String)` - Export successful
- `Error(message: String)` - Export failed

**Example:**
```kotlin
val result = exportResultUseCase.exportToFile(
    uri = outputUri,
    translation = translation,
    includeExplanation = true
)
when (result) {
    is ExportResultUseCase.ExportResult.Success -> {
        println("Exported to: ${result.fileName}")
    }
    is ExportResultUseCase.ExportResult.Error -> {
        println("Export failed: ${result.message}")
    }
}
```

##### createShareIntent(translation: TranslationResult, includeExplanation: Boolean): Intent

Creates an Android share intent for a translation.

**Parameters:**
- `translation: TranslationResult` - Translation to share
- `includeExplanation: Boolean` - Whether to include explanation

**Returns:** `Intent` - Share intent

**Example:**
```kotlin
val shareIntent = exportResultUseCase.createShareIntent(
    translation = translation,
    includeExplanation = false
)
startActivity(shareIntent)
```

##### exportBatchResults(translations: List<TranslationResult>, createFileCallback: suspend (String) -> Uri?): Map<String, ExportResult>

Exports multiple translation results.

**Parameters:**
- `translations: List<TranslationResult>` - Translations to export
- `createFileCallback: suspend (String) -> Uri?` - Callback to create output files

**Returns:** `Map<String, ExportResult>` - Map of filename to export result

---

## Repositories

### TranslationRepository

Manages translation data with cache-first strategy.

#### Methods

##### getAllTranslations(): Flow<List<TranslationResult>>

Gets all stored translations as a flow.

**Returns:** `Flow<List<TranslationResult>>`

##### getTranslationById(id: Long): TranslationResult?

Gets a translation by ID.

**Parameters:**
- `id: Long` - Translation ID

**Returns:** `TranslationResult?` - Translation or null

##### getTranslationByHash(fileHash: String): TranslationResult?

Gets a translation by file hash. Checks cache first, then database.

**Parameters:**
- `fileHash: String` - SHA-256 hash of file content

**Returns:** `TranslationResult?` - Translation or null

##### saveTranslation(translation: TranslationResult): Long

Saves a translation to database and cache.

**Parameters:**
- `translation: TranslationResult` - Translation to save

**Returns:** `Long` - Generated ID

##### updateTranslation(translation: TranslationResult)

Updates an existing translation.

##### deleteTranslation(translation: TranslationResult)

Deletes a translation from database and cache.

##### deleteAllTranslations()

Deletes all translations and clears cache.

##### getTranslationCount(): Int

Returns the total number of stored translations.

##### getCacheSize(): Int

Returns the current cache size.

##### clearCache()

Clears the in-memory cache.

---

### BatchJobRepository

Manages batch job lifecycle and persistence.

#### Methods

##### getAllBatchJobs(): Flow<List<BatchJob>>

Gets all batch jobs.

##### getBatchJobById(id: Long): BatchJob?

Gets a batch job by ID.

##### getBatchJobsByStatus(status: BatchJobStatus): Flow<List<BatchJob>>

Gets batch jobs filtered by status.

##### saveBatchJob(batchJob: BatchJob): Long

Saves a new batch job.

##### updateBatchJob(batchJob: BatchJob)

Updates an existing batch job.

##### deleteBatchJob(batchJob: BatchJob)

Deletes a batch job.

##### deleteCompletedJobs()

Deletes all completed, failed, or cancelled jobs.

---

## Utilities

### FileIOHelper

Handles file I/O operations using Storage Access Framework.

#### Constants

- `MAX_FILE_SIZE: Int = 10 * 1024 * 1024` - Maximum file size (10 MB)
- `SMALI_MIME_TYPE: String = "text/plain"` - MIME type for Smali files
- `JAVA_MIME_TYPE: String = "text/x-java"` - MIME type for Java files

#### Methods

##### readFileContent(uri: Uri): Result<String>

Reads file content from URI.

**Parameters:**
- `uri: Uri` - File URI

**Returns:** `Result<String>` - File content or error

##### writeFileContent(uri: Uri, content: String): Result<Unit>

Writes content to file at URI.

**Parameters:**
- `uri: Uri` - Destination URI
- `content: String` - Content to write

**Returns:** `Result<Unit>` - Success or error

##### createShareIntent(fileName: String, content: String): Intent

Creates a share intent for text content.

##### createSaveFileIntent(fileName: String, mimeType: String): Intent

Creates an intent to save a file using SAF.

##### createOpenFileIntent(): Intent

Creates an intent to open a single file.

##### createOpenMultipleFilesIntent(): Intent

Creates an intent to open multiple files.

##### getFileName(uri: Uri): String?

Gets the file name from a URI.

##### getFileExtension(uri: Uri): String?

Gets the file extension from a URI.

##### readMultipleFiles(uris: List<Uri>): Map<Uri, Result<String>>

Reads multiple files concurrently.

##### exportToUri(uri: Uri, content: String, fileName: String): Result<String>

Exports content to a URI.

---

### FileValidator

Validates Smali file format and content.

#### Methods

##### validate(content: String): FileValidationResult

Validates a file's content.

**Parameters:**
- `content: String` - File content to validate

**Returns:** `FileValidationResult`
- `Valid(content: String)` - File is valid
- `Invalid(error: ValidationError)` - File is invalid

**Validation Checks:**
1. File is not empty
2. File size within limits
3. Contains Smali format indicators
4. Has class definition
5. No malformed syntax

**Example:**
```kotlin
val result = fileValidator.validate(fileContent)
when (result) {
    is FileValidationResult.Valid -> {
        // Process valid content
    }
    is FileValidationResult.Invalid -> {
        println("Error: ${result.error.message}")
    }
}
```

##### validateBatch(contents: List<String>): List<Pair<Int, FileValidationResult>>

Validates multiple files.

**Parameters:**
- `contents: List<String>` - List of file contents

**Returns:** `List<Pair<Int, FileValidationResult>>` - Index and validation result pairs

---

### HashUtils

Utility for calculating file hashes.

#### Methods

##### calculateFileHash(content: String): String

Calculates SHA-256 hash of file content.

**Parameters:**
- `content: String` - File content

**Returns:** `String` - Hexadecimal hash string (64 characters)

**Example:**
```kotlin
val hash = HashUtils.calculateFileHash(fileContent)
// hash = "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"
```

---

## Data Models

### TranslationResult

Represents a completed translation.

#### Properties

- `id: Long = 0` - Database ID
- `fileName: String` - Original file name
- `smaliContent: String` - Original Smali content
- `javaContent: String` - Translated Java content
- `explanation: String` - Translation explanation
- `timestamp: Long` - Creation timestamp
- `fileHash: String` - SHA-256 hash of Smali content

---

### BatchJob

Represents a batch processing job.

#### Properties

- `id: Long = 0` - Job ID
- `files: List<String>` - List of file names
- `status: BatchJobStatus` - Current status
- `currentFileIndex: Int = 0` - Index of file being processed
- `totalFiles: Int` - Total number of files
- `completedFiles: Int = 0` - Number of successfully processed files
- `failedFiles: Int = 0` - Number of failed files
- `createdAt: Long` - Creation timestamp
- `updatedAt: Long` - Last update timestamp

#### BatchJobStatus enum

- `PENDING` - Job created but not started
- `PROCESSING` - Job is currently running
- `COMPLETED` - Job finished successfully
- `FAILED` - Job failed
- `CANCELLED` - Job was cancelled

---

### BatchProgress

Represents real-time progress of a batch job.

#### Properties

- `jobId: Long` - Associated job ID
- `currentFile: String` - Name of file being processed
- `currentIndex: Int` - Index of current file
- `totalFiles: Int` - Total number of files
- `completedCount: Int` - Number completed
- `failedCount: Int` - Number failed
- `progress: Float` - Progress ratio (0.0 to 1.0)

---

### FileValidationResult

Result of file validation.

#### Sealed Classes

- `Valid(content: String)` - File is valid
- `Invalid(error: ValidationError)` - File is invalid

#### ValidationError enum

- `EMPTY_FILE` - File is empty
- `NOT_SMALI_FORMAT` - Not in Smali format
- `MALFORMED_SYNTAX` - Syntax errors detected
- `FILE_TOO_LARGE` - Exceeds size limit
- `INVALID_ENCODING` - Unsupported encoding
- `MISSING_CLASS_DEFINITION` - No class definition found

---

## ViewModels

### TranslatorViewModel

Manages UI state for single file translation.

#### Properties

- `uiState: LiveData<TranslatorUiState>` - Current UI state
- `currentTranslation: LiveData<TranslationResult?>` - Current translation

#### Methods

##### translateFile(uri: Uri)

Initiates translation of a file.

##### exportToFile(uri: Uri, includeExplanation: Boolean)

Exports current translation to file.

##### createShareIntent(includeExplanation: Boolean)

Creates share intent for current translation.

##### resetState()

Resets UI state to idle.

#### TranslatorUiState

- `Idle` - Initial state
- `Loading` - Translation in progress
- `Success(translation)` - Translation completed
- `ValidationError(error)` - Validation failed
- `Error(message)` - Error occurred
- `Exporting` - Export in progress
- `ExportSuccess(fileName)` - Export completed
- `ShareReady(intent)` - Share intent ready

---

### BatchViewModel

Manages UI state for batch processing.

#### Properties

- `uiState: LiveData<BatchUiState>` - Current UI state
- `progress: LiveData<BatchProgress?>` - Current progress

#### Methods

##### startBatchTranslation(uris: List<Uri>)

Starts batch translation of multiple files.

##### cancelBatchJob()

Cancels the current batch job.

##### resetState()

Resets UI state to idle.

#### BatchUiState

- `Idle` - Initial state
- `Preparing` - Preparing batch job
- `Processing` - Batch job running
- `Completed(totalFiles, completed, failed)` - Batch completed
- `Cancelled` - Batch was cancelled
- `Error(message)` - Error occurred

---

## Error Handling

All asynchronous operations use Kotlin's `Result` type or sealed classes for error handling. Always handle all possible result types.

### Example: Comprehensive Error Handling

```kotlin
lifecycleScope.launch {
    try {
        val result = translateFileUseCase.execute(fileUri)
        when (result) {
            is TranslateFileUseCase.Result.Success -> {
                // Handle success
            }
            is TranslateFileUseCase.Result.ValidationError -> {
                when (result.error) {
                    ValidationError.EMPTY_FILE -> showError("File is empty")
                    ValidationError.NOT_SMALI_FORMAT -> showError("Not a Smali file")
                    ValidationError.MALFORMED_SYNTAX -> showError("Syntax error")
                    ValidationError.FILE_TOO_LARGE -> showError("File too large")
                    ValidationError.INVALID_ENCODING -> showError("Invalid encoding")
                    ValidationError.MISSING_CLASS_DEFINITION -> showError("Missing class")
                }
            }
            is TranslateFileUseCase.Result.IOError -> {
                showError("I/O Error: ${result.message}")
            }
            is TranslateFileUseCase.Result.TranslationError -> {
                showError("Translation Error: ${result.message}")
            }
        }
    } catch (e: Exception) {
        showError("Unexpected error: ${e.message}")
    }
}
```

## Threading Model

- All I/O operations run on `Dispatchers.IO`
- All database operations run on `Dispatchers.IO`
- UI updates use `Dispatchers.Main`
- Use `viewModelScope` or `lifecycleScope` for coroutines in UI components

## Best Practices

1. **Always observe LiveData** in lifecycle-aware components
2. **Collect Flows** with appropriate lifecycle scopes
3. **Handle all result types** in when expressions
4. **Cancel coroutines** when components are destroyed
5. **Use repository methods** instead of direct DAO access
6. **Validate files** before translation
7. **Check cache** before expensive operations
8. **Provide user feedback** for long-running operations
