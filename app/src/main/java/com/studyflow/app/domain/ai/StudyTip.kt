package com.studyflow.app.domain.ai

import kotlinx.serialization.Serializable

@Serializable
data class StudyTip(
    val title: String,
    val technique: String,
    val summary: String,
    val howToApply: String,
    val scientificBenefit: String,
    val icon: String = "💡"
)
