package com.studyflow.app.domain.ai

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

object AIHelper {
    suspend inline fun <reified T> withRetryAndTelemetry(
        operationName: String,
        maxRetries: Int = 3,
        estimatedTokens: Int = 500,
        cacheKey: String? = null,
        cache: AICache = AICacheManager,
        crossinline block: suspend () -> T
    ): T {
        if (cacheKey != null) {
            val cached = try {
                cache.get(cacheKey)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("AIHelper", "Falha ao obter entrada do cache para a chave $cacheKey", e)
                null
            }
            if (cached != null) {
                if (T::class == String::class) {
                    return cached as T
                }
            }
        }
        
        val startTime = System.currentTimeMillis()
        var currentAttempt = 0
        while (currentAttempt < maxRetries) {
            try {
                val result = block()
                val latency = System.currentTimeMillis() - startTime
                AITelemetry.logRequest(latency, true, currentAttempt > 0, estimatedTokens)
                
                if (cacheKey != null && result is String) {
                    try {
                        cache.put(cacheKey, result)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w("AIHelper", "Falha ao gravar no cache para a chave $cacheKey", e)
                    }
                }
                
                return result
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                currentAttempt++
                if (currentAttempt >= maxRetries) {
                    val latency = System.currentTimeMillis() - startTime
                    AITelemetry.logRequest(latency, false, true, 0)
                    throw e
                }
                delay(1000L * currentAttempt) // exponential backoff
            }
        }
        throw IllegalStateException("Should not reach here")
    }
}
