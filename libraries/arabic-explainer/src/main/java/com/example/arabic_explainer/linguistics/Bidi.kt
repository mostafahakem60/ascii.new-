package com.example.arabic_explainer.linguistics

object Bidi {
    const val LRI: Char = '\u2066'
    const val RLI: Char = '\u2067'
    const val PDI: Char = '\u2069'

    fun ltr(text: String): String = "$LRI$text$PDI"

    fun rtl(text: String): String = "$RLI$text$PDI"
}
