package com.loopline.puzzle.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.loopline.puzzle.MainActivity
import com.loopline.puzzle.R
import com.loopline.puzzle.game.DailyChallengeStore

private const val CHANNEL_ID = "daily_reminders"
private const val NOTIFICATION_ID = 1001

/** Rotating copy so the reminder doesn't read as the exact same robotic
 * line twice a day, every day - the actual "you have a streak going" line
 * further down still takes priority over these whenever it applies. */
private val genericNudges = listOf(
    "A quick LoopLine puzzle is waiting for you.",
    "Got a minute? One LoopLine level won't take long.",
    "Your next LoopLine puzzle is ready whenever you are.",
    "Time for a quick loop? Come untangle today's puzzle."
)

object NotificationHelper {

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Twice-daily nudges to keep your LoopLine streak alive."
        }
        manager.createNotificationChannel(channel)
    }

    /** True only when the app is actually allowed to post a notification
     * right now - callers should skip building/showing one otherwise
     * rather than let it silently no-op deep inside NotificationManager. */
    fun canNotify(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun showReminder(context: Context) {
        ensureChannel(context)

        val streak = DailyChallengeStore.currentStreak(context)
        val completedToday = DailyChallengeStore.isCompletedToday(context)

        val (title, body) = when {
            streak > 0 && !completedToday ->
                "Don't lose your $streak-day streak!" to
                    "You haven't played today's Daily Challenge yet - keep the streak going."
            else ->
                "LoopLine" to genericNudges.random()
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
