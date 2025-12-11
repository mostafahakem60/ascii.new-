package com.smali.translator.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translations ORDER BY timestamp DESC")
    fun getAllTranslations(): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translations WHERE id = :id")
    suspend fun getTranslationById(id: Long): TranslationEntity?

    @Query("SELECT * FROM translations WHERE fileHash = :fileHash")
    suspend fun getTranslationByHash(fileHash: String): TranslationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: TranslationEntity): Long

    @Update
    suspend fun updateTranslation(translation: TranslationEntity)

    @Delete
    suspend fun deleteTranslation(translation: TranslationEntity)

    @Query("DELETE FROM translations")
    suspend fun deleteAllTranslations()

    @Query("SELECT COUNT(*) FROM translations")
    suspend fun getTranslationCount(): Int
}
