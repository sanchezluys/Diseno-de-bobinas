package com.example.ui.screen.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CalculationResult
import com.example.model.CoilDesignState
import com.example.model.CoreMaterial
import com.example.model.CurrentType
import com.example.model.FrequencyUnit
import com.example.model.InductorType
import com.example.model.LengthUnit
import com.example.model.UserPreferences
import com.example.model.WireGauge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignScreen(
    viewModel: CoilViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToFormulas: () -> Unit,
    onNavigateToWireSpecs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var step1Expanded by remember { mutableStateOf(state.inductorType == null) }
    var step2Expanded by remember { mutableStateOf(false) }
    var step3Expanded by remember { mutableStateOf(false) }
    var step4Expanded by remember { mutableStateOf(false) }
    var step5Expanded by remember { mutableStateOf(false) }
    var step6Expanded by remember { mutableStateOf(false) }
    var step7Expanded by remember { mutableStateOf(false) }
    var step8Expanded by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(state.inductorType) {
        if (state.inductorType == null) {
            step1Expanded = true
            step2Expanded = false
            step3Expanded = false
            step4Expanded = false
            step5Expanded = false
            step6Expanded = false
            step7Expanded = false
            step8Expanded = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Diseño de Bobinas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Simulador electromagnético",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToWireSpecs,
                        modifier = Modifier.testTag("wire_specs_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Manual de cables esmaltados"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToFormulas,
                        modifier = Modifier.testTag("formulas_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = "Fórmulas y teoría"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset / Quick-load bar for students
            PresetsBar(
                onSelectPreset = { preset ->
                    viewModel.loadPreset(preset)
                    step1Expanded = false
                    step2Expanded = false
                    step3Expanded = false
                    step4Expanded = false
                    step5Expanded = false
                    step6Expanded = false
                    step7Expanded = false
                    step8Expanded = false
                },
                onReset = {
                    viewModel.resetDesign()
                    step1Expanded = true
                    step2Expanded = false
                    step3Expanded = false
                    step4Expanded = false
                    step5Expanded = false
                    step6Expanded = false
                    step7Expanded = false
                    step8Expanded = false
                }
            )

            // ================== PASO 1: TIPO DE INDUCTOR ==================
            StepCard(
                stepNumber = 1,
                title = "Tipo de Inductor",
                isComplete = state.isStep1Complete,
                summary = state.inductorType?.title,
                isExpanded = step1Expanded,
                onToggleExpand = { step1Expanded = !step1Expanded },
                collapsedContent = {
                    state.inductorType?.let { type ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { step1Expanded = true }
                                .testTag("step1_collapsed_card"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = type.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = type.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "Cambiar",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InductorType.entries.forEach { type ->
                        val isSelected = state.inductorType == type
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectInductorType(type)
                                    step1Expanded = false
                                    step2Expanded = true
                                }
                                .testTag("type_card_${type.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = type.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = type.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================== PASO 2: NÚCLEO ==================
            AnimatedVisibility(
                visible = state.isStep1Complete,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StepCard(
                    stepNumber = 2,
                    title = "Núcleo Magnético",
                    isComplete = state.isStep2Complete,
                    summary = when (state.hasCore) {
                        false -> "Sin núcleo (Aire)"
                        true -> state.coreMaterial?.displayName
                        null -> null
                    },
                    isExpanded = step2Expanded,
                    onToggleExpand = { step2Expanded = !step2Expanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.hasCore == false,
                                onClick = {
                                    viewModel.setHasCore(false)
                                    step2Expanded = false
                                    step3Expanded = true
                                },
                                label = { Text("Sin núcleo (Aire)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_core_air"),
                                colors = FilterChipDefaults.filterChipColors()
                            )
                            FilterChip(
                                selected = state.hasCore == true,
                                onClick = {
                                    viewModel.setHasCore(true)
                                    step2Expanded = true
                                },
                                label = { Text("Con núcleo") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_core_with_material"),
                                colors = FilterChipDefaults.filterChipColors()
                            )
                        }

                        // Si tiene núcleo, mostrar selector de material (lista fija)
                        if (state.hasCore == true) {
                            Text(
                                text = "Material del núcleo (Permeabilidad μr):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                CoreMaterial.entries.forEach { material ->
                                    val isMaterialSelected = state.coreMaterial == material
                                    OutlinedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectCoreMaterial(material)
                                                step2Expanded = false
                                                step3Expanded = true
                                            }
                                            .testTag("material_${material.name.lowercase()}"),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(
                                            width = if (isMaterialSelected) 2.dp else 1.dp,
                                            color = if (isMaterialSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        ),
                                        colors = CardDefaults.outlinedCardColors(
                                            containerColor = if (isMaterialSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = material.displayName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Text(
                                                        text = "μr = ${material.relativePermeability.toInt()}",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = material.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================== PASO 3: TIPO DE CORRIENTE ==================
            AnimatedVisibility(
                visible = state.isStep2Complete,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StepCard(
                    stepNumber = 3,
                    title = "Tipo de Corriente",
                    isComplete = state.isStep3Complete,
                    summary = state.currentType?.label,
                    isExpanded = step3Expanded,
                    onToggleExpand = { step3Expanded = !step3Expanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CurrentType.entries.forEach { cur ->
                                val isCurSelected = state.currentType == cur
                                OutlinedButton(
                                    onClick = {
                                        viewModel.selectCurrentType(cur)
                                        step3Expanded = false
                                        step4Expanded = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("button_current_${cur.name.lowercase()}"),
                                    border = BorderStroke(
                                        width = if (isCurSelected) 2.dp else 1.dp,
                                        color = if (isCurSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isCurSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (cur == CurrentType.AC) Icons.Default.Waves else Icons.Default.Timeline,
                                        contentDescription = null,
                                        tint = if (isCurSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cur.name,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        state.currentType?.let {
                            Text(
                                text = it.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ================== PASO 4: DIÁMETRO ==================
            AnimatedVisibility(
                visible = state.isStep3Complete,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StepCard(
                    stepNumber = 4,
                    title = "Diámetro de la Bobina",
                    isComplete = state.isStep4Complete,
                    summary = if (state.isStep4Complete) "${state.diameterInput} ${state.diameterUnit.symbol}" else null,
                    isExpanded = step4Expanded,
                    onToggleExpand = { step4Expanded = !step4Expanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.diameterInput,
                                onValueChange = { viewModel.setDiameterInput(it) },
                                label = { Text("Diámetro (${state.diameterUnit.symbol})") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        if (state.isStep4Complete) {
                                            step4Expanded = false
                                            step5Expanded = true
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_diameter"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            // Unit selector / conversion toggle
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilterChip(
                                    selected = state.diameterUnit == LengthUnit.MM,
                                    onClick = { viewModel.toggleDiameterUnit(LengthUnit.MM) },
                                    label = { Text("mm") },
                                    modifier = Modifier.testTag("unit_mm_chip")
                                )
                                FilterChip(
                                    selected = state.diameterUnit == LengthUnit.INCHES,
                                    onClick = { viewModel.toggleDiameterUnit(LengthUnit.INCHES) },
                                    label = { Text("in") },
                                    modifier = Modifier.testTag("unit_in_chip")
                                )
                            }
                        }

                        // Conversion hint
                        val rawD = state.diameterInput.toDoubleOrNull()
                        if (rawD != null && rawD > 0) {
                            val converted = if (state.diameterUnit == LengthUnit.MM) {
                                String.format(java.util.Locale.US, "%.3f pulgadas", rawD / 25.4)
                            } else {
                                String.format(java.util.Locale.US, "%.2f mm", rawD * 25.4)
                            }
                            Text(
                                text = "Equivalencia: $converted",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                if (state.isStep4Complete) {
                                    step4Expanded = false
                                    step5Expanded = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_diameter_button"),
                            enabled = state.isStep4Complete
                        ) {
                            Text("Continuar con este diámetro")
                        }
                    }
                }
            }

            // ================== PASO 5: CALIBRE DE CABLE ==================
            AnimatedVisibility(
                visible = state.isStep4Complete,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StepCard(
                    stepNumber = 5,
                    title = "Calibre de Cable Comercial",
                    isComplete = state.isStep5Complete,
                    summary = state.selectedGauge?.format(preferences.gaugeDisplayMode),
                    isExpanded = step5Expanded,
                    onToggleExpand = { step5Expanded = !step5Expanded }
                ) {
                    WireGaugeSelector(
                        selectedGauge = state.selectedGauge,
                        displayMode = preferences.gaugeDisplayMode,
                        onSelectGauge = { gauge ->
                            viewModel.selectWireGauge(gauge)
                            step5Expanded = false
                            step6Expanded = true
                        }
                    )
                }
            }

            // ================== PASO 6: NÚMERO DE VUELTAS ==================
            AnimatedVisibility(
                visible = state.isStep5Complete,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StepCard(
                    stepNumber = 6,
                    title = "Número de Vueltas (Espiras)",
                    isComplete = state.isStep6Complete,
                    summary = if (state.isStep6Complete) "N = ${state.turnsInput} vueltas" else null,
                    isExpanded = step6Expanded,
                    onToggleExpand = { step6Expanded = !step6Expanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.turnsInput,
                            onValueChange = { viewModel.setTurnsInput(it.filter { ch -> ch.isDigit() }) },
                            label = {
                                Text(if (state.inductorType == InductorType.TRANSFORMADOR) "Vueltas Primario (N1)" else "Vueltas (N)")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = if (state.inductorType == InductorType.TRANSFORMADOR) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (state.isStep6Complete) {
                                        step6Expanded = false
                                        if (state.currentType == CurrentType.AC) {
                                            step7Expanded = true
                                        } else {
                                            step8Expanded = true
                                        }
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_turns"),
                            singleLine = true
                        )

                        // If transformer, secondary turns
                        if (state.inductorType == InductorType.TRANSFORMADOR) {
                            OutlinedTextField(
                                value = state.secondaryTurnsInput,
                                onValueChange = { viewModel.setSecondaryTurnsInput(it.filter { ch -> ch.isDigit() }) },
                                label = { Text("Vueltas Secundario (N2)") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (state.isStep6Complete) {
                                            step6Expanded = false
                                            if (state.currentType == CurrentType.AC) {
                                                step7Expanded = true
                                            } else {
                                                step8Expanded = true
                                            }
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_secondary_turns"),
                                singleLine = true
                            )
                        }

                        Button(
                            onClick = {
                                if (state.isStep6Complete) {
                                    step6Expanded = false
                                    if (state.currentType == CurrentType.AC) {
                                        step7Expanded = true
                                    } else {
                                        step8Expanded = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_turns_button"),
                            enabled = state.isStep6Complete
                        ) {
                            Text("Continuar con estas vueltas")
                        }
                    }
                }
            }

            // ================== PASO 7: FRECUENCIA DE TRABAJO (SOLO SI AC) ==================
            AnimatedVisibility(
                visible = state.isStep6Complete && state.currentType == CurrentType.AC,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StepCard(
                    stepNumber = 7,
                    title = "Frecuencia de Trabajo",
                    isComplete = state.isStep7Complete,
                    summary = if (state.isStep7Complete) "${state.frequencyInput} ${state.frequencyUnit.symbol}" else null,
                    isExpanded = step7Expanded,
                    onToggleExpand = { step7Expanded = !step7Expanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.frequencyInput,
                                onValueChange = { viewModel.setFrequencyInput(it) },
                                label = { Text("Frecuencia (${state.frequencyUnit.symbol})") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        if (state.isStep7Complete) {
                                            step7Expanded = false
                                            step8Expanded = true
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_frequency"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FrequencyUnit.entries.forEach { unit ->
                                    FilterChip(
                                        selected = state.frequencyUnit == unit,
                                        onClick = { viewModel.selectFrequencyUnit(unit) },
                                        label = { Text(unit.symbol) },
                                        modifier = Modifier.testTag("freq_chip_${unit.name.lowercase()}")
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (state.isStep7Complete) {
                                    step7Expanded = false
                                    step8Expanded = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_frequency_button"),
                            enabled = state.isStep7Complete
                        ) {
                            Text("Continuar con esta frecuencia")
                        }
                    }
                }
            }

            // ================== PASO 8: VOLTAJE DE SIMULACIÓN ==================
            AnimatedVisibility(
                visible = state.isStep7Complete,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StepCard(
                    stepNumber = if (state.currentType == CurrentType.AC) 8 else 7,
                    title = "Voltaje de Simulación",
                    isComplete = state.isStep8Complete,
                    summary = if (state.isStep8Complete) "${state.voltageInput} V" else null,
                    isExpanded = step8Expanded,
                    onToggleExpand = { step8Expanded = !step8Expanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.voltageInput,
                            onValueChange = { viewModel.setVoltageInput(it) },
                            label = { Text("Voltaje de prueba (V)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.calculate()
                                    step8Expanded = false
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_voltage"),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.calculate()
                                step8Expanded = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("calculate_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Calcular y Simular Bobina",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ================== RESULTADOS CALCULADOS ==================
            AnimatedVisibility(
                visible = state.calculationResult != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                state.calculationResult?.let { result ->
                    ResultsSection(result = result)
                }
            }

            // ================== SIMULACIÓN 2D ==================
            AnimatedVisibility(
                visible = state.calculationResult != null && state.inductorType != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                state.calculationResult?.let { result ->
                    state.inductorType?.let { indType ->
                        CoilSimulationCanvas(
                            type = indType,
                            hasCore = state.hasCore ?: false,
                            coreMaterial = state.coreMaterial,
                            calculationResult = result,
                            isPaused = state.isSimulationPaused,
                            onTogglePause = { viewModel.toggleSimulationPause() },
                            onLiveVoltageChange = { viewModel.setLiveVoltage(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ---------------- SUB-COMPOSABLES ----------------

@Composable
private fun StepCard(
    stepNumber: Int,
    title: String,
    isComplete: Boolean,
    summary: String?,
    isExpanded: Boolean = true,
    onToggleExpand: (() -> Unit)? = null,
    collapsedContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val canToggle = onToggleExpand != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("step_card_$stepNumber"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (canToggle) Modifier.clickable { onToggleExpand() }
                        else Modifier
                    )
                    .testTag("step_header_$stepNumber"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isComplete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isComplete) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = "$stepNumber",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (summary != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = summary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (canToggle) {
                        IconButton(
                            onClick = onToggleExpand,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("step_toggle_$stepNumber")
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Recoger persiana" else "Desplegar persiana",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Vista resumida animada cuando la persiana se recoge
            AnimatedVisibility(
                visible = !isExpanded && isComplete && collapsedContent != null,
                enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(250, easing = FastOutSlowInEasing))
            ) {
                if (collapsedContent != null) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        collapsedContent()
                    }
                }
            }

            // Persiana animada que se despliega o se recoge
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(250)) + expandVertically(animationSpec = tween(350, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(300, easing = FastOutSlowInEasing))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WireGaugeSelector(
    selectedGauge: WireGauge?,
    displayMode: com.example.model.GaugeDisplayMode,
    onSelectGauge: (WireGauge) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedGauge?.format(displayMode) ?: "Seleccionar calibre",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .testTag("dropdown_wire_gauge"),
                label = { Text("Catálogo de cable comercial") }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                WireGauge.COMMERCIAL_CATALOG.forEach { gauge ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "AWG ${gauge.awg}",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "Ø %.2f mm", gauge.diameterMm),
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onSelectGauge(gauge)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        // Quick scrollable chips for fast one-tap selection
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(WireGauge.COMMERCIAL_CATALOG) { gauge ->
                FilterChip(
                    selected = selectedGauge == gauge,
                    onClick = { onSelectGauge(gauge) },
                    label = { Text("AWG ${gauge.awg}") },
                    modifier = Modifier.testTag("gauge_chip_${gauge.awg}")
                )
            }
        }

        selectedGauge?.let { g ->
            Text(
                text = "Diámetro: ${g.diameterMm} mm | Área transversal: ${String.format(java.util.Locale.US, "%.3f mm²", g.areaM2 * 1e6)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ResultsSection(result: CalculationResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("results_section_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Resultados del Cálculo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid of key values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResultItem(
                    label = "Resistencia DC",
                    value = result.formatResistance(),
                    hint = "R = ρ·L/A",
                    modifier = Modifier.weight(1f)
                )
                ResultItem(
                    label = "Inductancia L",
                    value = result.formatInductance(),
                    hint = "Wheeler",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResultItem(
                    label = if (result.isAC) "Impedancia Z (AC)" else "Impedancia Z (DC)",
                    value = result.formatImpedance(),
                    hint = if (result.isAC) "Z = √(R² + Xl²)" else "Z = R",
                    modifier = Modifier.weight(1f)
                )
                ResultItem(
                    label = "Corriente (I)",
                    value = result.formatCurrent(),
                    hint = "I = V / Z",
                    modifier = Modifier.weight(1f)
                )
            }

            if (result.isAC) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResultItem(
                        label = "Reactancia (Xl)",
                        value = result.formatReactance(),
                        hint = "Xl = 2π·f·L",
                        modifier = Modifier.weight(1f)
                    )
                    ResultItem(
                        label = "Flujo Magnético (Φ)",
                        value = result.formatMagneticFlux(),
                        hint = "Φ = L·I/N",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResultItem(
                        label = "Flujo Magnético (Φ)",
                        value = result.formatMagneticFlux(),
                        hint = "Φ = L·I/N",
                        modifier = Modifier.weight(1f)
                    )
                    ResultItem(
                        label = "Energía Almacenada",
                        value = result.formatEnergy(),
                        hint = "W = ½·L·I²",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // If transformer: display secondary parameters
            if (result.isTransformer) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Datos del Secundario (Transformador):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResultItem(
                        label = "Voltaje Secundario (V2)",
                        value = String.format(java.util.Locale.US, "%.2f V", result.secondaryVoltage),
                        hint = "V2 = V1 · (N2/N1)",
                        modifier = Modifier.weight(1f)
                    )
                    ResultItem(
                        label = "Corriente Secundario (I2)",
                        value = String.format(java.util.Locale.US, "%.2f A", result.secondaryCurrent),
                        hint = "I2 = I1 · (N1/N2)",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Wire length and dimensions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Longitud de alambre: ${result.formatWireLength()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Densidad de flujo: ${result.formatFluxDensity()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mandatory Educational Disclaimer
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Nota educativa",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚠️ Cálculos con fines educativos, no de precisión industrial. No consideran efecto pelicular (skin effect) extremo ni saturación no lineal del núcleo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultItem(
    label: String,
    value: String,
    hint: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PresetsBar(
    onSelectPreset: (String) -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = onReset,
            label = { Text("Reiniciar") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            modifier = Modifier.testTag("reset_preset_button")
        )

        AssistChip(
            onClick = { onSelectPreset("solenoid_rf") },
            label = { Text("Solenoide RF Aire") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            modifier = Modifier.testTag("preset_solenoid_rf")
        )

        AssistChip(
            onClick = { onSelectPreset("toroid_filter") },
            label = { Text("Filtro Toroidal Ferrita") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            modifier = Modifier.testTag("preset_toroid_filter")
        )

        AssistChip(
            onClick = { onSelectPreset("transformer_iron") },
            label = { Text("Transformador Hierro") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            modifier = Modifier.testTag("preset_transformer_iron")
        )
    }
}
