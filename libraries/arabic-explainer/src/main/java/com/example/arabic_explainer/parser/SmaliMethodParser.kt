package com.example.arabic_explainer.parser

import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliMethodIr

internal object SmaliMethodParser {

    fun parse(smaliText: String): ParseResult<SmaliMethodIr> {
        val instructions = mutableListOf<com.example.arabic_explainer.ir.SmaliInstructionIr>()
        var signature: String? = null

        val lines = smaliText.lines()
        for ((index, rawLine) in lines.withIndex()) {
            val lineNumber = index + 1
            val cleaned = rawLine.substringBefore('#').trim()

            if (cleaned.isBlank()) continue

            if (cleaned.startsWith(".method")) {
                signature = cleaned.removePrefix(".method").trim().ifBlank { null }
                continue
            }

            if (cleaned.startsWith(".end method")) {
                break
            }

            if (cleaned.startsWith(".")) {
                continue
            }

            val parsed = SmaliLineParser.parseInstruction(cleaned, lineNumber)
            when (parsed) {
                is ParseResult.Success -> instructions += parsed.value
                is ParseResult.Error -> return parsed
            }
        }

        if (instructions.isEmpty()) {
            return ParseResult.Error(
                ArabicExplainerError.ParseError(
                    lineNumber = 1,
                    rawLine = smaliText.lines().firstOrNull().orEmpty(),
                    details = "لم يتم العثور على أي تعليمات قابلة للشرح"
                )
            )
        }

        return ParseResult.Success(
            SmaliMethodIr(
                signature = signature,
                instructions = instructions
            )
        )
    }
}
