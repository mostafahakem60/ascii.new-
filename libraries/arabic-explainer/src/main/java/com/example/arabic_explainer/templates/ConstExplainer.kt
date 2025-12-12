package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.linguistics.ArabicFormat
import com.example.arabic_explainer.linguistics.ArabicTerms
import com.example.arabic_explainer.linguistics.Bidi

internal class ConstExplainer : InstructionExplainer {

    override fun supports(opcode: String): Boolean = opcode.startsWith("const")

    override fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val dest = instruction.operands.getOrNull(0)
            ?: return malformed(instruction, "مفقود مسجل الوجهة")
        val value = instruction.operands.getOrNull(1)
            ?: return malformed(instruction, "مفقود القيمة")

        val renderedDest = ArabicFormat.register(dest, options)
        val renderedValue = Bidi.ltr(value)

        val isString = instruction.normalizedOpcode.startsWith("const-string")
        val valueTerm = ArabicTerms.value(options)
        val registerTerm = ArabicTerms.register(options)

        val noun = if (isString) "النص" else valueTerm

        return TemplateResult.Success(
            "تضع $noun $renderedValue في $registerTerm $renderedDest."
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
