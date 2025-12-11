package com.smali.translator.domain.usecase

import android.net.Uri
import com.smali.translator.data.repository.BatchJobRepository
import com.smali.translator.data.repository.TranslationRepository
import com.smali.translator.domain.model.BatchJob
import com.smali.translator.domain.model.BatchJobStatus
import com.smali.translator.domain.model.BatchProgress
import com.smali.translator.util.FileIOHelper
import com.smali.translator.util.FileValidator
import com.smali.translator.util.HashUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BatchTranslateUseCase(
    private val fileIOHelper: FileIOHelper,
    private val fileValidator: FileValidator,
    private val translator: SmaliTranslator,
    private val translationRepository: TranslationRepository,
    private val batchJobRepository: BatchJobRepository
) {
    suspend fun createBatchJob(uris: List<Uri>): Long {
        val fileNames = uris.mapNotNull { fileIOHelper.getFileName(it) }
        val batchJob = BatchJob(
            files = fileNames,
            status = BatchJobStatus.PENDING
        )
        return batchJobRepository.saveBatchJob(batchJob)
    }

    fun executeBatch(jobId: Long, uris: List<Uri>): Flow<BatchProgress> = flow {
        var job = batchJobRepository.getBatchJobById(jobId) ?: return@flow
        
        job = job.copy(status = BatchJobStatus.PROCESSING)
        batchJobRepository.updateBatchJob(job)
        
        var completedCount = 0
        var failedCount = 0
        
        uris.forEachIndexed { index, uri ->
            val fileName = fileIOHelper.getFileName(uri) ?: "file_$index.smali"
            
            emit(BatchProgress(
                jobId = jobId,
                currentFile = fileName,
                currentIndex = index,
                totalFiles = uris.size,
                completedCount = completedCount,
                failedCount = failedCount
            ))
            
            try {
                val contentResult = fileIOHelper.readFileContent(uri)
                if (contentResult.isSuccess) {
                    val content = contentResult.getOrNull()!!
                    val fileHash = HashUtils.calculateFileHash(content)
                    
                    // Check if already translated
                    val existing = translationRepository.getTranslationByHash(fileHash)
                    if (existing != null) {
                        completedCount++
                    } else {
                        // Validate and translate
                        val validation = fileValidator.validate(content)
                        if (validation is com.smali.translator.domain.model.FileValidationResult.Valid) {
                            val javaContent = translator.translateToJava(content)
                            val explanation = translator.generateExplanation(content, javaContent)
                            
                            val translation = com.smali.translator.domain.model.TranslationResult(
                                fileName = fileName,
                                smaliContent = content,
                                javaContent = javaContent,
                                explanation = explanation,
                                fileHash = fileHash
                            )
                            
                            translationRepository.saveTranslation(translation)
                            completedCount++
                        } else {
                            failedCount++
                        }
                    }
                } else {
                    failedCount++
                }
            } catch (e: Exception) {
                failedCount++
            }
            
            // Update job progress
            job = job.copy(
                currentFileIndex = index,
                completedFiles = completedCount,
                failedFiles = failedCount,
                updatedAt = System.currentTimeMillis()
            )
            batchJobRepository.updateBatchJob(job)
        }
        
        // Mark job as completed
        job = job.copy(
            status = BatchJobStatus.COMPLETED,
            updatedAt = System.currentTimeMillis()
        )
        batchJobRepository.updateBatchJob(job)
        
        emit(BatchProgress(
            jobId = jobId,
            currentFile = "Completed",
            currentIndex = uris.size,
            totalFiles = uris.size,
            completedCount = completedCount,
            failedCount = failedCount
        ))
    }

    suspend fun cancelBatchJob(jobId: Long) {
        val job = batchJobRepository.getBatchJobById(jobId) ?: return
        batchJobRepository.updateBatchJob(
            job.copy(
                status = BatchJobStatus.CANCELLED,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun getBatchJobProgress(jobId: Long): Flow<BatchJob?> = flow {
        var previousJob: BatchJob? = null
        while (true) {
            val job = batchJobRepository.getBatchJobById(jobId)
            if (job != previousJob) {
                emit(job)
                previousJob = job
            }
            if (job?.status in listOf(BatchJobStatus.COMPLETED, BatchJobStatus.FAILED, BatchJobStatus.CANCELLED)) {
                break
            }
            kotlinx.coroutines.delay(500)
        }
    }
}
