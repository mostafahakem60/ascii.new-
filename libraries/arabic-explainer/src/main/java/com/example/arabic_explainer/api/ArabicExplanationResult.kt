package com.example.arabic_explainer.api

import com.example.arabic_explainer.errors.ArabicExplainerError

sealed class ArabicExplanationResult<out T> {
    data class Success<T>(val value: T) : ArabicExplanationResult<T>()

    data class Error(
        val error: ArabicExplainerError,
        val message: String
    ) : ArabicExplanationResult<Nothing>()
}
