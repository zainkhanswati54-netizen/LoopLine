package com.loopline.puzzle.game

import android.content.Context

/**
 * User-facing preferences (sound, vibration) plus the "reset everything"
 * action for the Settings screen. Kept separate from [ProgressStore]/
 * [DailyChallengeStore] since those are gameplay records, not preferences -
 * but [resetAllProgress] knows about all three prefs files so a reset is
 * actually complete.
 */
object SettingsStore {
    private const val PREFS_NAME = "loopline_settings"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun soundEnabled(context: Context): Boolean = prefs(context).getBoolean("sound_enabled", true)
    fun setSoundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun vibrationEnabled(context: Context): Boolean = prefs(context).getBoolean("vibration_enabled", true)
    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("vibration_enabled", enabled).apply()
    }

    /** Wipes best levels, in-progress sessions, lifetime stats, and the
     * Daily Challenge streak. Does not touch [soundEnabled]/[vibrationEnabled]
     * themselves - those are preferences, not progress. */
    fun resetAllProgress(context: Context) {
        context.getSharedPreferences("loopline_progress", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("loopline_daily", Context.MODE_PRIVATE).edit().clear().apply()
    }
}
