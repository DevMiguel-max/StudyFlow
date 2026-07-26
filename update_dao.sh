sed -i '/interface StudyFlowDao {/a \
    @Query("SELECT * FROM generated_materials ORDER BY createdAt DESC")\
    fun getAllGeneratedMaterials(): kotlinx.coroutines.flow.Flow<List<GeneratedMaterial>>\
\
    @Query("SELECT * FROM generated_materials WHERE type = :type ORDER BY createdAt DESC")\
    fun getGeneratedMaterialsByType(type: String): kotlinx.coroutines.flow.Flow<List<GeneratedMaterial>>\
\
    @Insert(onConflict = OnConflictStrategy.REPLACE)\
    suspend fun insertGeneratedMaterial(material: GeneratedMaterial): Long\
\
    @Delete\
    suspend fun deleteGeneratedMaterial(material: GeneratedMaterial)\
\
    @Query("SELECT * FROM generated_questions ORDER BY createdAt DESC")\
    fun getAllGeneratedQuestions(): kotlinx.coroutines.flow.Flow<List<GeneratedQuestion>>\
\
    @Insert(onConflict = OnConflictStrategy.REPLACE)\
    suspend fun insertGeneratedQuestion(question: GeneratedQuestion): Long\
\
    @Insert(onConflict = OnConflictStrategy.REPLACE)\
    suspend fun insertGeneratedQuestions(questions: List<GeneratedQuestion>)\
\
    @Update\
    suspend fun updateGeneratedQuestion(question: GeneratedQuestion)\
\
    @Delete\
    suspend fun deleteGeneratedQuestion(question: GeneratedQuestion)\
' app/src/main/java/com/example/data/local/StudyFlowDao.kt
