package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.DarkestBeforeDawnTheme
import com.example.ui.GameplayScreen
import com.example.ui.OptionsScreen
import com.example.ui.Screen
import com.example.ui.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DarkestBeforeDawnTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        Screen.OPTIONS -> {
                            OptionsScreen(viewModel = viewModel)
                        }
                        Screen.PLAY -> {
                            GameplayScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.setScreen(Screen.OPTIONS) }
                            )
                        }
                    }
                }
            }
        }
    }
}
