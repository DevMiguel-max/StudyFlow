package com.studyflow.app.data.repository

import com.studyflow.app.data.local.*
import com.studyflow.app.domain.repository.MentorRepository
import kotlinx.coroutines.flow.Flow

class MentorRepositoryImpl(private val dao: StudyFlowDao) : MentorRepository {
    override fun getMentorProfile(): Flow<MentorProfile?> = dao.getMentorProfile()
    override suspend fun saveMentorProfile(profile: MentorProfile) = dao.insertMentorProfile(profile)
    
    override fun getAllMentorRecommendations(): Flow<List<MentorRecommendation>> = dao.getAllMentorRecommendations()
    override fun getUnreadMentorRecommendations(): Flow<List<MentorRecommendation>> = dao.getUnreadMentorRecommendations()
    override suspend fun insertMentorRecommendation(recommendation: MentorRecommendation) = dao.insertMentorRecommendation(recommendation)
    override suspend fun updateMentorRecommendation(recommendation: MentorRecommendation) = dao.updateMentorRecommendation(recommendation)
    
    override fun getActiveSmartMissions(): Flow<List<SmartMission>> = dao.getActiveSmartMissions()
    override suspend fun insertSmartMission(mission: SmartMission) = dao.insertSmartMission(mission)
    override suspend fun updateSmartMission(mission: SmartMission) = dao.updateSmartMission(mission)
    
    override fun getMentorHistory(): Flow<List<MentorHistory>> = dao.getMentorHistory()
    override suspend fun insertMentorHistory(history: MentorHistory) = dao.insertMentorHistory(history)
}
