package com.studyflow.app.domain.repository

import com.studyflow.app.data.local.AnalyzedDocument
import kotlinx.coroutines.flow.Flow

interface DocumentAnalyzerRepository {
    fun getAllDocuments(): Flow<List<AnalyzedDocument>>
    suspend fun getDocumentById(id: Int): AnalyzedDocument?
    suspend fun insertDocument(doc: AnalyzedDocument): Long
    suspend fun updateDocument(doc: AnalyzedDocument)
    suspend fun deleteDocument(doc: AnalyzedDocument)
}
