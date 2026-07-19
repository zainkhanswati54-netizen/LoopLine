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

    fun recordLevelCompletion(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("total_levels_completed", prefs.getInt("total_levels_completed", 0) + 1).apply()
    }

    fun totalHintsUsed(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("total_hints_used", 0)
    }

    fun recordHintUsed(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("total_hints_used", prefs.getInt("total_hints_used", 0) + 1).apply()
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

    private fun sk(difficulty: Difficulty, field: String) = "session_${field}_${difficulty.name}"

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

    fun saveSession(context: Context, difficulty: Difficulty, session: SavedSession) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(sk(difficulty, "levelNumber"), session.levelNumber)
            .putInt(sk(difficulty, "rows"), session.rows)
            .putInt(sk(difficulty, "cols"), session.cols)
            .putString(sk(difficulty, "cells"), encodeCells(session.cells))
            .putString(sk(difficulty, "start"), encodeCells(listOf(session.start)))
            .putString(sk(difficulty, "accent"), session.accentKey)
            .putString(sk(difficulty, "path"), encodeCells(session.path))
            .putInt(sk(difficulty, "elapsed"), session.elapsedSeconds)
            .putInt(sk(difficulty, "hints"), session.hintsUsed)
            .apply()
    }

    fun loadSession(context: Context, difficulty: Difficulty): SavedSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val levelNumber = prefs.getInt(sk(difficulty, "levelNumber"), 0)
        if (levelNumber <= 0) return null
        val cellsRaw = prefs.getString(sk(difficulty, "cells"), null) ?: return null
        val startRaw = prefs.getString(sk(difficulty, "start"), null) ?: return null
        val cells = decodeCells(cellsRaw).toSet()
        val start = decodeCells(startRaw).firstOrNull() ?: return null
        if (cells.isEmpty() || start !in cells) return null
        val accent = prefs.getString(sk(difficulty, "accent"), null) ?: "gold"
        val path = decodeCells(prefs.getString(sk(difficulty, "path"), "") ?: "").ifEmpty { listOf(start) }
        return SavedSession(
            levelNumber = levelNumber,
            rows = prefs.getInt(sk(difficulty, "rows"), 1),
            cols = prefs.getInt(sk(difficulty, "cols"), 1),
            cells = cells,
            start = start,
            accentKey = accent,
            path = path,
            elapsedSeconds = prefs.getInt(sk(difficulty, "elapsed"), 0),
            hintsUsed = prefs.getInt(sk(difficulty, "hints"), 0)
        )
    }

    fun clearSession(context: Context, difficulty: Difficulty) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(sk(difficulty, "levelNumber"))
            .remove(sk(difficulty, "rows"))
            .remove(sk(difficulty, "cols"))
            .remove(sk(difficulty, "cells"))
            .remove(sk(difficulty, "start"))
            .remove(sk(difficulty, "accent"))
            .remove(sk(difficulty, "path"))
            .remove(sk(difficulty, "elapsed"))
            .remove(sk(difficulty, "hints"))
            .apply()
    }
}
