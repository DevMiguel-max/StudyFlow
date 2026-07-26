package com.example.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `mentor_profile` (`id` INTEGER NOT NULL, `mainGoal` TEXT NOT NULL, `examType` TEXT NOT NULL, `examDate` INTEGER NOT NULL, `hoursPerDay` INTEGER NOT NULL, `availableDays` TEXT NOT NULL, `preferredTime` TEXT NOT NULL, `favoriteSubjects` TEXT NOT NULL, `difficultSubjects` TEXT NOT NULL, `preferredStudyMethod` TEXT NOT NULL, `learningPace` TEXT NOT NULL, `coachModeEnabled` INTEGER NOT NULL, `mentorEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `mentor_recommendation` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `reason` TEXT NOT NULL, `type` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, `isAccepted` INTEGER)
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `smart_mission` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `xpReward` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `type` TEXT NOT NULL, `targetAmount` INTEGER NOT NULL, `currentAmount` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `mentor_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `eventType` TEXT NOT NULL, `details` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)
        """)
    }
}
