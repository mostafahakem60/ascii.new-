package com.smali.translator

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smali.translator.data.database.AppDatabase
import com.smali.translator.data.database.TranslationEntity
import com.smali.translator.domain.model.BatchJobStatus
import com.smali.translator.data.database.BatchJobEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveTranslation() = runBlocking {
        val translation = TranslationEntity(
            fileName = "Test.smali",
            smaliContent = ".class public LTest;",
            javaContent = "public class Test {}",
            explanation = "Test explanation",
            timestamp = System.currentTimeMillis(),
            fileHash = "hash123"
        )

        val id = database.translationDao().insertTranslation(translation)
        val retrieved = database.translationDao().getTranslationById(id)

        assert(retrieved != null)
        assert(retrieved?.fileName == "Test.smali")
        assert(retrieved?.fileHash == "hash123")
    }

    @Test
    fun testTranslationUniqueHash() = runBlocking {
        val translation1 = TranslationEntity(
            fileName = "Test.smali",
            smaliContent = ".class public LTest;",
            javaContent = "public class Test {}",
            explanation = "Test explanation",
            timestamp = System.currentTimeMillis(),
            fileHash = "hash123"
        )

        val translation2 = translation1.copy(fileName = "Test2.smali")

        database.translationDao().insertTranslation(translation1)
        database.translationDao().insertTranslation(translation2)

        val retrieved = database.translationDao().getTranslationByHash("hash123")
        assert(retrieved?.fileName == "Test2.smali") // Should replace the first one
    }

    @Test
    fun testBatchJobOperations() = runBlocking {
        val batchJob = BatchJobEntity(
            files = listOf("file1.smali", "file2.smali"),
            status = BatchJobStatus.PENDING,
            currentFileIndex = 0,
            totalFiles = 2,
            completedFiles = 0,
            failedFiles = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val id = database.batchJobDao().insertBatchJob(batchJob)
        val retrieved = database.batchJobDao().getBatchJobById(id)

        assert(retrieved != null)
        assert(retrieved?.files?.size == 2)
        assert(retrieved?.status == BatchJobStatus.PENDING)
    }

    @Test
    fun testBatchJobStatusUpdate() = runBlocking {
        val batchJob = BatchJobEntity(
            files = listOf("file1.smali"),
            status = BatchJobStatus.PENDING,
            currentFileIndex = 0,
            totalFiles = 1,
            completedFiles = 0,
            failedFiles = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val id = database.batchJobDao().insertBatchJob(batchJob)
        
        val updated = batchJob.copy(
            id = id,
            status = BatchJobStatus.COMPLETED,
            completedFiles = 1
        )
        database.batchJobDao().updateBatchJob(updated)

        val retrieved = database.batchJobDao().getBatchJobById(id)
        assert(retrieved?.status == BatchJobStatus.COMPLETED)
        assert(retrieved?.completedFiles == 1)
    }

    @Test
    fun testGetAllTranslations() = runBlocking {
        val translation1 = TranslationEntity(
            fileName = "Test1.smali",
            smaliContent = ".class public LTest1;",
            javaContent = "public class Test1 {}",
            explanation = "Test explanation 1",
            timestamp = System.currentTimeMillis(),
            fileHash = "hash1"
        )

        val translation2 = TranslationEntity(
            fileName = "Test2.smali",
            smaliContent = ".class public LTest2;",
            javaContent = "public class Test2 {}",
            explanation = "Test explanation 2",
            timestamp = System.currentTimeMillis(),
            fileHash = "hash2"
        )

        database.translationDao().insertTranslation(translation1)
        database.translationDao().insertTranslation(translation2)

        val allTranslations = database.translationDao().getAllTranslations().first()
        assert(allTranslations.size == 2)
    }

    @Test
    fun testDeleteCompletedJobs() = runBlocking {
        val pendingJob = BatchJobEntity(
            files = listOf("file1.smali"),
            status = BatchJobStatus.PENDING,
            currentFileIndex = 0,
            totalFiles = 1,
            completedFiles = 0,
            failedFiles = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val completedJob = pendingJob.copy(status = BatchJobStatus.COMPLETED)

        database.batchJobDao().insertBatchJob(pendingJob)
        database.batchJobDao().insertBatchJob(completedJob)

        database.batchJobDao().deleteCompletedJobs()

        val allJobs = database.batchJobDao().getAllBatchJobs().first()
        assert(allJobs.size == 1)
        assert(allJobs[0].status == BatchJobStatus.PENDING)
    }
}
