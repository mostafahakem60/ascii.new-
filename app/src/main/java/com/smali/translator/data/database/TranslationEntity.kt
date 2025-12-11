package com.smali.translator.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smali.translator.domain.model.TranslationResult

@Entity(
    tableName = "translations",
    indices = [Index(value = ["fileHash"], unique = true)]
)
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val smaliContent: String,
    val javaContent: String,
    val explanation: String,
    val timestamp: Long,
    val fileHash: String
)

fun TranslationEntity.toDomain(): TranslationResult {
    return TranslationResult(
        id = id,
        fileName = fileName,
        smaliContent = smaliContent,
        javaContent = javaContent,
        explanation = explanation,
        timestamp = timestamp,
        fileHash = fileHash
    )
}

fun TranslationResult.toEntity(): TranslationEntity {
    return TranslationEntity(
        id = id,
        fileName = fileName,
        smaliContent = smaliContent,
        javaContent = javaContent,
        explanation = explanation,
        timestamp = timestamp,
        fileHash = fileHash
    )
}
