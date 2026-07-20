package com.loopline.puzzle.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.loopline.puzzle.game.DAILY_CHALLENGE_LEVEL_ID
import com.loopline.puzzle.game.DailyChallengeStore
import com.loopline.puzzle.game.GameSession
import com.loopline.puzzle.game.ModeSession
import com.loopline.puzzle.game.PlayMode
import com.loopline.puzzle.ui.screens.DifficultySelectScreen
import com.loopline.puzzle.ui.screens.GameScreen
import com.loopline.puzzle.ui.screens.HomeScreen
import com.loopline.puzzle.ui.screens.LeaderboardScreen
import com.loopline.puzzle.ui.screens.SettingsScreen
import com.loopline.puzzle.ui.screens.SplashScreen
import com.loopline.puzzle.ui.screens.StatisticsScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val DIFFICULTY_SELECT = "difficulty_select"
    const val GAME = "game/{levelId}"
    const val SETTINGS = "settings"
    const val STATISTICS = "statistics"
    const val LEADERBOARD = "leaderboard"

    fun game(levelId: Int) = "game/$levelId"
}

@Composable
fun LoopLineNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onPlayClassic = { navController.navigate(Routes.DIFFICULTY_SELECT) },
                onPlayDaily = {
                    // The Daily Challenge doesn't go through GameSession's
                    // per-difficulty flow - it's cached under its own
                    // reserved id so GameScreen can look it up the same way
                    // as any other level.
                    val level = DailyChallengeStore.todayLevel().copy(id = DAILY_CHALLENGE_LEVEL_ID)
                    GameSession.cacheExternal(level)
                    navController.navigate(Routes.game(level.id))
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenStatistics = { navController.navigate(Routes.STATISTICS) },
                onOpenLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                onPlayZen = {
                    val level = ModeSession.resume(context, PlayMode.ZEN)
                    navController.navigate(Routes.game(level.id))
                },
                onPlayTimed = {
                    val level = ModeSession.resume(context, PlayMode.TIMED)
                    navController.navigate(Routes.game(level.id))
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.DIFFICULTY_SELECT) {
            DifficultySelectScreen(
                onBack = { navController.popBackStack() },
                onDifficultySelected = { difficulty ->
                    // Resumes that difficulty's own in-progress session if
                    // it has one (including one restored from disk after a
                    // full app restart); otherwise starts fresh at level 1.
                    // Each difficulty's progress is independent, so this
                    // never disturbs the other two.
                    val level = GameSession.resume(context, difficulty)
                    navController.navigate(Routes.game(level.id))
                },
                onRestartDifficulty = { difficulty ->
                    // Explicit, confirmed action: throw away progress on
                    // this difficulty and start over at level 1.
                    val level = GameSession.restart(context, difficulty)
                    navController.navigate(Routes.game(level.id))
                }
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument("levelId") { type = NavType.IntType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
            GameScreen(
                levelId = levelId,
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToLevel = { newId ->
                    // Pops the current Game entry (whichever one it is) off
                    // before pushing the new level, leaving whatever's
                    // underneath - Difficulty Select for Classic, Home for
                    // Daily/Zen/Timed - untouched. A hardcoded
                    // popUpTo(DIFFICULTY_SELECT) here would silently do
                    // nothing for the latter three (that route isn't on
                    // their back stack), so every "Next level" tap just
                    // stacked another Game entry on top instead of
                    // replacing the current one.
                    navController.navigate(Routes.game(newId)) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                },
                onGoHome = {
                    // "Go to Home" from the Pause Menu: clear the Difficulty
                    // Select / Game entries off the back stack entirely so
                    // a subsequent system-back from Home exits the app
                    // rather than re-entering the paused level.
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
