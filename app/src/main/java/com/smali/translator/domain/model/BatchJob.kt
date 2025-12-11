package com.smali.translator.domain.model

enum class BatchJobStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class BatchJob(
    val id: Long = 0,
    val files: List<String>,
    val status: BatchJobStatus = BatchJobStatus.PENDING,
    val currentFileIndex: Int = 0,
    val totalFiles: Int = files.size,
    val completedFiles: Int = 0,
    val failedFiles: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class BatchProgress(
    val jobId: Long,
    val currentFile: String,
    val currentIndex: Int,
    val totalFiles: Int,
    val completedCount: Int,
    val failedCount: Int,
    val progress: Float = if (totalFiles > 0) completedCount.toFloat() / totalFiles else 0f
)
