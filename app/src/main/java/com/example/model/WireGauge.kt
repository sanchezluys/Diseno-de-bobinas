package com.example.model

data class WireGauge(
    val awg: Int,
    val diameterMm: Double
) {
    val diameterMeters: Double get() = diameterMm * 1e-3
    val radiusMeters: Double get() = diameterMeters / 2.0
    val areaM2: Double get() = Math.PI * radiusMeters * radiusMeters

    fun format(mode: GaugeDisplayMode): String = when (mode) {
        GaugeDisplayMode.AWG -> "AWG $awg"
        GaugeDisplayMode.MM -> String.format(java.util.Locale.US, "Ø %.2f mm", diameterMm)
        GaugeDisplayMode.BOTH -> String.format(java.util.Locale.US, "AWG %d (%.2f mm)", awg, diameterMm)
    }

    companion object {
        val COMMERCIAL_CATALOG = listOf(
            WireGauge(awg = 10, diameterMm = 2.59),
            WireGauge(awg = 12, diameterMm = 2.05),
            WireGauge(awg = 14, diameterMm = 1.63),
            WireGauge(awg = 16, diameterMm = 1.29),
            WireGauge(awg = 18, diameterMm = 1.02),
            WireGauge(awg = 20, diameterMm = 0.81),
            WireGauge(awg = 22, diameterMm = 0.64),
            WireGauge(awg = 24, diameterMm = 0.51),
            WireGauge(awg = 26, diameterMm = 0.40),
            WireGauge(awg = 28, diameterMm = 0.32),
            WireGauge(awg = 30, diameterMm = 0.25)
        )

        val DEFAULT = COMMERCIAL_CATALOG[4] // AWG 18 (1.02 mm)
    }
}
