package com.studyflow.app.domain.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import java.util.Calendar

object WorkManagerHelper {
    private const val STUDY_REMINDER_WORK_NAME = "daily_study_reminder"

    fun scheduleDailyStudyReminder(context: Context) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18) // Scheduled for 6 PM (18:00)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val dailyWorkRequest = PeriodicWorkRequestBuilder<StudyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            STUDY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // Replace with UPDATE in newer WorkManager, but KEEP is also fine if we don't want to reschedule
            dailyWorkRequest
        )
    }
}
