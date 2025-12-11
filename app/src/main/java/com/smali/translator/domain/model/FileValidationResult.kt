package com.smali.translator.domain.model

sealed class FileValidationResult {
    data class Valid(val content: String) : FileValidationResult()
    data class Invalid(val error: ValidationError) : FileValidationResult()
}

enum class ValidationError(val message: String) {
    EMPTY_FILE("File is empty"),
    NOT_SMALI_FORMAT("File does not appear to be in Smali format"),
    MALFORMED_SYNTAX("File contains malformed Smali syntax"),
    FILE_TOO_LARGE("File size exceeds maximum allowed size"),
    INVALID_ENCODING("File encoding is not supported"),
    MISSING_CLASS_DEFINITION("File is missing class definition")
}
