package com.example.arabic_explainer.templates

import com.example.arabic_explainer.ir.SmaliMethodIr

internal data class ExplanationContext(
    val method: SmaliMethodIr? = null,
    val instructionIndex: Int = 0,
    val labelToInstructionIndex: Map<String, Int> = emptyMap()
)
