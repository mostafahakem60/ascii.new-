package com.smali.translator

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smali.translator.data.cache.TranslationCache
import com.smali.translator.data.database.AppDatabase
import com.smali.translator.data.repository.BatchJobRepository
import com.smali.translator.data.repository.TranslationRepository
import com.smali.translator.domain.usecase.BatchTranslateUseCase
import com.smali.translator.domain.usecase.SmaliTranslator
import com.smali.translator.util.FileIOHelper
import com.smali.translator.util.FileValidator
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BatchProcessingIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var batchTranslateUseCase: BatchTranslateUseCase
    private lateinit var testFiles: List<File>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        val cache = TranslationCache()
        val translationRepository = TranslationRepository(database.translationDao(), cache)
        val batchJobRepository = BatchJobRepository(database.batchJobDao())
        val fileIOHelper = FileIOHelper(context)
        val fileValidator = FileValidator()
        val translator = SmaliTranslator()

        batchTranslateUseCase = BatchTranslateUseCase(
            fileIOHelper,
            fileValidator,
            translator,
            translationRepository,
            batchJobRepository
        )

        // Create test files
        testFiles = createTestFiles()
    }

    @After
    fun teardown() {
        database.close()
        testFiles.forEach { it.delete() }
    }

    private fun createTestFiles(): List<File> {
        val file1 = File(context.cacheDir, "Test1.smali").apply {
            writeText("""
                .class public LTest1;
                .super Ljava/lang/Object;
                
                .method public test()V
                    return-void
                .end method
            """.trimIndent())
        }

        val file2 = File(context.cacheDir, "Test2.smali").apply {
            writeText("""
                .class public LTest2;
                .super Ljava/lang/Object;
                
                .field public value:I
                
                .method public getValue()I
                    return-void
                .end method
            """.trimIndent())
        }

        return listOf(file1, file2)
    }

    @Test
    fun testBatchJobCreation() = runBlocking {
        val uris = testFiles.map { Uri.fromFile(it) }
        val jobId = batchTranslateUseCase.createBatchJob(uris)
        
        assert(jobId > 0)
        
        val job = database.batchJobDao().getBatchJobById(jobId)
        assert(job != null)
        assert(job?.totalFiles == 2)
    }

    @Test
    fun testBatchExecution() = runBlocking {
        val uris = testFiles.map { Uri.fromFile(it) }
        val jobId = batchTranslateUseCase.createBatchJob(uris)
        
        val progressList = batchTranslateUseCase.executeBatch(jobId, uris).toList()
        
        assert(progressList.isNotEmpty())
        
        val lastProgress = progressList.last()
        assert(lastProgress.completedCount == 2)
        assert(lastProgress.failedCount == 0)
    }

    @Test
    fun testBatchWithInvalidFile() = runBlocking {
        val validFile = testFiles[0]
        val invalidFile = File(context.cacheDir, "Invalid.smali").apply {
            writeText("This is not a valid Smali file")
        }

        val uris = listOf(Uri.fromFile(validFile), Uri.fromFile(invalidFile))
        val jobId = batchTranslateUseCase.createBatchJob(uris)
        
        val progressList = batchTranslateUseCase.executeBatch(jobId, uris).toList()
        
        val lastProgress = progressList.last()
        assert(lastProgress.completedCount == 1)
        assert(lastProgress.failedCount == 1)
        
        invalidFile.delete()
    }

    @Test
    fun testBatchCancellation() = runBlocking {
        val uris = testFiles.map { Uri.fromFile(it) }
        val jobId = batchTranslateUseCase.createBatchJob(uris)
        
        batchTranslateUseCase.cancelBatchJob(jobId)
        
        val job = database.batchJobDao().getBatchJobById(jobId)
        assert(job?.status == com.smali.translator.domain.model.BatchJobStatus.CANCELLED)
    }
}
