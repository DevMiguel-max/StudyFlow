package com.studyflow.app.domain.ai

interface AICache {
    suspend fun get(key: String): String?
    suspend fun put(key: String, data: String)
    suspend fun clear()
}
