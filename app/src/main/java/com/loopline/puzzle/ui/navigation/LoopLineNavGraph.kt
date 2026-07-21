package com.loopline.puzzle.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.loopline.puzzle.ui.screens.GameScreen
import com.loopline.puzzle.ui.screens.HomeScreen
import com.loopline.puzzle.ui.screens.LeaderboardScreen
import com.loopline.puzzle.ui.screens.SettingsScreen
import com.loopline.puzzle.ui.screens.SplashScreen
import com.loopline.puzzle.ui.screens.StatisticsScreen
import com.loopline.puzzle.ui.screens.StudioSplashScreen

object Routes {
    const val STUDIO_SPLASH = "studio_splash"
    const val SPLASH = "splash"
    const val HOME = "home"
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

    // A single shared transition set for the whole graph rather than one
    // default (instant cut) - forward navigation slides the new screen in
    // from the right while the old one fades+drifts left, and back
    // navigation mirrors it in reverse. Small (220ms) and fade-backed so it
    // reads as polish rather than something the player has to wait through
    // on every tap.
    val forwardEnter = slideInHorizontally(animationSpec = tween(220)) { it / 4 } + fadeIn(tween(220))
    val forwardExit = slideOutHorizontally(animationSpec = tween(220)) { -it / 4 } + fadeOut(tween(220))
    val backEnter = slideInHorizontally(animationSpec = tween(220)) { -it / 4 } + fadeIn(tween(220))
    val backExit = slideOutHorizontally(animationSpec = tween(220)) { it / 4 } + fadeOut(tween(220))

    NavHost(
        navController = navController,
        startDestination = Routes.STUDIO_SPLASH,
        enterTransition = { forwardEnter },
        exitTransition = { forwardExit },
        popEnterTransition = { backEnter },
        popExitTransition = { backExit }
    ) {
        composable(Routes.STUDIO_SPLASH) {
            StudioSplashScreen(
                onFinished = {
                    navController.navigate(Routes.SPLASH) {
                        popUpTo(Routes.STUDIO_SPLASH) { inclusive = true }
                    }
                }
            )
        }

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
                onPlayClassic = {
                    // Straight into the puzzle - no Easy/Normal/Hard picker.
                    // Resumes Classic's one in-progress session if there is
                    // one (including after a full app restart), otherwise
                    // starts fresh at Level 1. The tier (Easy/Normal/Hard)
                    // is derived automatically from the level reached -
                    // see Difficulty.forLevel - not chosen up front.
                    val level = GameSession.resume(context)
                    navController.navigate(Routes.game(level.id))
                },
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
                    // Pops the current Game entry off before pushing the
                    // next level, leaving Home underneath untouched -
                    // every auto-advance replaces this screen instead of
                    // stacking another Game entry on top of it.
                    navController.navigate(Routes.game(newId)) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                },
                onGoHome = {
                    // "Go to Home" from the Pause Menu: clear the Game entry
                    // off the back stack entirely so a subsequent
                    // system-back from Home exits the app rather than
                    // re-entering the paused level.
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
