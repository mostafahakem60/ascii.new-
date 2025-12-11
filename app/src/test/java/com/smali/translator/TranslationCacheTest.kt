package com.smali.translator

import com.smali.translator.data.cache.TranslationCache
import com.smali.translator.domain.model.TranslationResult
import org.junit.Before
import org.junit.Test

class TranslationCacheTest {

    private lateinit var cache: TranslationCache

    @Before
    fun setup() {
        cache = TranslationCache()
    }

    @Test
    fun `test cache put and get`() {
        val translation = TranslationResult(
            fileName = "Test.smali",
            smaliContent = ".class public LTest;",
            javaContent = "public class Test {}",
            explanation = "Test explanation",
            fileHash = "hash123"
        )

        cache.put("hash123", translation)
        val retrieved = cache.get("hash123")

        assert(retrieved != null)
        assert(retrieved?.fileName == "Test.smali")
    }

    @Test
    fun `test cache miss`() {
        val retrieved = cache.get("nonexistent")
        assert(retrieved == null)
    }

    @Test
    fun `test cache invalidation`() {
        val translation = TranslationResult(
            fileName = "Test.smali",
            smaliContent = ".class public LTest;",
            javaContent = "public class Test {}",
            explanation = "Test explanation",
            fileHash = "hash123"
        )

        cache.put("hash123", translation)
        cache.invalidate("hash123")
        
        val retrieved = cache.get("hash123")
        assert(retrieved == null)
    }

    @Test
    fun `test cache clear`() {
        val translation1 = TranslationResult(
            fileName = "Test1.smali",
            smaliContent = ".class public LTest1;",
            javaContent = "public class Test1 {}",
            explanation = "Test explanation 1",
            fileHash = "hash1"
        )

        val translation2 = translation1.copy(
            fileName = "Test2.smali",
            fileHash = "hash2"
        )

        cache.put("hash1", translation1)
        cache.put("hash2", translation2)
        
        assert(cache.size() == 2)
        
        cache.clear()
        
        assert(cache.size() == 0)
        assert(cache.get("hash1") == null)
        assert(cache.get("hash2") == null)
    }

    @Test
    fun `test cache size limit`() {
        // Fill cache beyond max size
        for (i in 0..55) {
            val translation = TranslationResult(
                fileName = "Test$i.smali",
                smaliContent = ".class public LTest$i;",
                javaContent = "public class Test$i {}",
                explanation = "Test explanation $i",
                fileHash = "hash$i"
            )
            cache.put("hash$i", translation)
        }

        // Cache should not exceed max size (50)
        assert(cache.size() <= 50)
    }
}
