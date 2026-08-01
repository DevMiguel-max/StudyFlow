package com.studyflow.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyFlowDao {
    @Query("SELECT * FROM generated_materials ORDER BY createdAt DESC")
    fun getAllGeneratedMaterials(): kotlinx.coroutines.flow.Flow<List<GeneratedMaterial>>

    @Query("SELECT * FROM generated_materials WHERE type = :type ORDER BY createdAt DESC")
    fun getGeneratedMaterialsByType(type: String): kotlinx.coroutines.flow.Flow<List<GeneratedMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedMaterial(material: GeneratedMaterial): Long

    @Delete
    suspend fun deleteGeneratedMaterial(material: GeneratedMaterial)

    @Query("SELECT * FROM generated_questions ORDER BY createdAt DESC")
    fun getAllGeneratedQuestions(): kotlinx.coroutines.flow.Flow<List<GeneratedQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedQuestion(question: GeneratedQuestion): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedQuestions(questions: List<GeneratedQuestion>)

    @Update
    suspend fun updateGeneratedQuestion(question: GeneratedQuestion)

    @Delete
    suspend fun deleteGeneratedQuestion(question: GeneratedQuestion)

    @Query("SELECT * FROM analyzed_documents ORDER BY importDate DESC")
    fun getAllAnalyzedDocuments(): Flow<List<AnalyzedDocument>>

    @Query("SELECT * FROM analyzed_documents WHERE id = :id")
    suspend fun getAnalyzedDocumentById(id: Int): AnalyzedDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyzedDocument(doc: AnalyzedDocument): Long

    @Update
    suspend fun updateAnalyzedDocument(doc: AnalyzedDocument)

    @Delete
    suspend fun deleteAnalyzedDocument(doc: AnalyzedDocument)

    @Query("SELECT 1")
    suspend fun ping(): Int

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Int): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY date ASC, priority DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId")
    fun getTasksForSubject(subjectId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("DELETE FROM tasks WHERE subjectId = :subjectId")
    suspend fun deleteTasksBySubject(subjectId: Int)
    
    @Query("UPDATE tasks SET subjectId = :newSubjectId WHERE subjectId = :oldSubjectId")
    suspend fun moveTasksToSubject(oldSubjectId: Int, newSubjectId: Int)

    // Study Plans
    @Query("SELECT * FROM study_plans ORDER BY endDate ASC")
    fun getAllStudyPlans(): Flow<List<StudyPlan>>

    @Query("SELECT * FROM study_plans WHERE id = :id")
    suspend fun getStudyPlanById(id: Int): StudyPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlan(studyPlan: StudyPlan): Long

    @Update
    suspend fun updateStudyPlan(studyPlan: StudyPlan)

    @Delete
    suspend fun deleteStudyPlan(studyPlan: StudyPlan)

    // Phase 5 Entities
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySession): Long

    @Query("SELECT * FROM study_sessions ORDER BY date DESC")
    fun getAllStudySessions(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewSchedule(schedule: ReviewSchedule): Long

    @Query("SELECT * FROM review_schedules ORDER BY reviewDate ASC")
    fun getAllReviewSchedules(): Flow<List<ReviewSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<Flashcard>)

    @Query("SELECT * FROM flashcards WHERE subjectId = :subjectId")
    fun getFlashcardsBySubject(subjectId: Int): Flow<List<Flashcard>>
    
    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyNote(note: StudyNote): Long

    @Query("SELECT * FROM study_notes WHERE subjectId = :subjectId")
    fun getStudyNotesBySubject(subjectId: Int): Flow<List<StudyNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMindMapNode(node: MindMapNode): Long

    @Query("SELECT * FROM mind_map_nodes WHERE subjectId = :subjectId")
    fun getMindMapNodesBySubject(subjectId: Int): Flow<List<MindMapNode>>

    // Phase 6 Entities
    @Query("SELECT * FROM exam_goals ORDER BY examDate ASC")
    fun getAllExamGoals(): Flow<List<ExamGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamGoal(examGoal: ExamGoal): Long

    @Query("SELECT * FROM essay_themes ORDER BY createdAt DESC")
    fun getAllEssayThemes(): Flow<List<EssayTheme>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssayTheme(theme: EssayTheme): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssayThemes(themes: List<EssayTheme>)

    @Query("SELECT * FROM motivational_texts WHERE themeId = :themeId ORDER BY displayOrder ASC")
    suspend fun getMotivationalTextsForTheme(themeId: Int): List<MotivationalText>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotivationalText(text: MotivationalText): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotivationalTexts(texts: List<MotivationalText>)

    @Query("SELECT * FROM essay_submissions ORDER BY date DESC")
    fun getAllEssaySubmissions(): Flow<List<EssaySubmission>>

    @Query("SELECT * FROM essay_corrections")
    fun getAllEssayCorrections(): Flow<List<EssayCorrection>>
    
    @Query("SELECT * FROM essay_submissions WHERE id = :id")
    suspend fun getEssaySubmissionById(id: Int): EssaySubmission?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssaySubmission(submission: EssaySubmission): Long
    
    @Update
    suspend fun updateEssaySubmission(submission: EssaySubmission)

    @Query("SELECT * FROM essay_corrections WHERE submissionId = :submissionId LIMIT 1")
    suspend fun getEssayCorrectionForSubmission(submissionId: Int): EssayCorrection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssayCorrection(correction: EssayCorrection): Long

    @Query("SELECT * FROM simulations ORDER BY date DESC")
    fun getAllSimulations(): Flow<List<Simulation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulation(simulation: Simulation): Long

    // Global Migration Entities
    @Query("SELECT * FROM study_goals ORDER BY priority DESC, date ASC")
    fun getAllStudyGoals(): Flow<List<StudyGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyGoal(goal: StudyGoal): Long
    
    @Update
    suspend fun updateStudyGoal(goal: StudyGoal)

    @Delete
    suspend fun deleteStudyGoal(goal: StudyGoal)

    @Query("SELECT * FROM exam_notices WHERE goalId = :goalId ORDER BY publicationDate DESC")
    fun getExamNoticesForGoal(goalId: Int): Flow<List<ExamNotice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamNotice(notice: ExamNotice): Long

    @Query("SELECT * FROM exam_notice_versions WHERE noticeId = :noticeId ORDER BY versionNumber DESC")
    fun getExamNoticeVersions(noticeId: Int): Flow<List<ExamNoticeVersion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamNoticeVersion(version: ExamNoticeVersion): Long

    @Query("SELECT * FROM exam_notice_subjects WHERE noticeId = :noticeId")
    fun getExamNoticeSubjects(noticeId: Int): Flow<List<ExamNoticeSubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamNoticeSubject(subject: ExamNoticeSubject): Long

    @Query("SELECT * FROM study_documents ORDER BY importDate DESC")
    fun getAllStudyDocuments(): Flow<List<StudyDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyDocument(document: StudyDocument): Long

    @Query("SELECT * FROM document_analysis WHERE documentId = :documentId LIMIT 1")
    suspend fun getDocumentAnalysis(documentId: Int): DocumentAnalysis?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentAnalysis(analysis: DocumentAnalysis): Long

    // GAMIFICATION
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): kotlinx.coroutines.flow.Flow<UserProfile?>

    @androidx.room.Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): kotlinx.coroutines.flow.Flow<List<Achievement>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): kotlinx.coroutines.flow.Flow<List<Challenge>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<Challenge>)

    @Update
    suspend fun updateChallenge(challenge: Challenge)
    
    @Query("DELETE FROM challenges WHERE type = :type")
    suspend fun deleteChallengesByType(type: String)

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



    // Mentor
    @androidx.room.Query("SELECT * FROM mentor_profile WHERE id = 1")
    fun getMentorProfile(): kotlinx.coroutines.flow.Flow<MentorProfile?>

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertMentorProfile(profile: MentorProfile)

    @androidx.room.Query("SELECT * FROM mentor_recommendation ORDER BY timestamp DESC")
    fun getAllMentorRecommendations(): kotlinx.coroutines.flow.Flow<List<MentorRecommendation>>
    
    @androidx.room.Query("SELECT * FROM mentor_recommendation WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadMentorRecommendations(): kotlinx.coroutines.flow.Flow<List<MentorRecommendation>>

    @androidx.room.Insert
    suspend fun insertMentorRecommendation(recommendation: MentorRecommendation)

    @androidx.room.Update
    suspend fun updateMentorRecommendation(recommendation: MentorRecommendation)

    @androidx.room.Query("SELECT * FROM smart_mission WHERE isCompleted = 0 ORDER BY timestamp DESC")
    fun getActiveSmartMissions(): kotlinx.coroutines.flow.Flow<List<SmartMission>>

    @androidx.room.Insert
    suspend fun insertSmartMission(mission: SmartMission)

    @androidx.room.Update
    suspend fun updateSmartMission(mission: SmartMission)

    @androidx.room.Query("SELECT * FROM mentor_history ORDER BY timestamp DESC")
    fun getMentorHistory(): kotlinx.coroutines.flow.Flow<List<MentorHistory>>

    @androidx.room.Insert
    suspend fun insertMentorHistory(history: MentorHistory)
}
