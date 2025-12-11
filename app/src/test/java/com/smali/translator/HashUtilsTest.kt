package com.smali.translator

import com.smali.translator.util.HashUtils
import org.junit.Test

class HashUtilsTest {

    @Test
    fun `test hash generation`() {
        val content = "test content"
        val hash = HashUtils.calculateFileHash(content)
        
        assert(hash.isNotEmpty())
        assert(hash.length == 64) // SHA-256 produces 64 hex characters
    }

    @Test
    fun `test same content produces same hash`() {
        val content = "test content"
        val hash1 = HashUtils.calculateFileHash(content)
        val hash2 = HashUtils.calculateFileHash(content)
        
        assert(hash1 == hash2)
    }

    @Test
    fun `test different content produces different hash`() {
        val content1 = "test content 1"
        val content2 = "test content 2"
        
        val hash1 = HashUtils.calculateFileHash(content1)
        val hash2 = HashUtils.calculateFileHash(content2)
        
        assert(hash1 != hash2)
    }

    @Test
    fun `test empty content hash`() {
        val hash = HashUtils.calculateFileHash("")
        assert(hash.isNotEmpty())
        assert(hash.length == 64)
    }
}
