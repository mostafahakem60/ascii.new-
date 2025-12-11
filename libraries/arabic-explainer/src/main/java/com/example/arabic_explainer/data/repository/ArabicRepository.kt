package com.example.arabic_explainer.data.repository

import com.example.arabic_explainer.data.model.ArabicWord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ArabicRepository {
    fun getArabicWords(): Flow<List<ArabicWord>>
    suspend fun explainWord(word: String): ArabicWord?
}

class ArabicRepositoryImpl : ArabicRepository {
    override fun getArabicWords(): Flow<List<ArabicWord>> = flow {
        emit(
            listOf(
                ArabicWord(
                    word = "السلام",
                    translation = "Peace",
                    pronunciation = "assalaam",
                    definition = "A greeting meaning peace"
                ),
                ArabicWord(
                    word = "شكراً",
                    translation = "Thank you",
                    pronunciation = "shukran",
                    definition = "Expression of gratitude"
                )
            )
        )
    }

    override suspend fun explainWord(word: String): ArabicWord? {
        return ArabicWord(
            word = word,
            translation = "Translation of $word",
            pronunciation = "Pronunciation of $word",
            definition = "Definition of $word"
        )
    }
}
