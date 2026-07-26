with open("app/src/main/java/com/example/domain/repository/StudyFlowRepository.kt", "r") as f:
    content = f.read()

ai_methods = """
    // AI Chat
    fun getActiveAIConversations(): kotlinx.coroutines.flow.Flow<List<AIConversation>>
    suspend fun getAIConversationById(id: Long): AIConversation?
    suspend fun insertAIConversation(conversation: AIConversation): Long
    suspend fun updateAIConversation(conversation: AIConversation)
    suspend fun deleteAIConversation(conversation: AIConversation)
    fun getAIMessages(conversationId: Long): kotlinx.coroutines.flow.Flow<List<AIMessage>>
    suspend fun insertAIMessage(message: AIMessage): Long
    suspend fun deleteAIMessages(conversationId: Long)
"""

content = content.replace("suspend fun deleteChallengesByType(type: String)", "suspend fun deleteChallengesByType(type: String)\n" + ai_methods)

with open("app/src/main/java/com/example/domain/repository/StudyFlowRepository.kt", "w") as f:
    f.write(content)
