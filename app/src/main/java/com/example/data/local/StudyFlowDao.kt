package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyFlowDao {
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
}
