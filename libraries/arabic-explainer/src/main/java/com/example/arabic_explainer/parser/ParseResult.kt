package com.example.arabic_explainer.parser

import com.example.arabic_explainer.errors.ArabicExplainerError

internal sealed class ParseResult<out T> {
    data class Success<T>(val value: T) : ParseResult<T>()
    data class Error(val error: ArabicExplainerError.ParseError) : ParseResult<Nothing>()
}
