package com.loopline.puzzle.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.loopline.puzzle.game.GameSession
import com.loopline.puzzle.ui.screens.DifficultySelectScreen
import com.loopline.puzzle.ui.screens.GameScreen
import com.loopline.puzzle.ui.screens.HomeScreen
import com.loopline.puzzle.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val DIFFICULTY_SELECT = "difficulty_select"
    const val GAME = "game/{levelId}"

    fun game(levelId: Int) = "game/$levelId"
}

@Composable
fun LoopLineNavGraph() {
    val navController = rememberNavController()

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
                onPlayClassic = { navController.navigate(Routes.DIFFICULTY_SELECT) }
            )
        }

        composable(Routes.DIFFICULTY_SELECT) {
            DifficultySelectScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    val level = GameSession.currentLevel
                    if (level != null) {
                        navController.navigate(Routes.game(level.id))
                    }
                },
                onDifficultySelected = { difficulty ->
                    // An explicit tap on a difficulty always starts a fresh
                    // session at level 1, even if one was already active -
                    // "Continue" above is the path that resumes progress.
                    val level = GameSession.forceRestart(difficulty)
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
                    navController.popBackStack(Routes.DIFFICULTY_SELECT, inclusive = false)
                },
                onNavigateToLevel = { newId ->
                    navController.navigate(Routes.game(newId)) {
                        popUpTo(Routes.DIFFICULTY_SELECT) { inclusive = false }
                    }
                }
            )
        }
    }
}
