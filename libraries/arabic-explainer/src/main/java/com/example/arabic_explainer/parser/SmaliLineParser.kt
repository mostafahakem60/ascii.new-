package com.example.arabic_explainer.parser

import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr

internal object SmaliLineParser {

    fun parseInstruction(line: String, lineNumber: Int): ParseResult<SmaliInstructionIr> {
        val cleaned = line.substringBefore('#').trim()
        if (cleaned.isBlank()) {
            return ParseResult.Error(
                ArabicExplainerError.ParseError(
                    lineNumber = lineNumber,
                    rawLine = line,
                    details = "سطر فارغ أو تعليق فقط"
                )
            )
        }

        if (cleaned.startsWith(":")) {
            return ParseResult.Success(
                SmaliInstructionIr(
                    opcode = "label",
                    operands = listOf(cleaned),
                    raw = cleaned,
                    lineNumber = lineNumber
                )
            )
        }

        val parts = cleaned.split(Regex("\\s+"), limit = 2)
        val opcode = parts.firstOrNull()?.trim().orEmpty()
        if (opcode.isBlank()) {
            return ParseResult.Error(
                ArabicExplainerError.ParseError(
                    lineNumber = lineNumber,
                    rawLine = line,
                    details = "تعذّر استخراج اسم التعليمة (opcode)"
                )
            )
        }

        val operandsText = parts.getOrNull(1)?.trim().orEmpty()
        val operands = if (operandsText.isBlank()) emptyList() else SmaliOperandSplitter.split(operandsText)

        return ParseResult.Success(
            SmaliInstructionIr(
                opcode = opcode,
                operands = operands,
                raw = cleaned,
                lineNumber = lineNumber
            )
        )
    }
}
