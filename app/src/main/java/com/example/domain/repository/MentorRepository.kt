package com.example.domain.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

interface MentorRepository {
    fun getMentorProfile(): Flow<MentorProfile?>
    suspend fun saveMentorProfile(profile: MentorProfile)
    
    fun getAllMentorRecommendations(): Flow<List<MentorRecommendation>>
    fun getUnreadMentorRecommendations(): Flow<List<MentorRecommendation>>
    suspend fun insertMentorRecommendation(recommendation: MentorRecommendation)
    suspend fun updateMentorRecommendation(recommendation: MentorRecommendation)
    
    fun getActiveSmartMissions(): Flow<List<SmartMission>>
    suspend fun insertSmartMission(mission: SmartMission)
    suspend fun updateSmartMission(mission: SmartMission)
    
    fun getMentorHistory(): Flow<List<MentorHistory>>
    suspend fun insertMentorHistory(history: MentorHistory)
}
