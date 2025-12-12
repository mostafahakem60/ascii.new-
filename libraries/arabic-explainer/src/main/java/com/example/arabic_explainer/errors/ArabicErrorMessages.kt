package com.example.arabic_explainer.errors

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.linguistics.Bidi

internal object ArabicErrorMessages {

    fun message(error: ArabicExplainerError, options: ArabicExplainerOptions): String {
        return when (error) {
            is ArabicExplainerError.ParseError -> {
                val line = Bidi.ltr(error.rawLine.trim())
                "تعذّر تحليل سطر Smali رقم ${error.lineNumber}: ${error.details}. السطر: $line"
            }

            is ArabicExplainerError.MalformedInstruction -> {
                val lineNo = error.lineNumber?.let { "رقم $it" } ?: "(بدون رقم سطر)"
                val raw = Bidi.ltr(error.rawLine.trim())
                val opcode = Bidi.ltr(error.opcode.trim())
                "تعليمة Smali غير مكتملة في السطر $lineNo: $opcode. السبب: ${error.details}. السطر: $raw"
            }
        }
    }
}
