package com.erkan.reflex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.erkan.reflex.ui.GameScreen
import com.erkan.reflex.ui.theme.ReflexTheme
import com.erkan.reflex.viewmodel.GameViewModel
import com.erkan.reflex.viewmodel.TwoPlayerDuelViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()
    private val duelViewModel: TwoPlayerDuelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ReflexTheme {
                GameScreen(viewModel = gameViewModel, duelViewModel = duelViewModel)
            }
        }
    }
}
