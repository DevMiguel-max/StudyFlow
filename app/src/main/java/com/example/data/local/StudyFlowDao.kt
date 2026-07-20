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
}
