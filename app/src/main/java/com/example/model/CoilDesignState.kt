package com.example.model

enum class FrequencyUnit(val symbol: String, val multiplier: Double) {
    HZ(symbol = "Hz", multiplier = 1.0),
    KHZ(symbol = "kHz", multiplier = 1e3),
    MHZ(symbol = "MHz", multiplier = 1e6);

    fun toHz(value: Double): Double = value * multiplier
}

data class CoilDesignState(
    // Step 1: Inductor Type
    val inductorType: InductorType? = null,

    // Step 2: Core
    val hasCore: Boolean? = null,
    val coreMaterial: CoreMaterial? = null,

    // Step 3: Current Type
    val currentType: CurrentType? = null,

    // Step 4: Diameter
    val diameterInput: String = "25.0",
    val diameterUnit: LengthUnit = LengthUnit.MM,

    // Step 5: Wire Gauge
    val selectedGauge: WireGauge? = null,

    // Step 6: Turns (N)
    val turnsInput: String = "50",
    val secondaryTurnsInput: String = "25", // for transformer

    // Step 7: Frequency (Only if AC)
    val frequencyInput: String = "1000",
    val frequencyUnit: FrequencyUnit = FrequencyUnit.HZ,

    // Step 8: Simulation Voltage (V)
    val voltageInput: String = "12.0",

    // Calculation result
    val calculationResult: CalculationResult? = null,
    val calculationError: String? = null,

    // 2D Simulation controls
    val isSimulationPaused: Boolean = false,
    val liveVoltage: Double? = null // For instant slider adjustments in simulation
) {
    // Step completion checks for progressive disclosure:
    val isStep1Complete: Boolean get() = inductorType != null

    val isStep2Complete: Boolean get() {
        if (!isStep1Complete) return false
        val has = hasCore ?: return false
        return !has || coreMaterial != null
    }

    val isStep3Complete: Boolean get() = isStep2Complete && currentType != null

    val isStep4Complete: Boolean get() {
        if (!isStep3Complete) return false
        val d = diameterInput.toDoubleOrNull()
        return d != null && d > 0
    }

    val isStep5Complete: Boolean get() = isStep4Complete && selectedGauge != null

    val isStep6Complete: Boolean get() {
        if (!isStep5Complete) return false
        val t = turnsInput.toIntOrNull()
        if (t == null || t < 1) return false
        if (inductorType == InductorType.TRANSFORMADOR) {
            val sec = secondaryTurnsInput.toIntOrNull()
            return sec != null && sec >= 1
        }
        return true
    }

    val isStep7Complete: Boolean get() {
        if (!isStep6Complete) return false
        if (currentType == CurrentType.DC) return true
        val f = frequencyInput.toDoubleOrNull()
        return f != null && f > 0
    }

    val isStep8Complete: Boolean get() {
        if (!isStep7Complete) return false
        val v = voltageInput.toDoubleOrNull()
        return v != null && v > 0
    }

    val canCalculate: Boolean get() = isStep8Complete
}
