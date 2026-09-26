package com.example.ui.screen.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalculationResult
import com.example.model.CoreMaterial
import com.example.model.InductorType
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun CoilSimulationCanvas(
    type: InductorType,
    hasCore: Boolean,
    coreMaterial: CoreMaterial?,
    calculationResult: CalculationResult,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onLiveVoltageChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Continuous time counter that freezes when paused without losing position
    var simTimeSec by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            var lastNanos = withFrameNanos { it }
            while (isActive) {
                withFrameNanos { currentNanos ->
                    val dt = (currentNanos - lastNanos).coerceAtLeast(0L) / 1_000_000_000f
                    lastNanos = currentNanos
                    // Cap dt to avoid large jumps on frames drops
                    simTimeSec += dt.coerceIn(0f, 0.05f)
                }
            }
        }
    }

    var voltageSliderVal by remember(calculationResult.simulationVoltage) {
        mutableFloatStateOf(calculationResult.simulationVoltage.toFloat())
    }

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("coil_simulation_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with simulation status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Simulación 2D de Bobina y Campo B",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (calculationResult.isAC) {
                            "Régimen AC oscilante (${String.format(Locale.US, "%.1f", calculationResult.frequencyHz)} Hz)"
                        } else {
                            "Régimen DC estacionario (flujo continuo)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                FilledTonalIconButton(
                    onClick = onTogglePause,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("pause_resume_simulation_button")
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Reanudar simulación" else "Pausar simulación"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Box with dark oscilloscope/lab backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .background(Color(0xFF070B14), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .testTag("simulation_canvas")
                ) {
                    drawLabGrid()

                    // Physics parameters derived from live state and time
                    val acFrequencyPeriodSec = 2.0f // perceptible human frequency for visual tracking
                    val omega = (2f * PI.toFloat()) / acFrequencyPeriodSec
                    val phase = simTimeSec * omega
                    val acFactor = if (calculationResult.isAC) sin(phase) else 1.0f

                    when (type) {
                        InductorType.CILINDRICO -> {
                            drawCylindricalCoil(
                                hasCore = hasCore,
                                material = coreMaterial,
                                turns = calculationResult.turns,
                                acFactor = acFactor,
                                simTimeSec = simTimeSec,
                                calculationResult = calculationResult,
                                textMeasurer = textMeasurer
                            )
                        }
                        InductorType.TOROIDAL -> {
                            drawToroidalCoil(
                                material = coreMaterial,
                                turns = calculationResult.turns,
                                acFactor = acFactor,
                                simTimeSec = simTimeSec,
                                calculationResult = calculationResult,
                                textMeasurer = textMeasurer
                            )
                        }
                        InductorType.ANTENA -> {
                            drawLoopAntenna(
                                material = coreMaterial,
                                turns = calculationResult.turns,
                                acFactor = acFactor,
                                simTimeSec = simTimeSec,
                                calculationResult = calculationResult,
                                textMeasurer = textMeasurer
                            )
                        }
                        InductorType.TRANSFORMADOR -> {
                            drawTransformer(
                                material = coreMaterial,
                                primaryTurns = calculationResult.turns,
                                secondaryTurns = max(1, calculationResult.secondaryTurns),
                                acFactor = acFactor,
                                simTimeSec = simTimeSec,
                                calculationResult = calculationResult,
                                textMeasurer = textMeasurer
                            )
                        }
                    }
                }

                // Telemetry overlay badge (Top-Left)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xDD0B132B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isPaused) Color(0xFFFBBF24) else Color(0xFF10B981),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPaused) "PAUSADO" else "EN VIVO",
                                color = if (isPaused) Color(0xFFFBBF24) else Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "V: ${String.format(Locale.US, "%.1f", calculationResult.simulationVoltage)}V | I: ${calculationResult.formatCurrent()}",
                                color = Color(0xFFF1F5F9),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "B: ${calculationResult.formatFluxDensity()} | Φ: ${calculationResult.formatMagneticFlux()} | L: ${calculationResult.formatInductance()}",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Small legend (Bottom-Start)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xB3030712)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF38BDF8), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Líneas celestes: Flujo magnético B",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Live Voltage Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ajustar Voltaje en vivo:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = String.format(Locale.US, "%.1f V", voltageSliderVal),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Slider(
                value = voltageSliderVal,
                onValueChange = { newValue ->
                    voltageSliderVal = newValue
                    onLiveVoltageChange(newValue.toDouble())
                },
                valueRange = 1f..100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_voltage_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

// ---------------- CANVAS DRAWING HELPERS ----------------

private fun DrawScope.drawLabGrid() {
    val gridColor = Color(0x1238BDF8)
    val step = 28f
    var x = 0f
    while (x <= size.width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.8f)
        x += step
    }
    var y = 0f
    while (y <= size.height) {
        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.8f)
        y += step
    }
}

private fun DrawScope.drawArrowHead(tip: Offset, angleRad: Float, length: Float = 8f, color: Color) {
    val leftAngle = angleRad + (5f * PI.toFloat() / 6f)
    val rightAngle = angleRad - (5f * PI.toFloat() / 6f)
    val p1 = Offset(tip.x + length * cos(leftAngle), tip.y + length * sin(leftAngle))
    val p2 = Offset(tip.x + length * cos(rightAngle), tip.y + length * sin(rightAngle))
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }
    drawPath(path, color = color)
}

// ---------------- 1. CYLINDRICAL COIL (SOLENOID 3D) ----------------

private fun DrawScope.drawCylindricalCoil(
    hasCore: Boolean,
    material: CoreMaterial?,
    turns: Int,
    acFactor: Float,
    simTimeSec: Float,
    calculationResult: CalculationResult,
    textMeasurer: TextMeasurer
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    val coreWidth = size.width * 0.52f
    val coreHeight = size.height * 0.26f
    val coreLeft = centerX - (coreWidth / 2f)
    val coreTop = centerY - (coreHeight / 2f)
    val coreBottom = centerY + (coreHeight / 2f)

    val currentTurns = min(14, max(4, turns))
    val turnSpacing = coreWidth / (currentTurns + 1)
    val wireRadiusY = coreHeight * 0.58f // slightly taller than core for wrapping around
    val topWireY = centerY - wireRadiusY
    val bottomWireY = centerY + wireRadiusY

    // Polarity and Flux direction
    val isAC = calculationResult.isAC
    val isReversed = acFactor < 0
    val intensity = kotlin.math.abs(acFactor).coerceIn(0.12f, 1.0f)

    // Voltage scaling effect on flux brightness
    val voltageBoost = (calculationResult.simulationVoltage / 30.0).coerceIn(0.6, 1.6).toFloat()
    val fluxAlpha = (intensity * 0.85f * voltageBoost).coerceIn(0.2f, 0.95f)
    val fluxColor = Color(0xFF00E5FF).copy(alpha = fluxAlpha)

    // Dash offset follows physical AC flow reversal or continuous DC flow
    val flowDirection = if (isAC) acFactor else 1.0f
    val fluxDashOffset = flowDirection * simTimeSec * 30f

    // 1. MAGNETIC FIELD LINES (Outer Dipole Loops)
    // Symmetrical expanding loops emerging from N pole and returning to S pole
    val loopHeights = listOf(coreHeight * 1.5f, coreHeight * 2.1f, coreHeight * 2.8f)
    val loopWidths = listOf(coreWidth * 1.15f, coreWidth * 1.35f, coreWidth * 1.55f)

    for (k in loopHeights.indices) {
        val lh = loopHeights[k]
        val lw = loopWidths[k]

        // Top dipole loop
        val topPath = Path().apply {
            moveTo(centerX - lw / 2f, centerY)
            cubicTo(
                centerX - lw / 2f, centerY - lh,
                centerX + lw / 2f, centerY - lh,
                centerX + lw / 2f, centerY
            )
        }
        drawPath(
            path = topPath,
            color = fluxColor,
            style = Stroke(
                width = 1.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), fluxDashOffset)
            )
        )

        // Bottom dipole loop
        val bottomPath = Path().apply {
            moveTo(centerX + lw / 2f, centerY)
            cubicTo(
                centerX + lw / 2f, centerY + lh,
                centerX - lw / 2f, centerY + lh,
                centerX - lw / 2f, centerY
            )
        }
        drawPath(
            path = bottomPath,
            color = fluxColor,
            style = Stroke(
                width = 1.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), fluxDashOffset)
            )
        )
    }

    // 2. BACK WIRES (Drawn FIRST, so the core occludes them!)
    val copperShadow = Color(0xFF7C2D12)
    for (i in 0 until currentTurns) {
        val xStart = coreLeft + (i + 1) * turnSpacing
        val xEnd = xStart + turnSpacing * 0.5f

        val backPath = Path().apply {
            moveTo(xStart, bottomWireY)
            cubicTo(
                xStart - turnSpacing * 0.25f, centerY + wireRadiusY * 0.3f,
                xEnd - turnSpacing * 0.25f, centerY - wireRadiusY * 0.3f,
                xEnd, topWireY
            )
        }
        drawPath(
            path = backPath,
            color = copperShadow,
            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
        )
    }

    // 3. MAGNETIC CORE (Drawn SECOND, in the middle layer)
    if (hasCore && material != null && material != CoreMaterial.AIRE) {
        val (coreMainColor, coreHighlightColor) = when (material) {
            CoreMaterial.FERRITA -> Color(0xFF1E232A) to Color(0xFF3B4252)
            CoreMaterial.HIERRO -> Color(0xFF334155) to Color(0xFF64748B)
            else -> Color(0xFF1E293B) to Color(0xFF475569)
        }

        // 3D cylinder body
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(coreHighlightColor, coreMainColor, coreMainColor.copy(alpha = 0.95f))
            ),
            topLeft = Offset(coreLeft, coreTop),
            size = Size(coreWidth, coreHeight),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = coreHighlightColor,
            topLeft = Offset(coreLeft, coreTop),
            size = Size(coreWidth, coreHeight),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 1.5f)
        )

        // Subtle core label
        drawText(
            textMeasurer = textMeasurer,
            text = material.displayName,
            topLeft = Offset(centerX - 24f, centerY - 6f),
            style = TextStyle(color = Color(0x88CBD5E1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        )
    } else {
        // Air Core (translucent former mandril)
        drawRoundRect(
            color = Color(0x2238BDF8),
            topLeft = Offset(coreLeft, coreTop),
            size = Size(coreWidth, coreHeight),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = Color(0x5538BDF8),
            topLeft = Offset(coreLeft, coreTop),
            size = Size(coreWidth, coreHeight),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "Núcleo de Aire",
            topLeft = Offset(centerX - 35f, centerY - 6f),
            style = TextStyle(color = Color(0x8838BDF8), fontSize = 10.sp, fontWeight = FontWeight.Medium)
        )
    }

    // Dense axial magnetic beam through the core center
    drawLine(
        color = Color(0xFF00E5FF).copy(alpha = fluxAlpha),
        start = Offset(coreLeft - 45f, centerY),
        end = Offset(coreLeft + coreWidth + 45f, centerY),
        strokeWidth = 3f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), fluxDashOffset)
    )

    // 4. FRONT WIRES (Drawn THIRD, on top of the core)
    val copperHighlight = Color(0xFFFDBA74)
    val copperPrimary = Color(0xFFEA580C)
    val copperBrush = Brush.verticalGradient(listOf(copperHighlight, copperPrimary, Color(0xFFC2410C)))

    for (i in 0 until currentTurns) {
        val xStart = coreLeft + (i + 1) * turnSpacing + turnSpacing * 0.5f
        val xNext = coreLeft + (i + 2) * turnSpacing

        val frontPath = Path().apply {
            moveTo(xStart, topWireY)
            cubicTo(
                xStart + turnSpacing * 0.25f, centerY - wireRadiusY * 0.3f,
                xNext + turnSpacing * 0.25f, centerY + wireRadiusY * 0.3f,
                xNext, bottomWireY
            )
        }
        drawPath(
            path = frontPath,
            brush = copperBrush,
            style = Stroke(width = 5.5f, cap = StrokeCap.Round)
        )
    }

    // 5. TERMINALS AND LEADS
    val leadLeftX = coreLeft + turnSpacing
    val leadRightX = coreLeft + (currentTurns + 1) * turnSpacing
    val leadBottomY = bottomWireY + 30f

    // Wire extensions
    drawLine(copperPrimary, Offset(leadLeftX, bottomWireY), Offset(leadLeftX, leadBottomY), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(copperPrimary, Offset(leadRightX, bottomWireY), Offset(leadRightX, leadBottomY), strokeWidth = 5f, cap = StrokeCap.Round)

    // Terminal polarity indicators (+ and -)
    val posColor = Color(0xFFEF4444)
    val negColor = Color(0xFF3B82F6)

    val leftTerminalColor = if (isAC && isReversed) negColor else posColor
    val rightTerminalColor = if (isAC && isReversed) posColor else negColor

    drawCircle(leftTerminalColor, radius = 7f, center = Offset(leadLeftX, leadBottomY + 8f))
    drawCircle(rightTerminalColor, radius = 7f, center = Offset(leadRightX, leadBottomY + 8f))

    drawText(
        textMeasurer = textMeasurer,
        text = if (isAC && isReversed) "-" else "+",
        topLeft = Offset(leadLeftX - 3f, leadBottomY + 1f),
        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = if (isAC && isReversed) "+" else "-",
        topLeft = Offset(leadRightX - 3f, leadBottomY + 1f),
        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
    )

    // Current arrows along leads
    val currentArrowDir = if (isAC && isReversed) -1f else 1f
    val arrowY = bottomWireY + 15f
    drawArrowHead(
        tip = Offset(leadLeftX, arrowY + (currentArrowDir * 5f)),
        angleRad = if (currentArrowDir > 0) (PI.toFloat() / 2f) else (-PI.toFloat() / 2f),
        length = 7f,
        color = Color(0xFFFEF08A)
    )

    // 6. MAGNETIC POLES (N and S)
    // By right-hand rule: positive flow creates North at right, South at left
    val northColor = Color(0xFFEF4444)
    val southColor = Color(0xFF38BDF8)

    val leftPoleIsNorth = isReversed
    val leftPoleText = if (leftPoleIsNorth) "N" else "S"
    val rightPoleText = if (leftPoleIsNorth) "S" else "N"
    val leftPoleColor = if (leftPoleIsNorth) northColor else southColor
    val rightPoleColor = if (leftPoleIsNorth) southColor else northColor

    // Pole badges
    drawRoundRect(
        color = leftPoleColor.copy(alpha = 0.25f),
        topLeft = Offset(coreLeft - 48f, centerY - 16f),
        size = Size(26f, 32f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = leftPoleText,
        topLeft = Offset(coreLeft - 41f, centerY - 12f),
        style = TextStyle(color = leftPoleColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
    )

    drawRoundRect(
        color = rightPoleColor.copy(alpha = 0.25f),
        topLeft = Offset(coreLeft + coreWidth + 22f, centerY - 16f),
        size = Size(26f, 32f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = rightPoleText,
        topLeft = Offset(coreLeft + coreWidth + 29f, centerY - 12f),
        style = TextStyle(color = rightPoleColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
    )
}

// ---------------- 2. TOROIDAL COIL ----------------

private fun DrawScope.drawToroidalCoil(
    material: CoreMaterial?,
    turns: Int,
    acFactor: Float,
    simTimeSec: Float,
    calculationResult: CalculationResult,
    textMeasurer: TextMeasurer
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val rOuter = min(size.width, size.height) * 0.40f
    val rInner = rOuter * 0.46f
    val rMean = (rOuter + rInner) / 2f
    val coreThickness = rOuter - rInner

    val isAC = calculationResult.isAC
    val intensity = kotlin.math.abs(acFactor).coerceIn(0.15f, 1.0f)
    val voltageBoost = (calculationResult.simulationVoltage / 30.0).coerceIn(0.6, 1.6).toFloat()
    val fluxAlpha = (intensity * 0.9f * voltageBoost).coerceIn(0.2f, 1.0f)

    val flowDirection = if (isAC) acFactor else 1.0f
    val fluxDashOffset = flowDirection * simTimeSec * 35f

    val numWindings = min(20, max(8, turns))
    val copperBrush = Brush.linearGradient(listOf(Color(0xFFFED7AA), Color(0xFFEA580C)))

    // 1. BACK WIRE SEGMENTS (Inner hole through back)
    for (i in 0 until numWindings) {
        val angle = (2.0 * PI / numWindings) * i - (PI / 2.0)
        val cosA = cos(angle).toFloat()
        val sinA = sin(angle).toFloat()

        val pIn = Offset(center.x + (rInner - 5f) * cosA, center.y + (rInner - 5f) * sinA)
        val pMid = Offset(center.x + rMean * cosA, center.y + rMean * sinA)

        drawLine(
            color = Color(0xFF7C2D12),
            start = pIn,
            end = pMid,
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )
    }

    // 2. TOROID DONUT CORE (Accurately centered on rMean!)
    val (coreColor, coreHighlight) = when (material) {
        CoreMaterial.FERRITA -> Color(0xFF1E232A) to Color(0xFF3B4252)
        CoreMaterial.HIERRO -> Color(0xFF334155) to Color(0xFF64748B)
        else -> Color(0xFF1E293B) to Color(0xFF475569)
    }

    // Body of the donut
    drawCircle(
        color = coreColor,
        radius = rMean,
        center = center,
        style = Stroke(width = coreThickness)
    )
    // Outer and Inner boundary rings
    drawCircle(
        color = coreHighlight,
        radius = rOuter,
        center = center,
        style = Stroke(width = 1.5f)
    )
    drawCircle(
        color = coreHighlight,
        radius = rInner,
        center = center,
        style = Stroke(width = 1.5f)
    )

    // 3. CONFINED MAGNETIC FLUX LINES (Inside the donut core!)
    val fluxRadiusList = listOf(
        rMean - coreThickness * 0.22f,
        rMean,
        rMean + coreThickness * 0.22f
    )
    for (rFlux in fluxRadiusList) {
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = fluxAlpha),
            radius = rFlux,
            center = center,
            style = Stroke(
                width = 2.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), fluxDashOffset)
            )
        )
    }

    // 4. FRONT WIRE SEGMENTS (Wrapping over the front of the donut)
    for (i in 0 until numWindings) {
        val angle = (2.0 * PI / numWindings) * i - (PI / 2.0)
        val cosA = cos(angle).toFloat()
        val sinA = sin(angle).toFloat()

        val pMid = Offset(center.x + rMean * cosA, center.y + rMean * sinA)
        val pOut = Offset(center.x + (rOuter + 6f) * cosA, center.y + (rOuter + 6f) * sinA)

        drawLine(
            brush = copperBrush,
            start = pMid,
            end = pOut,
            strokeWidth = 5.0f,
            cap = StrokeCap.Round
        )
    }

    // Direction text in the center hole
    drawText(
        textMeasurer = textMeasurer,
        text = "Flujo B\nConfinado",
        topLeft = Offset(center.x - 26f, center.y - 14f),
        style = TextStyle(
            color = Color(0xFF38BDF8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    )
}

// ---------------- 3. LOOP ANTENNA ----------------

private fun DrawScope.drawLoopAntenna(
    material: CoreMaterial?,
    turns: Int,
    acFactor: Float,
    simTimeSec: Float,
    calculationResult: CalculationResult,
    textMeasurer: TextMeasurer
) {
    val center = Offset(size.width / 2f, size.height / 2f - 14f)
    val radius = min(size.width, size.height) * 0.35f

    val isAC = calculationResult.isAC
    val intensity = kotlin.math.abs(acFactor).coerceIn(0.15f, 1.0f)
    val voltageBoost = (calculationResult.simulationVoltage / 30.0).coerceIn(0.6, 1.6).toFloat()
    val waveAlpha = (intensity * 0.8f * voltageBoost).coerceIn(0.15f, 0.95f)

    // 1. RADIATED ELECTROMAGNETIC WAVES (Concentric expanding wavefronts)
    val maxWaveRadius = size.width * 0.46f
    for (step in 1..4) {
        val waveOffset = (simTimeSec * 45f + step * 26f) % (maxWaveRadius - radius)
        val rWave = radius + waveOffset
        val decay = (1.0f - (waveOffset / (maxWaveRadius - radius))).coerceIn(0.1f, 1.0f)

        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = waveAlpha * decay * 0.7f),
            radius = rWave,
            center = center,
            style = Stroke(
                width = 1.8f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
            )
        )
    }

    // 2. ANTENNA CONDUCTOR LOOP (Arc with gap at the bottom for feed terminals!)
    // 90 degrees is the bottom. Leave a 28 degree gap at the bottom (from 76 to 104 deg)
    val gapDeg = 28f
    val startAngle = 90f + (gapDeg / 2f) // 104 deg
    val sweepAngle = 360f - gapDeg       // 332 deg (ends at 76 deg)

    val currentTurns = min(5, max(1, turns))
    val copperBrush = Brush.sweepGradient(listOf(Color(0xFFEA580C), Color(0xFFFED7AA), Color(0xFFEA580C)))

    for (t in 0 until currentTurns) {
        val rLoop = radius - (t * 7f)
        drawArc(
            brush = copperBrush,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - rLoop, center.y - rLoop),
            size = Size(rLoop * 2f, rLoop * 2f),
            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
        )
    }

    // 3. FEED TERMINALS AT BOTTOM (Correctly attached to the arc ends!)
    val terminalAngle1Rad = (90f - gapDeg / 2f) * (PI.toFloat() / 180f) // 76 deg
    val terminalAngle2Rad = (90f + gapDeg / 2f) * (PI.toFloat() / 180f) // 104 deg

    val feedEnd1 = Offset(center.x + radius * cos(terminalAngle1Rad), center.y + radius * sin(terminalAngle1Rad))
    val feedEnd2 = Offset(center.x + radius * cos(terminalAngle2Rad), center.y + radius * sin(terminalAngle2Rad))

    val feedLeadLength = 26f
    val terminal1 = Offset(feedEnd1.x, feedEnd1.y + feedLeadLength)
    val terminal2 = Offset(feedEnd2.x, feedEnd2.y + feedLeadLength)

    drawLine(Color(0xFFEA580C), feedEnd1, terminal1, strokeWidth = 4.5f, cap = StrokeCap.Round)
    drawLine(Color(0xFFEA580C), feedEnd2, terminal2, strokeWidth = 4.5f, cap = StrokeCap.Round)

    // RF Generator connection dots
    drawCircle(Color(0xFFEF4444), radius = 6f, center = terminal1)
    drawCircle(Color(0xFF3B82F6), radius = 6f, center = terminal2)

    drawText(
        textMeasurer = textMeasurer,
        text = "RF IN",
        topLeft = Offset(center.x - 18f, terminal1.y + 10f),
        style = TextStyle(color = Color(0xFFCBD5E1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )

    drawText(
        textMeasurer = textMeasurer,
        text = "Antena Loop RF",
        topLeft = Offset(center.x - 42f, center.y - 8f),
        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    )
}

// ---------------- 4. TRANSFORMER ----------------

private fun DrawScope.drawTransformer(
    material: CoreMaterial?,
    primaryTurns: Int,
    secondaryTurns: Int,
    acFactor: Float,
    simTimeSec: Float,
    calculationResult: CalculationResult,
    textMeasurer: TextMeasurer
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    val coreWidth = size.width * 0.70f
    val coreHeight = size.height * 0.62f
    val legThickness = 32f

    val left = centerX - coreWidth / 2f
    val top = centerY - coreHeight / 2f

    val isAC = calculationResult.isAC
    val intensity = kotlin.math.abs(acFactor).coerceIn(0.15f, 1.0f)
    val voltageBoost = (calculationResult.simulationVoltage / 30.0).coerceIn(0.6, 1.6).toFloat()
    val fluxAlpha = (intensity * 0.9f * voltageBoost).coerceIn(0.2f, 1.0f)

    val flowDirection = if (isAC) acFactor else 1.0f
    val fluxDashOffset = flowDirection * simTimeSec * 35f

    // 1. RECTANGULAR CORE FRAME
    val (coreColor, coreBorder) = when (material) {
        CoreMaterial.FERRITA -> Color(0xFF1E232A) to Color(0xFF475569)
        CoreMaterial.HIERRO -> Color(0xFF334155) to Color(0xFF64748B)
        else -> Color(0xFF1E293B) to Color(0xFF475569)
    }

    val outerRect = Rect(left, top, left + coreWidth, top + coreHeight)
    val innerRect = Rect(
        left + legThickness,
        top + legThickness,
        left + coreWidth - legThickness,
        top + coreHeight - legThickness
    )

    val corePath = Path().apply {
        addRoundRect(RoundRect(outerRect, CornerRadius(8f, 8f)))
        addRoundRect(RoundRect(innerRect, CornerRadius(4f, 4f)))
    }

    drawPath(corePath, color = coreColor)
    drawPath(corePath, color = coreBorder, style = Stroke(width = 1.5f))

    // 2. CLOSED MAGNETIC FLUX CIRCUIT
    val fluxMidX = (left + legThickness / 2f)
    val fluxMidRightX = (left + coreWidth - legThickness / 2f)
    val fluxMidY = (top + legThickness / 2f)
    val fluxMidBottomY = (top + coreHeight - legThickness / 2f)

    val fluxPath = Path().apply {
        moveTo(fluxMidX, fluxMidY)
        lineTo(fluxMidRightX, fluxMidY)
        lineTo(fluxMidRightX, fluxMidBottomY)
        lineTo(fluxMidX, fluxMidBottomY)
        close()
    }

    drawPath(
        path = fluxPath,
        color = Color(0xFF00E5FF).copy(alpha = fluxAlpha),
        style = Stroke(
            width = 2.8f,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), fluxDashOffset)
        )
    )

    // 3. PRIMARY WINDING (Left leg - Orange copper)
    val pTurnsCount = min(12, max(4, primaryTurns))
    val pSpacing = (innerRect.height - 12f) / pTurnsCount
    for (i in 0 until pTurnsCount) {
        val y = innerRect.top + 6f + i * pSpacing
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFFED7AA))),
            topLeft = Offset(left - 6f, y),
            size = Size(legThickness + 12f, pSpacing * 0.70f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }

    // 4. SECONDARY WINDING (Right leg - Amber / Gold copper)
    val sTurnsCount = min(12, max(4, secondaryTurns))
    val sSpacing = (innerRect.height - 12f) / sTurnsCount
    for (i in 0 until sTurnsCount) {
        val y = innerRect.top + 6f + i * sSpacing
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFFDE68A))),
            topLeft = Offset(innerRect.right - 6f, y),
            size = Size(legThickness + 12f, sSpacing * 0.70f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }

    // 5. INFORMATIVE LABELS & SECONDARY TELEMETRY
    drawText(
        textMeasurer = textMeasurer,
        text = "Primario N1=${primaryTurns}",
        topLeft = Offset(left - 8f, top - 20f),
        style = TextStyle(color = Color(0xFFEA580C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = "Secundario N2=${secondaryTurns}",
        topLeft = Offset(innerRect.right - 14f, top - 20f),
        style = TextStyle(color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )

    // Live transformer ratio badge inside window
    val ratio = secondaryTurns.toDouble() / max(1, primaryTurns).toDouble()
    val ratioType = when {
        ratio > 1.05 -> "Elevador"
        ratio < 0.95 -> "Reductor"
        else -> "Aislador 1:1"
    }
    val secVolts = calculationResult.secondaryVoltage

    drawText(
        textMeasurer = textMeasurer,
        text = "$ratioType (r=%.2f)\nV2 ≈ %.1f V".format(Locale.US, ratio, secVolts),
        topLeft = Offset(centerX - 36f, centerY - 14f),
        style = TextStyle(
            color = Color(0xFF38BDF8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    )
}
