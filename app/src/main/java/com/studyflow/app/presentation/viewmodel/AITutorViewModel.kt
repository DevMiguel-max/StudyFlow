package com.studyflow.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.local.AIConversation
import com.studyflow.app.data.local.AIMessage
import com.studyflow.app.data.local.MessageStatus
import com.studyflow.app.domain.ai.AITutorService
import com.studyflow.app.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.studyflow.app.domain.ai.AIConfig

data class AITutorState(
    val conversations: List<AIConversation> = emptyList(),
    val currentConversationId: Long? = null,
    val messages: List<AIMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val error: String? = null,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val selectedModel: String = AIConfig.MODEL
)

class AITutorViewModel(
    private val repository: StudyFlowRepository,
    private val aiService: AITutorService = AITutorService()
) : ViewModel() {

    private val _state = MutableStateFlow(AITutorState())
    val state: StateFlow<AITutorState> = _state

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            repository.getActiveAIConversations().collect { convs ->
                _state.update { it.copy(conversations = convs) }
            }
        }
    }

    fun selectConversation(id: Long) {
        _state.update { it.copy(currentConversationId = id, error = null) }
        viewModelScope.launch {
            repository.getAIMessages(id).collect { msgs ->
                _state.update { it.copy(messages = msgs) }
            }
        }
    }

    fun createNewConversation(title: String = "Nova Conversa") {
        viewModelScope.launch {
            val conv = AIConversation(title = title)
            val id = repository.insertAIConversation(conv)
            selectConversation(id)
        }
    }

    fun updateSettings(temp: Float, max: Int, model: String) {
        _state.update { it.copy(temperature = temp, maxTokens = max, selectedModel = model) }
    }

    fun sendMessage(text: String) {
        val convId = state.value.currentConversationId ?: return
        val currentMessages = state.value.messages
        
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, error = null) }
            
            // User message
            val userMsg = AIMessage(conversationId = convId, text = text, isUser = true)
            repository.insertAIMessage(userMsg)
            
            // Build history
            val history = currentMessages.map { Pair(it.text, it.isUser) }
            
            try {
                val responseText = aiService.sendMessage(
                    modelName = state.value.selectedModel,
                    temperature = state.value.temperature,
                    maxTokens = state.value.maxTokens,
                    history = history,
                    message = text
                )
                
                val aiMsg = AIMessage(conversationId = convId, text = responseText, isUser = false)
                repository.insertAIMessage(aiMsg)
                
                // Update conversation timestamp
                val conv = repository.getAIConversationById(convId)
                if (conv != null) {
                    val newTitle = if (conv.title == "Nova Conversa" && currentMessages.isEmpty()) {
                         text.take(30) + (if (text.length > 30) "..." else "")
                    } else conv.title
                    repository.updateAIConversation(conv.copy(updatedAt = System.currentTimeMillis(), title = newTitle))
                }
                
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage ?: "Erro ao se comunicar com o tutor AI.") }
            } finally {
                _state.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun regenerateLastMessage() {
        val convId = state.value.currentConversationId ?: return
        val currentMessages = state.value.messages
        val lastUserMessage = currentMessages.lastOrNull { it.isUser } ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, error = null) }
            
            // History prior to the last user message
            val index = currentMessages.lastIndexOf(lastUserMessage)
            val history = if (index > 0) {
                currentMessages.take(index).map { Pair(it.text, it.isUser) }
            } else emptyList()
            
            try {
                val responseText = aiService.sendMessage(
                    modelName = state.value.selectedModel,
                    temperature = state.value.temperature,
                    maxTokens = state.value.maxTokens,
                    history = history,
                    message = lastUserMessage.text
                )
                
                val aiMsg = AIMessage(conversationId = convId, text = responseText, isUser = false)
                repository.insertAIMessage(aiMsg)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage ?: "Erro ao regenerar resposta.") }
            } finally {
                _state.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            val conv = repository.getAIConversationById(id)
            if (conv != null) {
                repository.deleteAIConversation(conv)
                if (_state.value.currentConversationId == id) {
                    _state.update { it.copy(currentConversationId = null, messages = emptyList()) }
                }
            }
        }
    }
    
    fun renameConversation(id: Long, newTitle: String) {
        viewModelScope.launch {
            val conv = repository.getAIConversationById(id)
            if (conv != null) {
                repository.updateAIConversation(conv.copy(title = newTitle))
            }
        }
    }

    fun clearHistory() {
        val convId = state.value.currentConversationId ?: return
        viewModelScope.launch {
            repository.deleteAIMessages(convId)
        }
    }
}
