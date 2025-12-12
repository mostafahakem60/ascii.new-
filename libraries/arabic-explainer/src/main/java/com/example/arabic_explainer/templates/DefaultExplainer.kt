package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.linguistics.ArabicFormat
import com.example.arabic_explainer.linguistics.ArabicTerms

internal class DefaultExplainer : InstructionExplainer {

    override fun supports(opcode: String): Boolean = true

    override fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val instructionTerm = ArabicTerms.instruction(options)
        val renderedOpcode = ArabicFormat.opcode(instruction.opcode)

        val operandsText = if (instruction.operands.isEmpty()) {
            "بدون معاملات"
        } else {
            instruction.operands.joinToString(separator = "، ") { ArabicFormat.opcode(it) }
        }

        return TemplateResult.Success(
            "ينفّذ $instructionTerm $renderedOpcode مع $operandsText."
        )
    }
}
