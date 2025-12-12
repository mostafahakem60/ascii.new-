package com.example.arabic_explainer.api

data class ArabicExplainerOptions(
    val enableDiacritics: Boolean = false,
    val wrapRtl: Boolean = true,
    val numberSteps: Boolean = true,
    val useArabicIndicDigits: Boolean = true
)
