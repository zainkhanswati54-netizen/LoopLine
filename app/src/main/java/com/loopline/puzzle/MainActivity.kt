package com.loopline.puzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.loopline.puzzle.ui.navigation.LoopLineNavGraph
import com.loopline.puzzle.ui.theme.LoopLineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoopLineTheme {
                LoopLineNavGraph()
            }
        }
    }
}
