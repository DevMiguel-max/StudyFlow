with open("app/src/main/java/com/example/data/local/StudyFlowDao.kt", "r") as f:
    content = f.read()

ai_dao_methods = """
    // AI Chat
    @Query("SELECT * FROM ai_conversations WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getActiveAIConversations(): kotlinx.coroutines.flow.Flow<List<AIConversation>>

    @Query("SELECT * FROM ai_conversations WHERE id = :id LIMIT 1")
    suspend fun getAIConversationById(id: Long): AIConversation?

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAIConversation(conversation: AIConversation): Long

    @Update
    suspend fun updateAIConversation(conversation: AIConversation)

    @Delete
    suspend fun deleteAIConversation(conversation: AIConversation)

    @Query("SELECT * FROM ai_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getAIMessages(conversationId: Long): kotlinx.coroutines.flow.Flow<List<AIMessage>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAIMessage(message: AIMessage): Long

    @Query("DELETE FROM ai_messages WHERE conversationId = :conversationId")
    suspend fun deleteAIMessages(conversationId: Long)
"""

content = content.replace("suspend fun deleteChallengesByType(type: String)", "suspend fun deleteChallengesByType(type: String)\n" + ai_dao_methods)

with open("app/src/main/java/com/example/data/local/StudyFlowDao.kt", "w") as f:
    f.write(content)
