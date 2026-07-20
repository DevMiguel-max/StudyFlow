package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Subject::class,
        Task::class,
        StudyMethod::class,
        Goal::class,
        Achievement::class,
        UserProfile::class,
        ExamGoal::class,
        EssayTheme::class,
        MotivationalText::class,
        EssaySubmission::class,
        Simulation::class,
        StudyPlan::class,
        StudySession::class,
        ReviewSchedule::class,
        Flashcard::class,
        StudyNote::class,
        MindMapNode::class
    ],
    version = 4,
    exportSchema = false
)
abstract class StudyFlowDatabase : RoomDatabase() {
    abstract fun studyFlowDao(): StudyFlowDao
}
