package com.studyflow.app.domain.util

import com.studyflow.app.data.local.Flashcard

object LeitnerScheduler {
    const val ONE_DAY_MILLIS = 1L * 24 * 60 * 60 * 1000       // 86.400.000 ms
    const val THREE_DAYS_MILLIS = 3L * 24 * 60 * 60 * 1000    // 259.200.000 ms
    const val SEVEN_DAYS_MILLIS = 7L * 24 * 60 * 60 * 1000    // 604.800.000 ms

    /**
     * Aplica o algoritmo de repetição espaçada de Leitner com 3 caixas:
     * - Caixa 1: intervalo de 1 dia
     * - Caixa 2: intervalo de 3 dias
     * - Caixa 3: intervalo de 7 dias
     *
     * Regras:
     * - Acertou (correct = true):
     *     - Caixa 1 -> sobe para Caixa 2 e agenda para nowMillis + 3 dias
     *     - Caixa 2 -> sobe para Caixa 3 e agenda para nowMillis + 7 dias
     *     - Caixa 3 -> permanece na Caixa 3 e agenda para nowMillis + 7 dias
     * - Errou (correct = false):
     *     - Qualquer caixa -> volta para Caixa 1 e agenda para nowMillis + 1 dia
     *
     * Não utiliza chamadas a System.currentTimeMillis() internamente; todas as
     * datas são derivadas exclusivamente do parâmetro nowMillis.
     */
    fun next(card: Flashcard, correct: Boolean, nowMillis: Long): Flashcard {
        return if (correct) {
            when (card.box) {
                1 -> card.copy(
                    box = 2,
                    nextReviewDate = nowMillis + THREE_DAYS_MILLIS
                )
                2 -> card.copy(
                    box = 3,
                    nextReviewDate = nowMillis + SEVEN_DAYS_MILLIS
                )
                else -> card.copy(
                    box = 3,
                    nextReviewDate = nowMillis + SEVEN_DAYS_MILLIS
                )
            }
        } else {
            card.copy(
                box = 1,
                nextReviewDate = nowMillis + ONE_DAY_MILLIS
            )
        }
    }

    /**
     * Verifica se um cartão está vencido para revisão no momento especificado.
     */
    fun isDue(card: Flashcard, nowMillis: Long): Boolean {
        return card.nextReviewDate <= nowMillis
    }
}
