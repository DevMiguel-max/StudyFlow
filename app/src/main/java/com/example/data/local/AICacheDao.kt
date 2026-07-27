package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AICacheDao {
    @Query("SELECT * FROM ai_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun getCache(key: String): AICacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: AICacheEntity)

    @Query("DELETE FROM ai_cache WHERE cacheKey = :key")
    suspend fun deleteCache(key: String)

    @Query("DELETE FROM ai_cache WHERE timestamp < :expirationTime")
    suspend fun deleteExpiredCache(expirationTime: Long)

    @Query("DELETE FROM ai_cache")
    suspend fun clearAll()
}
