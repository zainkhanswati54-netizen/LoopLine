package com.loopline.puzzle.game

/**
 * One place to turn a Home-screen card fully on or off. Unlike the old
 * `enabled = false` pattern on [com.loopline.puzzle.ui.screens.GameMode]
 * (which still rendered a greyed-out "Coming soon" card that opened a
 * dialog on tap), flipping a flag here removes the card from the grid
 * entirely - nothing to tap, nothing half-finished visible to the player.
 *
 * Flip a flag to `true` once its feature is actually built and tested,
 * then it reappears in the Home grid on the next build with no other code
 * changes needed.
 */
object FeatureFlags {
    /** Zen mode (no timer). Not implemented yet - see ModeSession/PlayMode.ZEN. */
    const val ZEN_MODE_ENABLED = false

    /** Timed mode (beat the clock). Not implemented yet - see PlayMode.TIMED. */
    const val TIMED_MODE_ENABLED = false

    /**
     * The Leaderboard screen itself is fully functional (it's an honest
     * "your personal bests" view - see LeaderboardScreen's doc comment for
     * why it doesn't fake other players' scores). Default this to true;
     * set it to false only if you'd rather hide it until a real online
     * leaderboard backend exists.
     */
    const val LEADERBOARD_ENABLED = true
}
