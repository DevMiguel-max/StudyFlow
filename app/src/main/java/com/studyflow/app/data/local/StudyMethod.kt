package com.studyflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_methods")
data class StudyMethod(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = ""
)
