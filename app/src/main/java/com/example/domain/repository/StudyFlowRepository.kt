package com.example.domain.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

interface StudyFlowRepository {
    fun getSubjects(): Flow<List<Subject>>
    suspend fun getSubjectById(id: Int): Subject?
    suspend fun insertSubject(subject: Subject): Long
    suspend fun updateSubject(subject: Subject)
    suspend fun deleteSubject(subject: Subject)

    fun getTasks(): Flow<List<Task>>
    fun getTasksForSubject(subjectId: Int): Flow<List<Task>>
    suspend fun getTaskById(id: Int): Task?
    suspend fun insertTask(task: Task): Long
    suspend fun insertTasks(tasks: List<Task>)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun deleteTasksBySubject(subjectId: Int)
    suspend fun moveTasksToSubject(oldSubjectId: Int, newSubjectId: Int)

    fun getStudyPlans(): Flow<List<StudyPlan>>
    suspend fun getStudyPlanById(id: Int): StudyPlan?
    suspend fun insertStudyPlan(studyPlan: StudyPlan): Long
    suspend fun updateStudyPlan(studyPlan: StudyPlan)
    suspend fun deleteStudyPlan(studyPlan: StudyPlan)

    fun getAllStudySessions(): Flow<List<StudySession>>
    suspend fun insertStudySession(session: StudySession): Long

    fun getAllReviewSchedules(): Flow<List<ReviewSchedule>>
    suspend fun insertReviewSchedule(schedule: ReviewSchedule): Long

    fun getFlashcardsBySubject(subjectId: Int): Flow<List<Flashcard>>
    suspend fun insertFlashcard(flashcard: Flashcard): Long
    suspend fun updateFlashcard(flashcard: Flashcard)

    fun getStudyNotesBySubject(subjectId: Int): Flow<List<StudyNote>>
    suspend fun insertStudyNote(note: StudyNote): Long

    fun getMindMapNodesBySubject(subjectId: Int): Flow<List<MindMapNode>>
    suspend fun insertMindMapNode(node: MindMapNode): Long

    // Phase 6
    fun getAllExamGoals(): Flow<List<ExamGoal>>
    suspend fun insertExamGoal(examGoal: ExamGoal): Long

    fun getAllEssayThemes(): Flow<List<EssayTheme>>
    suspend fun insertEssayTheme(theme: EssayTheme): Long
    suspend fun insertEssayThemes(themes: List<EssayTheme>)

    suspend fun getMotivationalTextsForTheme(themeId: Int): List<MotivationalText>
    suspend fun insertMotivationalText(text: MotivationalText): Long
    suspend fun insertMotivationalTexts(texts: List<MotivationalText>)

    fun getAllEssayCorrections(): Flow<List<EssayCorrection>>

    fun getAllEssaySubmissions(): Flow<List<EssaySubmission>>
    suspend fun getEssaySubmissionById(id: Int): EssaySubmission?
    suspend fun insertEssaySubmission(submission: EssaySubmission): Long
    suspend fun updateEssaySubmission(submission: EssaySubmission)

    suspend fun getEssayCorrectionForSubmission(submissionId: Int): EssayCorrection?
    suspend fun insertEssayCorrection(correction: EssayCorrection): Long

    fun getAllSimulations(): Flow<List<Simulation>>
    suspend fun insertSimulation(simulation: Simulation): Long

    // Global Migration Entities
    fun getAllStudyGoals(): Flow<List<StudyGoal>>
    suspend fun insertStudyGoal(goal: StudyGoal): Long
    suspend fun updateStudyGoal(goal: StudyGoal)
    suspend fun deleteStudyGoal(goal: StudyGoal)

    fun getExamNoticesForGoal(goalId: Int): Flow<List<ExamNotice>>
    suspend fun insertExamNotice(notice: ExamNotice): Long

    fun getExamNoticeVersions(noticeId: Int): Flow<List<ExamNoticeVersion>>
    suspend fun insertExamNoticeVersion(version: ExamNoticeVersion): Long

    fun getExamNoticeSubjects(noticeId: Int): Flow<List<ExamNoticeSubject>>
    suspend fun insertExamNoticeSubject(subject: ExamNoticeSubject): Long

    fun getAllStudyDocuments(): Flow<List<StudyDocument>>
    suspend fun insertStudyDocument(document: StudyDocument): Long

    suspend fun getDocumentAnalysis(documentId: Int): DocumentAnalysis?
    suspend fun insertDocumentAnalysis(analysis: DocumentAnalysis): Long

    // Gamification
    fun getUserProfile(): kotlinx.coroutines.flow.Flow<com.example.data.local.UserProfile?>
    suspend fun insertUserProfile(userProfile: com.example.data.local.UserProfile)
    suspend fun updateUserProfile(userProfile: com.example.data.local.UserProfile)
    
    fun getAllAchievements(): kotlinx.coroutines.flow.Flow<List<com.example.data.local.Achievement>>
    suspend fun insertAchievements(achievements: List<com.example.data.local.Achievement>)
    suspend fun updateAchievement(achievement: com.example.data.local.Achievement)
    
    fun getAllChallenges(): kotlinx.coroutines.flow.Flow<List<com.example.data.local.Challenge>>
    suspend fun insertChallenges(challenges: List<com.example.data.local.Challenge>)
    suspend fun updateChallenge(challenge: com.example.data.local.Challenge)
    suspend fun deleteChallengesByType(type: String)

    // AI Chat
    fun getActiveAIConversations(): kotlinx.coroutines.flow.Flow<List<AIConversation>>
    suspend fun getAIConversationById(id: Long): AIConversation?
    suspend fun insertAIConversation(conversation: AIConversation): Long
    suspend fun updateAIConversation(conversation: AIConversation)
    suspend fun deleteAIConversation(conversation: AIConversation)
    fun getAIMessages(conversationId: Long): kotlinx.coroutines.flow.Flow<List<AIMessage>>
    suspend fun insertAIMessage(message: AIMessage): Long
    suspend fun deleteAIMessages(conversationId: Long)


}
