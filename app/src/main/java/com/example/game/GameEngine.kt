package com.example.game

import android.content.Context
import android.os.Vibrator
import androidx.compose.ui.geometry.Offset
import com.example.data.Achievement
import com.example.data.GamePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

enum class Stage(val displayName: String, val index: Int) {
    TUBE("The Tube", 0),
    STREET("The Street", 1),
    OUTDOORS("The Outdoors", 2),
    ABOVE("The Above", 3)
}

data class LightSource(
    val position: Offset,
    val radius: Float = 120f
)

data class Elevator(
    val id: String,
    val x: Float,
    val groundY: Float,
    val topY: Float,
    var currentY: Float,
    var state: ElevatorState = ElevatorState.IDLE, // IDLE, GOING_UP, GOING_DOWN
    val achievementId: String = ""
)

enum class ElevatorState {
    IDLE, GOING_UP, GOING_DOWN
}

data class Harpy(
    var id: Int,
    var position: Offset,
    var patrolLeft: Float,
    var patrolRight: Float,
    var direction: Float = 1f, // -1 or 1
    var isPursuing: Boolean = false,
    var isRunningAway: Boolean = false,
    var isDead: Boolean = false
)

data class PlayerState(
    var position: Offset = Offset(100f, 450f), // X, Y (Ground is usually at Y=450)
    var life: Float = 100f,
    val maxLife: Float = 100f,
    var isDead: Boolean = false,
    var fadeOutIntensity: Float = 0f,
    var goingUp: Boolean = false,
    var stage: Stage = Stage.TUBE,
    var gameWin: Boolean = false
)

class GameEngine(
    private val gamePreferences: GamePreferences,
    context: Context
) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val random = Random()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _elevators = MutableStateFlow<List<Elevator>>(emptyList())
    val elevators: StateFlow<List<Elevator>> = _elevators.asStateFlow()

    private val _lights = MutableStateFlow<List<LightSource>>(emptyList())
    val lights: StateFlow<List<LightSource>> = _lights.asStateFlow()

    private val _harpies = MutableStateFlow<List<Harpy>>(emptyList())
    val harpies: StateFlow<List<Harpy>> = _harpies.asStateFlow()

    // Configuration values based on settings
    private var gammaFactor: Float = 1.0f // modified by setting: Darkest=1.0, Average=1.3, Brightest=1.6
    private var damageMultiplier: Float = 1.0f // Beautiful=1.0, Average=1.3, Fastest=1.6 (or similar)

    init {
        resetGame()
    }

    fun applySettings(quality: Int, gamma: Int) {
        gammaFactor = when (gamma) {
            0 -> 1.0f // Darkest
            1 -> 1.3f // Average
            2 -> 1.6f // Brightest
            else -> 1.0f
        }
        damageMultiplier = when (quality) {
            0 -> 1.0f // Beautiful
            1 -> 1.3f // Average
            2 -> 1.6f // Fastest
            else -> 1.0f
        }
    }

    fun resetGame() {
        val currentStage = _playerState.value.stage
        _playerState.value = PlayerState(
            position = Offset(100f, 450f),
            life = 100f,
            isDead = false,
            fadeOutIntensity = 0f,
            goingUp = false,
            stage = currentStage,
            gameWin = false
        )
        loadStage(currentStage)
    }

    fun changeStage(stage: Stage) {
        _playerState.value = _playerState.value.copy(
            stage = stage,
            position = Offset(100f, 450f),
            goingUp = false
        )
        loadStage(stage)
    }

    private fun loadStage(stage: Stage) {
        // Define Lights for each stage
        val newLights = when (stage) {
            Stage.TUBE -> listOf(
                LightSource(Offset(150f, 450f)),
                LightSource(Offset(450f, 450f)),
                LightSource(Offset(750f, 450f))
            )
            Stage.STREET -> listOf(
                LightSource(Offset(100f, 450f)),
                LightSource(Offset(350f, 450f)),
                LightSource(Offset(600f, 450f)),
                LightSource(Offset(850f, 450f))
            )
            Stage.OUTDOORS -> listOf(
                LightSource(Offset(100f, 450f)),
                LightSource(Offset(500f, 450f)),
                LightSource(Offset(900f, 450f))
            )
            Stage.ABOVE -> listOf(
                LightSource(Offset(100f, 450f)),
                LightSource(Offset(300f, 350f)),
                LightSource(Offset(550f, 250f)),
                LightSource(Offset(800f, 150f)),
                LightSource(Offset(950f, 100f)) // Morning sun beacon
            )
        }
        _lights.value = newLights

        // Define Elevators for each stage
        val newElevators = when (stage) {
            Stage.TUBE -> listOf(
                Elevator("tube_elevator_1", 900f, 450f, 150f, 450f, achievementId = "stage_1_complete")
            )
            Stage.STREET -> listOf(
                Elevator("street_elevator_1", 400f, 450f, 150f, 450f, achievementId = "stage_2_elevator_1"),
                Elevator("street_elevator_2", 900f, 450f, 150f, 450f, achievementId = "stage_2_elevator_2")
            )
            Stage.OUTDOORS -> listOf(
                Elevator("outdoors_elevator_1", 900f, 450f, 150f, 450f, achievementId = "stage_3_complete")
            )
            Stage.ABOVE -> listOf(
                Elevator("above_elevator_1", 250f, 450f, 350f, 450f, achievementId = "stage_4_elevator_1"),
                Elevator("above_elevator_2", 500f, 350f, 250f, 350f),
                Elevator("above_elevator_3", 750f, 250f, 150f, 250f),
                Elevator("above_elevator_4", 900f, 150f, 100f, 150f)
            )
        }
        _elevators.value = newElevators

        // Define Harpies for each stage
        val newHarpies = when (stage) {
            Stage.TUBE -> listOf(
                Harpy(1, Offset(300f, 430f), 200f, 400f),
                Harpy(2, Offset(600f, 430f), 500f, 700f)
            )
            Stage.STREET -> listOf(
                Harpy(3, Offset(200f, 430f), 100f, 300f),
                Harpy(4, Offset(500f, 430f), 450f, 580f),
                Harpy(5, Offset(800f, 430f), 700f, 900f)
            )
            Stage.OUTDOORS -> listOf(
                Harpy(6, Offset(300f, 430f), 200f, 500f),
                Harpy(7, Offset(700f, 430f), 600f, 850f)
            )
            Stage.ABOVE -> listOf(
                Harpy(8, Offset(200f, 430f), 150f, 280f),
                Harpy(9, Offset(400f, 330f), 350f, 480f),
                Harpy(10, Offset(650f, 230f), 600f, 720f),
                Harpy(11, Offset(850f, 130f), 800f, 880f)
            )
        }
        _harpies.value = newHarpies
    }

    fun movePlayer(direction: Float) {
        val state = _playerState.value
        if (state.isDead || state.gameWin || state.goingUp) return

        val speed = 8f
        var newX = state.position.x + direction * speed
        newX = max(20f, min(980f, newX))

        _playerState.value = state.copy(
            position = Offset(newX, state.position.y)
        )
    }

    suspend fun update(deltaTime: Float) {
        val state = _playerState.value
        if (state.isDead || state.gameWin) return

        // 1. Process nearest light source and apply health regeneration/damage and visual fading
        var nearestLightDist = Float.MAX_VALUE
        for (light in _lights.value) {
            val dist = sqrt(
                (light.position.x - state.position.x) * (light.position.x - state.position.x) +
                (light.position.y - state.position.y) * (light.position.y - state.position.y)
            )
            if (dist < nearestLightDist) {
                nearestLightDist = dist
            }
        }

        // We scale the security & danger thresholds according to gamma settings
        val securityDistance = 60f * gammaFactor
        val dangerDistance = 140f * gammaFactor

        var currentLife = state.life
        var fadeOutIntensity = 0f

        if (nearestLightDist <= securityDistance) {
            // Safe in light. Heal!
            currentLife = min(state.maxLife, currentLife + deltaTime * 1.8f)
            fadeOutIntensity = 0f
        } else {
            // In the darkness. Compute fade out intensity.
            val rawFactor = (nearestLightDist - securityDistance) / (dangerDistance - securityDistance)
            fadeOutIntensity = max(0f, min(1.0f, rawFactor))

            // Drain life depending on fade out intensity
            val baseDrain = 5f * damageMultiplier
            currentLife = max(0f, currentLife - baseDrain * fadeOutIntensity * deltaTime)
        }

        // 2. Elevator physics and transitions
        var currentlyGoingUp = false
        val currentElevators = _elevators.value.map { elevator ->
            val playerOnElevator = !state.goingUp &&
                    kotlin.math.abs(state.position.x - elevator.x) < 30f &&
                    kotlin.math.abs(state.position.y - elevator.groundY) < 15f

            var newY = elevator.currentY
            var newState = elevator.state

            if (playerOnElevator && elevator.state == ElevatorState.IDLE) {
                // Activate elevator!
                newState = ElevatorState.GOING_UP
                vibrator?.vibrate(100)
            }

            if (newState == ElevatorState.GOING_UP) {
                newY -= 3f // Move upwards
                if (newY <= elevator.topY) {
                    newY = elevator.topY
                    newState = ElevatorState.IDLE

                    // If the player completed this stage, handle transitions!
                    handleElevatorReachedTop(elevator)
                } else {
                    currentlyGoingUp = true
                    // Lock player position to elevator
                    _playerState.value = _playerState.value.copy(
                        position = Offset(elevator.x, newY)
                    )
                }
            }
            elevator.copy(currentY = newY, state = newState)
        }
        _elevators.value = currentElevators

        // 3. Harpies AI
        val currentHarpies = _harpies.value.map { harpy ->
            if (harpy.isDead) return@map harpy

            val distToPlayer = sqrt(
                (harpy.position.x - state.position.x) * (harpy.position.x - state.position.x) +
                (harpy.position.y - state.position.y) * (harpy.position.y - state.position.y)
            )

            var isPursuing = false
            var isRunningAway = false
            var newPos = harpy.position
            var direction = harpy.direction

            // If player is in the light, Harpy gets scared and flees. Otherwise, it pursues.
            val isPlayerInLight = nearestLightDist <= securityDistance

            if (isPlayerInLight) {
                // Flee from player!
                isRunningAway = true
                direction = if (state.position.x < harpy.position.x) 1f else -1f
                newPos = Offset(
                    max(50f, min(950f, harpy.position.x + direction * 4f)),
                    harpy.position.y
                )
            } else if (distToPlayer < 250f && !state.goingUp) {
                // Pursue!
                isPursuing = true
                direction = if (state.position.x > harpy.position.x) 1f else -1f
                newPos = Offset(
                    harpy.position.x + direction * 3f,
                    harpy.position.y + (state.position.y - 20f - harpy.position.y) * 0.05f
                )

                // Damage player on contact
                if (distToPlayer < 35f) {
                    currentLife = max(0f, currentLife - deltaTime * 12f)
                    vibrator?.vibrate(50)
                }
            } else {
                // Patrol
                var x = harpy.position.x + direction * 1.5f
                if (x >= harpy.patrolRight) {
                    x = harpy.patrolRight
                    direction = -1f
                } else if (x <= harpy.patrolLeft) {
                    x = harpy.patrolLeft
                    direction = 1f
                }
                // Hover animation
                val hoverOffset = kotlin.math.sin(System.currentTimeMillis() / 150.0).toFloat() * 0.5f
                newPos = Offset(x, harpy.position.y + hoverOffset)
            }

            harpy.copy(
                position = newPos,
                direction = direction,
                isPursuing = isPursuing,
                isRunningAway = isRunningAway
            )
        }
        _harpies.value = currentHarpies

        // Check death
        val isDead = currentLife <= 0f

        _playerState.value = _playerState.value.copy(
            life = currentLife,
            fadeOutIntensity = fadeOutIntensity,
            goingUp = currentlyGoingUp,
            isDead = isDead
        )
    }

    private suspend fun handleElevatorReachedTop(elevator: Elevator) {
        val currentState = _playerState.value

        // Register achievements
        if (elevator.achievementId.isNotEmpty()) {
            val name = when (elevator.achievementId) {
                "stage_1_complete" -> "Tube Escaped"
                "stage_2_elevator_1" -> "Subway exit scaled"
                "stage_2_elevator_2" -> "Back-alley climbed"
                "stage_3_complete" -> "Outdoors Surpassed"
                else -> "Elevator Ascended"
            }
            gamePreferences.saveAchievement(elevator.achievementId, name)
        }

        // Determine next stage
        when (currentState.stage) {
            Stage.TUBE -> {
                changeStage(Stage.STREET)
            }
            Stage.STREET -> {
                changeStage(Stage.OUTDOORS)
            }
            Stage.OUTDOORS -> {
                changeStage(Stage.ABOVE)
            }
            Stage.ABOVE -> {
                if (elevator.id == "above_elevator_4") {
                    // REACHED THE VERY DAWN SUMMIT! PLAYER WINS!
                    _playerState.value = currentState.copy(
                        gameWin = true,
                        goingUp = false
                    )
                    gamePreferences.saveAchievement("dawn_reached", "Dawn Reached (You Survived!)")
                } else {
                    // Transition between elevators in Stage.ABOVE
                    val nextElevatorIdx = when (elevator.id) {
                        "above_elevator_1" -> 1
                        "above_elevator_2" -> 2
                        "above_elevator_3" -> 3
                        else -> 0
                    }
                    val nextElevator = _elevators.value.getOrNull(nextElevatorIdx)
                    if (nextElevator != null) {
                        _playerState.value = currentState.copy(
                            position = Offset(nextElevator.x - 40f, nextElevator.groundY),
                            goingUp = false
                        )
                    }
                }
            }
        }
    }
}
