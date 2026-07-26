package com.example.domain.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AITelemetryData(
    val totalRequests: Int = 0,
    val successfulRequests: Int = 0,
    val failedRequests: Int = 0,
    val retriedRequests: Int = 0,
    val totalLatencyMs: Long = 0,
    val averageLatencyMs: Long = 0,
    val estimatedTokensUsed: Int = 0
)

object AITelemetry {
    private val _stats = MutableStateFlow(AITelemetryData())
    val stats: StateFlow<AITelemetryData> = _stats.asStateFlow()

    fun logRequest(latencyMs: Long, isSuccess: Boolean, isRetry: Boolean, estimatedTokens: Int) {
        _stats.update { current ->
            val newTotal = current.totalRequests + 1
            val newSuccess = current.successfulRequests + (if (isSuccess) 1 else 0)
            val newFailed = current.failedRequests + (if (!isSuccess) 1 else 0)
            val newRetries = current.retriedRequests + (if (isRetry) 1 else 0)
            val newTotalLatency = current.totalLatencyMs + latencyMs
            val newAvgLatency = if (newTotal > 0) newTotalLatency / newTotal else 0
            val newTokens = current.estimatedTokensUsed + estimatedTokens

            current.copy(
                totalRequests = newTotal,
                successfulRequests = newSuccess,
                failedRequests = newFailed,
                retriedRequests = newRetries,
                totalLatencyMs = newTotalLatency,
                averageLatencyMs = newAvgLatency,
                estimatedTokensUsed = newTokens
            )
        }
    }
    
    fun clearStats() {
        _stats.value = AITelemetryData()
    }
}
