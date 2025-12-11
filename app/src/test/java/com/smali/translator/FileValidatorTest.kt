package com.smali.translator

import com.smali.translator.domain.model.FileValidationResult
import com.smali.translator.domain.model.ValidationError
import com.smali.translator.util.FileValidator
import org.junit.Before
import org.junit.Test

class FileValidatorTest {

    private lateinit var validator: FileValidator

    @Before
    fun setup() {
        validator = FileValidator()
    }

    @Test
    fun `test valid smali file`() {
        val content = """
            .class public LTestClass;
            .super Ljava/lang/Object;
            
            .method public test()V
                return-void
            .end method
        """.trimIndent()

        val result = validator.validate(content)
        assert(result is FileValidationResult.Valid)
    }

    @Test
    fun `test empty file`() {
        val result = validator.validate("")
        assert(result is FileValidationResult.Invalid)
        assert((result as FileValidationResult.Invalid).error == ValidationError.EMPTY_FILE)
    }

    @Test
    fun `test non-smali file`() {
        val content = "This is just plain text"
        val result = validator.validate(content)
        assert(result is FileValidationResult.Invalid)
        assert((result as FileValidationResult.Invalid).error == ValidationError.NOT_SMALI_FORMAT)
    }

    @Test
    fun `test missing class definition`() {
        val content = """
            .method public test()V
                return-void
            .end method
        """.trimIndent()

        val result = validator.validate(content)
        assert(result is FileValidationResult.Invalid)
        assert((result as FileValidationResult.Invalid).error == ValidationError.MISSING_CLASS_DEFINITION)
    }

    @Test
    fun `test malformed syntax with unmatched method`() {
        val content = """
            .class public LTestClass;
            .super Ljava/lang/Object;
            
            .method public test()V
                return-void
        """.trimIndent()

        val result = validator.validate(content)
        assert(result is FileValidationResult.Invalid)
        assert((result as FileValidationResult.Invalid).error == ValidationError.MALFORMED_SYNTAX)
    }

    @Test
    fun `test file with super directive`() {
        val content = """
            .super Ljava/lang/Object;
            .class public LTestClass;
        """.trimIndent()

        val result = validator.validate(content)
        assert(result is FileValidationResult.Valid)
    }

    @Test
    fun `test batch validation`() {
        val validContent = """
            .class public LTest;
            .super Ljava/lang/Object;
        """.trimIndent()

        val invalidContent = "Not Smali"

        val results = validator.validateBatch(listOf(validContent, invalidContent))
        
        assert(results.size == 2)
        assert(results[0].second is FileValidationResult.Valid)
        assert(results[1].second is FileValidationResult.Invalid)
    }
}
