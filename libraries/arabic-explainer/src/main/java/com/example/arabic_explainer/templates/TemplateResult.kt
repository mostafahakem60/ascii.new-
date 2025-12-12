package com.example.arabic_explainer.templates

import com.example.arabic_explainer.errors.ArabicExplainerError

internal sealed class TemplateResult {
    data class Success(val text: String) : TemplateResult()
    data class Error(val error: ArabicExplainerError) : TemplateResult()
}
