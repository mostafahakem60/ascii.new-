package com.smali.translator.data.cache

import com.smali.translator.domain.model.TranslationResult
import java.util.concurrent.ConcurrentHashMap

class TranslationCache {
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val maxSize = 50
    private val maxAgeMillis = 3600000L // 1 hour

    data class CacheEntry(
        val result: TranslationResult,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun get(fileHash: String): TranslationResult? {
        cleanExpiredEntries()
        val entry = cache[fileHash] ?: return null
        
        return if (isValid(entry)) {
            entry.result
        } else {
            cache.remove(fileHash)
            null
        }
    }

    fun put(fileHash: String, result: TranslationResult) {
        if (cache.size >= maxSize) {
            evictOldest()
        }
        cache[fileHash] = CacheEntry(result)
    }

    fun invalidate(fileHash: String) {
        cache.remove(fileHash)
    }

    fun clear() {
        cache.clear()
    }

    fun size(): Int = cache.size

    private fun isValid(entry: CacheEntry): Boolean {
        return System.currentTimeMillis() - entry.timestamp < maxAgeMillis
    }

    private fun cleanExpiredEntries() {
        val now = System.currentTimeMillis()
        cache.entries.removeIf { (_, entry) ->
            now - entry.timestamp >= maxAgeMillis
        }
    }

    private fun evictOldest() {
        val oldestKey = cache.entries
            .minByOrNull { it.value.timestamp }
            ?.key
        
        oldestKey?.let { cache.remove(it) }
    }
}
