package com.loopline.puzzle.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Runs on whatever twice-daily schedule [ReminderScheduler] set up. Bails
 * out quietly (still reports success, so WorkManager doesn't retry/backoff
 * a deliberate no-op) if the player turned reminders off in Settings, or if
 * notifications aren't actually permitted right now - e.g. they denied the
 * OS prompt, or disabled the app's notifications after the fact.
 */
class DailyReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val context = applicationContext
        if (!ReminderStore.remindersEnabled(context)) return Result.success()
        if (!NotificationHelper.canNotify(context)) return Result.success()

        NotificationHelper.showReminder(context)
        return Result.success()
    }
}
