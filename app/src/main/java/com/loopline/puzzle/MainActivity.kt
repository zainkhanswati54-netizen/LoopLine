package com.loopline.puzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.loopline.puzzle.game.GameSession
import com.loopline.puzzle.ui.navigation.LoopLineNavGraph
import com.loopline.puzzle.ui.theme.LoopLineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Rebuilds any in-progress sessions from disk before anything else
        // reads GameSession's state, so "Continue · Level N" is accurate
        // even on the very first frame after a full app restart.
        GameSession.hydrate(applicationContext)
        setContent {
            LoopLineTheme {
                LoopLineNavGraph()
            }
        }
    }
}
