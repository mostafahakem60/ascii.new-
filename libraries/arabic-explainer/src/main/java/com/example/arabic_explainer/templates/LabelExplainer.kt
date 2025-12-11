package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.linguistics.ArabicFormat

internal class LabelExplainer : InstructionExplainer {

    override fun supports(opcode: String): Boolean = opcode == "label"

    override fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val label = instruction.operands.firstOrNull()
            ?: return TemplateResult.Error(
                ArabicExplainerError.MalformedInstruction(
                    lineNumber = instruction.lineNumber,
                    opcode = instruction.opcode,
                    rawLine = instruction.raw,
                    details = "تعليمة label بدون اسم علامة"
                )
            )

        val renderedLabel = ArabicFormat.label(label)
        return TemplateResult.Success(
            "العلامة $renderedLabel تُعرّف موضعًا يمكن أن تقفز إليه تعليمات التحكم بالتدفق."
        )
    }
}
