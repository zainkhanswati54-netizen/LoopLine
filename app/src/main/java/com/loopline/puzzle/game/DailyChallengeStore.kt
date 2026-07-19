package com.loopline.puzzle.game

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Reserved id GameSession caches today's daily puzzle under - negative so
 * it can never collide with the positive, auto-incrementing ids handed out
 * to regular endless-mode levels. */
const val DAILY_CHALLENGE_LEVEL_ID = -1

/**
 * The Daily Challenge: one puzzle per calendar day, identical for every
 * player (seeded from the date itself, see [LevelGenerator.generateDaily]),
 * live for 24 hours before rotating to the next. Tracks whether today's has
 * been solved yet and a day-streak, all in a small SharedPreferences file -
 * no server needed since the puzzle is derived from the date rather than
 * fetched.
 *
 * Uses `java.util.Calendar` / `SimpleDateFormat` rather than `java.time`
 * on purpose - `java.time` needs API 26+ (or core library desugaring,
 * which this project doesn't have enabled), and this app's minSdk is 24.
 */
object DailyChallengeStore {
    private const val PREFS_NAME = "loopline_daily"
    private val dayFormat get() = SimpleDateFormat("yyyyMMdd", Locale.US)

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private fun keyFor(calendar: Calendar): String = dayFormat.format(calendar.time)
    private fun todayKey(): String = keyFor(Calendar.getInstance())

    /** Today's puzzle - same shape for every player until local midnight. */
    fun todayLevel(): Level = LevelGenerator.generateDaily(seed = todayKey().toLong(), accentKey = "gold")

    fun isCompletedToday(context: Context): Boolean =
        prefs(context).getString("last_completed_day", null) == todayKey()

    /** Best (lowest) solve time recorded for *today's* puzzle, if solved. */
    fun bestTimeSecondsToday(context: Context): Int? {
        if (!isCompletedToday(context)) return null
        return prefs(context).getInt("today_best_seconds", -1).takeIf { it >= 0 }
    }

    fun currentStreak(context: Context): Int = prefs(context).getInt("streak", 0)
    fun bestStreak(context: Context): Int = prefs(context).getInt("best_streak", 0)

    /**
     * Records a solve of today's puzzle. Solving it again the same day just
     * keeps the better time. The streak extends by one if yesterday was
     * also completed, otherwise it restarts at 1 - a day skipped breaks it,
     * same as most daily-challenge games.
     */
    fun recordCompletion(context: Context, elapsedSeconds: Int) {
        val p = prefs(context)
        val today = todayKey()
        val lastCompleted = p.getString("last_completed_day", null)

        if (lastCompleted == today) {
            val currentBest = p.getInt("today_best_seconds", Int.MAX_VALUE)
            if (elapsedSeconds < currentBest) p.edit().putInt("today_best_seconds", elapsedSeconds).apply()
            return
        }

        val yesterday = keyFor(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) })
        val newStreak = if (lastCompleted == yesterday) p.getInt("streak", 0) + 1 else 1
        p.edit()
            .putString("last_completed_day", today)
            .putInt("today_best_seconds", elapsedSeconds)
            .putInt("streak", newStreak)
            .putInt("best_streak", maxOf(newStreak, p.getInt("best_streak", 0)))
            .apply()
    }

    /** Seconds remaining until this local day rolls over - drives the live
     * "Resets in HH:MM:SS" countdown on the Home banner. */
    fun secondsUntilReset(): Long {
        val now = Calendar.getInstance()
        val midnight = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return ((midnight.timeInMillis - now.timeInMillis) / 1000).coerceAtLeast(0)
    }

    /**
     * The last 7 days (oldest first) as (single-letter weekday label,
     * completed) - powers the small calendar strip on the Home banner.
     * Derived from [lastCompleted]/[streak] alone (no per-day log is kept):
     * the `streak` days ending on `lastCompleted` count as done, everything
     * else in the window doesn't.
     */
    fun last7Days(context: Context): List<Pair<String, Boolean>> {
        val p = prefs(context)
        val lastCompleted = p.getString("last_completed_day", null)
        val streak = p.getInt("streak", 0)

        val streakDays: Set<String> = if (lastCompleted != null && streak > 0) {
            val anchor = dayFormat.parse(lastCompleted)
            val anchorCal = Calendar.getInstance().apply { if (anchor != null) time = anchor }
            (0 until streak).map { i ->
                val c = anchorCal.clone() as Calendar
                c.add(Calendar.DAY_OF_YEAR, -i)
                keyFor(c)
            }.toSet()
        } else {
            emptySet()
        }

        val labelFormat = SimpleDateFormat("EEEEE", Locale.US) // single-letter weekday initial
        return (6 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
            val key = keyFor(cal)
            labelFormat.format(cal.time) to (key in streakDays)
        }
    }
}
