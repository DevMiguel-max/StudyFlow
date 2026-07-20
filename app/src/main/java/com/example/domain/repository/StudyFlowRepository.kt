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
}
