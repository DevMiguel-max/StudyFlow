package com.studyflow.app.domain.ai

import kotlinx.coroutines.delay

object AIHelper {
    suspend inline fun <reified T> withRetryAndTelemetry(
        operationName: String,
        maxRetries: Int = 3,
        estimatedTokens: Int = 500,
        cacheKey: String? = null,
        crossinline block: suspend () -> T
    ): T {
        if (cacheKey != null) {
            try {
                val cached = AICacheManager.get(cacheKey)
                if (cached != null) {
                    if (T::class == String::class) {
                        return cached as T
                    }
                }
            } catch (ignored: Throwable) {
                // Ignore cache access issues when running outside full app context (e.g. unit tests)
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
                        AICacheManager.put(cacheKey, result)
                    } catch (ignored: Throwable) {
                        // Ignore cache write issues
                    }
                }
                
                return result
            } catch (e: kotlinx.coroutines.CancellationException) {
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
