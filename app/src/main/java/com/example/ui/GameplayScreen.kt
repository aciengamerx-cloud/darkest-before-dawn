package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.ElevatorState
import com.example.game.Stage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplayScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.gameEngine.playerState.collectAsState()
    val elevators by viewModel.gameEngine.elevators.collectAsState()
    val lights by viewModel.gameEngine.lights.collectAsState()
    val harpies by viewModel.gameEngine.harpies.collectAsState()

    val scope = rememberCoroutineScope()

    // Control States
    var isLeftPressed by remember { mutableStateOf(false) }
    var isRightPressed by remember { mutableStateOf(false) }

    // Drive movement when keys are held
    LaunchedEffect(isLeftPressed, isRightPressed) {
        while (isLeftPressed) {
            viewModel.gameEngine.movePlayer(-1f)
            delay(16)
        }
        while (isRightPressed) {
            viewModel.gameEngine.movePlayer(1f)
            delay(16)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Dynamic Sky / Progress Gradient Background
        val skyGradient = remember(playerState.stage, playerState.position.x) {
            when (playerState.stage) {
                Stage.TUBE -> {
                    // Jet black subway
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF030712), Color(0xFF000000))
                    )
                }
                Stage.STREET -> {
                    // Deep night blue
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                    )
                }
                Stage.OUTDOORS -> {
                    // Dark midnight with starry tint
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF030712))
                    )
                }
                Stage.ABOVE -> {
                    // Dynamic dawn gradient that brightens as player approaches the top!
                    val progress = playerState.position.x / 1000f
                    val topColor = Color(
                        red = (0.1f + progress * 0.6f).coerceIn(0f, 1f),
                        green = (0.1f + progress * 0.4f).coerceIn(0f, 1f),
                        blue = (0.3f - progress * 0.1f).coerceIn(0f, 1f)
                    )
                    val bottomColor = Color(
                        red = (0.05f + progress * 0.3f).coerceIn(0f, 1f),
                        green = (0.02f + progress * 0.2f).coerceIn(0f, 1f),
                        blue = (0.15f - progress * 0.05f).coerceIn(0f, 1f)
                    )
                    Brush.verticalGradient(colors = listOf(topColor, bottomColor))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(skyGradient)
        )

        // Game Drawing Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_viewport_canvas")
        ) {
            val width = size.width
            val height = size.height

            // Scale factor to map game engine coordinates (1000x600) to actual canvas dimensions
            val scaleX = width / 1000f
            val scaleY = height / 600f

            // 1. Draw Starry Sky Background for Outdoors
            if (playerState.stage == Stage.OUTDOORS) {
                drawCircle(Color.White.copy(alpha = 0.6f), radius = 2f, center = Offset(100f * scaleX, 120f * scaleY))
                drawCircle(Color.White.copy(alpha = 0.8f), radius = 3f, center = Offset(300f * scaleX, 80f * scaleY))
                drawCircle(Color.White.copy(alpha = 0.5f), radius = 1.5f, center = Offset(500f * scaleX, 200f * scaleY))
                drawCircle(Color.White.copy(alpha = 0.9f), radius = 2.5f, center = Offset(700f * scaleX, 100f * scaleY))
                drawCircle(Color.White.copy(alpha = 0.7f), radius = 2f, center = Offset(850f * scaleX, 150f * scaleY))
            }

            // 2. Draw Decorative Stage Elements (Platform base)
            val groundY = 465f * scaleY
            when (playerState.stage) {
                Stage.TUBE -> {
                    // Draw subway tile lines or columns
                    for (i in 0..10) {
                        drawRect(
                            color = Color.DarkGray.copy(alpha = 0.2f),
                            topLeft = Offset((i * 100f) * scaleX, 0f),
                            size = Size(15f * scaleX, height)
                        )
                    }
                }
                Stage.STREET -> {
                    // Draw city buildings silhouette
                    drawRect(
                        color = Color(0xFF1E293B).copy(alpha = 0.3f),
                        topLeft = Offset(50f * scaleX, 150f * scaleY),
                        size = Size(180f * scaleX, 315f * scaleY)
                    )
                    drawRect(
                        color = Color(0xFF1E293B).copy(alpha = 0.3f),
                        topLeft = Offset(450f * scaleX, 200f * scaleY),
                        size = Size(200f * scaleX, 265f * scaleY)
                    )
                }
                Stage.OUTDOORS -> {
                    // Draw trees or mountains silhouette
                    drawCircle(
                        color = Color(0xFF14532D).copy(alpha = 0.25f),
                        radius = 180f,
                        center = Offset(250f * scaleX, 465f * scaleY)
                    )
                    drawCircle(
                        color = Color(0xFF14532D).copy(alpha = 0.25f),
                        radius = 220f,
                        center = Offset(750f * scaleX, 465f * scaleY)
                    )
                }
                Stage.ABOVE -> {
                    // Draw sky platform scaffold lines
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(200f * scaleX, 465f * scaleY),
                        end = Offset(900f * scaleX, 150f * scaleY),
                        strokeWidth = 3f
                    )
                }
            }

            // 3. Draw Active Lights (Cones and Beacons)
            for (light in lights) {
                val lightX = light.position.x * scaleX
                val lightY = light.position.y * scaleY
                val lightRadius = light.radius * scaleX

                // Radial cone of safety
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFEF08A).copy(alpha = 0.45f), // Warm yellow light center
                            Color(0xFFFEF08A).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(lightX, lightY),
                        radius = lightRadius
                    ),
                    radius = lightRadius,
                    center = Offset(lightX, lightY)
                )

                // The light source pole/bulb itself
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(lightX, lightY),
                    end = Offset(lightX, groundY),
                    strokeWidth = 4f
                )
                drawCircle(
                    color = Color(0xFFFDE047),
                    radius = 8f,
                    center = Offset(lightX, lightY)
                )
            }

            // 4. Draw Elevators
            for (elevator in elevators) {
                val eleX = elevator.x * scaleX
                val eleY = elevator.currentY * scaleY
                val eleWidth = 80f * scaleX
                val eleHeight = 15f * scaleY

                // Vertical elevator tracks
                drawLine(
                    color = Color.DarkGray.copy(alpha = 0.4f),
                    start = Offset(eleX, elevator.topY * scaleY),
                    end = Offset(eleX, elevator.groundY * scaleY),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                // Elevator lift platform
                drawRoundRect(
                    color = if (elevator.state == ElevatorState.GOING_UP) Color(0xFFFF9F0A) else Color(0xFF475569),
                    topLeft = Offset(eleX - eleWidth / 2, eleY),
                    size = Size(eleWidth, eleHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Glowing arrow indicators
                if (elevator.state == ElevatorState.GOING_UP) {
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.8f),
                        radius = 5f,
                        center = Offset(eleX, eleY - 10f)
                    )
                }
            }

            // 5. Draw The Ground Platform
            drawRect(
                color = Color(0xFF1E293B), // Dark solid slate ground
                topLeft = Offset(0f, groundY),
                size = Size(width, height - groundY)
            )
            // Accent line on ground edge
            drawLine(
                color = Color(0xFFE50914),
                start = Offset(0f, groundY),
                end = Offset(width, groundY),
                strokeWidth = 3f
            )

            // 6. Draw The Player character
            val playerX = playerState.position.x * scaleX
            val playerY = playerState.position.y * scaleY
            val playerRadius = 15f * scaleX

            // Glowing safe-boundary circle
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = 45f * scaleX,
                center = Offset(playerX, playerY)
            )

            // Body
            drawCircle(
                color = if (playerState.goingUp) Color(0xFFFF9F0A) else Color(0xFFE50914),
                radius = playerRadius,
                center = Offset(playerX, playerY - playerRadius)
            )
            // Head
            drawCircle(
                color = Color(0xFFFCA5A5),
                radius = playerRadius * 0.6f,
                center = Offset(playerX, playerY - playerRadius * 2.1f)
            )
            // Flashlight beam
            drawArc(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFEF08A).copy(alpha = 0.35f), Color.Transparent)
                ),
                startAngle = -20f,
                sweepAngle = 40f,
                useCenter = true,
                topLeft = Offset(playerX - 10f, playerY - playerRadius * 1.5f),
                size = Size(120f * scaleX, 60f * scaleY)
            )

            // 7. Draw Harpies (Shadow Beasts)
            for (harpy in harpies) {
                if (harpy.isDead) continue

                val harpyX = harpy.position.x * scaleX
                val harpyY = harpy.position.y * scaleY
                val harpyWidth = 30f * scaleX
                val harpyHeight = 20f * scaleY

                // Wings beating animation
                val wingBeat = kotlin.math.sin(System.currentTimeMillis() / 80.0).toFloat() * 15f

                // Body (Purple void mist)
                drawOval(
                    color = if (harpy.isPursuing) Color(0xFF701A75) else Color(0xFF3B0764),
                    topLeft = Offset(harpyX - harpyWidth / 2, harpyY - harpyHeight / 2),
                    size = Size(harpyWidth, harpyHeight)
                )

                // Glowing red eyes
                val eyeOffset = if (harpy.direction > 0) 6f else -6f
                drawCircle(Color.Red, radius = 3f, center = Offset(harpyX + eyeOffset, harpyY - 2f))

                // Left wing
                drawLine(
                    color = Color.Black,
                    start = Offset(harpyX - harpyWidth / 4, harpyY),
                    end = Offset(harpyX - harpyWidth, harpyY - wingBeat),
                    strokeWidth = 4f
                )
                // Right wing
                drawLine(
                    color = Color.Black,
                    start = Offset(harpyX + harpyWidth / 4, harpyY),
                    end = Offset(harpyX + harpyWidth, harpyY - wingBeat),
                    strokeWidth = 4f
                )

                // Draw alert indicator if pursuing
                if (harpy.isPursuing) {
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(harpyX - 2f, harpyY - 35f),
                        size = Size(4f, 12f)
                    )
                    drawCircle(Color.Red, radius = 2f, center = Offset(harpyX, harpyY - 18f))
                }
            }
        }

        // Full Screen Vignette Overlays (Vary with darkness fade intensity!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = playerState.fadeOutIntensity.coerceIn(0f, 0.95f))
                )
        )

        // Health / Status HUD (Top Left)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .width(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = playerState.stage.displayName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "HP: ${playerState.life.toInt()}",
                    color = if (playerState.life < 30f) Color(0xFFEF4444) else Color(0xFF10B981),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Health bar container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.DarkGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(playerState.life / playerState.maxLife)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            if (playerState.life < 30f) Color(0xFFEF4444)
                            else Color(0xFF10B981)
                        )
                )
            }
        }

        // Quick Stage Navigator (Top Right)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .testTag("back_to_menu_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Menu",
                    tint = Color.White
                )
            }
        }

        // On-Screen D-Pad Controls (Bottom Left and Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left D-Pad button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isLeftPressed = true
                                tryAwaitRelease()
                                isLeftPressed = false
                            }
                        )
                    }
                    .testTag("left_dpad_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Move Left",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Quick level skips for testers / seamless UX
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Stage.values().forEach { stage ->
                    val isSelected = playerState.stage == stage
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFFE50914) else Color(0xFF374151))
                            .clickable { viewModel.selectStage(stage) }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stage.name.take(1),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right D-Pad button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isRightPressed = true
                                tryAwaitRelease()
                                isRightPressed = false
                            }
                        )
                    }
                    .testTag("right_dpad_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Move Right",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Overlay: "Going Up..." Elevator Animation
        AnimatedVisibility(
            visible = playerState.goingUp,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.going_up),
                    contentDescription = "Going Up Image",
                    modifier = Modifier.size(240.dp, 120.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Overlay: Game Over Screen (Vaporized by the Dark!)
        AnimatedVisibility(
            visible = playerState.isDead,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF7F1D1D).copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "THE SHADOW CONSUMED YOU",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Stay closer to the lamps to survive the dark.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Image(
                        painter = painterResource(id = R.drawable.restart),
                        contentDescription = "Restart Button",
                        modifier = Modifier
                            .size(160.dp, 56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.restartGame() }
                            .testTag("restart_game_button")
                    )
                }
            }
        }

        // Overlay: Win / Victory Screen (Dawn Reached!)
        AnimatedVisibility(
            visible = playerState.gameWin,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFB703), Color(0xFFFB8500))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "DAWN REACHED",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You survived the longest, darkest night.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            viewModel.setScreen(Screen.OPTIONS)
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("back_to_menu_win_button")
                    ) {
                        Text(
                            text = "Main Menu",
                            color = Color(0xFFFB8500),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
