package com.example.ui.screen.design

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CoilCalculator
import com.example.data.PreferencesRepository
import com.example.model.CoilDesignState
import com.example.model.CoreMaterial
import com.example.model.CurrentType
import com.example.model.FrequencyUnit
import com.example.model.InductorType
import com.example.model.LengthUnit
import com.example.model.UserPreferences
import com.example.model.WireGauge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CoilViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application)

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _uiState = MutableStateFlow(CoilDesignState())
    val uiState: StateFlow<CoilDesignState> = _uiState.asStateFlow()

    init {
        // Sync default unit from preferences when preferences load
        viewModelScope.launch {
            userPreferences.collect { prefs ->
                _uiState.update { current ->
                    if (current.selectedGauge == null) {
                        current.copy(
                            diameterUnit = prefs.lengthUnit,
                            selectedGauge = WireGauge.DEFAULT
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    // Step 1: Inductor Type
    fun selectInductorType(type: InductorType) {
        _uiState.update { current ->
            val newState = current.copy(
                inductorType = type,
                // If toroidal or transformer, usually has core by default
                hasCore = if (type == InductorType.TOROIDAL || type == InductorType.TRANSFORMADOR) true else current.hasCore,
                coreMaterial = if (type == InductorType.TOROIDAL) CoreMaterial.FERRITA else current.coreMaterial
            )
            recalculateIfPossible(newState)
        }
    }

    // Step 2: Core
    fun setHasCore(hasCore: Boolean) {
        _uiState.update { current ->
            val material = if (!hasCore) CoreMaterial.AIRE else current.coreMaterial ?: CoreMaterial.FERRITA
            val newState = current.copy(
                hasCore = hasCore,
                coreMaterial = material
            )
            recalculateIfPossible(newState)
        }
    }

    fun selectCoreMaterial(material: CoreMaterial) {
        _uiState.update { current ->
            val newState = current.copy(coreMaterial = material)
            recalculateIfPossible(newState)
        }
    }

    // Step 3: Current Type
    fun selectCurrentType(type: CurrentType) {
        _uiState.update { current ->
            val newState = current.copy(currentType = type)
            recalculateIfPossible(newState)
        }
    }

    // Step 4: Diameter
    fun setDiameterInput(input: String) {
        _uiState.update { current ->
            val newState = current.copy(diameterInput = input)
            recalculateIfPossible(newState)
        }
    }

    fun toggleDiameterUnit(newUnit: LengthUnit) {
        _uiState.update { current ->
            if (current.diameterUnit == newUnit) return@update current
            val currentVal = current.diameterInput.toDoubleOrNull()
            val convertedVal = if (currentVal != null) {
                if (newUnit == LengthUnit.INCHES) {
                    // mm to inches
                    LengthUnit.INCHES.fromMillimeters(currentVal)
                } else {
                    // inches to mm
                    LengthUnit.INCHES.toMillimeters(currentVal)
                }
            } else null

            val formattedInput = if (convertedVal != null) {
                String.format(java.util.Locale.US, "%.2f", convertedVal)
            } else current.diameterInput

            val newState = current.copy(
                diameterUnit = newUnit,
                diameterInput = formattedInput
            )
            recalculateIfPossible(newState)
        }
    }

    // Step 5: Wire Gauge
    fun selectWireGauge(gauge: WireGauge) {
        _uiState.update { current ->
            val newState = current.copy(selectedGauge = gauge)
            recalculateIfPossible(newState)
        }
    }

    // Step 6: Turns
    fun setTurnsInput(input: String) {
        _uiState.update { current ->
            val newState = current.copy(turnsInput = input)
            recalculateIfPossible(newState)
        }
    }

    fun setSecondaryTurnsInput(input: String) {
        _uiState.update { current ->
            val newState = current.copy(secondaryTurnsInput = input)
            recalculateIfPossible(newState)
        }
    }

    // Step 7: Frequency
    fun setFrequencyInput(input: String) {
        _uiState.update { current ->
            val newState = current.copy(frequencyInput = input)
            recalculateIfPossible(newState)
        }
    }

    fun selectFrequencyUnit(unit: FrequencyUnit) {
        _uiState.update { current ->
            val newState = current.copy(frequencyUnit = unit)
            recalculateIfPossible(newState)
        }
    }

    // Step 8: Voltage
    fun setVoltageInput(input: String) {
        _uiState.update { current ->
            val newState = current.copy(voltageInput = input)
            recalculateIfPossible(newState)
        }
    }

    // Simulation Live Voltage adjustment from slider
    fun setLiveVoltage(voltage: Double) {
        _uiState.update { current ->
            val formatted = String.format(java.util.Locale.US, "%.1f", voltage)
            val updated = current.copy(voltageInput = formatted, liveVoltage = voltage)
            recalculateIfPossible(updated)
        }
    }

    fun toggleSimulationPause() {
        _uiState.update { it.copy(isSimulationPaused = !it.isSimulationPaused) }
    }

    fun calculate() {
        _uiState.update { recalculateIfPossible(it) }
    }

    private fun recalculateIfPossible(state: CoilDesignState): CoilDesignState {
        if (!state.canCalculate) {
            return state.copy(calculationResult = null, calculationError = null)
        }

        val type = state.inductorType ?: return state
        val hasCore = state.hasCore ?: false
        val material = if (hasCore) state.coreMaterial else CoreMaterial.AIRE
        val currentType = state.currentType ?: return state
        val rawDiameter = state.diameterInput.toDoubleOrNull() ?: return state
        val diameterMm = state.diameterUnit.toMillimeters(rawDiameter)
        val diameterMeters = diameterMm * 1e-3
        val gauge = state.selectedGauge ?: return state
        val turns = state.turnsInput.toIntOrNull() ?: return state
        val secondaryTurns = state.secondaryTurnsInput.toIntOrNull() ?: 0

        val freqVal = if (currentType == CurrentType.AC) {
            val f = state.frequencyInput.toDoubleOrNull() ?: 0.0
            state.frequencyUnit.toHz(f)
        } else 0.0

        val voltage = state.voltageInput.toDoubleOrNull() ?: return state

        return try {
            val result = CoilCalculator.calculate(
                type = type,
                hasCore = hasCore,
                material = material,
                currentType = currentType,
                diameterMeters = diameterMeters,
                gauge = gauge,
                turns = turns,
                secondaryTurns = secondaryTurns,
                frequencyHz = freqVal,
                voltage = voltage
            )
            state.copy(calculationResult = result, calculationError = null)
        } catch (e: Exception) {
            state.copy(calculationResult = null, calculationError = "Error al calcular: ${e.message}")
        }
    }

    fun loadPreset(presetName: String) {
        when (presetName) {
            "solenoid_rf" -> {
                _uiState.value = CoilDesignState(
                    inductorType = InductorType.CILINDRICO,
                    hasCore = false,
                    coreMaterial = CoreMaterial.AIRE,
                    currentType = CurrentType.AC,
                    diameterInput = "20.0",
                    diameterUnit = LengthUnit.MM,
                    selectedGauge = WireGauge.COMMERCIAL_CATALOG[5], // AWG 20
                    turnsInput = "30",
                    frequencyInput = "10.0",
                    frequencyUnit = FrequencyUnit.MHZ,
                    voltageInput = "5.0"
                ).let { recalculateIfPossible(it) }
            }
            "toroid_filter" -> {
                _uiState.value = CoilDesignState(
                    inductorType = InductorType.TOROIDAL,
                    hasCore = true,
                    coreMaterial = CoreMaterial.FERRITA,
                    currentType = CurrentType.AC,
                    diameterInput = "35.0",
                    diameterUnit = LengthUnit.MM,
                    selectedGauge = WireGauge.COMMERCIAL_CATALOG[4], // AWG 18
                    turnsInput = "60",
                    frequencyInput = "100.0",
                    frequencyUnit = FrequencyUnit.KHZ,
                    voltageInput = "12.0"
                ).let { recalculateIfPossible(it) }
            }
            "transformer_iron" -> {
                _uiState.value = CoilDesignState(
                    inductorType = InductorType.TRANSFORMADOR,
                    hasCore = true,
                    coreMaterial = CoreMaterial.HIERRO,
                    currentType = CurrentType.AC,
                    diameterInput = "40.0",
                    diameterUnit = LengthUnit.MM,
                    selectedGauge = WireGauge.COMMERCIAL_CATALOG[3], // AWG 16
                    turnsInput = "120",
                    secondaryTurnsInput = "24",
                    frequencyInput = "60.0",
                    frequencyUnit = FrequencyUnit.HZ,
                    voltageInput = "120.0"
                ).let { recalculateIfPossible(it) }
            }
        }
    }

    fun resetDesign() {
        val prefUnit = userPreferences.value.lengthUnit
        _uiState.value = CoilDesignState(
            diameterUnit = prefUnit,
            selectedGauge = WireGauge.DEFAULT
        )
    }
}
