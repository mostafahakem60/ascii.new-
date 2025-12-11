package com.smali.translator.util

import com.smali.translator.domain.model.FileValidationResult
import com.smali.translator.domain.model.ValidationError

class FileValidator {
    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10 MB
        private val SMALI_CLASS_PATTERN = Regex("^\\.class\\s+", RegexOption.MULTILINE)
        private val SMALI_METHOD_PATTERN = Regex("^\\.method\\s+", RegexOption.MULTILINE)
        private val SMALI_FIELD_PATTERN = Regex("^\\.field\\s+", RegexOption.MULTILINE)
        private val SMALI_SUPER_PATTERN = Regex("^\\.super\\s+", RegexOption.MULTILINE)
    }

    fun validate(content: String): FileValidationResult {
        // Check if file is empty
        if (content.isBlank()) {
            return FileValidationResult.Invalid(ValidationError.EMPTY_FILE)
        }

        // Check file size
        if (content.length > MAX_FILE_SIZE) {
            return FileValidationResult.Invalid(ValidationError.FILE_TOO_LARGE)
        }

        // Check if file appears to be in Smali format
        if (!isSmaliFormat(content)) {
            return FileValidationResult.Invalid(ValidationError.NOT_SMALI_FORMAT)
        }

        // Check for class definition
        if (!hasClassDefinition(content)) {
            return FileValidationResult.Invalid(ValidationError.MISSING_CLASS_DEFINITION)
        }

        // Check for malformed syntax
        if (hasMalformedSyntax(content)) {
            return FileValidationResult.Invalid(ValidationError.MALFORMED_SYNTAX)
        }

        return FileValidationResult.Valid(content)
    }

    private fun isSmaliFormat(content: String): Boolean {
        return SMALI_CLASS_PATTERN.containsMatchIn(content) ||
                SMALI_SUPER_PATTERN.containsMatchIn(content)
    }

    private fun hasClassDefinition(content: String): Boolean {
        return SMALI_CLASS_PATTERN.containsMatchIn(content)
    }

    private fun hasMalformedSyntax(content: String): Boolean {
        val lines = content.lines()
        
        // Check for basic syntax issues
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue
            }

            // Check for unmatched directives
            if (trimmed.startsWith(".method") && !content.contains(".end method")) {
                return true
            }
            if (trimmed.startsWith(".field") && trimmed.endsWith("\\")) {
                return true
            }
        }

        // Check for balanced .method/.end method pairs
        val methodCount = SMALI_METHOD_PATTERN.findAll(content).count()
        val endMethodCount = Regex("^\\.end method", RegexOption.MULTILINE).findAll(content).count()
        
        if (methodCount != endMethodCount) {
            return true
        }

        return false
    }

    fun validateBatch(contents: List<String>): List<Pair<Int, FileValidationResult>> {
        return contents.mapIndexed { index, content ->
            index to validate(content)
        }
    }
}
