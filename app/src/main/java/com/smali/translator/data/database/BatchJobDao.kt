package com.smali.translator.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchJobDao {
    @Query("SELECT * FROM batch_jobs ORDER BY createdAt DESC")
    fun getAllBatchJobs(): Flow<List<BatchJobEntity>>

    @Query("SELECT * FROM batch_jobs WHERE id = :id")
    suspend fun getBatchJobById(id: Long): BatchJobEntity?

    @Query("SELECT * FROM batch_jobs WHERE status = :status ORDER BY createdAt DESC")
    fun getBatchJobsByStatus(status: String): Flow<List<BatchJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatchJob(batchJob: BatchJobEntity): Long

    @Update
    suspend fun updateBatchJob(batchJob: BatchJobEntity)

    @Delete
    suspend fun deleteBatchJob(batchJob: BatchJobEntity)

    @Query("DELETE FROM batch_jobs WHERE status = 'COMPLETED' OR status = 'FAILED' OR status = 'CANCELLED'")
    suspend fun deleteCompletedJobs()
}
