package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Subject::class,
        Task::class,
        StudyMethod::class,
        Goal::class, // The old one
        Achievement::class,
        UserProfile::class,
        ExamGoal::class, // The older Phase 6 one, keeping for compatibility
        EssayTheme::class,
        MotivationalText::class,
        EssaySubmission::class,
        EssayCorrection::class,
        Simulation::class,
        StudyPlan::class,
        StudySession::class,
        ReviewSchedule::class,
        Flashcard::class,
        StudyNote::class,
        MindMapNode::class,
        
        // New Global Migration Entities
        StudyGoal::class,
        ExamNotice::class,
        ExamNoticeVersion::class,
        ExamNoticeSubject::class,
        StudyDocument::class,
        DocumentAnalysis::class,
        Challenge::class,
        AIConversation::class,
        AIMessage::class,
        AnalyzedDocument::class,
        GeneratedMaterial::class,
        GeneratedQuestion::class,
        MentorProfile::class,
        MentorRecommendation::class,
        SmartMission::class,
        MentorHistory::class
        , AICacheEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class StudyFlowDatabase : RoomDatabase() {
    abstract fun studyFlowDao(): StudyFlowDao
    abstract fun aiCacheDao(): AICacheDao
}
