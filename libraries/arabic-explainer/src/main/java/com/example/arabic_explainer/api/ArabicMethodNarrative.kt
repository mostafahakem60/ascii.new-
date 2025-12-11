package com.example.arabic_explainer.api

data class ArabicMethodNarrative(
    val methodSignature: String? = null,
    val steps: List<String>
) {
    fun asMultilineText(): String = steps.joinToString(separator = "\n")
}
