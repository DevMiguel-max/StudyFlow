package com.studyflow.app.domain.ai

class FakeAICache : AICache {
    private val memory = mutableMapOf<String, String>()

    override suspend fun get(key: String): String? {
        return memory[key]
    }

    override suspend fun put(key: String, data: String) {
        memory[key] = data
    }

    override suspend fun clear() {
        memory.clear()
    }
}
