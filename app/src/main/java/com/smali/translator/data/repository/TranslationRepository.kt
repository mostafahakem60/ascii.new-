package com.smali.translator.data.repository

import com.smali.translator.data.cache.TranslationCache
import com.smali.translator.data.database.TranslationDao
import com.smali.translator.data.database.toDomain
import com.smali.translator.data.database.toEntity
import com.smali.translator.domain.model.TranslationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TranslationRepository(
    private val translationDao: TranslationDao,
    private val cache: TranslationCache
) {
    fun getAllTranslations(): Flow<List<TranslationResult>> {
        return translationDao.getAllTranslations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getTranslationById(id: Long): TranslationResult? {
        return translationDao.getTranslationById(id)?.toDomain()
    }

    suspend fun getTranslationByHash(fileHash: String): TranslationResult? {
        // Check cache first
        cache.get(fileHash)?.let { return it }

        // Check database
        val result = translationDao.getTranslationByHash(fileHash)?.toDomain()
        result?.let { cache.put(fileHash, it) }
        
        return result
    }

    suspend fun saveTranslation(translation: TranslationResult): Long {
        val id = translationDao.insertTranslation(translation.toEntity())
        val savedTranslation = translation.copy(id = id)
        cache.put(translation.fileHash, savedTranslation)
        return id
    }

    suspend fun updateTranslation(translation: TranslationResult) {
        translationDao.updateTranslation(translation.toEntity())
        cache.put(translation.fileHash, translation)
    }

    suspend fun deleteTranslation(translation: TranslationResult) {
        translationDao.deleteTranslation(translation.toEntity())
        cache.invalidate(translation.fileHash)
    }

    suspend fun deleteAllTranslations() {
        translationDao.deleteAllTranslations()
        cache.clear()
    }

    suspend fun getTranslationCount(): Int {
        return translationDao.getTranslationCount()
    }

    fun getCacheSize(): Int {
        return cache.size()
    }

    fun clearCache() {
        cache.clear()
    }
}
