package com.studyflow.app.domain.ai

import com.studyflow.app.data.local.*
import com.studyflow.app.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MentorAnalysisResult(
    val recommendations: List<RecommendationDto>,
    val missions: List<MissionDto>,
    val adaptiveScheduleSuggestions: List<String>,
    val overallProgressAssessment: String
)

@Serializable
data class RecommendationDto(
    val text: String,
    val reason: String,
    val type: String
)

@Serializable
data class MissionDto(
    val title: String,
    val description: String,
    val type: String,
    val targetAmount: Int,
    val xpReward: Int
)

class MentorService {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeProgress(
        profile: MentorProfile,
        subjects: List<Subject>,
        tasks: List<Task>,
        sessions: List<StudySession>,
        goals: List<StudyGoal>,
        simulations: List<Simulation>
    ): MentorAnalysisResult {
        
        val apiKey = BuildConfig.NVIDIA_API_KEY.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY
        
        val prompt = """
            Você é um Mentor Inteligente de estudos.
            Analise os dados do estudante e forneça recomendações personalizadas e missões.
            
            Perfil do Estudante:
            - Objetivo: ${profile.mainGoal}
            - Tipo de prova: ${profile.examType}
            - Horas por dia: ${profile.hoursPerDay}
            - Disciplinas Favoritas: ${profile.favoriteSubjects}
            - Disciplinas com Dificuldade: ${profile.difficultSubjects}
            
            Dados atuais:
            - Disciplinas estudadas: ${subjects.map { it.name }}
            - Tarefas ativas: ${tasks.size}
            - Sessões de estudo recentes: ${sessions.size}
            
            Retorne APENAS um JSON válido no seguinte formato exato, sem marcações markdown:
            {
              "recommendations": [
                {
                  "text": "Ex: Você está há 5 dias sem revisar Biologia.",
                  "reason": "Ex: Revisão espaçada é essencial.",
                  "type": "Review" // Warning, Motivation, Schedule, Review
                }
              ],
              "missions": [
                {
                  "title": "Ex: Revise Informática hoje",
                  "description": "Ex: Resolva 20 questões",
                  "type": "Review", // Review, Flashcard, Question, Pomodoro
                  "targetAmount": 20,
                  "xpReward": 50
                }
              ],
              "adaptiveScheduleSuggestions": ["Sugestão 1"],
              "overallProgressAssessment": "Avaliação geral..."
            }
        """.trimIndent()

        val request = NvidiaChatRequest(
            model = "meta/llama-3.1-405b-instruct",
            messages = listOf(
                NvidiaMessage(role = "system", content = "Você é um AI Mentor para estudos."),
                NvidiaMessage(role = "user", content = prompt)
            )
        )

        return try {
            val response = NvidiaApiClient.api.generateCompletion("Bearer $apiKey", request)
            var jsonText = response.choices.firstOrNull()?.message?.content ?: "{}"
            jsonText = jsonText.replace("```json", "").replace("```", "").trim()
            
            json.decodeFromString<MentorAnalysisResult>(jsonText)
        } catch (e: java.net.UnknownHostException) {
            throw Exception("Sem conexão com a internet. Verifique sua rede.", e)
        } catch (e: retrofit2.HttpException) {
            throw Exception("Erro da API (código ${e.code()}). Verifique a chave de API configurada.", e)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
