package com.studyflow.app.domain

import com.studyflow.app.data.local.UserProfile
import com.studyflow.app.domain.manager.GamificationManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class GamificationSessionTest {

    private lateinit var fakeRepository: FakeStudyFlowRepository
    private lateinit var gamificationManager: GamificationManager

    @Before
    fun setup() {
        fakeRepository = FakeStudyFlowRepository()
        gamificationManager = GamificationManager(fakeRepository)
    }

    @Test
    fun completeStudySession_addsXpAndUpdatesStreak() = runTest {
        val initialProfile = UserProfile(
            xp = 50,
            level = 1,
            streakDays = 1,
            maxStreakDays = 1,
            lastStudyDate = 0L,
            gamificationEnabled = true
        )
        fakeRepository.userProfileState.value = initialProfile

        // Concluir sessão de estudo dispara "SESSION_COMPLETED" (+15 XP e atualiza streak)
        gamificationManager.completeAction("SESSION_COMPLETED")

        val updated = fakeRepository.updatedUserProfile
        assertNotNull(updated)
        // Verifica que o XP foi incrementado em 15 (+15 XP)
        assertEquals(65, updated!!.xp)
        // Primeira sessão com lastStudyDate = 0 inicializa streak com 1
        assertEquals(1, updated.streakDays)
        assertTrue(updated.lastStudyDate > 0L)
    }

    @Test
    fun streakIncrements_onConsecutiveDays() = runTest {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        val profileYesterday = UserProfile(
            xp = 100,
            level = 1,
            streakDays = 3,
            maxStreakDays = 3,
            lastStudyDate = yesterday,
            gamificationEnabled = true
        )
        fakeRepository.userProfileState.value = profileYesterday

        gamificationManager.updateStreak()

        val captured = fakeRepository.updatedUserProfile
        assertNotNull(captured)
        assertEquals(4, captured!!.streakDays)
        assertEquals(4, captured.maxStreakDays)
    }

    @Test
    fun streakResets_whenDayIsSkipped() = runTest {
        val threeDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -3)
        }.timeInMillis

        val profileSkipped = UserProfile(
            xp = 100,
            level = 1,
            streakDays = 5,
            maxStreakDays = 5,
            lastStudyDate = threeDaysAgo,
            gamificationEnabled = true
        )
        fakeRepository.userProfileState.value = profileSkipped

        gamificationManager.updateStreak()

        val captured = fakeRepository.updatedUserProfile
        assertNotNull(captured)
        // Perdeu o streak consecutivo -> reseta para 1
        assertEquals(1, captured!!.streakDays)
        // Recorde mantido
        assertEquals(5, captured.maxStreakDays)
    }
}
