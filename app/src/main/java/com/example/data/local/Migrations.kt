package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val tables = listOf(
            "subjects", "tasks", "study_goals", "study_sessions",
            "review_schedules", "simulations", "essay_submissions",
            "user_profile", "study_plans"
        )
        for (table in tables) {
            db.execSQL("ALTER TABLE $table ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        }
        
        // Add name, email, mainGoal to user_profile
        db.execSQL("ALTER TABLE user_profile ADD COLUMN name TEXT NOT NULL DEFAULT 'Estudante'")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN email TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN mainGoal TEXT NOT NULL DEFAULT 'Aprovação'")
    }
}
