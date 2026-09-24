package com.example.data

import com.example.model.CalculationResult
import com.example.model.CoreMaterial
import com.example.model.CurrentType
import com.example.model.InductorType
import com.example.model.WireGauge
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

object CoilCalculator {
    const val COPPER_RESISTIVITY = 1.68e-8 // Ohm * meter (ρ a 20°C)
    const val VACUUM_PERMEABILITY = 4.0 * PI * 1e-7 // H/m (μ0)

    /**
     * Realiza todos los cálculos físicos y eléctricos de la bobina.
     */
    fun calculate(
        type: InductorType,
        hasCore: Boolean,
        material: CoreMaterial?,
        currentType: CurrentType,
        diameterMeters: Double,
        gauge: WireGauge,
        turns: Int,
        secondaryTurns: Int = 0,
        frequencyHz: Double = 0.0,
        voltage: Double
    ): CalculationResult {
        val safeTurns = max(1, turns)
        val safeDiameter = max(1e-4, diameterMeters)
        val coilRadius = safeDiameter / 2.0
        val coilCrossSectionArea = PI * coilRadius * coilRadius

        // Permeabilidad relativa (μr)
        val muR = if (!hasCore || material == null) {
            1.0
        } else {
            material.relativePermeability
        }

        // Longitud del conductor (L_cable = N * circunferencia media)
        val circumference = PI * safeDiameter
        val primaryWireLength = safeTurns * circumference
        val secondaryWireLength = if (type == InductorType.TRANSFORMADOR && secondaryTurns > 0) {
            secondaryTurns * circumference
        } else 0.0
        val totalWireLength = primaryWireLength + secondaryWireLength

        // Área transversal del cable (A = π * r_cable^2)
        val wireCrossSection = gauge.areaM2

        // 1. Resistencia DC (R = ρ * L / A)
        val dcResistance = (COPPER_RESISTIVITY * primaryWireLength) / wireCrossSection

        // Largo de la bobina aproximado (l = N * diametro_cable)
        val estimatedCoilLength = max(1e-4, safeTurns * gauge.diameterMeters)

        // 2. Cálculo de Inductancia aproximada (L) según tipo de inductor
        val inductanceHenrys = when (type) {
            InductorType.CILINDRICO -> {
                // Fórmula de Wheeler para bobina cilíndrica de una capa:
                // L = (μ0 * μr * N^2 * π * r^2) / (largo + 0.9 * r)
                val numerator = VACUUM_PERMEABILITY * muR * safeTurns * safeTurns * coilCrossSectionArea
                val denominator = estimatedCoilLength + (0.9 * coilRadius)
                numerator / denominator
            }
            InductorType.TOROIDAL -> {
                // Bobina toroidal:
                // L = (μ0 * μr * N^2 * A_core) / (2 * π * r_medio)
                // Se asume un radio de sección del toroide r_core proporcional (~0.25 del radio medio)
                val rCore = max(gauge.diameterMeters * 2, coilRadius * 0.25)
                val aCore = PI * rCore * rCore
                val numerator = VACUUM_PERMEABILITY * muR * safeTurns * safeTurns * aCore
                val denominator = 2.0 * PI * coilRadius
                numerator / denominator
            }
            InductorType.ANTENA -> {
                // Antena de cuadro circular / bucle magnético:
                // L = μ0 * μr * N^2 * r * (ln(8 * r / r_cable) - 2)
                val ratio = max(1.1, (8.0 * coilRadius) / gauge.radiusMeters)
                val logTerm = max(0.1, ln(ratio) - 2.0)
                VACUUM_PERMEABILITY * muR * safeTurns * safeTurns * coilRadius * logTerm
            }
            InductorType.TRANSFORMADOR -> {
                // Inductancia del devanado primario sobre núcleo cerrado:
                // L1 = (μ0 * μr * N1^2 * A_core) / l_magnética
                val magneticPathLength = max(1e-3, PI * safeDiameter * 1.2)
                val aCore = coilCrossSectionArea * 0.8
                (VACUUM_PERMEABILITY * muR * safeTurns * safeTurns * aCore) / magneticPathLength
            }
        }

        // 3. Frecuencia e Impedancia AC
        val isAC = currentType == CurrentType.AC
        val safeFrequency = if (isAC) max(0.0, frequencyHz) else 0.0

        // Xl = 2 * π * f * L
        val inductiveReactance = if (isAC) {
            2.0 * PI * safeFrequency * inductanceHenrys
        } else {
            0.0
        }

        // Z = √(R^2 + Xl^2)
        val impedance = if (isAC) {
            sqrt((dcResistance * dcResistance) + (inductiveReactance * inductiveReactance))
        } else {
            dcResistance
        }

        // 4. Corriente (I = V / Z)
        val safeVoltage = max(0.0, voltage)
        val currentAmps = if (impedance > 0.0) safeVoltage / impedance else 0.0

        // 5. Flujo magnético aproximado: Φ = (L * I) / N
        val magneticFluxWebers = if (safeTurns > 0) {
            (inductanceHenrys * currentAmps) / safeTurns
        } else {
            0.0
        }

        // Densidad de flujo B = Φ / Area
        val magneticFluxDensityTesla = if (coilCrossSectionArea > 0.0) {
            magneticFluxWebers / coilCrossSectionArea
        } else {
            0.0
        }

        // 6. Energía magnética almacenada: W = 0.5 * L * I^2
        val storedEnergyJoules = 0.5 * inductanceHenrys * currentAmps * currentAmps

        // 7. Cálculos de secundario para transformador
        val isTransformer = type == InductorType.TRANSFORMADOR && secondaryTurns > 0
        val secondaryVoltage = if (isTransformer && safeTurns > 0) {
            safeVoltage * (secondaryTurns.toDouble() / safeTurns.toDouble())
        } else 0.0
        val secondaryCurrent = if (isTransformer && secondaryTurns > 0) {
            currentAmps * (safeTurns.toDouble() / secondaryTurns.toDouble())
        } else 0.0

        return CalculationResult(
            wireLengthMeters = totalWireLength,
            wireCrossSectionM2 = wireCrossSection,
            dcResistance = dcResistance,
            inductanceHenrys = inductanceHenrys,
            inductiveReactance = inductiveReactance,
            impedance = impedance,
            simulationVoltage = safeVoltage,
            currentAmps = currentAmps,
            magneticFluxWebers = magneticFluxWebers,
            magneticFluxDensityTesla = magneticFluxDensityTesla,
            storedEnergyJoules = storedEnergyJoules,
            isAC = isAC,
            frequencyHz = safeFrequency,
            coilLengthMeters = estimatedCoilLength,
            turns = safeTurns,
            secondaryTurns = secondaryTurns,
            secondaryVoltage = secondaryVoltage,
            secondaryCurrent = secondaryCurrent,
            isTransformer = isTransformer
        )
    }
}
