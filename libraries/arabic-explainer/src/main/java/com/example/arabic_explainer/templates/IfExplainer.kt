package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.linguistics.ArabicFormat
import com.example.arabic_explainer.linguistics.ArabicTerms

internal class IfExplainer : InstructionExplainer {

    override fun supports(opcode: String): Boolean = opcode.startsWith("if-")

    override fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val operands = instruction.operands
        val target = operands.lastOrNull()
            ?: return malformed(instruction, "مفقود هدف القفز")

        val regs = operands.dropLast(1)
        if (regs.isEmpty()) return malformed(instruction, "مفقود المسجل/المسجلات الخاصة بالشرط")

        val conditionText = buildConditionText(instruction.normalizedOpcode, regs, options)
            ?: return malformed(instruction, "صيغة شرط غير مدعومة")

        val targetRendered = buildTargetText(target, context, options)
        val ifWord = ArabicTerms.conditionalIf(options)
        val goToWord = ArabicTerms.goTo(options)

        return TemplateResult.Success(
            "$ifWord $conditionText فـ$goToWord إلى $targetRendered."
        )
    }

    private fun buildTargetText(label: String, context: ExplanationContext, options: ArabicExplainerOptions): String {
        val renderedLabel = ArabicFormat.label(label)
        val index = context.labelToInstructionIndex[label]
        return if (index != null) {
            val step = ArabicFormat.arabicStepIndex(index + 1, options)
            "$renderedLabel (الخطوة $step)"
        } else {
            renderedLabel
        }
    }

    private fun buildConditionText(
        opcode: String,
        regs: List<String>,
        options: ArabicExplainerOptions
    ): String? {
        val regTerm = ArabicTerms.register(options)

        val isZ = opcode.endsWith("z")
        return if (isZ) {
            val reg = regs.getOrNull(0) ?: return null
            val renderedReg = ArabicFormat.register(reg, options)

            val predicate = when (opcode) {
                "if-eqz" -> "تساوي قيمة $regTerm $renderedReg صفرًا"
                "if-nez" -> "لا تساوي قيمة $regTerm $renderedReg صفرًا"
                "if-ltz" -> "تكون قيمة $regTerm $renderedReg أصغر من صفر"
                "if-lez" -> "تكون قيمة $regTerm $renderedReg أصغر أو تساوي صفرًا"
                "if-gtz" -> "تكون قيمة $regTerm $renderedReg أكبر من صفر"
                "if-gez" -> "تكون قيمة $regTerm $renderedReg أكبر أو تساوي صفرًا"
                else -> null
            }
            predicate
        } else {
            val left = regs.getOrNull(0) ?: return null
            val right = regs.getOrNull(1) ?: return null

            val renderedLeft = ArabicFormat.register(left, options)
            val renderedRight = ArabicFormat.register(right, options)

            val comparator = when (opcode) {
                "if-eq" -> "تساوي"
                "if-ne" -> "لا تساوي"
                "if-lt" -> "أصغر من"
                "if-le" -> "أصغر أو تساوي"
                "if-gt" -> "أكبر من"
                "if-ge" -> "أكبر أو تساوي"
                else -> null
            } ?: return null

            "قيمة $regTerm $renderedLeft $comparator قيمة $regTerm $renderedRight"
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
