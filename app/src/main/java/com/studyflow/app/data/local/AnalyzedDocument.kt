package com.studyflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analyzed_documents")
data class AnalyzedDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val originalUri: String, // Uri as String
    val textContent: String, 
    val importDate: Long = System.currentTimeMillis(),
    val documentType: String, // "EDITAL", "LIVRO", "RESUMO", "OUTRO"
    val summaryShort: String? = null,
    val summaryMedium: String? = null,
    val summaryComplete: String? = null,
    val analysisResult: String? = null, // JSON with keywords, formulas, dates, topics, or plain text
    val studyPlanId: Int? = null // Reference to StudyPlan if generated
)
