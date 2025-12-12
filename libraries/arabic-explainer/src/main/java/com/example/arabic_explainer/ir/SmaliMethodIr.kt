package com.example.arabic_explainer.ir

data class SmaliMethodIr(
    val signature: String? = null,
    val instructions: List<SmaliInstructionIr>
)
