package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.linguistics.ArabicFormat
import com.example.arabic_explainer.linguistics.ArabicTerms

internal class MoveExplainer : InstructionExplainer {

    override fun supports(opcode: String): Boolean = opcode.startsWith("move")

    override fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val op = instruction.normalizedOpcode
        val registerTerm = ArabicTerms.register(options)

        if (op == "move-result" || op == "move-result-object" || op == "move-result-wide") {
            val dest = instruction.operands.getOrNull(0)
                ?: return malformed(instruction, "مفقود مسجل الوجهة")
            val renderedDest = ArabicFormat.register(dest, options)
            return TemplateResult.Success("ينقل نتيجة آخر استدعاء إلى $registerTerm $renderedDest.")
        }

        val dest = instruction.operands.getOrNull(0)
            ?: return malformed(instruction, "مفقود مسجل الوجهة")
        val src = instruction.operands.getOrNull(1)
            ?: return malformed(instruction, "مفقود مسجل المصدر")

        val renderedDest = ArabicFormat.register(dest, options)
        val renderedSrc = ArabicFormat.register(src, options)

        return TemplateResult.Success(
            "ينقل قيمة $registerTerm $renderedSrc إلى $registerTerm $renderedDest."
        )
    }

    private fun malformed(instruction: SmaliInstructionIr, details: String): TemplateResult.Error =
        TemplateResult.Error(
            ArabicExplainerError.MalformedInstruction(
                lineNumber = instruction.lineNumber,
                opcode = instruction.opcode,
                rawLine = instruction.raw,
                details = details
            )
        )
}
