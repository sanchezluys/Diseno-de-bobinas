package com.example.model

enum class LengthUnit(val symbol: String, val label: String) {
    MM(symbol = "mm", label = "Milímetros (mm)"),
    INCHES(symbol = "in", label = "Pulgadas (in)");

    fun toMillimeters(value: Double): Double = when (this) {
        MM -> value
        INCHES -> value * 25.4
    }

    fun fromMillimeters(valueMm: Double): Double = when (this) {
        MM -> valueMm
        INCHES -> valueMm / 25.4
    }
}

enum class GaugeDisplayMode(val label: String) {
    AWG(label = "Solo AWG"),
    MM(label = "Solo milímetros (mm)"),
    BOTH(label = "Ambos (AWG y mm)")
}

data class UserPreferences(
    val lengthUnit: LengthUnit = LengthUnit.MM,
    val gaugeDisplayMode: GaugeDisplayMode = GaugeDisplayMode.BOTH
)
