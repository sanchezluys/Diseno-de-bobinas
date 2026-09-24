package com.example.ui.screen.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val infiniteTransition = rememberInfiniteTransition(label = "CoilAnimation")
    val animatedPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPaused) 0f else (2f * PI.toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (calculationResult.isAC) 2000 else 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

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
                        text = "Simulación 2D de Bobina y Flujo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (calculationResult.isAC) "Régimen AC oscilante (${calculationResult.frequencyHz} Hz)" else "Régimen DC estacionario",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                FilledTonalIconButton(
                    onClick = onTogglePause,
                    modifier = Modifier.size(40.dp).testTag("pause_resume_simulation_button")
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Reanudar animación" else "Pausar animación"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Box with dark oscilloscope/lab backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFF090D16), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .testTag("simulation_canvas")
                ) {
                    drawLabGrid()

                    val phase = if (isPaused) 0f else animatedPhase
                    val acFactor = if (calculationResult.isAC) sin(phase) else 1.0f

                    when (type) {
                        InductorType.CILINDRICO -> {
                            drawCylindricalCoil(
                                hasCore = hasCore,
                                material = coreMaterial,
                                turns = calculationResult.turns,
                                acFactor = acFactor,
                                phase = phase,
                                calculationResult = calculationResult,
                                textMeasurer = textMeasurer
                            )
                        }
                        InductorType.TOROIDAL -> {
                            drawToroidalCoil(
                                material = coreMaterial,
                                turns = calculationResult.turns,
                                acFactor = acFactor,
                                phase = phase,
                                calculationResult = calculationResult,
                                textMeasurer = textMeasurer
                            )
                        }
                        InductorType.ANTENA -> {
                            drawLoopAntenna(
                                material = coreMaterial,
                                turns = calculationResult.turns,
                                acFactor = acFactor,
                                phase = phase,
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
                                phase = phase,
                                calculationResult = calculationResult,
                                textMeasurer = textMeasurer
                            )
                        }
                    }
                }

                // Small telemetry overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xCC0F172A)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isPaused) Color.Yellow else Color(0xFF38BDF8),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "B ≈ ${calculationResult.formatFluxDensity()} | Φ ≈ ${calculationResult.formatMagneticFlux()}",
                            color = Color(0xFFE2E8F0),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    text = String.format(java.util.Locale.US, "%.1f V", voltageSliderVal),
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
    val gridColor = Color(0x1A38BDF8)
    val step = 30f
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

private fun DrawScope.drawCylindricalCoil(
    hasCore: Boolean,
    material: CoreMaterial?,
    turns: Int,
    acFactor: Float,
    phase: Float,
    calculationResult: CalculationResult,
    textMeasurer: TextMeasurer
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    val coreWidth = size.width * 0.55f
    val coreHeight = size.height * 0.28f
    val coreLeft = centerX - (coreWidth / 2f)
    val coreTop = centerY - (coreHeight / 2f)

    val currentTurns = min(18, max(4, turns))
    val turnSpacing = coreWidth / (currentTurns + 1)

    // 1. Draw Magnetic Field Lines (Outer Loops)
    val fluxAlpha = (kotlin.math.abs(acFactor) * 0.8f).coerceIn(0.15f, 0.95f)
    val fluxColor = Color(0xFF38BDF8).copy(alpha = fluxAlpha)

    val loopOffsets = listOf(0.40f, 0.65f, 0.90f)
    for (factor in loopOffsets) {
        val loopPath = Path()
        val loopHeight = coreHeight * (1.6f + factor * 1.8f)
        val loopWidth = coreWidth * (1.1f + factor * 0.4f)

        // Top arc
        loopPath.moveTo(centerX - loopWidth / 2f, centerY)
        loopPath.cubicTo(
            centerX - loopWidth / 2f, centerY - loopHeight / 2f,
            centerX + loopWidth / 2f, centerY - loopHeight / 2f,
            centerX + loopWidth / 2f, centerY
        )
        // Bottom arc
        loopPath.cubicTo(
            centerX + loopWidth / 2f, centerY + loopHeight / 2f,
            centerX - loopWidth / 2f, centerY + loopHeight / 2f,
            centerX - loopWidth / 2f, centerY
        )
        loopPath.close()

        drawPath(
            path = loopPath,
            color = fluxColor,
            style = Stroke(
                width = 1.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), phase * 8f)
            )
        )
    }

    // 2. Draw Magnetic Core (if any)
    if (hasCore && material != null && material != CoreMaterial.AIRE) {
        val coreColor = when (material) {
            CoreMaterial.FERRITA -> Color(0xFF262626) // Deep graphite
            CoreMaterial.HIERRO -> Color(0xFF475569) // Slate steel
            else -> Color.Transparent
        }
        val highlightColor = when (material) {
            CoreMaterial.FERRITA -> Color(0xFF404040)
            CoreMaterial.HIERRO -> Color(0xFF94A3B8)
            else -> Color.Transparent
        }

        // Core body with subtle gradient
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(highlightColor, coreColor, coreColor.copy(alpha = 0.9f))
            ),
            topLeft = Offset(coreLeft, coreTop),
            size = Size(coreWidth, coreHeight),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = Color(0xFF64748B),
            topLeft = Offset(coreLeft, coreTop),
            size = Size(coreWidth, coreHeight),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 1.5f)
        )
    } else {
        // Air Core (dashed subtle indicator)
        drawRoundRect(
            color = Color(0x3338BDF8),
            topLeft = Offset(coreLeft, coreTop),
            size = Size(coreWidth, coreHeight),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
        )
    }

    // Central axial flux beam
    drawLine(
        color = Color(0xFF00E5FF).copy(alpha = fluxAlpha),
        start = Offset(coreLeft - 30f, centerY),
        end = Offset(coreLeft + coreWidth + 30f, centerY),
        strokeWidth = 3f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), phase * 10f)
    )

    // 3. Draw Copper Windings (Front and Back arcs for 3D effect)
    val copperPrimary = Color(0xFFEA580C)
    val copperShadow = Color(0xFF9A3412)
    val copperHighlight = Color(0xFFFDBA74)

    val turnWidth = turnSpacing * 0.7f
    val turnHeight = coreHeight * 1.3f

    // Back wires (darker, drawn behind)
    for (i in 0 until currentTurns) {
        val tx = coreLeft + (i + 1) * turnSpacing
        drawArc(
            color = copperShadow,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(tx - turnWidth / 2f, centerY - turnHeight / 2f),
            size = Size(turnWidth, turnHeight),
            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
        )
    }

    // Front wires (bright copper with highlight)
    for (i in 0 until currentTurns) {
        val tx = coreLeft + (i + 1) * turnSpacing
        drawArc(
            brush = Brush.verticalGradient(listOf(copperHighlight, copperPrimary)),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(tx - turnWidth / 2f, centerY - turnHeight / 2f),
            size = Size(turnWidth, turnHeight),
            style = Stroke(width = 5.5f, cap = StrokeCap.Round)
        )
    }

    // Leads / Terminals
    val leadStart = Offset(coreLeft + turnSpacing - turnWidth / 2f, centerY + turnHeight / 2f)
    val leadEnd = Offset(coreLeft + currentTurns * turnSpacing + turnWidth / 2f, centerY + turnHeight / 2f)

    drawLine(copperPrimary, leadStart, Offset(leadStart.x, leadStart.y + 25f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(copperPrimary, leadEnd, Offset(leadEnd.x, leadEnd.y + 25f), strokeWidth = 5f, cap = StrokeCap.Round)

    // Polarities (+, -)
    drawCircle(Color(0xFFEF4444), radius = 8f, center = Offset(leadStart.x, leadStart.y + 35f))
    drawCircle(Color(0xFF3B82F6), radius = 8f, center = Offset(leadEnd.x, leadEnd.y + 35f))

    // Magnetic Poles (N and S)
    val northColor = Color(0xFFEF4444)
    val southColor = Color(0xFF3B82F6)
    val isReversed = acFactor < 0

    val leftPoleText = if (isReversed) "S" else "N"
    val rightPoleText = if (isReversed) "N" else "S"
    val leftPoleColor = if (isReversed) southColor else northColor
    val rightPoleColor = if (isReversed) northColor else southColor

    drawText(
        textMeasurer = textMeasurer,
        text = leftPoleText,
        topLeft = Offset(coreLeft - 40f, centerY - 12f),
        style = TextStyle(color = leftPoleColor, fontSize = 16.sp, fontWeight = FontWeight.Black)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = rightPoleText,
        topLeft = Offset(coreLeft + coreWidth + 24f, centerY - 12f),
        style = TextStyle(color = rightPoleColor, fontSize = 16.sp, fontWeight = FontWeight.Black)
    )
}

private fun DrawScope.drawToroidalCoil(
    material: CoreMaterial?,
    turns: Int,
    acFactor: Float,
    phase: Float,
    calculationResult: CalculationResult,
    textMeasurer: TextMeasurer
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val rOuter = min(size.width, size.height) * 0.40f
    val rInner = rOuter * 0.48f
    val rMean = (rOuter + rInner) / 2f

    // 1. Toroid Core (Donut)
    val coreColor = when (material) {
        CoreMaterial.FERRITA -> Color(0xFF262626)
        CoreMaterial.HIERRO -> Color(0xFF475569)
        else -> Color(0x3338BDF8)
    }

    drawCircle(
        color = coreColor,
        radius = rOuter,
        center = center,
        style = Stroke(width = rOuter - rInner)
    )
    drawCircle(
        color = Color(0xFF64748B),
        radius = rOuter,
        center = center,
        style = Stroke(width = 1.5f)
    )
    drawCircle(
        color = Color(0xFF64748B),
        radius = rInner,
        center = center,
        style = Stroke(width = 1.5f)
    )

    // 2. Confined Magnetic Flux Lines (Inside the donut core!)
    val fluxAlpha = (kotlin.math.abs(acFactor) * 0.85f).coerceIn(0.2f, 1f)
    val fluxRadiusList = listOf(rInner + (rOuter - rInner) * 0.35f, rMean, rInner + (rOuter - rInner) * 0.65f)
    for (rFlux in fluxRadiusList) {
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = fluxAlpha),
            radius = rFlux,
            center = center,
            style = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase * 15f)
            )
        )
    }

    // 3. Radial Windings
    val numWindings = min(24, max(8, turns))
    val copperBrush = Brush.linearGradient(listOf(Color(0xFFFDBA74), Color(0xFFEA580C)))
    for (i in 0 until numWindings) {
        val angle = (2.0 * PI / numWindings) * i
        val cosA = cos(angle).toFloat()
        val sinA = sin(angle).toFloat()

        val p1 = Offset(center.x + (rInner - 8f) * cosA, center.y + (rInner - 8f) * sinA)
        val p2 = Offset(center.x + (rOuter + 8f) * cosA, center.y + (rOuter + 8f) * sinA)

        drawLine(
            brush = copperBrush,
            start = p1,
            end = p2,
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )
    }

    // Direction Label in center hole
    drawText(
        textMeasurer = textMeasurer,
        text = "Flujo\nConfinado",
        topLeft = Offset(center.x - 28f, center.y - 14f),
        style = TextStyle(color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )
}

private fun DrawScope.drawLoopAntenna(
    material: CoreMaterial?,
    turns: Int,
    acFactor: Float,
    phase: Float,
    calculationResult: CalculationResult,
    textMeasurer: TextMeasurer
) {
    val center = Offset(size.width / 2f, size.height / 2f - 10f)
    val radius = min(size.width, size.height) * 0.34f

    // Radiated Wavefronts / Dipole Loops
    val waveAlpha = (kotlin.math.abs(acFactor) * 0.7f).coerceIn(0.1f, 0.8f)
    for (step in 1..4) {
        val rWave = radius + (step * 24f + (phase * 10f) % 24f)
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = waveAlpha / step),
            radius = rWave,
            center = center,
            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )
    }

    // Antenna Loop
    val copperBrush = Brush.sweepGradient(listOf(Color(0xFFEA580C), Color(0xFFFDBA74), Color(0xFFEA580C)))
    val currentTurns = min(6, max(1, turns))
    for (t in 0 until currentTurns) {
        val rLoop = radius - (t * 6f)
        drawArc(
            brush = copperBrush,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - rLoop, center.y - rLoop),
            size = Size(rLoop * 2f, rLoop * 2f),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
    }

    // Feed point at bottom
    val feedStart = Offset(center.x - 15f, center.y + radius)
    val feedEnd = Offset(center.x + 15f, center.y + radius)
    drawLine(Color(0xFFEA580C), feedStart, Offset(feedStart.x, feedStart.y + 25f), strokeWidth = 4f)
    drawLine(Color(0xFFEA580C), feedEnd, Offset(feedEnd.x, feedEnd.y + 25f), strokeWidth = 4f)

    drawText(
        textMeasurer = textMeasurer,
        text = "Antena Loop RF",
        topLeft = Offset(center.x - 38f, center.y - 8f),
        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    )
}

private fun DrawScope.drawTransformer(
    material: CoreMaterial?,
    primaryTurns: Int,
    secondaryTurns: Int,
    acFactor: Float,
    phase: Float,
    calculationResult: CalculationResult,
    textMeasurer: TextMeasurer
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    val coreWidth = size.width * 0.70f
    val coreHeight = size.height * 0.65f
    val legThickness = 32f

    val left = centerX - coreWidth / 2f
    val top = centerY - coreHeight / 2f

    // 1. Magnetic Core Frame (Rectangular Closed Core)
    val coreColor = when (material) {
        CoreMaterial.FERRITA -> Color(0xFF262626)
        CoreMaterial.HIERRO -> Color(0xFF475569)
        else -> Color(0xFF1E293B)
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
    drawPath(corePath, color = Color(0xFF64748B), style = Stroke(width = 1.5f))

    // 2. Closed Magnetic Flux Loop around the core rectangle
    val fluxAlpha = (kotlin.math.abs(acFactor) * 0.85f).coerceIn(0.2f, 1f)
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
            width = 2.5f,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase * 12f)
        )
    )

    // 3. Primary Winding (Left limb)
    val pTurnsCount = min(12, max(4, primaryTurns))
    val pSpacing = (innerRect.height - 10f) / pTurnsCount
    for (i in 0 until pTurnsCount) {
        val y = innerRect.top + 5f + i * pSpacing
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFFED7AA))),
            topLeft = Offset(left - 6f, y),
            size = Size(legThickness + 12f, pSpacing * 0.65f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }

    // 4. Secondary Winding (Right limb)
    val sTurnsCount = min(12, max(4, secondaryTurns))
    val sSpacing = (innerRect.height - 10f) / sTurnsCount
    for (i in 0 until sTurnsCount) {
        val y = innerRect.top + 5f + i * sSpacing
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFFDE68A))),
            topLeft = Offset(innerRect.right - 6f, y),
            size = Size(legThickness + 12f, sSpacing * 0.65f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }

    // Labels
    drawText(
        textMeasurer = textMeasurer,
        text = "Primario (N1)",
        topLeft = Offset(left - 12f, top - 20f),
        style = TextStyle(color = Color(0xFFEA580C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = "Secundario (N2)",
        topLeft = Offset(innerRect.right - 18f, top - 20f),
        style = TextStyle(color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )
}
