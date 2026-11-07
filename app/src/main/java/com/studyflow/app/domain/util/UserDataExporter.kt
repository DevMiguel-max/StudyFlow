package com.studyflow.app.domain.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.studyflow.app.data.local.StudyFlowDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserDataExporter(
    private val dao: StudyFlowDao
) {
    suspend fun exportUserDataToJson(context: Context): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val rootJson = JSONObject()
            
            // Metadados da exportação (Art. 18, V da LGPD)
            val metadata = JSONObject().apply {
                put("app", "StudyFlow")
                put("law_reference", "LGPD - Lei 13.709/2018, Artigo 18, V (Portabilidade de Dados)")
                put("export_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                put("export_timestamp", System.currentTimeMillis())
                put("format", "JSON")
            }
            rootJson.put("export_metadata", metadata)

            // 1. Perfil do Usuário
            val userProfile = dao.getUserProfile().firstOrNull()
            if (userProfile != null) {
                rootJson.put("user_profile", JSONObject().apply {
                    put("id", userProfile.id)
                    put("name", userProfile.name)
                    put("email", userProfile.email)
                    put("main_goal", userProfile.mainGoal)
                    put("level", userProfile.level)
                    put("xp", userProfile.xp)
                    put("streak_days", userProfile.streakDays)
                    put("max_streak_days", userProfile.maxStreakDays)
                    put("last_study_date", userProfile.lastStudyDate)
                    put("avatar_icon", userProfile.avatarIcon)
                    put("avatar_color", userProfile.avatarColor)
                    put("gamification_enabled", userProfile.gamificationEnabled)
                    put("sounds_enabled", userProfile.soundsEnabled)
                    put("notifications_enabled", userProfile.notificationsEnabled)
                    put("updated_at", userProfile.updatedAt)
                })
            }

            // 2. Disciplinas (Subjects)
            val subjects = dao.getAllSubjects().firstOrNull() ?: emptyList()
            val subjectsArray = JSONArray()
            subjects.forEach { s ->
                subjectsArray.put(JSONObject().apply {
                    put("id", s.id)
                    put("name", s.name)
                    put("color", s.color)
                    put("icon", s.icon)
                    put("professor", s.professor)
                    put("description", s.description)
                    put("difficulty", s.difficulty)
                    put("weekly_hours_target", s.weeklyHoursTarget)
                    put("status", s.status)
                    put("exam_date", s.examDate)
                    put("created_at", s.createdAt)
                    put("updated_at", s.updatedAt)
                })
            }
            rootJson.put("subjects", subjectsArray)

            // 3. Tarefas (Tasks)
            val tasks = dao.getAllTasks().firstOrNull() ?: emptyList()
            val tasksArray = JSONArray()
            tasks.forEach { t ->
                tasksArray.put(JSONObject().apply {
                    put("id", t.id)
                    put("subject_id", t.subjectId)
                    put("title", t.title)
                    put("description", t.description)
                    put("estimated_minutes", t.estimatedMinutes)
                    put("date", t.date)
                    put("priority", t.priority)
                    put("status", t.status)
                    put("type", t.type)
                    put("recommended_method_id", t.recommendedMethodId)
                    put("updated_at", t.updatedAt)
                })
            }
            rootJson.put("tasks", tasksArray)

            // 4. Metas de Estudo (StudyGoals)
            val studyGoals = dao.getAllStudyGoals().firstOrNull() ?: emptyList()
            val studyGoalsArray = JSONArray()
            studyGoals.forEach { g ->
                studyGoalsArray.put(JSONObject().apply {
                    put("id", g.id)
                    put("type", g.type)
                    put("title", g.title)
                    put("description", g.description)
                    put("institution", g.institution)
                    put("role", g.role)
                    put("course", g.course)
                    put("exam_board", g.examBoard)
                    put("date", g.date)
                    put("target_score", g.targetScore)
                    put("priority", g.priority)
                    put("status", g.status)
                    put("updated_at", g.updatedAt)
                })
            }
            rootJson.put("study_goals", studyGoalsArray)

            // 5. Sessões de Estudo (StudySessions)
            val sessions = dao.getAllStudySessions().firstOrNull() ?: emptyList()
            val sessionsArray = JSONArray()
            sessions.forEach { sess ->
                sessionsArray.put(JSONObject().apply {
                    put("id", sess.id)
                    put("subject_id", sess.subjectId)
                    put("duration_minutes", sess.durationMinutes)
                    put("date", sess.date)
                    put("technique_used", sess.techniqueUsed)
                    put("status", sess.status)
                    put("notes", sess.notes)
                })
            }
            rootJson.put("study_sessions", sessionsArray)

            // 6. Flashcards
            val flashcards = dao.getAllFlashcards().firstOrNull() ?: emptyList()
            val flashcardsArray = JSONArray()
            flashcards.forEach { f ->
                flashcardsArray.put(JSONObject().apply {
                    put("id", f.id)
                    put("subject_id", f.subjectId)
                    put("question", f.question)
                    put("answer", f.answer)
                    put("box", f.box)
                    put("next_review_date", f.nextReviewDate)
                })
            }
            rootJson.put("flashcards", flashcardsArray)

            // 7. Anotações (StudyNotes)
            val notes = dao.getAllStudyNotes().firstOrNull() ?: emptyList()
            val notesArray = JSONArray()
            notes.forEach { n ->
                notesArray.put(JSONObject().apply {
                    put("id", n.id)
                    put("subject_id", n.subjectId)
                    put("title", n.title)
                    put("type", n.type)
                    put("content_main", n.contentMain)
                    put("content_secondary", n.contentSecondary)
                    put("summary", n.summary)
                    put("date", n.date)
                })
            }
            rootJson.put("study_notes", notesArray)

            // 8. Mapas Mentais (MindMapNodes)
            val mindMaps = dao.getAllMindMapNodes().firstOrNull() ?: emptyList()
            val mindMapsArray = JSONArray()
            mindMaps.forEach { m ->
                mindMapsArray.put(JSONObject().apply {
                    put("id", m.id)
                    put("subject_id", m.subjectId)
                    put("text", m.text)
                    put("parent_id", m.parentId)
                    put("x_pos", m.xPos)
                    put("y_pos", m.yPos)
                })
            }
            rootJson.put("mind_map_nodes", mindMapsArray)

            // 9. Redações (EssaySubmissions & Corrections)
            val submissions = dao.getAllEssaySubmissions().firstOrNull() ?: emptyList()
            val essaysArray = JSONArray()
            submissions.forEach { sub ->
                val correction = dao.getEssayCorrectionForSubmission(sub.id)
                essaysArray.put(JSONObject().apply {
                    put("id", sub.id)
                    put("theme_id", sub.themeId)
                    put("text", sub.text)
                    put("status", sub.status)
                    put("word_count", sub.wordCount)
                    put("char_count", sub.charCount)
                    put("time_spent_seconds", sub.timeSpentSeconds)
                    put("date", sub.date)
                    put("updated_at", sub.updatedAt)
                    if (correction != null) {
                        put("correction", JSONObject().apply {
                            put("comp1", correction.comp1)
                            put("comp2", correction.comp2)
                            put("comp3", correction.comp3)
                            put("comp4", correction.comp4)
                            put("comp5", correction.comp5)
                            put("total_score", correction.totalScore)
                            put("general_comment", correction.generalComment)
                            put("strengths", correction.strengths)
                            put("weaknesses", correction.weaknesses)
                            put("suggestions", correction.suggestions)
                        })
                    }
                })
            }
            rootJson.put("essay_submissions", essaysArray)

            // 10. Simulados (Simulations)
            val simulations = dao.getAllSimulations().firstOrNull() ?: emptyList()
            val simulationsArray = JSONArray()
            simulations.forEach { sim ->
                simulationsArray.put(JSONObject().apply {
                    put("id", sim.id)
                    put("name", sim.name)
                    put("exam_type", sim.examType)
                    put("date", sim.date)
                    put("total_questions", sim.totalQuestions)
                    put("correct_answers", sim.correctAnswers)
                    put("wrong_answers", sim.wrongAnswers)
                    put("score", sim.score)
                    put("updated_at", sim.updatedAt)
                })
            }
            rootJson.put("simulations", simulationsArray)

            // 11. Conquistas & Desafios (Achievements & Challenges)
            val achievements = dao.getAllAchievements().firstOrNull() ?: emptyList()
            val achievementsArray = JSONArray()
            achievements.forEach { a ->
                achievementsArray.put(JSONObject().apply {
                    put("id", a.id)
                    put("title", a.title)
                    put("description", a.description)
                    put("tier", a.tier)
                    put("category", a.category)
                    put("progress", a.progress)
                    put("max_progress", a.maxProgress)
                    put("is_unlocked", a.isUnlocked)
                    put("unlocked_at", a.unlockedAt)
                })
            }
            rootJson.put("achievements", achievementsArray)

            val challenges = dao.getAllChallenges().firstOrNull() ?: emptyList()
            val challengesArray = JSONArray()
            challenges.forEach { c ->
                challengesArray.put(JSONObject().apply {
                    put("id", c.id)
                    put("title", c.title)
                    put("description", c.description)
                    put("type", c.type)
                    put("current_amount", c.currentAmount)
                    put("required_amount", c.requiredAmount)
                    put("xp_reward", c.xpReward)
                    put("is_completed", c.isCompleted)
                    put("expires_at", c.expiresAt)
                })
            }
            rootJson.put("challenges", challengesArray)

            // 12. Materiais Gerados (GeneratedMaterials & GeneratedQuestions)
            val materials = dao.getAllGeneratedMaterials().firstOrNull() ?: emptyList()
            val materialsArray = JSONArray()
            materials.forEach { mat ->
                materialsArray.put(JSONObject().apply {
                    put("id", mat.id)
                    put("type", mat.type)
                    put("title", mat.title)
                    put("content", mat.content)
                    put("source", mat.source)
                    put("created_at", mat.createdAt)
                })
            }
            rootJson.put("generated_materials", materialsArray)

            val questions = dao.getAllGeneratedQuestions().firstOrNull() ?: emptyList()
            val questionsArray = JSONArray()
            questions.forEach { q ->
                questionsArray.put(JSONObject().apply {
                    put("id", q.id)
                    put("type", q.type)
                    put("statement", q.statement)
                    put("options", q.options)
                    put("correct_answer", q.correctAnswer)
                    put("explanation", q.explanation)
                    put("difficulty", q.difficulty)
                    put("subject", q.subject)
                    put("topic", q.topic)
                    put("source", q.source)
                })
            }
            rootJson.put("generated_questions", questionsArray)

            // NOTA: ai_cache é expressamente omitido por se tratar de cache temporário de respostas de terceiros

            // Gravação do arquivo no diretório de cache
            val fileName = "studyflow_export_dados_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
            val exportFile = File(context.cacheDir, fileName)
            FileOutputStream(exportFile).use { fos ->
                fos.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", exportFile)
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun shareExportedFile(context: Context, uri: Uri) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Exportação de Dados Pessoais - StudyFlow")
                putExtra(Intent.EXTRA_TEXT, "Segue em anexo a exportação dos seus dados cadastrais e de estudos no StudyFlow (formato JSON - LGPD).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Exportar meus dados (LGPD)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
