package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Achievement
import com.example.data.GamePreferences
import com.example.data.GameSettings
import com.example.game.GameEngine
import com.example.game.PlayerState
import com.example.game.Stage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class Screen {
    OPTIONS, PLAY
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = GamePreferences(application)

    val gameEngine = GameEngine(prefs, application)

    private val _currentScreen = MutableStateFlow(Screen.OPTIONS)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private var gameLoopJob: Job? = null

    init {
        // Load settings and achievements from preferences
        viewModelScope.launch {
            prefs.settingsFlow.collectLatest { current ->
                _settings.value = current
                gameEngine.applySettings(current.quality, current.gamma)
            }
        }

        viewModelScope.launch {
            prefs.achievementsFlow.collectLatest { list ->
                _achievements.value = list
            }
        }
    }

    fun setQuality(quality: Int) {
        viewModelScope.launch {
            val updated = _settings.value.copy(quality = quality)
            prefs.saveSettings(updated)
            gameEngine.applySettings(quality, updated.gamma)
        }
    }

    fun setGamma(gamma: Int) {
        viewModelScope.launch {
            val updated = _settings.value.copy(gamma = gamma)
            prefs.saveSettings(updated)
            gameEngine.applySettings(updated.quality, gamma)
        }
    }

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
        if (screen == Screen.PLAY) {
            startGameLoop()
        } else {
            stopGameLoop()
        }
    }

    fun restartGame() {
        gameEngine.resetGame()
        startGameLoop()
    }

    fun selectStage(stage: Stage) {
        gameEngine.changeStage(stage)
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (true) {
                val now = System.currentTimeMillis()
                val deltaTime = (now - lastTime) / 1000f
                lastTime = now

                // Cap deltaTime to prevent massive jumps when the app loses focus
                val clampedDelta = if (deltaTime > 0.1f) 0.1f else deltaTime
                gameEngine.update(clampedDelta)

                delay(16) // Target ~60fps
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
