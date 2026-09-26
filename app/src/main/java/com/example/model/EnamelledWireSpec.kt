package com.example.model

import java.util.Locale
import kotlin.math.PI

/**
 * Información técnica de las clases térmicas de los barnices de aislamiento
 * según normas NEMA MW 1000 e IEC 60317 para alambres esmaltados de bobinado.
 */
data class WireVarnishClass(
    val className: String,
    val maxTemperatureC: Int,
    val standardCode: String,
    val baseResin: String,
    val isSolderable: Boolean,
    val solderTemperature: String,
    val typicalUses: String,
    val advantages: String,
    val dielectricStrengthKvMm: Double = 100.0 // Rigidez dieléctrica típica kV/mm
) {
    companion object {
        val ALL_CLASSES = listOf(
            WireVarnishClass(
                className = "Clase B (130°C)",
                maxTemperatureC = 130,
                standardCode = "NEMA MW 75 / IEC 60317-20",
                baseResin = "Poliuretano (UEW)",
                isSolderable = true,
                solderTemperature = "360°C – 390°C (Directa)",
                typicalUses = "Bobinas pequeñas, relés, transformadores de audio, inductores de RF, balastos electrónicos.",
                advantages = "Termosoldable directamente sin necesidad de raspar o decapar el esmalte químicamente."
            ),
            WireVarnishClass(
                className = "Clase F (155°C)",
                maxTemperatureC = 155,
                standardCode = "NEMA MW 79 / IEC 60317-21",
                baseResin = "Poliuretano modificado termosoldable (UEW-F)",
                isSolderable = true,
                solderTemperature = "380°C – 420°C",
                typicalUses = "Transformadores toroidales, fuentes conmutadas (SMPS), bobinas automotrices y solenoides.",
                advantages = "Excelente combinación de autosoldabilidad con mayor margen térmico continuo."
            ),
            WireVarnishClass(
                className = "Clase H (180°C)",
                maxTemperatureC = 180,
                standardCode = "NEMA MW 30 / IEC 60317-08",
                baseResin = "Poliéster-imida (PEI / EI)",
                isSolderable = false,
                solderTemperature = "Requiere decapado mecánico o térmico a > 480°C",
                typicalUses = "Motores de tracción, alternadores, transformadores de potencia en seco, bobinas de frenos.",
                advantages = "Muy alta resistencia a choques térmicos, disolventes de impregnación y sobrecargas mecánicas."
            ),
            WireVarnishClass(
                className = "Clase C / Dual Coat (200°C)",
                maxTemperatureC = 200,
                standardCode = "NEMA MW 35 / IEC 60317-13",
                baseResin = "Poliéster-imida + sobrecapa de Poliamida-imida (EI/AIW)",
                isSolderable = false,
                solderTemperature = "Requiere decapado previo",
                typicalUses = "Motores herméticos de refrigeración, inversores industriales, bobinados sometidos a pulsos PWM de alta frecuencia.",
                advantages = "Doble capa que previene microfisuras por descargas parciales y resistencia química extrema a refrigerantes Freón/R134a."
            ),
            WireVarnishClass(
                className = "Clase 220°C Ultra",
                maxTemperatureC = 220,
                standardCode = "NEMA MW 16 / IEC 60317-46",
                baseResin = "Poliimida pura (PI / Pyre-ML)",
                isSolderable = false,
                solderTemperature = "Requiere decapado abrasivo",
                typicalUses = "Aeroespacial, aplicaciones militares, bobinas para reactores nucleares o ambientes de radiación y ultra alta temperatura.",
                advantages = "Máxima estabilidad térmica y resistencia a radiaciones electromagnéticas ionizantes."
            )
        )
    }
}

/**
 * Especificación técnica de un alambre de cobre electrolítico recocido esmaltado (Magnet Wire).
 */
data class EnamelledWireSpec(
    val awg: Int,
    val bareDiameterMm: Double,
    val grade1DiameterMm: Double, // Aislamiento simple / Single build
    val grade2DiameterMm: Double, // Aislamiento doble / Heavy build
    val breakdownVoltageGrade1V: Int,
    val breakdownVoltageGrade2V: Int
) {
    val bareDiameterInches: Double get() = bareDiameterMm / 25.4

    // Área en mm² y m²
    val crossSectionMm2: Double get() {
        val r = bareDiameterMm / 2.0
        return PI * r * r
    }
    val crossSectionM2: Double get() = crossSectionMm2 * 1e-6

    // Resistencia nominal a 20°C: R = ρ * L / A con ρ = 1.68e-8 Ω·m
    val resistancePerMeterOhm: Double get() = (1.68e-8 * 1.0) / crossSectionM2
    val resistancePerKmOhm: Double get() = resistancePerMeterOhm * 1000.0

    // Densidad del cobre ≈ 8.89 g/cm³ = 8890 kg/m³
    // Masa por metro = volumen_metro * densidad = (A en m² * 1 m) * 8890 kg/m³
    val weightKgPerMeter: Double get() = crossSectionM2 * 8890.0
    val metersPerKg: Double get() = if (weightKgPerMeter > 0) 1.0 / weightKgPerMeter else 0.0

    // Corriente máxima recomendada según densidad de corriente:
    // 3 A/mm² (conservador para transformadores cerrados o bobinas sin ventilación)
    val maxCurrent3A: Double get() = crossSectionMm2 * 3.0
    // 5 A/mm² (estándar para bobinas abiertas o régimen intermitente)
    val maxCurrent5A: Double get() = crossSectionMm2 * 5.0

    // Espesor medio del esmalte en micras (μm)
    val insulationThicknessGrade1Um: Double get() = ((grade1DiameterMm - bareDiameterMm) / 2.0) * 1000.0
    val insulationThicknessGrade2Um: Double get() = ((grade2DiameterMm - bareDiameterMm) / 2.0) * 1000.0

    /**
     * Resistencia corregida a una temperatura T en °C usando el coeficiente térmico del cobre α = 0.00393 / °C.
     */
    fun resistanceAtTemperature(tempC: Double): Double {
        val alpha = 0.00393
        return resistancePerMeterOhm * (1.0 + alpha * (tempC - 20.0))
    }

    fun formatResistancePerMeter(): String {
        return if (resistancePerMeterOhm < 0.01) {
            String.format(Locale.US, "%.3f mΩ/m", resistancePerMeterOhm * 1000.0)
        } else if (resistancePerMeterOhm < 1.0) {
            String.format(Locale.US, "%.4f Ω/m", resistancePerMeterOhm)
        } else {
            String.format(Locale.US, "%.3f Ω/m", resistancePerMeterOhm)
        }
    }

    fun formatResistancePerKm(): String {
        return if (resistancePerKmOhm < 1000.0) {
            String.format(Locale.US, "%.2f Ω/km", resistancePerKmOhm)
        } else {
            String.format(Locale.US, "%.2f kΩ/km", resistancePerKmOhm / 1000.0)
        }
    }

    companion object {
        /**
         * Catálogo comercial detallado de calibres AWG 10 hasta AWG 32 para bobinado.
         * Diámetros con esmalte basados en estándares NEMA MW 1000 Grado 1 y Grado 2.
         */
        val CATALOG: List<EnamelledWireSpec> = listOf(
            EnamelledWireSpec(awg = 10, bareDiameterMm = 2.588, grade1DiameterMm = 2.662, grade2DiameterMm = 2.730, breakdownVoltageGrade1V = 3200, breakdownVoltageGrade2V = 6400),
            EnamelledWireSpec(awg = 12, bareDiameterMm = 2.053, grade1DiameterMm = 2.121, grade2DiameterMm = 2.184, breakdownVoltageGrade1V = 3000, breakdownVoltageGrade2V = 6000),
            EnamelledWireSpec(awg = 14, bareDiameterMm = 1.628, grade1DiameterMm = 1.692, grade2DiameterMm = 1.748, breakdownVoltageGrade1V = 2800, breakdownVoltageGrade2V = 5600),
            EnamelledWireSpec(awg = 16, bareDiameterMm = 1.291, grade1DiameterMm = 1.349, grade2DiameterMm = 1.402, breakdownVoltageGrade1V = 2600, breakdownVoltageGrade2V = 5200),
            EnamelledWireSpec(awg = 18, bareDiameterMm = 1.024, grade1DiameterMm = 1.077, grade2DiameterMm = 1.125, breakdownVoltageGrade1V = 2400, breakdownVoltageGrade2V = 4800),
            EnamelledWireSpec(awg = 20, bareDiameterMm = 0.812, grade1DiameterMm = 0.861, grade2DiameterMm = 0.904, breakdownVoltageGrade1V = 2200, breakdownVoltageGrade2V = 4400),
            EnamelledWireSpec(awg = 22, bareDiameterMm = 0.644, grade1DiameterMm = 0.688, grade2DiameterMm = 0.729, breakdownVoltageGrade1V = 2000, breakdownVoltageGrade2V = 4000),
            EnamelledWireSpec(awg = 24, bareDiameterMm = 0.511, grade1DiameterMm = 0.551, grade2DiameterMm = 0.587, breakdownVoltageGrade1V = 1800, breakdownVoltageGrade2V = 3600),
            EnamelledWireSpec(awg = 26, bareDiameterMm = 0.405, grade1DiameterMm = 0.442, grade2DiameterMm = 0.472, breakdownVoltageGrade1V = 1600, breakdownVoltageGrade2V = 3200),
            EnamelledWireSpec(awg = 28, bareDiameterMm = 0.321, grade1DiameterMm = 0.353, grade2DiameterMm = 0.381, breakdownVoltageGrade1V = 1400, breakdownVoltageGrade2V = 2800),
            EnamelledWireSpec(awg = 30, bareDiameterMm = 0.255, grade1DiameterMm = 0.282, grade2DiameterMm = 0.307, breakdownVoltageGrade1V = 1200, breakdownVoltageGrade2V = 2400),
            EnamelledWireSpec(awg = 32, bareDiameterMm = 0.202, grade1DiameterMm = 0.226, grade2DiameterMm = 0.246, breakdownVoltageGrade1V = 1000, breakdownVoltageGrade2V = 2000)
        )

        fun findByAwg(awg: Int): EnamelledWireSpec? = CATALOG.find { it.awg == awg }
    }
}
