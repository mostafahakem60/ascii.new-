package com.example.smali_translator.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface SmaliRepository {
    fun getSmaliInstructions(): Flow<List<String>>
    suspend fun translateSmali(code: String): String
}

class SmaliRepositoryImpl : SmaliRepository {
    override fun getSmaliInstructions(): Flow<List<String>> = flow {
        emit(
            listOf(
                "add-int/2addr",
                "const-string",
                "invoke-virtual",
                "return-void"
            )
        )
    }

    override suspend fun translateSmali(code: String): String {
        return "Translated: $code"
    }
}
