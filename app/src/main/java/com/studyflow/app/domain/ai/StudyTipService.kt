package com.studyflow.app.domain.ai

import android.util.Log
import kotlinx.serialization.json.Json
import java.util.Calendar

class StudyTipService(
    private val geminiClient: GeminiClient = GeminiClient()
) {
    companion object {
        private const val TAG = "StudyTipService"
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private var cachedTip: StudyTip? = null
    private var lastCachedDayOfYear: Int = -1

    private val fallbackTips = listOf(
        StudyTip(
            title = "Fixe o conteúdo antes de esquecer",
            technique = "Active Recall (Recordação Ativa)",
            summary = "Forçar ativamente o cérebro a recuperar a informação da memória consolida conexões neurais muito mais do que a releitura passiva.",
            howToApply = "Após ler um tópico, feche o livro e escreva em uma folha tudo o que conseguir lembrar. Depois confira o que faltou.",
            scientificBenefit = "Aumenta a retenção de longo prazo em até 50% segundo o Efeito de Testagem (Roediger & Karpicke).",
            icon = "🧠"
        ),
        StudyTip(
            title = "Foco Total sem Esgotamento",
            technique = "Técnica Pomodoro (25/5 ou 50/10)",
            summary = "Blocos de foco ininterrupto intercalados com pausas restauradoras preservam a clareza mental e eliminam a procrastinação.",
            howToApply = "Defina um alarme de 25 min para uma única tarefa. Quando tocar, pause 5 min longe de celular e telas antes do próximo ciclo.",
            scientificBenefit = "Pausas curtas recarregam os níveis de glicose no córtex pré-frontal e evitam a fadiga de decisão.",
            icon = "🍅"
        ),
        StudyTip(
            title = "Aprenda Ensinando em Termos Simples",
            technique = "Técnica de Feynman",
            summary = "Se você não consegue explicar um conceito em termos simples, você ainda não o compreendeu de verdade.",
            howToApply = "Explique a matéria em voz alta como se ensinasse para uma criança de 10 anos. Onde você hesitar ou travar, volte e revise.",
            scientificBenefit = "Elimina a 'ilusão de competência' e força a estruturação lógica profunda do conteúdo na memória.",
            icon = "🎯"
        ),
        StudyTip(
            title = "Vença a Curva do Esquecimento",
            technique = "Repetição Espaçada (Spaced Repetition)",
            summary = "Revisar os conteúdos em intervalos estratégicos no tempo reseta o declínio natural da curva da memória.",
            howToApply = "Revise o conteúdo de hoje após 24 horas, depois de 3 dias, 1 semana e 1 mês usando flashcards ou questões.",
            scientificBenefit = "Estimula a potenciação de longa duração (LTP) nas sinapses, transformando memória efêmera em conhecimento permanente.",
            icon = "⏳"
        ),
        StudyTip(
            title = "Varie os Tópicos para Fixação Real",
            technique = "Interleaving (Estudo Intercalado)",
            summary = "Alternar matérias ou tipos de problemas na mesma sessão treina o cérebro a discernir e escolher estratégias corretas.",
            howToApply = "Em vez de 4 horas seguidas de uma única disciplina, faça blocos de 50 min alternando matérias de raciocínio e leitura.",
            scientificBenefit = "Desenvolve flexibilidade cognitiva e prepara o cérebro para provas onde as matérias aparecem misturadas.",
            icon = "⚡"
        ),
        StudyTip(
            title = "Despejo Rápido de Memória",
            technique = "Método Blurting",
            summary = "Descarregar todo o conhecimento em um papel sob pressão de tempo revela instantaneamente seus pontos cegos.",
            howToApply = "Cronometre 5 minutos e anote todas as fórmulas, conceitos e dados do tópico sem olhar. Depois corrija com caneta colorida.",
            scientificBenefit = "Ativa o sistema de alerta da memória e acelera a identificação de lacunas de compreensão.",
            icon = "📝"
        )
    )

    suspend fun getDailyTip(forceRefresh: Boolean = false): StudyTip {
        val currentDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (!forceRefresh && cachedTip != null && lastCachedDayOfYear == currentDayOfYear) {
            return cachedTip!!
        }

        return try {
            val prompt = """
                Você é um especialista em neurociência da aprendizagem e técnicas de estudo de alto rendimento.
                Gere uma 'Dica do Dia' prática, motivadora e moderna sobre técnicas de estudo consagradas (ex: Pomodoro, Active Recall / Recordação Ativa, Método Feynman, Repetição Espaçada, Interleaving, Método Blurting ou Técnica Cornell).
                
                Retorne APENAS um objeto JSON válido (sem marcadores markdown de bloco de código) com o seguinte formato exato:
                {
                  "title": "Título chamativo e motivador",
                  "technique": "Nome da técnica de estudo",
                  "summary": "Resumo explicativo em 1 a 2 frases claras e práticas",
                  "howToApply": "Instrução de como aplicar nos estudos de hoje em 2 a 3 passos",
                  "scientificBenefit": "Benefício cognitivo ou científico comprovado",
                  "icon": "Emoji condizente com a técnica (ex: 🍅, 🧠, 🎯, ⏳, ⚡, 📝, 📖)"
                }
            """.trimIndent()

            val rawResponse = geminiClient.generateText(
                prompt = prompt,
                systemInstruction = "Você é um assistente de estudos que responde exclusivamente em formato JSON.",
                temperature = 0.7f,
                maxOutputTokens = 600,
                modelName = "gemini-3.5-flash"
            )

            val cleaned = cleanJson(rawResponse)
            val parsedTip = json.decodeFromString<StudyTip>(cleaned)
            
            cachedTip = parsedTip
            lastCachedDayOfYear = currentDayOfYear
            parsedTip
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao consultar Gemini para dica do dia. Usando dica curada de fallback.", e)
            val fallbackIndex = (currentDayOfYear + if (forceRefresh) (1..5).random() else 0) % fallbackTips.size
            val fallback = fallbackTips[fallbackIndex.coerceIn(0, fallbackTips.lastIndex)]
            cachedTip = fallback
            lastCachedDayOfYear = currentDayOfYear
            fallback
        }
    }

    private fun cleanJson(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }
        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        return if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            text.substring(firstBrace, lastBrace + 1)
        } else {
            text
        }
    }
}
