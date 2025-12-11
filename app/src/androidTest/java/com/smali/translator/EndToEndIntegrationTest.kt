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
import com.smali.translator.domain.usecase.*
import com.smali.translator.util.FileIOHelper
import com.smali.translator.util.FileValidator
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class EndToEndIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var translateFileUseCase: TranslateFileUseCase
    private lateinit var exportResultUseCase: ExportResultUseCase
    private lateinit var batchTranslateUseCase: BatchTranslateUseCase
    private lateinit var testFile: File

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

        translateFileUseCase = TranslateFileUseCase(
            fileIOHelper,
            fileValidator,
            translator,
            translationRepository
        )

        exportResultUseCase = ExportResultUseCase(fileIOHelper)

        batchTranslateUseCase = BatchTranslateUseCase(
            fileIOHelper,
            fileValidator,
            translator,
            translationRepository,
            batchJobRepository
        )

        // Create test file
        testFile = createTestFile()
    }

    @After
    fun teardown() {
        database.close()
        testFile.delete()
    }

    private fun createTestFile(): File {
        return File(context.cacheDir, "CompleteTest.smali").apply {
            writeText("""
                .class public Lcom/example/CompleteTest;
                .super Ljava/lang/Object;
                
                .field public value:I
                .field private name:Ljava/lang/String;
                
                .method public constructor <init>()V
                    .locals 1
                    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
                    return-void
                .end method
                
                .method public getValue()I
                    .locals 1
                    iget v0, p0, Lcom/example/CompleteTest;->value:I
                    return v0
                .end method
                
                .method public setValue(I)V
                    .locals 0
                    iput p1, p0, Lcom/example/CompleteTest;->value:I
                    return-void
                .end method
            """.trimIndent())
        }
    }

    @Test
    fun testCompleteTranslationFlow() = runBlocking {
        // Step 1: Translate file
        val uri = Uri.fromFile(testFile)
        val result = translateFileUseCase.execute(uri)
        
        assert(result is TranslateFileUseCase.Result.Success)
        
        val translation = (result as TranslateFileUseCase.Result.Success).translation
        assert(translation.fileName == "CompleteTest.smali")
        assert(translation.javaContent.contains("class CompleteTest"))
        assert(translation.javaContent.contains("int value"))
        assert(translation.javaContent.contains("getValue()"))
        assert(translation.explanation.isNotEmpty())
        
        // Step 2: Verify it's saved in database
        val savedTranslation = database.translationDao().getTranslationById(translation.id)
        assert(savedTranslation != null)
        assert(savedTranslation?.fileName == translation.fileName)
        
        // Step 3: Export to file
        val outputFile = File(context.cacheDir, "CompleteTest.java")
        val exportUri = Uri.fromFile(outputFile)
        
        val exportResult = exportResultUseCase.exportToFile(
            exportUri,
            translation,
            includeExplanation = true
        )
        
        assert(exportResult is ExportResultUseCase.ExportResult.Success)
        assert(outputFile.exists())
        
        val exportedContent = outputFile.readText()
        assert(exportedContent.contains(translation.explanation))
        assert(exportedContent.contains(translation.javaContent))
        
        outputFile.delete()
    }

    @Test
    fun testCacheHit() = runBlocking {
        val uri = Uri.fromFile(testFile)
        
        // First translation
        val result1 = translateFileUseCase.execute(uri)
        assert(result1 is TranslateFileUseCase.Result.Success)
        val translation1 = (result1 as TranslateFileUseCase.Result.Success).translation
        
        // Second translation should hit cache
        val result2 = translateFileUseCase.execute(uri)
        assert(result2 is TranslateFileUseCase.Result.Success)
        val translation2 = (result2 as TranslateFileUseCase.Result.Success).translation
        
        // Should be the same translation (from cache)
        assert(translation1.fileHash == translation2.fileHash)
        assert(translation1.id == translation2.id)
    }

    @Test
    fun testInvalidFileHandling() = runBlocking {
        val invalidFile = File(context.cacheDir, "Invalid.smali").apply {
            writeText("This is not a valid Smali file at all")
        }
        
        val uri = Uri.fromFile(invalidFile)
        val result = translateFileUseCase.execute(uri)
        
        assert(result is TranslateFileUseCase.Result.ValidationError)
        
        invalidFile.delete()
    }

    @Test
    fun testEmptyFileHandling() = runBlocking {
        val emptyFile = File(context.cacheDir, "Empty.smali").apply {
            writeText("")
        }
        
        val uri = Uri.fromFile(emptyFile)
        val result = translateFileUseCase.execute(uri)
        
        assert(result is TranslateFileUseCase.Result.ValidationError)
        assert((result as TranslateFileUseCase.Result.ValidationError).error == 
            com.smali.translator.domain.model.ValidationError.EMPTY_FILE)
        
        emptyFile.delete()
    }

    @Test
    fun testBatchProcessingEndToEnd() = runBlocking {
        // Create multiple test files
        val file1 = File(context.cacheDir, "Batch1.smali").apply {
            writeText("""
                .class public LBatch1;
                .super Ljava/lang/Object;
            """.trimIndent())
        }
        
        val file2 = File(context.cacheDir, "Batch2.smali").apply {
            writeText("""
                .class public LBatch2;
                .super Ljava/lang/Object;
            """.trimIndent())
        }
        
        val file3 = File(context.cacheDir, "Batch3.smali").apply {
            writeText("""
                .class public LBatch3;
                .super Ljava/lang/Object;
            """.trimIndent())
        }
        
        val uris = listOf(
            Uri.fromFile(file1),
            Uri.fromFile(file2),
            Uri.fromFile(file3)
        )
        
        // Create and execute batch job
        val jobId = batchTranslateUseCase.createBatchJob(uris)
        assert(jobId > 0)
        
        var completedCount = 0
        var finalProgress: com.smali.translator.domain.model.BatchProgress? = null
        
        batchTranslateUseCase.executeBatch(jobId, uris)
            .collect { progress ->
                finalProgress = progress
                if (progress.currentIndex >= progress.totalFiles) {
                    completedCount = progress.completedCount
                }
            }
        
        // Verify all files were processed
        assert(finalProgress != null)
        assert(completedCount == 3)
        assert(finalProgress?.failedCount == 0)
        
        // Verify all translations are in database
        val allTranslations = database.translationDao().getTranslationCount()
        assert(allTranslations >= 3)
        
        // Cleanup
        file1.delete()
        file2.delete()
        file3.delete()
    }

    @Test
    fun testShareIntentCreation() = runBlocking {
        val uri = Uri.fromFile(testFile)
        val result = translateFileUseCase.execute(uri)
        
        assert(result is TranslateFileUseCase.Result.Success)
        val translation = (result as TranslateFileUseCase.Result.Success).translation
        
        val shareIntent = exportResultUseCase.createShareIntent(translation, true)
        
        assert(shareIntent.action == android.content.Intent.ACTION_CHOOSER)
        assert(shareIntent.hasExtra(android.content.Intent.EXTRA_INTENT))
    }

    @Test
    fun testPersistenceAcrossOperations() = runBlocking {
        // Translate a file
        val uri = Uri.fromFile(testFile)
        val result1 = translateFileUseCase.execute(uri)
        assert(result1 is TranslateFileUseCase.Result.Success)
        val translation1 = (result1 as TranslateFileUseCase.Result.Success).translation
        
        // Clear cache (simulating app restart)
        val cache = TranslationCache()
        cache.clear()
        
        // Try to translate same file again
        val result2 = translateFileUseCase.execute(uri)
        assert(result2 is TranslateFileUseCase.Result.Success)
        val translation2 = (result2 as TranslateFileUseCase.Result.Success).translation
        
        // Should retrieve from database
        assert(translation1.fileHash == translation2.fileHash)
        assert(translation1.javaContent == translation2.javaContent)
    }
}
