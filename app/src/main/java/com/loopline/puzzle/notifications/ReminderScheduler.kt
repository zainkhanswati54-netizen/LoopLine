package com.loopline.puzzle.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val WORK_NAME_A = "daily_reminder_slot_a"
private const val WORK_NAME_B = "daily_reminder_slot_b"

/**
 * Schedules the "twice a day" reminder, Duolingo-style: two independent
 * 24-hour repeating jobs, one started right away and one started 12 hours
 * from now, so together they land roughly twice a day without either one
 * needing to know a wall-clock time or survive reboots itself (WorkManager
 * persists its own schedule to disk and resumes it automatically).
 *
 * [ExistingPeriodicWorkPolicy.KEEP] makes calling this safe on every app
 * launch (see MainActivity) - if both jobs are already scheduled, this is
 * a no-op instead of resetting their timers.
 */
object ReminderScheduler {

    fun scheduleIfEnabled(context: Context) {
        if (!ReminderStore.remindersEnabled(context)) {
            cancel(context)
            return
        }

        val workManager = WorkManager.getInstance(context)

        val slotA = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(0, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME_A, ExistingPeriodicWorkPolicy.KEEP, slotA)

        val slotB = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(12, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME_B, ExistingPeriodicWorkPolicy.KEEP, slotB)
    }

    fun cancel(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_NAME_A)
        workManager.cancelUniqueWork(WORK_NAME_B)
    }
}
