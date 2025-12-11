package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.ir.SmaliInstructionIr

internal class InstructionExplainerRegistry(
    private val explainers: List<InstructionExplainer>
) {

    fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val opcode = instruction.normalizedOpcode
        val explainer = explainers.firstOrNull { it.supports(opcode) }
        return (explainer ?: DefaultExplainer()).explain(instruction, context, options)
    }

    companion object {
        fun default(): InstructionExplainerRegistry = InstructionExplainerRegistry(
            explainers = listOf(
                LabelExplainer(),
                ConstExplainer(),
                MoveExplainer(),
                AddIntExplainer(),
                NewInstanceExplainer(),
                InvokeExplainer(),
                IfExplainer(),
                GotoExplainer(),
                ReturnExplainer()
            )
        )
    }
}
