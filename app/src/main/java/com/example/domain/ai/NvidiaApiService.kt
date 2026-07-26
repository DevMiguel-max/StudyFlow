package com.example.domain.ai

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

@Serializable
data class NvidiaChatRequest(
    val model: String,
    val messages: List<NvidiaMessage>,
    val temperature: Float = 0.2f,
    val top_p: Float = 0.7f,
    val max_tokens: Int = 1024
)

@Serializable
data class NvidiaMessage(
    val role: String,
    val content: String
)

@Serializable
data class NvidiaChatResponse(
    val id: String,
    val choices: List<NvidiaChoice>
)

@Serializable
data class NvidiaChoice(
    val index: Int,
    val message: NvidiaMessage
)

interface NvidiaApi {
    @POST("v1/chat/completions")
    suspend fun generateCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: NvidiaChatRequest
    ): NvidiaChatResponse
}

object NvidiaApiClient {
    private const val BASE_URL = "https://integrate.api.nvidia.com/"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: NvidiaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NvidiaApi::class.java)
    }
}
