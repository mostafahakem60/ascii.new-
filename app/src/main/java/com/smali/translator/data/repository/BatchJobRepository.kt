package com.smali.translator.data.repository

import com.smali.translator.data.database.BatchJobDao
import com.smali.translator.data.database.toDomain
import com.smali.translator.data.database.toEntity
import com.smali.translator.domain.model.BatchJob
import com.smali.translator.domain.model.BatchJobStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BatchJobRepository(
    private val batchJobDao: BatchJobDao
) {
    fun getAllBatchJobs(): Flow<List<BatchJob>> {
        return batchJobDao.getAllBatchJobs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getBatchJobById(id: Long): BatchJob? {
        return batchJobDao.getBatchJobById(id)?.toDomain()
    }

    fun getBatchJobsByStatus(status: BatchJobStatus): Flow<List<BatchJob>> {
        return batchJobDao.getBatchJobsByStatus(status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveBatchJob(batchJob: BatchJob): Long {
        return batchJobDao.insertBatchJob(batchJob.toEntity())
    }

    suspend fun updateBatchJob(batchJob: BatchJob) {
        batchJobDao.updateBatchJob(batchJob.toEntity())
    }

    suspend fun deleteBatchJob(batchJob: BatchJob) {
        batchJobDao.deleteBatchJob(batchJob.toEntity())
    }

    suspend fun deleteCompletedJobs() {
        batchJobDao.deleteCompletedJobs()
    }
}
