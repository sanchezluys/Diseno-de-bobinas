package com.example.model

import java.util.Locale

data class CalculationResult(
    val wireLengthMeters: Double,
    val wireCrossSectionM2: Double,
    val dcResistance: Double, // Ohms (R)
    val inductanceHenrys: Double, // H (L)
    val inductiveReactance: Double, // Ohms (Xl)
    val impedance: Double, // Ohms (Z)
    val simulationVoltage: Double, // V
    val currentAmps: Double, // A (I)
    val magneticFluxWebers: Double, // Wb (Φ)
    val magneticFluxDensityTesla: Double, // T (B)
    val storedEnergyJoules: Double, // J (W)
    val isAC: Boolean,
    val frequencyHz: Double,
    val coilLengthMeters: Double,
    val turns: Int,
    val secondaryTurns: Int = 0,
    val secondaryVoltage: Double = 0.0,
    val secondaryCurrent: Double = 0.0,
    val isTransformer: Boolean = false
) {
    fun formatResistance(): String {
        return if (dcResistance < 1e-2) {
            String.format(Locale.US, "%.3f mΩ", dcResistance * 1e3)
        } else if (dcResistance < 1000) {
            String.format(Locale.US, "%.3f Ω", dcResistance)
        } else {
            String.format(Locale.US, "%.2f kΩ", dcResistance / 1e3)
        }
    }

    fun formatInductance(): String {
        return if (inductanceHenrys < 1e-6) {
            String.format(Locale.US, "%.2f nH", inductanceHenrys * 1e9)
        } else if (inductanceHenrys < 1e-3) {
            String.format(Locale.US, "%.2f µH", inductanceHenrys * 1e6)
        } else if (inductanceHenrys < 1.0) {
            String.format(Locale.US, "%.3f mH", inductanceHenrys * 1e3)
        } else {
            String.format(Locale.US, "%.3f H", inductanceHenrys)
        }
    }

    fun formatReactance(): String {
        return if (!isAC) "0.00 Ω (DC)"
        else if (inductiveReactance < 1000) {
            String.format(Locale.US, "%.2f Ω", inductiveReactance)
        } else {
            String.format(Locale.US, "%.2f kΩ", inductiveReactance / 1e3)
        }
    }

    fun formatImpedance(): String {
        return if (impedance < 1000) {
            String.format(Locale.US, "%.2f Ω", impedance)
        } else {
            String.format(Locale.US, "%.2f kΩ", impedance / 1e3)
        }
    }

    fun formatCurrent(): String {
        return if (currentAmps < 1e-3) {
            String.format(Locale.US, "%.2f µA", currentAmps * 1e6)
        } else if (currentAmps < 1.0) {
            String.format(Locale.US, "%.2f mA", currentAmps * 1e3)
        } else {
            String.format(Locale.US, "%.3f A", currentAmps)
        }
    }

    fun formatMagneticFlux(): String {
        val absFlux = kotlin.math.abs(magneticFluxWebers)
        return if (absFlux < 1e-6) {
            String.format(Locale.US, "%.2f nWb", magneticFluxWebers * 1e9)
        } else if (absFlux < 1e-3) {
            String.format(Locale.US, "%.2f µWb", magneticFluxWebers * 1e6)
        } else if (absFlux < 1.0) {
            String.format(Locale.US, "%.3f mWb", magneticFluxWebers * 1e3)
        } else {
            String.format(Locale.US, "%.3e Wb", magneticFluxWebers)
        }
    }

    fun formatFluxDensity(): String {
        val absB = kotlin.math.abs(magneticFluxDensityTesla)
        return if (absB < 1e-3) {
            String.format(Locale.US, "%.2f µT", magneticFluxDensityTesla * 1e6)
        } else if (absB < 1.0) {
            String.format(Locale.US, "%.2f mT", magneticFluxDensityTesla * 1e3)
        } else {
            String.format(Locale.US, "%.3f T", magneticFluxDensityTesla)
        }
    }

    fun formatEnergy(): String {
        return if (storedEnergyJoules < 1e-6) {
            String.format(Locale.US, "%.2f nJ", storedEnergyJoules * 1e9)
        } else if (storedEnergyJoules < 1e-3) {
            String.format(Locale.US, "%.2f µJ", storedEnergyJoules * 1e6)
        } else if (storedEnergyJoules < 1.0) {
            String.format(Locale.US, "%.2f mJ", storedEnergyJoules * 1e3)
        } else {
            String.format(Locale.US, "%.3f J", storedEnergyJoules)
        }
    }

    fun formatWireLength(): String {
        return if (wireLengthMeters < 1.0) {
            String.format(Locale.US, "%.1f cm", wireLengthMeters * 100)
        } else {
            String.format(Locale.US, "%.2f m", wireLengthMeters)
        }
    }
}
