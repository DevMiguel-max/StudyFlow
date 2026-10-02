package com.studyflow.app.domain

import com.studyflow.app.data.local.*
import com.studyflow.app.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

open class FakeStudyFlowRepository : StudyFlowRepository {
    var userProfileState = MutableStateFlow<UserProfile?>(null)
    var updatedUserProfile: UserProfile? = null
    val insertedSimulations = mutableListOf<Simulation>()
    val insertedSessions = mutableListOf<StudySession>()

    override fun getUserProfile(): Flow<UserProfile?> = userProfileState

    override suspend fun insertUserProfile(userProfile: UserProfile) {
        updatedUserProfile = userProfile
        userProfileState.value = userProfile
    }

    override suspend fun updateUserProfile(userProfile: UserProfile) {
        updatedUserProfile = userProfile
        userProfileState.value = userProfile
    }

    override fun getAllAchievements(): Flow<List<Achievement>> = flowOf(emptyList())
    override suspend fun insertAchievements(achievements: List<Achievement>) {}
    override suspend fun updateAchievement(achievement: Achievement) {}

    override fun getAllChallenges(): Flow<List<Challenge>> = flowOf(emptyList())
    override suspend fun insertChallenges(challenges: List<Challenge>) {}
    override suspend fun updateChallenge(challenge: Challenge) {}
    override suspend fun deleteChallengesByType(type: String) {}

    override fun getAllSimulations(): Flow<List<Simulation>> = flowOf(insertedSimulations)
    override suspend fun insertSimulation(simulation: Simulation): Long {
        insertedSimulations.add(simulation)
        return insertedSimulations.size.toLong()
    }

    override fun getAllStudySessions(): Flow<List<StudySession>> = flowOf(insertedSessions)
    override suspend fun insertStudySession(session: StudySession): Long {
        insertedSessions.add(session)
        return insertedSessions.size.toLong()
    }

    override fun getSubjects(): Flow<List<Subject>> = flowOf(emptyList())
    override suspend fun getSubjectById(id: Int): Subject? = null
    override suspend fun insertSubject(subject: Subject): Long = 0L
    override suspend fun updateSubject(subject: Subject) {}
    override suspend fun deleteSubject(subject: Subject) {}

    override fun getTasks(): Flow<List<Task>> = flowOf(emptyList())
    override fun getTasksForSubject(subjectId: Int): Flow<List<Task>> = flowOf(emptyList())
    override suspend fun getTaskById(id: Int): Task? = null
    override suspend fun insertTask(task: Task): Long = 0L
    override suspend fun insertTasks(tasks: List<Task>) {}
    override suspend fun updateTask(task: Task) {}
    override suspend fun deleteTask(task: Task) {}
    override suspend fun deleteTasksBySubject(subjectId: Int) {}
    override suspend fun moveTasksToSubject(oldSubjectId: Int, newSubjectId: Int) {}

    override fun getStudyPlans(): Flow<List<StudyPlan>> = flowOf(emptyList())
    override suspend fun getStudyPlanById(id: Int): StudyPlan? = null
    override suspend fun insertStudyPlan(studyPlan: StudyPlan): Long = 0L
    override suspend fun updateStudyPlan(studyPlan: StudyPlan) {}
    override suspend fun deleteStudyPlan(studyPlan: StudyPlan) {}

    override fun getAllReviewSchedules(): Flow<List<ReviewSchedule>> = flowOf(emptyList())
    override suspend fun insertReviewSchedule(schedule: ReviewSchedule): Long = 0L

    override fun getFlashcardsBySubject(subjectId: Int): Flow<List<Flashcard>> = flowOf(emptyList())
    override suspend fun insertFlashcard(flashcard: Flashcard): Long = 0L
    override suspend fun updateFlashcard(flashcard: Flashcard) {}

    override fun getStudyNotesBySubject(subjectId: Int): Flow<List<StudyNote>> = flowOf(emptyList())
    override suspend fun insertStudyNote(note: StudyNote): Long = 0L

    override fun getMindMapNodesBySubject(subjectId: Int): Flow<List<MindMapNode>> = flowOf(emptyList())
    override suspend fun insertMindMapNode(node: MindMapNode): Long = 0L

    override fun getAllExamGoals(): Flow<List<ExamGoal>> = flowOf(emptyList())
    override suspend fun insertExamGoal(examGoal: ExamGoal): Long = 0L

    override fun getAllEssayThemes(): Flow<List<EssayTheme>> = flowOf(emptyList())
    override suspend fun insertEssayTheme(theme: EssayTheme): Long = 0L
    override suspend fun insertEssayThemes(themes: List<EssayTheme>) {}

    override suspend fun getMotivationalTextsForTheme(themeId: Int): List<MotivationalText> = emptyList()
    override suspend fun insertMotivationalText(text: MotivationalText): Long = 0L
    override suspend fun insertMotivationalTexts(texts: List<MotivationalText>) {}

    override fun getAllEssayCorrections(): Flow<List<EssayCorrection>> = flowOf(emptyList())
    override fun getAllEssaySubmissions(): Flow<List<EssaySubmission>> = flowOf(emptyList())
    override suspend fun getEssaySubmissionById(id: Int): EssaySubmission? = null
    override suspend fun insertEssaySubmission(submission: EssaySubmission): Long = 0L
    override suspend fun updateEssaySubmission(submission: EssaySubmission) {}
    override suspend fun getEssayCorrectionForSubmission(submissionId: Int): EssayCorrection? = null
    override suspend fun insertEssayCorrection(correction: EssayCorrection): Long = 0L

    override fun getAllStudyGoals(): Flow<List<StudyGoal>> = flowOf(emptyList())
    override suspend fun insertStudyGoal(goal: StudyGoal): Long = 0L
    override suspend fun updateStudyGoal(goal: StudyGoal) {}
    override suspend fun deleteStudyGoal(goal: StudyGoal) {}

    override fun getExamNoticesForGoal(goalId: Int): Flow<List<ExamNotice>> = flowOf(emptyList())
    override suspend fun insertExamNotice(notice: ExamNotice): Long = 0L

    override fun getExamNoticeVersions(noticeId: Int): Flow<List<ExamNoticeVersion>> = flowOf(emptyList())
    override suspend fun insertExamNoticeVersion(version: ExamNoticeVersion): Long = 0L

    override fun getExamNoticeSubjects(noticeId: Int): Flow<List<ExamNoticeSubject>> = flowOf(emptyList())
    override suspend fun insertExamNoticeSubject(subject: ExamNoticeSubject): Long = 0L

    override fun getAllStudyDocuments(): Flow<List<StudyDocument>> = flowOf(emptyList())
    override suspend fun insertStudyDocument(document: StudyDocument): Long = 0L

    override suspend fun getDocumentAnalysis(documentId: Int): DocumentAnalysis? = null
    override suspend fun insertDocumentAnalysis(analysis: DocumentAnalysis): Long = 0L

    override fun getActiveAIConversations(): Flow<List<AIConversation>> = flowOf(emptyList())
    override suspend fun getAIConversationById(id: Long): AIConversation? = null
    override suspend fun insertAIConversation(conversation: AIConversation): Long = 0L
    override suspend fun updateAIConversation(conversation: AIConversation) {}
    override suspend fun deleteAIConversation(conversation: AIConversation) {}

    override fun getAIMessages(conversationId: Long): Flow<List<AIMessage>> = flowOf(emptyList())
    override suspend fun insertAIMessage(message: AIMessage): Long = 0L
    override suspend fun deleteAIMessages(conversationId: Long) {}
}
