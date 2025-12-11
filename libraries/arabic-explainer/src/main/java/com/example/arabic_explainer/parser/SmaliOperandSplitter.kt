package com.example.arabic_explainer.parser

internal object SmaliOperandSplitter {
    fun split(operandsText: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()

        var braceDepth = 0
        var inQuotes = false
        var isEscaped = false

        fun flush() {
            val token = current.toString().trim()
            if (token.isNotEmpty()) result += token
            current.setLength(0)
        }

        for (ch in operandsText) {
            if (inQuotes) {
                current.append(ch)
                if (isEscaped) {
                    isEscaped = false
                    continue
                }
                when (ch) {
                    '\\' -> isEscaped = true
                    '"' -> inQuotes = false
                }
                continue
            }

            when (ch) {
                '"' -> {
                    inQuotes = true
                    current.append(ch)
                }

                '{' -> {
                    braceDepth++
                    current.append(ch)
                }

                '}' -> {
                    if (braceDepth > 0) braceDepth--
                    current.append(ch)
                }

                ',' -> {
                    if (braceDepth == 0) {
                        flush()
                    } else {
                        current.append(ch)
                    }
                }

                else -> current.append(ch)
            }
        }

        flush()
        return result
    }
}
