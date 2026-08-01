package com.studyflow.app.data.repository

import com.studyflow.app.data.local.AnalyzedDocument
import com.studyflow.app.data.local.StudyFlowDao
import com.studyflow.app.domain.repository.DocumentAnalyzerRepository
import kotlinx.coroutines.flow.Flow

class DocumentAnalyzerRepositoryImpl(
    private val dao: StudyFlowDao
) : DocumentAnalyzerRepository {
    override fun getAllDocuments(): Flow<List<AnalyzedDocument>> = dao.getAllAnalyzedDocuments()
    
    override suspend fun getDocumentById(id: Int): AnalyzedDocument? = dao.getAnalyzedDocumentById(id)
    
    override suspend fun insertDocument(doc: AnalyzedDocument): Long = dao.insertAnalyzedDocument(doc)
    
    override suspend fun updateDocument(doc: AnalyzedDocument) {
        dao.updateAnalyzedDocument(doc)
    }
    
    override suspend fun deleteDocument(doc: AnalyzedDocument) {
        dao.deleteAnalyzedDocument(doc)
    }
}
