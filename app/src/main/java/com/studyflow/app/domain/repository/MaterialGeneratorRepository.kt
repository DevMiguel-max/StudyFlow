package com.studyflow.app.domain.repository

import com.studyflow.app.data.local.GeneratedMaterial
import com.studyflow.app.data.local.GeneratedQuestion
import com.studyflow.app.data.local.Flashcard
import kotlinx.coroutines.flow.Flow

interface MaterialGeneratorRepository {
    fun getAllMaterials(): Flow<List<GeneratedMaterial>>
    fun getMaterialsByType(type: String): Flow<List<GeneratedMaterial>>
    suspend fun insertMaterial(material: GeneratedMaterial): Long
    suspend fun deleteMaterial(material: GeneratedMaterial)
    
    fun getAllQuestions(): Flow<List<GeneratedQuestion>>
    suspend fun insertQuestion(question: GeneratedQuestion): Long
    suspend fun insertQuestions(questions: List<GeneratedQuestion>)
    suspend fun updateQuestion(question: GeneratedQuestion)
    suspend fun deleteQuestion(question: GeneratedQuestion)
    
    suspend fun insertFlashcards(flashcards: List<Flashcard>)
}
