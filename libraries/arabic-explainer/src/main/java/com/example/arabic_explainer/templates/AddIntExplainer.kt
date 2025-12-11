package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.linguistics.ArabicFormat
import com.example.arabic_explainer.linguistics.ArabicTerms

internal class AddIntExplainer : InstructionExplainer {

    override fun supports(opcode: String): Boolean = opcode.startsWith("add-int")

    override fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val op = instruction.normalizedOpcode
        val registerTerm = ArabicTerms.register(options)

        return if (op.contains("/2addr")) {
            val dest = instruction.operands.getOrNull(0)
                ?: return malformed(instruction, "مفقود مسجل الوجهة")
            val src = instruction.operands.getOrNull(1)
                ?: return malformed(instruction, "مفقود مسجل الإضافة")

            val renderedDest = ArabicFormat.register(dest, options)
            val renderedSrc = ArabicFormat.register(src, options)

            TemplateResult.Success(
                "يجمع قيمة $registerTerm $renderedDest مع قيمة $registerTerm $renderedSrc ثم يُحدّث $registerTerm $renderedDest بالناتج."
            )
        } else {
            val dest = instruction.operands.getOrNull(0)
                ?: return malformed(instruction, "مفقود مسجل الوجهة")
            val left = instruction.operands.getOrNull(1)
                ?: return malformed(instruction, "مفقود المعامل الأول")
            val right = instruction.operands.getOrNull(2)
                ?: return malformed(instruction, "مفقود المعامل الثاني")

            val renderedDest = ArabicFormat.register(dest, options)
            val renderedLeft = ArabicFormat.register(left, options)
            val renderedRight = ArabicFormat.register(right, options)

            TemplateResult.Success(
                "يجمع قيمتي $registerTerm $renderedLeft و$registerTerm $renderedRight ثم يخزن الناتج في $registerTerm $renderedDest."
            )
        }
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
