package com.loopline.puzzle.game

import android.content.Context

/**
 * Tracks the best (highest) level number the player has reached per
 * difficulty, saved across app restarts. Deliberately minimal - just one
 * int per difficulty - so it works with plain SharedPreferences instead of
 * pulling in a database or DataStore dependency for a single stat.
 */
object ProgressStore {
    private const val PREFS_NAME = "loopline_progress"

    private fun keyFor(difficulty: Difficulty) = "best_level_${difficulty.name}"

    fun bestLevel(context: Context, difficulty: Difficulty): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(keyFor(difficulty), 0)
    }

    fun recordLevelReached(context: Context, difficulty: Difficulty, levelNumber: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(keyFor(difficulty), 0)
        if (levelNumber > current) {
            prefs.edit().putInt(keyFor(difficulty), levelNumber).apply()
        }
    }
}
