package com.example.arabic_explainer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ArabicWord(
    val word: String,
    val translation: String,
    val pronunciation: String,
    val definition: String
)
