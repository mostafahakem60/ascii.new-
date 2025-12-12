package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.linguistics.ArabicFormat
import com.example.arabic_explainer.linguistics.ArabicTerms

internal class GotoExplainer : InstructionExplainer {

    override fun supports(opcode: String): Boolean = opcode.startsWith("goto")

    override fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val target = instruction.operands.getOrNull(0)
            ?: return malformed(instruction, "مفقود هدف القفز")

        val renderedLabel = ArabicFormat.label(target)
        val index = context.labelToInstructionIndex[target]

        val targetText = if (index != null) {
            val step = ArabicFormat.arabicStepIndex(index + 1, options)
            "$renderedLabel (الخطوة $step)"
        } else {
            renderedLabel
        }

        val goToWord = ArabicTerms.goTo(options)
        return TemplateResult.Success("$goToWord بدون شرط إلى $targetText.")
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
