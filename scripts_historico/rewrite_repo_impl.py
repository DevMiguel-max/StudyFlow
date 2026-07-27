with open("app/src/main/java/com/example/data/repository/StudyFlowRepositoryImpl.kt", "r") as f:
    content = f.read()

ai_methods_impl = """
    // AI Chat
    override fun getActiveAIConversations(): kotlinx.coroutines.flow.Flow<List<AIConversation>> = dao.getActiveAIConversations()
    override suspend fun getAIConversationById(id: Long): AIConversation? = dao.getAIConversationById(id)
    override suspend fun insertAIConversation(conversation: AIConversation): Long = dao.insertAIConversation(conversation)
    override suspend fun updateAIConversation(conversation: AIConversation) = dao.updateAIConversation(conversation)
    override suspend fun deleteAIConversation(conversation: AIConversation) = dao.deleteAIConversation(conversation)
    override fun getAIMessages(conversationId: Long): kotlinx.coroutines.flow.Flow<List<AIMessage>> = dao.getAIMessages(conversationId)
    override suspend fun insertAIMessage(message: AIMessage): Long = dao.insertAIMessage(message)
    override suspend fun deleteAIMessages(conversationId: Long) = dao.deleteAIMessages(conversationId)
"""

content = content.replace("override suspend fun deleteChallengesByType(type: String) = dao.deleteChallengesByType(type)", "override suspend fun deleteChallengesByType(type: String) = dao.deleteChallengesByType(type)\n" + ai_methods_impl)

with open("app/src/main/java/com/example/data/repository/StudyFlowRepositoryImpl.kt", "w") as f:
    f.write(content)
