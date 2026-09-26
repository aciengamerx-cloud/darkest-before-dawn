package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GameSettings(
    val id: Int = 1,
    val quality: Int = 0, // 0 = Beautiful, 1 = Average, 2 = Fastest
    val gamma: Int = 0,   // 0 = Darkest, 1 = Average, 2 = Brightest
    val highestStageReached: Int = 0
)

data class Achievement(
    val id: String,
    val name: String,
    val unlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("darkest_dawn_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<GameSettings> = _settingsFlow.asStateFlow()

    private val _achievementsFlow = MutableStateFlow(loadAchievements())
    val achievementsFlow: StateFlow<List<Achievement>> = _achievementsFlow.asStateFlow()

    private fun loadSettings(): GameSettings {
        val quality = prefs.getInt("settings_quality", 0)
        val gamma = prefs.getInt("settings_gamma", 0)
        val highest = prefs.getInt("settings_highest_stage", 0)
        return GameSettings(quality = quality, gamma = gamma, highestStageReached = highest)
    }

    private fun loadAchievements(): List<Achievement> {
        val unlockedIds = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
        val allAchievements = mutableListOf<Achievement>()

        // Reconstruct our list of standard achievements based on what's unlocked in SharedPreferences
        val achievementNames = mapOf(
            "stage_1_complete" to "Tube Escaped",
            "stage_2_elevator_1" to "Subway exit scaled",
            "stage_2_elevator_2" to "Back-alley climbed",
            "stage_3_complete" to "Outdoors Surpassed",
            "dawn_reached" to "Dawn Reached (You Survived!)"
        )

        for ((id, name) in achievementNames) {
            if (unlockedIds.contains(id)) {
                val unlockedAt = prefs.getLong("achievement_time_$id", 0L)
                allAchievements.add(Achievement(id, name, true, unlockedAt))
            }
        }
        return allAchievements
    }

    fun saveSettings(settings: GameSettings) {
        prefs.edit()
            .putInt("settings_quality", settings.quality)
            .putInt("settings_gamma", settings.gamma)
            .putInt("settings_highest_stage", settings.highestStageReached)
            .apply()
        _settingsFlow.value = settings
    }

    fun saveAchievement(id: String, name: String) {
        val currentUnlocked = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
        if (!currentUnlocked.contains(id)) {
            val updated = currentUnlocked.toMutableSet()
            updated.add(id)
            val now = System.currentTimeMillis()

            prefs.edit()
                .putStringSet("unlocked_achievements", updated)
                .putLong("achievement_time_$id", now)
                .apply()

            _achievementsFlow.value = loadAchievements()
        }
    }
}
