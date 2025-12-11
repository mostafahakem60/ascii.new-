package com.smali.translator.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.smali.translator.domain.model.BatchJob
import com.smali.translator.domain.model.BatchJobStatus

@Entity(tableName = "batch_jobs")
@TypeConverters(BatchJobConverters::class)
data class BatchJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val files: List<String>,
    val status: BatchJobStatus,
    val currentFileIndex: Int,
    val totalFiles: Int,
    val completedFiles: Int,
    val failedFiles: Int,
    val createdAt: Long,
    val updatedAt: Long
)

class BatchJobConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(separator = "||")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split("||")
    }

    @TypeConverter
    fun fromBatchJobStatus(status: BatchJobStatus): String {
        return status.name
    }

    @TypeConverter
    fun toBatchJobStatus(value: String): BatchJobStatus {
        return BatchJobStatus.valueOf(value)
    }
}

fun BatchJobEntity.toDomain(): BatchJob {
    return BatchJob(
        id = id,
        files = files,
        status = status,
        currentFileIndex = currentFileIndex,
        totalFiles = totalFiles,
        completedFiles = completedFiles,
        failedFiles = failedFiles,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun BatchJob.toEntity(): BatchJobEntity {
    return BatchJobEntity(
        id = id,
        files = files,
        status = status,
        currentFileIndex = currentFileIndex,
        totalFiles = totalFiles,
        completedFiles = completedFiles,
        failedFiles = failedFiles,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
