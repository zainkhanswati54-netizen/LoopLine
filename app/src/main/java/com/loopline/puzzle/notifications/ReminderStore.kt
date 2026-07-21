package com.loopline.puzzle.notifications

import android.content.Context

/**
 * The single on/off preference for the twice-daily "come back and play"
 * reminders, surfaced as a toggle on the Settings screen (same pattern as
 * [com.loopline.puzzle.game.SettingsStore]'s sound/vibration switches).
 * Kept in its own tiny file rather than folded into SettingsStore so the
 * whole reminders feature - preference, scheduling, and the worker that
 * fires it - lives in one package.
 */
object ReminderStore {
    private const val PREFS_NAME = "loopline_reminders"
    private const val KEY_ENABLED = "reminders_enabled"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Reminders default to on - most players who'd be annoyed by them will
     * simply never grant the notification permission in the first place
     * (see MainActivity), so this only reaches people who allowed it. */
    fun remindersEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, true)

    fun setRemindersEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}
