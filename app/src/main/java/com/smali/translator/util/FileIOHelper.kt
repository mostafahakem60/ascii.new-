package com.smali.translator.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class FileIOHelper(
    private val context: Context,
    private val contentResolver: ContentResolver = context.contentResolver
) {
    companion object {
        const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10 MB
        const val SMALI_MIME_TYPE = "text/plain"
        const val JAVA_MIME_TYPE = "text/x-java"
    }

    suspend fun readFileContent(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileSize = getFileSize(uri)
            if (fileSize > MAX_FILE_SIZE) {
                return@withContext Result.failure(
                    IOException("File size ($fileSize bytes) exceeds maximum allowed size ($MAX_FILE_SIZE bytes)")
                )
            }

            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val content = reader.readText()
                    Result.success(content)
                }
            } ?: Result.failure(IOException("Unable to open file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun writeFileContent(uri: Uri, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
                Result.success(Unit)
            } ?: Result.failure(IOException("Unable to create file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createShareIntent(fileName: String, content: String): Intent {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        return Intent.createChooser(intent, "Share $fileName")
    }

    fun createSaveFileIntent(fileName: String, mimeType: String = JAVA_MIME_TYPE): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
    }

    fun createOpenFileIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = SMALI_MIME_TYPE
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
    }

    fun createOpenMultipleFilesIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = SMALI_MIME_TYPE
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
    }

    fun getFileName(uri: Uri): String? {
        val documentFile = DocumentFile.fromSingleUri(context, uri)
        return documentFile?.name
    }

    private fun getFileSize(uri: Uri): Long {
        val documentFile = DocumentFile.fromSingleUri(context, uri)
        return documentFile?.length() ?: 0L
    }

    suspend fun readMultipleFiles(uris: List<Uri>): Map<Uri, Result<String>> = withContext(Dispatchers.IO) {
        uris.associateWith { uri ->
            readFileContent(uri)
        }
    }

    fun getFileExtension(uri: Uri): String? {
        val fileName = getFileName(uri) ?: return null
        return fileName.substringAfterLast('.', "")
    }

    suspend fun exportToUri(uri: Uri, content: String, fileName: String): Result<String> {
        return writeFileContent(uri, content).map { fileName }
    }
}
