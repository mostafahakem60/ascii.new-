package com.example.arabic_explainer.templates

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.errors.ArabicExplainerError
import com.example.arabic_explainer.ir.SmaliInstructionIr
import com.example.arabic_explainer.linguistics.ArabicFormat
import com.example.arabic_explainer.linguistics.ArabicTerms
import com.example.arabic_explainer.linguistics.Bidi

internal class InvokeExplainer : InstructionExplainer {

    override fun supports(opcode: String): Boolean = opcode.startsWith("invoke-")

    override fun explain(
        instruction: SmaliInstructionIr,
        context: ExplanationContext,
        options: ArabicExplainerOptions
    ): TemplateResult {
        val registersToken = instruction.operands.getOrNull(0)
            ?: return malformed(instruction, "مفقود قائمة المسجلات")
        val methodRef = instruction.operands.getOrNull(1)
            ?: return malformed(instruction, "مفقود مرجع الدالة")

        val registers = parseRegistersList(registersToken)
        val parsedMethodName = parseMethodName(methodRef)
        val renderedMethodName = Bidi.ltr(parsedMethodName ?: methodRef)
        val renderedMethodRef = ArabicFormat.methodRef(methodRef)

        val isStatic = instruction.normalizedOpcode.startsWith("invoke-static")
        val registerTerm = ArabicTerms.register(options)

        val description = if (isStatic) {
            val args = registers.joinToString(separator = "، ") { ArabicFormat.register(it, options) }
            val argsPart = if (registers.isNotEmpty()) "باستخدام الوسائط: $args" else "بدون وسائط"
            "يستدعي دالة ساكنة $renderedMethodName $argsPart. مرجع الدالة: $renderedMethodRef."
        } else {
            val receiver = registers.firstOrNull()
            val args = registers.drop(1)

            val receiverPart = if (receiver != null) {
                "على الكائن $registerTerm ${ArabicFormat.register(receiver, options)}"
            } else {
                "(بدون كائن مستقبِل واضح)"
            }

            val argsPart = if (args.isNotEmpty()) {
                val renderedArgs = args.joinToString(separator = "، ") { ArabicFormat.register(it, options) }
                "باستخدام الوسائط: $renderedArgs"
            } else {
                "بدون وسائط"
            }

            "يستدعي الدالة $renderedMethodName $receiverPart $argsPart. مرجع الدالة: $renderedMethodRef."
        }

        return TemplateResult.Success(description)
    }

    private fun parseRegistersList(token: String): List<String> {
        val trimmed = token.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return listOf(trimmed)

        val inside = trimmed.removePrefix("{").removeSuffix("}").trim()
        if (inside.isBlank()) return emptyList()

        return inside.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun parseMethodName(methodRef: String): String? {
        val arrow = methodRef.indexOf("->")
        if (arrow < 0) return null
        val afterArrow = arrow + 2
        val paren = methodRef.indexOf('(', startIndex = afterArrow)
        if (paren < 0) return null
        return methodRef.substring(afterArrow, paren)
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
