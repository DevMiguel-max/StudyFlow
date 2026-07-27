package com.example.domain.ai

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.aiMetadataDataStore by preferencesDataStore(name = "ai_metadata")

class AIMetadataManager(private val context: Context) {
    companion object {
        val LAST_CLEANUP_TIME = longPreferencesKey("last_cleanup_time")
        val CACHE_ENABLED = booleanPreferencesKey("cache_enabled")
    }

    val lastCleanupTime: Flow<Long> = context.aiMetadataDataStore.data.map { it[LAST_CLEANUP_TIME] ?: 0L }
    val cacheEnabled: Flow<Boolean> = context.aiMetadataDataStore.data.map { it[CACHE_ENABLED] ?: true }

    suspend fun setLastCleanupTime(time: Long) {
        context.aiMetadataDataStore.edit { it[LAST_CLEANUP_TIME] = time }
    }

    suspend fun setCacheEnabled(enabled: Boolean) {
        context.aiMetadataDataStore.edit { it[CACHE_ENABLED] = enabled }
    }
}
