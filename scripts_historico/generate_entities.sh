#!/bin/bash
DIR="app/src/main/java/com/example/data/local"
mkdir -p $DIR

cat << 'ENT' > $DIR/Subject.kt
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val color: String = ""
)
ENT

cat << 'ENT' > $DIR/Task.kt
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val subjectId: Int = 0
)
ENT

cat << 'ENT' > $DIR/StudySession.kt
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int = 0
)
ENT

cat << 'ENT' > $DIR/StudyMethod.kt
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_methods")
data class StudyMethod(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = ""
)
ENT

cat << 'ENT' > $DIR/Goal.kt
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String = ""
)
ENT

cat << 'ENT' > $DIR/Achievement.kt
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = ""
)
ENT

cat << 'ENT' > $DIR/UserProfile.kt
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val level: Int = 1
)
ENT

cat << 'ENT' > $DIR/StudyFlowDao.kt
package com.example.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface StudyFlowDao {
    @Query("SELECT 1")
    suspend fun ping(): Int
}
ENT

cat << 'ENT' > $DIR/StudyFlowDatabase.kt
package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Subject::class,
        Task::class,
        StudySession::class,
        StudyMethod::class,
        Goal::class,
        Achievement::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudyFlowDatabase : RoomDatabase() {
    abstract fun studyFlowDao(): StudyFlowDao
}
ENT
