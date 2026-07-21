package com.loopline.puzzle.game

import android.content.Context

/**
 * Tracks progress across app restarts. Two independent things live here:
 *
 * 1. **Best level** - the highest level number ever reached per difficulty.
 *    Deliberately minimal, one int per difficulty.
 * 2. **In-progress session** - the exact puzzle the player was mid-stroke
 *    on for each difficulty (its shape, their path so far, elapsed time,
 *    hints used). Bug this fixes: `GameSession` used to be pure in-memory
 *    state, so killing the app (not just backgrounding it) silently threw
 *    away not just the current stroke but which level the player was even
 *    on - "Continue" would quietly vanish and everything reset to Level 1.
 *    Session fields are written to SharedPreferences as plain
 *    semicolon/comma-separated strings (see [encodeCells]/[decodeCells])
 *    rather than pulling in a JSON library for a handful of ints and a cell
 *    list.
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

    // ---- Lifetime stats (Statistics screen) ----

    fun totalLevelsCompleted(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("total_levels_completed", 0)
    }

    /**
     * [difficulty] is optional so callers outside a difficulty-scoped run
     * (e.g. Daily Challenge, which doesn't use [Difficulty] at all) can
     * still bump the lifetime total without it counting toward any single
     * difficulty's per-difficulty breakdown below.
     */
    fun recordLevelCompletion(context: Context, difficulty: Difficulty? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("total_levels_completed", prefs.getInt("total_levels_completed", 0) + 1)
        if (difficulty != null) {
            val key = "levels_completed_${difficulty.name}"
            editor.putInt(key, prefs.getInt(key, 0) + 1)
        }
        editor.apply()
    }

    fun levelsCompletedFor(context: Context, difficulty: Difficulty): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("levels_completed_${difficulty.name}", 0)
    }

    fun totalHintsUsed(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("total_hints_used", 0)
    }

    fun recordHintUsed(context: Context, difficulty: Difficulty? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("total_hints_used", prefs.getInt("total_hints_used", 0) + 1)
        if (difficulty != null) {
            val key = "hints_used_${difficulty.name}"
            editor.putInt(key, prefs.getInt(key, 0) + 1)
        }
        editor.apply()
    }

    fun hintsUsedFor(context: Context, difficulty: Difficulty): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("hints_used_${difficulty.name}", 0)
    }

    // ---- Average solve time per difficulty ----
    // Stored as a running (sum, count) pair rather than a list of every
    // solve time - a full history would need unbounded storage and this
    // app's SharedPreferences-based store isn't set up for that; sum/count
    // is O(1) to update and gives an exact average.

    fun recordSolveDuration(context: Context, difficulty: Difficulty, seconds: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sumKey = "solve_seconds_sum_${difficulty.name}"
        val countKey = "solve_seconds_count_${difficulty.name}"
        prefs.edit()
            .putLong(sumKey, prefs.getLong(sumKey, 0L) + seconds)
            .putInt(countKey, prefs.getInt(countKey, 0) + 1)
            .apply()
    }

    /** Null until at least one level has been completed at this difficulty. */
    fun averageSolveSeconds(context: Context, difficulty: Difficulty): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt("solve_seconds_count_${difficulty.name}", 0)
        if (count <= 0) return null
        val sum = prefs.getLong("solve_seconds_sum_${difficulty.name}", 0L)
        return (sum / count).toInt()
    }

    /**
     * Total seconds spent actively solving levels, across every difficulty.
     * Reuses the same sum-per-difficulty values [recordSolveDuration] already
     * keeps for the average, rather than tracking a separate running total.
     */
    fun totalPlayTimeSeconds(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Difficulty.entries.sumOf { prefs.getLong("solve_seconds_sum_${it.name}", 0L) }
    }

    // ---- Personal-best solve times (Leaderboard screen) ----
    // There's no server behind this app, so a "Leaderboard" can only
    // honestly show the player's own best runs, not other people's -
    // presenting fabricated competitor scores would be misleading.

    private fun fastestKeyFor(difficulty: Difficulty) = "fastest_seconds_${difficulty.name}"

    fun fastestSeconds(context: Context, difficulty: Difficulty): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(fastestKeyFor(difficulty), -1).takeIf { it >= 0 }
    }

    fun recordSolveTime(context: Context, difficulty: Difficulty, seconds: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(fastestKeyFor(difficulty), Int.MAX_VALUE)
        if (seconds < current) {
            prefs.edit().putInt(fastestKeyFor(difficulty), seconds).apply()
        }
    }

    // ---- In-progress session persistence ----

    data class SavedSession(
        val levelNumber: Int,
        val rows: Int,
        val cols: Int,
        val cells: Set<Cell>,
        val start: Cell,
        val accentKey: String,
        val path: List<Cell>,
        val elapsedSeconds: Int,
        val hintsUsed: Int
    )

    private fun sk(key: String, field: String) = "session_${field}_${key}"

    private fun encodeCells(cells: Collection<Cell>): String =
        cells.joinToString(";") { "${it.row},${it.col}" }

    private fun decodeCells(raw: String): List<Cell> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { pair ->
            val parts = pair.split(",")
            if (parts.size != 2) return@mapNotNull null
            val row = parts[0].toIntOrNull() ?: return@mapNotNull null
            val col = parts[1].toIntOrNull() ?: return@mapNotNull null
            Cell(row, col)
        }
    }

    fun saveSession(context: Context, difficulty: Difficulty, session: SavedSession) =
        saveSessionByKey(context, difficulty.name, session)

    fun loadSession(context: Context, difficulty: Difficulty): SavedSession? =
        loadSessionByKey(context, difficulty.name)

    fun clearSession(context: Context, difficulty: Difficulty) =
        clearSessionByKey(context, difficulty.name)

    fun saveSessionByKey(context: Context, key: String, session: SavedSession) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(sk(key, "levelNumber"), session.levelNumber)
            .putInt(sk(key, "rows"), session.rows)
            .putInt(sk(key, "cols"), session.cols)
            .putString(sk(key, "cells"), encodeCells(session.cells))
            .putString(sk(key, "start"), encodeCells(listOf(session.start)))
            .putString(sk(key, "accent"), session.accentKey)
            .putString(sk(key, "path"), encodeCells(session.path))
            .putInt(sk(key, "elapsed"), session.elapsedSeconds)
            .putInt(sk(key, "hints"), session.hintsUsed)
            .apply()
    }

    fun loadSessionByKey(context: Context, key: String): SavedSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val levelNumber = prefs.getInt(sk(key, "levelNumber"), 0)
        if (levelNumber <= 0) return null
        val cellsRaw = prefs.getString(sk(key, "cells"), null) ?: return null
        val startRaw = prefs.getString(sk(key, "start"), null) ?: return null
        val cells = decodeCells(cellsRaw).toSet()
        val start = decodeCells(startRaw).firstOrNull() ?: return null
        if (cells.isEmpty() || start !in cells) return null
        val accent = prefs.getString(sk(key, "accent"), null) ?: "gold"
        val path = decodeCells(prefs.getString(sk(key, "path"), "") ?: "").ifEmpty { listOf(start) }
        return SavedSession(
            levelNumber = levelNumber,
            rows = prefs.getInt(sk(key, "rows"), 1),
            cols = prefs.getInt(sk(key, "cols"), 1),
            cells = cells,
            start = start,
            accentKey = accent,
            path = path,
            elapsedSeconds = prefs.getInt(sk(key, "elapsed"), 0),
            hintsUsed = prefs.getInt(sk(key, "hints"), 0)
        )
    }

    fun clearSessionByKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(sk(key, "levelNumber"))
            .remove(sk(key, "rows"))
            .remove(sk(key, "cols"))
            .remove(sk(key, "cells"))
            .remove(sk(key, "start"))
            .remove(sk(key, "accent"))
            .remove(sk(key, "path"))
            .remove(sk(key, "elapsed"))
            .remove(sk(key, "hints"))
            .apply()
    }
}
