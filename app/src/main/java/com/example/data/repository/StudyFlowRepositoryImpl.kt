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

    // Phase 6
    override fun getAllExamGoals(): Flow<List<ExamGoal>> = dao.getAllExamGoals()
    override suspend fun insertExamGoal(examGoal: ExamGoal): Long = dao.insertExamGoal(examGoal)

    override fun getAllEssayThemes(): Flow<List<EssayTheme>> = dao.getAllEssayThemes()
    override suspend fun insertEssayTheme(theme: EssayTheme): Long = dao.insertEssayTheme(theme)
    override suspend fun insertEssayThemes(themes: List<EssayTheme>) = dao.insertEssayThemes(themes)

    override suspend fun getMotivationalTextsForTheme(themeId: Int): List<MotivationalText> = dao.getMotivationalTextsForTheme(themeId)
    override suspend fun insertMotivationalText(text: MotivationalText): Long = dao.insertMotivationalText(text)
    override suspend fun insertMotivationalTexts(texts: List<MotivationalText>) = dao.insertMotivationalTexts(texts)

    override fun getAllEssayCorrections(): Flow<List<EssayCorrection>> = dao.getAllEssayCorrections()

    override fun getAllEssaySubmissions(): Flow<List<EssaySubmission>> = dao.getAllEssaySubmissions()
    override suspend fun getEssaySubmissionById(id: Int): EssaySubmission? = dao.getEssaySubmissionById(id)
    override suspend fun insertEssaySubmission(submission: EssaySubmission): Long = dao.insertEssaySubmission(submission)
    override suspend fun updateEssaySubmission(submission: EssaySubmission) = dao.updateEssaySubmission(submission)

    override suspend fun getEssayCorrectionForSubmission(submissionId: Int): EssayCorrection? = dao.getEssayCorrectionForSubmission(submissionId)
    override suspend fun insertEssayCorrection(correction: EssayCorrection): Long = dao.insertEssayCorrection(correction)

    override fun getAllSimulations(): Flow<List<Simulation>> = dao.getAllSimulations()
    override suspend fun insertSimulation(simulation: Simulation): Long = dao.insertSimulation(simulation)

    // Global Migration Entities
    override fun getAllStudyGoals(): Flow<List<StudyGoal>> = dao.getAllStudyGoals()
    override suspend fun insertStudyGoal(goal: StudyGoal): Long = dao.insertStudyGoal(goal)
    override suspend fun updateStudyGoal(goal: StudyGoal) = dao.updateStudyGoal(goal)
    override suspend fun deleteStudyGoal(goal: StudyGoal) = dao.deleteStudyGoal(goal)

    override fun getExamNoticesForGoal(goalId: Int): Flow<List<ExamNotice>> = dao.getExamNoticesForGoal(goalId)
    override suspend fun insertExamNotice(notice: ExamNotice): Long = dao.insertExamNotice(notice)

    override fun getExamNoticeVersions(noticeId: Int): Flow<List<ExamNoticeVersion>> = dao.getExamNoticeVersions(noticeId)
    override suspend fun insertExamNoticeVersion(version: ExamNoticeVersion): Long = dao.insertExamNoticeVersion(version)

    override fun getExamNoticeSubjects(noticeId: Int): Flow<List<ExamNoticeSubject>> = dao.getExamNoticeSubjects(noticeId)
    override suspend fun insertExamNoticeSubject(subject: ExamNoticeSubject): Long = dao.insertExamNoticeSubject(subject)

    override fun getAllStudyDocuments(): Flow<List<StudyDocument>> = dao.getAllStudyDocuments()
    override suspend fun insertStudyDocument(document: StudyDocument): Long = dao.insertStudyDocument(document)

    override suspend fun getDocumentAnalysis(documentId: Int): DocumentAnalysis? = dao.getDocumentAnalysis(documentId)
    override suspend fun insertDocumentAnalysis(analysis: DocumentAnalysis): Long = dao.insertDocumentAnalysis(analysis)
}
