package com.example.arabic_explainer.ir

data class SmaliInstructionIr(
    val opcode: String,
    val operands: List<String>,
    val raw: String,
    val lineNumber: Int? = null
) {
    val normalizedOpcode: String = opcode.trim().lowercase()
}
