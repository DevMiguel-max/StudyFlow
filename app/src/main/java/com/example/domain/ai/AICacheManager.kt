package com.example.domain.ai

import java.util.concurrent.ConcurrentHashMap

data class CacheEntry(
    val data: String,
    val timestamp: Long = System.currentTimeMillis()
)

object AICacheManager {
    private const val CACHE_EXPIRATION_MS = 1000 * 60 * 60 * 24 // 24 hours
    
    private val memoryCache = ConcurrentHashMap<String, CacheEntry>()
    
    fun get(key: String): String? {
        val entry = memoryCache[key] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > CACHE_EXPIRATION_MS) {
            memoryCache.remove(key)
            return null
        }
        return entry.data
    }
    
    fun put(key: String, data: String) {
        memoryCache[key] = CacheEntry(data)
    }
    
    fun clear() {
        memoryCache.clear()
    }
}
