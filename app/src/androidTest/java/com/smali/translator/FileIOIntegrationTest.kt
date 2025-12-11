package com.smali.translator

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smali.translator.util.FileIOHelper
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FileIOIntegrationTest {

    private lateinit var context: Context
    private lateinit var fileIOHelper: FileIOHelper

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        fileIOHelper = FileIOHelper(context)
    }

    @Test
    fun testReadFileContent() = runBlocking {
        // Create a temporary file
        val testFile = File(context.cacheDir, "test.smali")
        val testContent = """
            .class public LTestClass;
            .super Ljava/lang/Object;
            
            .method public test()V
                return-void
            .end method
        """.trimIndent()
        
        testFile.writeText(testContent)
        val uri = Uri.fromFile(testFile)
        
        val result = fileIOHelper.readFileContent(uri)
        assert(result.isSuccess)
        assert(result.getOrNull()?.contains(".class public LTestClass;") == true)
        
        testFile.delete()
    }

    @Test
    fun testWriteFileContent() = runBlocking {
        val testFile = File(context.cacheDir, "output.java")
        val uri = Uri.fromFile(testFile)
        
        val content = "public class TestClass { }"
        val result = fileIOHelper.writeFileContent(uri, content)
        
        assert(result.isSuccess)
        assert(testFile.exists())
        assert(testFile.readText() == content)
        
        testFile.delete()
    }

    @Test
    fun testFileSizeLimit() = runBlocking {
        val testFile = File(context.cacheDir, "large.smali")
        val largeContent = "a".repeat(FileIOHelper.MAX_FILE_SIZE + 1)
        testFile.writeText(largeContent)
        
        val uri = Uri.fromFile(testFile)
        val result = fileIOHelper.readFileContent(uri)
        
        assert(result.isFailure)
        
        testFile.delete()
    }
}
