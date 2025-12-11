package com.example.arabic_explainer

import com.example.arabic_explainer.api.ArabicExplainerOptions
import com.example.arabic_explainer.api.ArabicExplanationResult
import com.example.arabic_explainer.api.ArabicSmaliExplainer
import com.example.arabic_explainer.linguistics.Bidi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicSmaliExplainerTest {

    @Test
    fun `const explanation is RTL wrapped and preserves LTR register token`() {
        val explainer = ArabicSmaliExplainer(
            options = ArabicExplainerOptions(
                enableDiacritics = false,
                wrapRtl = true
            )
        )

        val result = explainer.explainInstructionText("const/4 v0, 0x1")
        assertTrue(result is ArabicExplanationResult.Success)

        val text = (result as ArabicExplanationResult.Success).value
        assertTrue(text.first() == Bidi.RLI)
        assertTrue(text.last() == Bidi.PDI)
        assertTrue(text.contains("${Bidi.LRI}v0${Bidi.PDI}"))
        assertTrue(text.contains("المسجل"))
    }

    @Test
    fun `diacritics option affects terminology`() {
        val explainer = ArabicSmaliExplainer(
            options = ArabicExplainerOptions(
                enableDiacritics = true,
                wrapRtl = false
            )
        )

        val result = explainer.explainInstructionText("const/4 v0, 0x1")
        assertTrue(result is ArabicExplanationResult.Success)
        val text = (result as ArabicExplanationResult.Success).value

        assertTrue(text.contains("المُسَجِّل"))
        assertTrue(text.any { it == '\u064f' || it == '\u0651' })
    }

    @Test
    fun `control flow explanation references label`() {
        val explainer = ArabicSmaliExplainer()
        val result = explainer.explainInstructionText("if-eq v0, v1, :cond_0")
        assertTrue(result is ArabicExplanationResult.Success)

        val text = (result as ArabicExplanationResult.Success).value
        assertTrue(text.contains("إذا"))
        assertTrue(text.contains("${Bidi.LRI}:cond_0${Bidi.PDI}"))
    }

    @Test
    fun `malformed instruction returns localized error`() {
        val explainer = ArabicSmaliExplainer()
        val result = explainer.explainInstructionText("const/4 v0", lineNumber = 7)
        assertTrue(result is ArabicExplanationResult.Error)

        val errorText = (result as ArabicExplanationResult.Error).message
        assertTrue(errorText.first() == Bidi.RLI)
        assertTrue(errorText.contains("غير مكتملة"))
        assertTrue(errorText.contains("7"))
    }

    @Test
    fun `method narrative explains each instruction and adds step numbers`() {
        val smali = """
            .method public test()V
                .locals 1
                const/4 v0, 0x1
                if-eqz v0, :cond_0
                return-void
            :cond_0
                return-void
            .end method
        """.trimIndent()

        val explainer = ArabicSmaliExplainer(
            options = ArabicExplainerOptions(
                wrapRtl = true,
                numberSteps = true,
                useArabicIndicDigits = true
            )
        )

        val result = explainer.explainMethodText(smali)
        assertTrue(result is ArabicExplanationResult.Success)

        val narrative = (result as ArabicExplanationResult.Success).value
        assertEquals(5, narrative.steps.size)

        val ifStep = narrative.steps[1]
        assertTrue(ifStep.contains("الخطوة ٤"))

        val firstStep = narrative.steps.first()
        assertTrue(firstStep.first() == Bidi.RLI)
        assertTrue(firstStep.contains("١)"))
    }
}
