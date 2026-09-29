package com.studyflow.app.domain.ai

import com.studyflow.app.data.local.*
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

class MentorService(
    private val geminiClient: GeminiClient = GeminiClient()
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun analyzeProgress(
        profile: MentorProfile,
        subjects: List<Subject>,
        tasks: List<Task>,
        sessions: List<StudySession>,
        goals: List<StudyGoal>,
        simulations: List<Simulation>
    ): MentorAnalysisResult {
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

        return try {
            val responseText = geminiClient.generateText(
                prompt = prompt,
                systemInstruction = "Você é um AI Mentor para estudos."
            )
            var cleanJson = responseText.replace("```json", "").replace("```", "").trim()
            val startIndex = cleanJson.indexOf("{")
            val endIndex = cleanJson.lastIndexOf("}")
            if (startIndex != -1 && endIndex != -1) {
                cleanJson = cleanJson.substring(startIndex, endIndex + 1)
            }
            
            json.decodeFromString<MentorAnalysisResult>(cleanJson)
        } catch (e: java.net.UnknownHostException) {
            throw Exception("Sem conexão com a internet. Verifique sua rede.", e)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
