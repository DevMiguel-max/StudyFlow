package com.studyflow.app.data.repository

import com.studyflow.app.data.local.GeneratedMaterial
import com.studyflow.app.data.local.GeneratedQuestion
import com.studyflow.app.data.local.Flashcard
import com.studyflow.app.data.local.StudyFlowDao
import com.studyflow.app.domain.repository.MaterialGeneratorRepository
import kotlinx.coroutines.flow.Flow

class MaterialGeneratorRepositoryImpl(
    private val dao: StudyFlowDao
) : MaterialGeneratorRepository {
    
    override fun getAllMaterials(): Flow<List<GeneratedMaterial>> = dao.getAllGeneratedMaterials()
    
    override fun getMaterialsByType(type: String): Flow<List<GeneratedMaterial>> = dao.getGeneratedMaterialsByType(type)
    
    override suspend fun insertMaterial(material: GeneratedMaterial): Long = dao.insertGeneratedMaterial(material)
    
    override suspend fun deleteMaterial(material: GeneratedMaterial) = dao.deleteGeneratedMaterial(material)
    
    override fun getAllQuestions(): Flow<List<GeneratedQuestion>> = dao.getAllGeneratedQuestions()
    
    override suspend fun insertQuestion(question: GeneratedQuestion): Long = dao.insertGeneratedQuestion(question)
    
    override suspend fun insertQuestions(questions: List<GeneratedQuestion>) = dao.insertGeneratedQuestions(questions)
    
    override suspend fun updateQuestion(question: GeneratedQuestion) = dao.updateGeneratedQuestion(question)
    
    override suspend fun deleteQuestion(question: GeneratedQuestion) = dao.deleteGeneratedQuestion(question)
    
    override suspend fun insertFlashcards(flashcards: List<Flashcard>) = dao.insertFlashcards(flashcards)
}
