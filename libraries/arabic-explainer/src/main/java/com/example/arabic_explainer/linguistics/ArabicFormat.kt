package com.example.arabic_explainer.linguistics

import com.example.arabic_explainer.api.ArabicExplainerOptions

internal object ArabicFormat {
    fun register(reg: String, options: ArabicExplainerOptions): String {
        val token = reg.trim()
        return Bidi.ltr(token)
    }

    fun label(label: String): String = Bidi.ltr(label.trim())

    fun opcode(op: String): String = Bidi.ltr(op.trim())

    fun methodRef(ref: String): String = Bidi.ltr(ref.trim())

    fun arabicStepIndex(index1Based: Int, options: ArabicExplainerOptions): String {
        if (!options.useArabicIndicDigits) return index1Based.toString()
        return index1Based.toString().map { ch ->
            when (ch) {
                '0' -> '٠'
                '1' -> '١'
                '2' -> '٢'
                '3' -> '٣'
                '4' -> '٤'
                '5' -> '٥'
                '6' -> '٦'
                '7' -> '٧'
                '8' -> '٨'
                '9' -> '٩'
                else -> ch
            }
        }.joinToString(separator = "")
    }
}
