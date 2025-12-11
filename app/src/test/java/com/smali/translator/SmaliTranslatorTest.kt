package com.smali.translator

import com.smali.translator.domain.usecase.SmaliTranslator
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SmaliTranslatorTest {

    private lateinit var translator: SmaliTranslator

    @Before
    fun setup() {
        translator = SmaliTranslator()
    }

    @Test
    fun `test basic class translation`() = runBlocking {
        val smaliContent = """
            .class public LTestClass;
            .super Ljava/lang/Object;
        """.trimIndent()

        val javaContent = translator.translateToJava(smaliContent)

        assert(javaContent.contains("class TestClass"))
        assert(javaContent.contains("public"))
    }

    @Test
    fun `test class with package translation`() = runBlocking {
        val smaliContent = """
            .class public Lcom/example/TestClass;
            .super Ljava/lang/Object;
        """.trimIndent()

        val javaContent = translator.translateToJava(smaliContent)

        assert(javaContent.contains("package com.example"))
        assert(javaContent.contains("class TestClass"))
    }

    @Test
    fun `test class with methods translation`() = runBlocking {
        val smaliContent = """
            .class public LTestClass;
            .super Ljava/lang/Object;
            
            .method public test()V
                return-void
            .end method
        """.trimIndent()

        val javaContent = translator.translateToJava(smaliContent)

        assert(javaContent.contains("void test()"))
    }

    @Test
    fun `test class with fields translation`() = runBlocking {
        val smaliContent = """
            .class public LTestClass;
            .super Ljava/lang/Object;
            
            .field public value:I
        """.trimIndent()

        val javaContent = translator.translateToJava(smaliContent)

        assert(javaContent.contains("int value"))
    }

    @Test
    fun `test explanation generation`() = runBlocking {
        val smaliContent = """
            .class public LTestClass;
            .super Ljava/lang/Object;
            
            .field public value:I
            
            .method public getValue()I
                return-void
            .end method
        """.trimIndent()

        val javaContent = translator.translateToJava(smaliContent)
        val explanation = translator.generateExplanation(smaliContent, javaContent)

        assert(explanation.contains("Explanation"))
        assert(explanation.contains("Fields: 1"))
        assert(explanation.contains("Methods: 1"))
    }

    @Test
    fun `test complex method signature translation`() = runBlocking {
        val smaliContent = """
            .class public LTestClass;
            .super Ljava/lang/Object;
            
            .method public test(ILjava/lang/String;)Z
                return-void
            .end method
        """.trimIndent()

        val javaContent = translator.translateToJava(smaliContent)

        assert(javaContent.contains("boolean test"))
    }
}
