package com.smali.translator.domain.model

data class TranslationResult(
    val id: Long = 0,
    val fileName: String,
    val smaliContent: String,
    val javaContent: String,
    val explanation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fileHash: String
)
