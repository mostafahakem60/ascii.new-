package com.smali.translator.domain.usecase

import android.content.Intent
import android.net.Uri
import com.smali.translator.domain.model.TranslationResult
import com.smali.translator.util.FileIOHelper

class ExportResultUseCase(
    private val fileIOHelper: FileIOHelper
) {
    sealed class ExportResult {
        data class Success(val fileName: String) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    suspend fun exportToFile(uri: Uri, translation: TranslationResult, includeExplanation: Boolean): ExportResult {
        val content = if (includeExplanation) {
            buildString {
                appendLine("/*")
                appendLine(translation.explanation)
                appendLine("*/")
                appendLine()
                append(translation.javaContent)
            }
        } else {
            translation.javaContent
        }

        val javaFileName = translation.fileName.replace(".smali", ".java")
        
        return fileIOHelper.exportToUri(uri, content, javaFileName)
            .fold(
                onSuccess = { ExportResult.Success(it) },
                onFailure = { ExportResult.Error(it.message ?: "Export failed") }
            )
    }

    fun createShareIntent(translation: TranslationResult, includeExplanation: Boolean): Intent {
        val content = if (includeExplanation) {
            buildString {
                appendLine("Translation Explanation:")
                appendLine(translation.explanation)
                appendLine()
                appendLine("Java Code:")
                append(translation.javaContent)
            }
        } else {
            translation.javaContent
        }

        val javaFileName = translation.fileName.replace(".smali", ".java")
        return fileIOHelper.createShareIntent(javaFileName, content)
    }

    suspend fun exportBatchResults(
        translations: List<TranslationResult>,
        createFileCallback: suspend (String) -> Uri?
    ): Map<String, ExportResult> {
        return translations.associate { translation ->
            val javaFileName = translation.fileName.replace(".smali", ".java")
            val uri = createFileCallback(javaFileName)
            
            if (uri != null) {
                javaFileName to exportToFile(uri, translation, includeExplanation = false)
            } else {
                javaFileName to ExportResult.Error("Failed to create file")
            }
        }
    }
}
