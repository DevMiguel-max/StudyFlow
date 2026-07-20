package com.example.data.repository

import com.example.data.local.*
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.Flow

class StudyFlowRepositoryImpl(
    private val dao: StudyFlowDao
) : StudyFlowRepository {
    override fun getSubjects(): Flow<List<Subject>> = dao.getAllSubjects()
    override suspend fun getSubjectById(id: Int): Subject? = dao.getSubjectById(id)
    override suspend fun insertSubject(subject: Subject): Long = dao.insertSubject(subject)
    override suspend fun updateSubject(subject: Subject) = dao.updateSubject(subject)
    override suspend fun deleteSubject(subject: Subject) = dao.deleteSubject(subject)

    override fun getTasks(): Flow<List<Task>> = dao.getAllTasks()
    override fun getTasksForSubject(subjectId: Int): Flow<List<Task>> = dao.getTasksForSubject(subjectId)
    override suspend fun getTaskById(id: Int): Task? = dao.getTaskById(id)
    override suspend fun insertTask(task: Task): Long = dao.insertTask(task)
    override suspend fun insertTasks(tasks: List<Task>) = dao.insertTasks(tasks)
    override suspend fun updateTask(task: Task) = dao.updateTask(task)
    override suspend fun deleteTask(task: Task) = dao.deleteTask(task)
    override suspend fun deleteTasksBySubject(subjectId: Int) = dao.deleteTasksBySubject(subjectId)
    override suspend fun moveTasksToSubject(oldSubjectId: Int, newSubjectId: Int) = dao.moveTasksToSubject(oldSubjectId, newSubjectId)

    override fun getStudyPlans(): Flow<List<StudyPlan>> = dao.getAllStudyPlans()
    override suspend fun getStudyPlanById(id: Int): StudyPlan? = dao.getStudyPlanById(id)
    override suspend fun insertStudyPlan(studyPlan: StudyPlan): Long = dao.insertStudyPlan(studyPlan)
    override suspend fun updateStudyPlan(studyPlan: StudyPlan) = dao.updateStudyPlan(studyPlan)
    override suspend fun deleteStudyPlan(studyPlan: StudyPlan) = dao.deleteStudyPlan(studyPlan)

    override fun getAllStudySessions(): Flow<List<StudySession>> = dao.getAllStudySessions()
    override suspend fun insertStudySession(session: StudySession): Long = dao.insertStudySession(session)

    override fun getAllReviewSchedules(): Flow<List<ReviewSchedule>> = dao.getAllReviewSchedules()
    override suspend fun insertReviewSchedule(schedule: ReviewSchedule): Long = dao.insertReviewSchedule(schedule)

    override fun getFlashcardsBySubject(subjectId: Int): Flow<List<Flashcard>> = dao.getFlashcardsBySubject(subjectId)
    override suspend fun insertFlashcard(flashcard: Flashcard): Long = dao.insertFlashcard(flashcard)
    override suspend fun updateFlashcard(flashcard: Flashcard) = dao.updateFlashcard(flashcard)

    override fun getStudyNotesBySubject(subjectId: Int): Flow<List<StudyNote>> = dao.getStudyNotesBySubject(subjectId)
    override suspend fun insertStudyNote(note: StudyNote): Long = dao.insertStudyNote(note)

    override fun getMindMapNodesBySubject(subjectId: Int): Flow<List<MindMapNode>> = dao.getMindMapNodesBySubject(subjectId)
    override suspend fun insertMindMapNode(node: MindMapNode): Long = dao.insertMindMapNode(node)
}
