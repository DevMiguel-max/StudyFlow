package com.example.domain.ai

import kotlinx.serialization.Serializable

@Serializable
data class CompetencyDetail(
    val score: Int,
    val explanation: String,
    val strengths: String,
    val weaknesses: String,
    val suggestions: String
)

@Serializable
data class DetailedError(
    val type: String, // e.g. "Gramática", "Ortografia", "Coesão"
    val snippet: String,
    val explanation: String,
    val suggestion: String
)

@Serializable
data class AICorrectionResult(
    val comp1: CompetencyDetail,
    val comp2: CompetencyDetail,
    val comp3: CompetencyDetail,
    val comp4: CompetencyDetail,
    val comp5: CompetencyDetail,
    val totalScore: Int,
    val generalComment: String,
    val essayLevel: String,
    val performanceEstimate: String,
    val detailedErrors: List<DetailedError>,
    val revisedVersion: String
)
