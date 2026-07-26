import re

dao_methods = """
    // GAMIFICATION
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): kotlinx.coroutines.flow.Flow<UserProfile?>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): kotlinx.coroutines.flow.Flow<List<Achievement>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): kotlinx.coroutines.flow.Flow<List<Challenge>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<Challenge>)

    @Update
    suspend fun updateChallenge(challenge: Challenge)
    
    @Query("DELETE FROM challenges WHERE type = :type")
    suspend fun deleteChallengesByType(type: String)
"""

with open("app/src/main/java/com/example/data/local/StudyFlowDao.kt", "r") as f:
    content = f.read()

content = content.replace("}", dao_methods + "\n}")

with open("app/src/main/java/com/example/data/local/StudyFlowDao.kt", "w") as f:
    f.write(content)

repo_interface_methods = """
    // Gamification
    fun getUserProfile(): kotlinx.coroutines.flow.Flow<com.example.data.local.UserProfile?>
    suspend fun insertUserProfile(userProfile: com.example.data.local.UserProfile)
    suspend fun updateUserProfile(userProfile: com.example.data.local.UserProfile)
    
    fun getAllAchievements(): kotlinx.coroutines.flow.Flow<List<com.example.data.local.Achievement>>
    suspend fun insertAchievements(achievements: List<com.example.data.local.Achievement>)
    suspend fun updateAchievement(achievement: com.example.data.local.Achievement)
    
    fun getAllChallenges(): kotlinx.coroutines.flow.Flow<List<com.example.data.local.Challenge>>
    suspend fun insertChallenges(challenges: List<com.example.data.local.Challenge>)
    suspend fun updateChallenge(challenge: com.example.data.local.Challenge)
    suspend fun deleteChallengesByType(type: String)
"""

with open("app/src/main/java/com/example/domain/repository/StudyFlowRepository.kt", "r") as f:
    repo_content = f.read()
    
repo_content = repo_content.replace("}", repo_interface_methods + "\n}")
with open("app/src/main/java/com/example/domain/repository/StudyFlowRepository.kt", "w") as f:
    f.write(repo_content)

repo_impl_methods = """
    // Gamification
    override fun getUserProfile() = dao.getUserProfile()
    override suspend fun insertUserProfile(userProfile: com.example.data.local.UserProfile) = dao.insertUserProfile(userProfile)
    override suspend fun updateUserProfile(userProfile: com.example.data.local.UserProfile) = dao.updateUserProfile(userProfile)
    
    override fun getAllAchievements() = dao.getAllAchievements()
    override suspend fun insertAchievements(achievements: List<com.example.data.local.Achievement>) = dao.insertAchievements(achievements)
    override suspend fun updateAchievement(achievement: com.example.data.local.Achievement) = dao.updateAchievement(achievement)
    
    override fun getAllChallenges() = dao.getAllChallenges()
    override suspend fun insertChallenges(challenges: List<com.example.data.local.Challenge>) = dao.insertChallenges(challenges)
    override suspend fun updateChallenge(challenge: com.example.data.local.Challenge) = dao.updateChallenge(challenge)
    override suspend fun deleteChallengesByType(type: String) = dao.deleteChallengesByType(type)
"""

with open("app/src/main/java/com/example/data/repository/StudyFlowRepositoryImpl.kt", "r") as f:
    impl_content = f.read()

impl_content = impl_content.replace("}", repo_impl_methods + "\n}")
with open("app/src/main/java/com/example/data/repository/StudyFlowRepositoryImpl.kt", "w") as f:
    f.write(impl_content)
