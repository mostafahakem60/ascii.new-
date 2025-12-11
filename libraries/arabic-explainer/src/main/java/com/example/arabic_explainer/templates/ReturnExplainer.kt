package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.linguistics.ArabicFormat
import com.example.arabic_explainer.linguistics.ArabicTerms

internal class ReturnExplainer : InstructionExplainer {

    override fun supports(opcode: String): Boolean = opcode.startsWith("return")

    override fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val op = instruction.normalizedOpcode
        val registerTerm = ArabicTerms.register(options)

        if (op == "return-void") {
            return TemplateResult.Success("تنهي الدالة التنفيذ بدون قيمة مُرجعة.")
        }

        val value = instruction.operands.getOrNull(0)
            ?: return malformed(instruction, "مفقود المسجل المُرجَع")

        val renderedValue = ArabicFormat.register(value, options)
        return TemplateResult.Success("تُرجِع قيمة $registerTerm $renderedValue كناتج للدالة.")
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
