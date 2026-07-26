package com.example.domain.repository

import com.example.data.local.AnalyzedDocument
import kotlinx.coroutines.flow.Flow

interface DocumentAnalyzerRepository {
    fun getAllDocuments(): Flow<List<AnalyzedDocument>>
    suspend fun getDocumentById(id: Int): AnalyzedDocument?
    suspend fun insertDocument(doc: AnalyzedDocument): Long
    suspend fun updateDocument(doc: AnalyzedDocument)
    suspend fun deleteDocument(doc: AnalyzedDocument)
}
