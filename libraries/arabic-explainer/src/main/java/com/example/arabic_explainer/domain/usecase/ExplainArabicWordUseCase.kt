package com.example.arabic_explainer.domain.usecase

import com.example.arabic_explainer.data.model.ArabicWord
import com.example.arabic_explainer.data.repository.ArabicRepository
import javax.inject.Inject

class ExplainArabicWordUseCase @Inject constructor(
    private val repository: ArabicRepository
) {
    suspend operator fun invoke(word: String): ArabicWord? {
        return repository.explainWord(word)
    }
}
