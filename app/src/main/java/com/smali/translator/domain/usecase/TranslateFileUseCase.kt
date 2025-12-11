package com.smali.translator.domain.usecase

import android.net.Uri
import com.smali.translator.data.repository.TranslationRepository
import com.smali.translator.domain.model.FileValidationResult
import com.smali.translator.domain.model.TranslationResult
import com.smali.translator.util.FileIOHelper
import com.smali.translator.util.FileValidator
import com.smali.translator.util.HashUtils

class TranslateFileUseCase(
    private val fileIOHelper: FileIOHelper,
    private val fileValidator: FileValidator,
    private val translator: SmaliTranslator,
    private val repository: TranslationRepository
) {
    sealed class Result {
        data class Success(val translation: TranslationResult) : Result()
        data class ValidationError(val error: com.smali.translator.domain.model.ValidationError) : Result()
        data class IOError(val message: String) : Result()
        data class TranslationError(val message: String) : Result()
    }

    suspend fun execute(uri: Uri): Result {
        // Read file content
        val fileName = fileIOHelper.getFileName(uri) ?: "unknown.smali"
        val contentResult = fileIOHelper.readFileContent(uri)
        
        if (contentResult.isFailure) {
            return Result.IOError(contentResult.exceptionOrNull()?.message ?: "Failed to read file")
        }
        
        val content = contentResult.getOrNull() ?: return Result.IOError("Empty file content")
        
        // Calculate file hash
        val fileHash = HashUtils.calculateFileHash(content)
        
        // Check cache/database first
        repository.getTranslationByHash(fileHash)?.let {
            return Result.Success(it)
        }
        
        // Validate file
        val validationResult = fileValidator.validate(content)
        if (validationResult is FileValidationResult.Invalid) {
            return Result.ValidationError(validationResult.error)
        }
        
        // Translate
        return try {
            val javaContent = translator.translateToJava(content)
            val explanation = translator.generateExplanation(content, javaContent)
            
            val translation = TranslationResult(
                fileName = fileName,
                smaliContent = content,
                javaContent = javaContent,
                explanation = explanation,
                fileHash = fileHash
            )
            
            // Save to database
            val id = repository.saveTranslation(translation)
            
            Result.Success(translation.copy(id = id))
        } catch (e: Exception) {
            Result.TranslationError(e.message ?: "Translation failed")
        }
    }
}
