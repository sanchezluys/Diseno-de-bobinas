package com.example

import com.example.data.CoilCalculator
import com.example.model.CoreMaterial
import com.example.model.CurrentType
import com.example.model.InductorType
import com.example.model.WireGauge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDcResistanceCalculation() {
        val gauge = WireGauge(awg = 18, diameterMm = 1.02)
        val result = CoilCalculator.calculate(
            type = InductorType.CILINDRICO,
            hasCore = false,
            material = CoreMaterial.AIRE,
            currentType = CurrentType.DC,
            diameterMeters = 0.025, // 25 mm
            gauge = gauge,
            turns = 50,
            voltage = 12.0
        )

        // L_cable = 50 * π * 0.025 ≈ 3.927 m
        // A = π * (1.02e-3 / 2)^2 ≈ 8.17e-7 m^2
        // R = 1.68e-8 * 3.927 / 8.17e-7 ≈ 0.0807 Ohms
        assertTrue("DC Resistance should be around 0.08 Ohms", result.dcResistance in 0.07..0.09)
        assertEquals(result.dcResistance, result.impedance, 1e-6)
        assertTrue(result.currentAmps > 0)
        assertTrue(result.inductanceHenrys > 0)
    }

    @Test
    fun testAcImpedanceCalculation() {
        val gauge = WireGauge(awg = 18, diameterMm = 1.02)
        val result = CoilCalculator.calculate(
            type = InductorType.CILINDRICO,
            hasCore = true,
            material = CoreMaterial.FERRITA,
            currentType = CurrentType.AC,
            diameterMeters = 0.025,
            gauge = gauge,
            turns = 100,
            frequencyHz = 100_000.0, // 100 kHz
            voltage = 10.0
        )

        assertTrue("Inductance with Ferrite should be high", result.inductanceHenrys > 1e-4)
        assertTrue("AC Reactance should be greater than 0", result.inductiveReactance > 0)
        assertTrue("Impedance should be greater than DC resistance", result.impedance > result.dcResistance)
    }

    @Test
    fun testToroidCalculation() {
        val gauge = WireGauge(awg = 20, diameterMm = 0.81)
        val result = CoilCalculator.calculate(
            type = InductorType.TOROIDAL,
            hasCore = true,
            material = CoreMaterial.FERRITA,
            currentType = CurrentType.AC,
            diameterMeters = 0.035, // 35 mm
            gauge = gauge,
            turns = 80,
            frequencyHz = 50_000.0,
            voltage = 12.0
        )

        assertTrue(result.inductanceHenrys > 0)
        assertTrue(result.magneticFluxWebers > 0)
    }

    @Test
    fun testEnamelledWireSpecs() {
        val spec18 = com.example.model.EnamelledWireSpec.findByAwg(18)
        org.junit.Assert.assertNotNull(spec18)
        spec18!!

        // AWG 18 bare diameter is ~1.024 mm
        assertTrue(spec18.bareDiameterMm in 1.0..1.05)
        // Grade 1 enamel increases outer diameter
        assertTrue(spec18.grade1DiameterMm > spec18.bareDiameterMm)
        // Grade 2 enamel has even thicker coating than Grade 1
        assertTrue(spec18.grade2DiameterMm > spec18.grade1DiameterMm)

        // Resistance for AWG 18 at 20°C: ~0.020 Ω/m
        assertTrue(spec18.resistancePerMeterOhm in 0.018..0.023)

        // Thermal variation: at 100°C resistance increases by ~31.4%
        val rAt100 = spec18.resistanceAtTemperature(100.0)
        assertTrue("Resistance must increase with temperature", rAt100 > spec18.resistancePerMeterOhm)
        val ratio = rAt100 / spec18.resistancePerMeterOhm
        assertTrue("Ratio should be approx 1 + 0.00393*80 ≈ 1.314", ratio in 1.30..1.33)
    }

    @Test
    fun testWireVarnishClasses() {
        val classes = com.example.model.WireVarnishClass.ALL_CLASSES
        assertTrue("Should have multiple standard thermal classes", classes.size >= 4)
        val classH = classes.find { it.className.contains("180") }
        org.junit.Assert.assertNotNull(classH)
        assertEquals(180, classH!!.maxTemperatureC)
        assertTrue(classH.baseResin.isNotEmpty())
    }
}
