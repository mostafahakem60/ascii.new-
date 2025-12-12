package com.example.arabic_explainer.errors

sealed class ArabicExplainerError {
    data class ParseError(
        val lineNumber: Int,
        val rawLine: String,
        val details: String
    ) : ArabicExplainerError()

    data class MalformedInstruction(
        val lineNumber: Int?,
        val opcode: String,
        val rawLine: String,
        val details: String
    ) : ArabicExplainerError()
}
