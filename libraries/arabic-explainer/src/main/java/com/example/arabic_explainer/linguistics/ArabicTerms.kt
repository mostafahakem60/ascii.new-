package com.example.arabic_explainer.linguistics

import com.example.arabic_explainer.api.ArabicExplainerOptions

internal object ArabicTerms {
    fun register(options: ArabicExplainerOptions): String =
        if (options.enableDiacritics) "المُسَجِّل" else "المسجل"

    fun instruction(options: ArabicExplainerOptions): String =
        if (options.enableDiacritics) "التَّعْليمة" else "التعليمة"

    fun value(options: ArabicExplainerOptions): String =
        if (options.enableDiacritics) "القِيمة" else "القيمة"

    fun goTo(options: ArabicExplainerOptions): String =
        if (options.enableDiacritics) "اِنْتَقِل" else "انتقل"

    fun conditionalIf(options: ArabicExplainerOptions): String =
        if (options.enableDiacritics) "إِذا" else "إذا"
}
