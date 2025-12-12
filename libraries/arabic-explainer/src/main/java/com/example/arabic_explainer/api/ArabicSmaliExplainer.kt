package com.example.arabic_explainer.api

import com.example.arabic_explainer.errors.ArabicErrorMessages
import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.ir.SmaliMethodIr
import com.example.arabic_explainer.linguistics.ArabicFormat
import com.example.arabic_explainer.linguistics.Bidi
import com.example.arabic_explainer.parser.ParseResult
import com.example.arabic_explainer.parser.SmaliLineParser
import com.example.arabic_explainer.parser.SmaliMethodParser
import com.example.arabic_explainer.templates.ExplanationContext
import com.example.arabic_explainer.templates.InstructionExplainerRegistry
import com.example.arabic_explainer.templates.TemplateResult

class ArabicSmaliExplainer(
    private val options: ArabicExplainerOptions = ArabicExplainerOptions(),
    private val registry: InstructionExplainerRegistry = InstructionExplainerRegistry.default()
) {

    fun explainInstruction(instruction: SmaliInstructionIr): ArabicExplanationResult<String> {
        val context = ExplanationContext(instructionIndex = 0)
        return explainInstructionInternal(instruction, context, numberStep = null)
    }

    fun explainInstructionText(smaliLine: String, lineNumber: Int = 1): ArabicExplanationResult<String> {
        return when (val parsed = SmaliLineParser.parseInstruction(smaliLine, lineNumber)) {
            is ParseResult.Success -> explainInstruction(parsed.value)
            is ParseResult.Error -> errorResult(parsed.error)
        }
    }

    fun explainMethod(method: SmaliMethodIr): ArabicExplanationResult<ArabicMethodNarrative> {
        val labelMap = method.instructions
            .withIndex()
            .mapNotNull { indexed ->
                val instr = indexed.value
                if (instr.normalizedOpcode != "label") return@mapNotNull null
                val label = instr.operands.firstOrNull() ?: return@mapNotNull null
                label to indexed.index
            }
            .toMap()

        val steps = method.instructions.mapIndexed { index, instruction ->
            val context = ExplanationContext(
                method = method,
                instructionIndex = index,
                labelToInstructionIndex = labelMap
            )

            when (val explained = registry.explain(instruction, context, options)) {
                is TemplateResult.Success -> wrapStep(explained.text, index)
                is TemplateResult.Error -> wrapStep(
                    ArabicErrorMessages.message(explained.error, options),
                    index
                )
            }
        }

        return ArabicExplanationResult.Success(
            ArabicMethodNarrative(
                methodSignature = method.signature,
                steps = steps
            )
        )
    }

    fun explainMethodText(smaliText: String): ArabicExplanationResult<ArabicMethodNarrative> {
        return when (val parsed = SmaliMethodParser.parse(smaliText)) {
            is ParseResult.Success -> explainMethod(parsed.value)
            is ParseResult.Error -> errorResult(parsed.error)
        }
    }

    private fun explainInstructionInternal(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        numberStep: Int?
    ): ArabicExplanationResult<String> {
        return when (val explained = registry.explain(instruction, context, options)) {
            is TemplateResult.Success -> {
                val text = numberStep?.let { prefixStep(explained.text, it) } ?: explained.text
                ArabicExplanationResult.Success(wrapRtlIfEnabled(text))
            }

            is TemplateResult.Error -> errorResult(explained.error)
        }
    }

    private fun wrapStep(text: String, index0: Int): String {
        val numbered = if (options.numberSteps) prefixStep(text, index0 + 1) else text
        return wrapRtlIfEnabled(numbered)
    }

    private fun prefixStep(text: String, index1: Int): String {
        val number = ArabicFormat.arabicStepIndex(index1, options)
        return "$number) $text"
    }

    private fun wrapRtlIfEnabled(text: String): String = if (options.wrapRtl) Bidi.rtl(text) else text

    private fun errorResult(error: ArabicExplainerError): ArabicExplanationResult.Error {
        val msg = ArabicErrorMessages.message(error, options)
        return ArabicExplanationResult.Error(
            error = error,
            message = wrapRtlIfEnabled(msg)
        )
    }
}
