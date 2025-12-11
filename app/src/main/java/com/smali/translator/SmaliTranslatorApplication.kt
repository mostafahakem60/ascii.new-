package com.smali.translator

import android.app.Application
import com.smali.translator.data.cache.TranslationCache
import com.smali.translator.data.database.AppDatabase
import com.smali.translator.data.repository.BatchJobRepository
import com.smali.translator.data.repository.TranslationRepository
import com.smali.translator.domain.usecase.BatchTranslateUseCase
import com.smali.translator.domain.usecase.ExportResultUseCase
import com.smali.translator.domain.usecase.SmaliTranslator
import com.smali.translator.domain.usecase.TranslateFileUseCase
import com.smali.translator.util.FileIOHelper
import com.smali.translator.util.FileValidator

class SmaliTranslatorApplication : Application() {
    
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val translationCache by lazy { TranslationCache() }
    
    val translationRepository by lazy {
        TranslationRepository(database.translationDao(), translationCache)
    }
    
    val batchJobRepository by lazy {
        BatchJobRepository(database.batchJobDao())
    }
    
    val fileIOHelper by lazy { FileIOHelper(this) }
    val fileValidator by lazy { FileValidator() }
    val smaliTranslator by lazy { SmaliTranslator() }
    
    val translateFileUseCase by lazy {
        TranslateFileUseCase(
            fileIOHelper,
            fileValidator,
            smaliTranslator,
            translationRepository
        )
    }
    
    val batchTranslateUseCase by lazy {
        BatchTranslateUseCase(
            fileIOHelper,
            fileValidator,
            smaliTranslator,
            translationRepository,
            batchJobRepository
        )
    }
    
    val exportResultUseCase by lazy {
        ExportResultUseCase(fileIOHelper)
    }
}
