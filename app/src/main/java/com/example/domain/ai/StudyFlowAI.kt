package com.example.domain.ai

import java.io.File

/**
 * Interfaces e Contratos para o futuro módulo de Inteligência Artificial do StudyFlow.
 * Esta arquitetura foi preparada para integração futura com a API da NVIDIA.
 */

interface IntelligentTutor {
    suspend fun answerQuestion(context: String, question: String): String
    suspend fun explainConcept(concept: String, difficultyLevel: String): String
}

interface EssayCorrector {
    suspend fun correctEssay(essayText: String, examType: String, theme: String): EssayCorrectionResult
}

data class EssayCorrectionResult(
    val scoresByCompetence: Map<String, Int>,
    val totalScore: Int,
    val strengths: String,
    val weaknesses: String,
    val suggestions: String
)

interface FlashcardGenerator {
    suspend fun generateFlashcardsFromText(text: String, count: Int): List<AIFlashcard>
}

data class AIFlashcard(val front: String, val back: String)

interface QuestionGenerator {
    suspend fun generateQuestions(subject: String, difficulty: String, count: Int): List<AIQuestion>
}

data class AIQuestion(
    val statement: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

interface SummaryGenerator {
    suspend fun summarizeText(text: String, format: String): String
}

interface NoticeAnalyzer {
    suspend fun analyzeExamNotice(noticeText: String): ExamNoticeExtraction
}

data class ExamNoticeExtraction(
    val examBoard: String,
    val role: String,
    val subjects: List<String>,
    val weights: Map<String, Float>,
    val importantDates: Map<String, Long>
)

interface PDFReader {
    suspend fun extractTextFromFile(file: File): String
}

interface SmartPlanner {
    suspend fun generateStudySchedule(
        goals: List<String>,
        availableHoursPerWeek: Int,
        weakSubjects: List<String>
    ): String
}

interface StudyMentor {
    suspend fun getMotivationalAdvice(userProfile: String, recentPerformance: String): String
}
