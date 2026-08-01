package com.studyflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_notices")
data class ExamNotice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int, // Refere-se a StudyGoal
    val title: String,
    val institution: String? = null,
    val role: String? = null,
    val examBoard: String? = null,
    val link: String? = null,
    val publicationDate: Long? = null,
    val examDate: Long? = null,
    val phases: Int = 1
)

@Entity(tableName = "exam_notice_versions")
data class ExamNoticeVersion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noticeId: Int,
    val versionNumber: Int,
    val summaryOfChanges: String? = null,
    val analysisDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "exam_notice_subjects")
data class ExamNoticeSubject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noticeId: Int,
    val name: String,
    val weight: Float = 1f,
    val questionCount: Int? = null,
    val category: String? = null // Conhecimentos Básicos, Específicos
)

@Entity(tableName = "document_analysis")
data class DocumentAnalysis(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val documentId: Int, // Refere-se a StudyDocument
    val extractedText: String? = null,
    val summary: String? = null,
    val analysisDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_documents")
data class StudyDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "PDF", "DOCX", "TXT", "IMAGE"
    val uri: String,
    val importDate: Long = System.currentTimeMillis(),
    val isAnalyzed: Boolean = false,
    val relatedGoalId: Int? = null
)
