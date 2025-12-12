package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.linguistics.ArabicFormat
import com.example.arabic_explainer.linguistics.ArabicTerms
import com.example.arabic_explainer.linguistics.Bidi

internal class NewInstanceExplainer : InstructionExplainer {

    override fun supports(opcode: String): Boolean = opcode == "new-instance"

    override fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val dest = instruction.operands.getOrNull(0)
            ?: return malformed(instruction, "مفقود مسجل الوجهة")
        val type = instruction.operands.getOrNull(1)
            ?: return malformed(instruction, "مفقود نوع الكائن")

        val registerTerm = ArabicTerms.register(options)
        val renderedDest = ArabicFormat.register(dest, options)
        val renderedType = Bidi.ltr(type)

        return TemplateResult.Success(
            "ينشئ كائنًا جديدًا من النوع $renderedType ويضع مرجعه في $registerTerm $renderedDest."
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
