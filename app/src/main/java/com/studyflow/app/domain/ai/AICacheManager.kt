package com.studyflow.app.domain.ai

import com.studyflow.app.data.local.AICacheDao
import com.studyflow.app.data.local.AICacheEntity
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object AICacheManager : KoinComponent {
    private const val CACHE_EXPIRATION_MS = 1000L * 60 * 60 * 24 // 24 hours
    
    private val dao: AICacheDao by inject()
    private val metadataManager: AIMetadataManager by inject()

    suspend fun get(key: String): String? {
        val isEnabled = metadataManager.cacheEnabled.first()
        if (!isEnabled) return null

        val entry = dao.getCache(key) ?: return null
        if (System.currentTimeMillis() - entry.timestamp > CACHE_EXPIRATION_MS) {
            dao.deleteCache(key)
            return null
        }
        return entry.content
    }
    
    suspend fun put(key: String, data: String) {
        val isEnabled = metadataManager.cacheEnabled.first()
        if (!isEnabled) return

        val type = when {
            key.startsWith("material_") -> "material"
            key.startsWith("flashcards_") -> "flashcards"
            key.startsWith("questions_") -> "questions"
            key.startsWith("doc_analysis_") -> "doc_analysis"
            else -> "general"
        }
        dao.insertCache(AICacheEntity(key, data, System.currentTimeMillis(), type))
        dao.deleteExpiredCache(System.currentTimeMillis() - CACHE_EXPIRATION_MS)
    }
    
    suspend fun clear() {
        dao.clearAll()
        metadataManager.setLastCleanupTime(System.currentTimeMillis())
    }
}
