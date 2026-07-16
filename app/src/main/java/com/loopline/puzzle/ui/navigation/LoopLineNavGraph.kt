package com.loopline.puzzle.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.loopline.puzzle.ui.screens.GameScreen
import com.loopline.puzzle.ui.screens.HomeScreen
import com.loopline.puzzle.ui.screens.LevelSelectScreen
import com.loopline.puzzle.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val LEVEL_SELECT = "level_select"
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
                onPlayClassic = { navController.navigate(Routes.LEVEL_SELECT) }
            )
        }

        composable(Routes.LEVEL_SELECT) {
            LevelSelectScreen(
                onBack = { navController.popBackStack() },
                onLevelSelected = { levelId -> navController.navigate(Routes.game(levelId)) }
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
                    navController.popBackStack(Routes.LEVEL_SELECT, inclusive = false)
                },
                onNextLevel = { nextId ->
                    navController.navigate(Routes.game(nextId)) {
                        popUpTo(Routes.LEVEL_SELECT) { inclusive = false }
                    }
                },
                onNoMoreLevels = {
                    navController.popBackStack(Routes.LEVEL_SELECT, inclusive = false)
                }
            )
        }
    }
}
