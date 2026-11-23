package com.odorik.odorikbuddy.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartAxisTest {

    private fun assertScale(maxValue: Double, step: Double, max: Double) {
        val scale = spendingAxisScale(maxValue)
        assertEquals("step for $maxValue", step, scale.step, 1e-9)
        assertEquals("max for $maxValue", max, scale.max, 1e-9)
    }

    @Test
    fun `typical week of a few koruny uses whole steps`() {
        // 3,28 Kč + 15 % headroom: the case that showed 0,8 / 1,6 / 2,4 / 3,2.
        assertScale(3.28 * 1.15, step = 1.0, max = 4.0)
        assertEquals(5, spendingAxisScale(3.28 * 1.15).labelCount)
    }

    @Test
    fun `small spending uses haler steps`() {
        assertScale(0.575, step = 0.2, max = 0.6)
        assertScale(1.8, step = 0.5, max = 2.0)
        assertFalse(spendingAxisScale(1.8).wholeSteps)
    }

    @Test
    fun `larger spending uses 1-2-5 steps`() {
        assertScale(8.0, step = 2.0, max = 8.0)
        assertScale(17.0, step = 5.0, max = 20.0)
        assertScale(138.0, step = 50.0, max = 150.0)
        assertScale(1150.0, step = 500.0, max = 1500.0)
        assertTrue(spendingAxisScale(138.0).wholeSteps)
    }

    @Test
    fun `never more than five labels`() {
        listOf(0.01, 0.37, 0.99, 2.3, 4.01, 9.99, 41.0, 77.7, 333.0, 9999.0).forEach { v ->
            val scale = spendingAxisScale(v)
            assertTrue("$v fits", scale.max >= v)
            assertTrue("$v has ${scale.labelCount} labels", scale.labelCount in 2..5)
        }
    }

    @Test
    fun `zero or missing spending still gives a valid axis`() {
        assertScale(0.0, step = 1.0, max = 1.0)
        assertScale(Double.NaN, step = 1.0, max = 1.0)
    }

    @Test
    fun `labels are whole koruny or always two decimals`() {
        val nbsp = ' '
        assertEquals("2 Kč", formatAxisAmount(2.0, wholeSteps = true, language = "cs"))
        assertEquals("1${nbsp}500 Kč", formatAxisAmount(1500.0, wholeSteps = true, language = "cs"))
        assertEquals("0,50 Kč", formatAxisAmount(0.5, wholeSteps = false, language = "cs"))
        assertEquals("0 Kč", formatAxisAmount(0.0, wholeSteps = false, language = "cs"))
        assertEquals("0.20 CZK", formatAxisAmount(0.2, wholeSteps = false, language = "en"))
        assertEquals("4 CZK", formatAxisAmount(4.0, wholeSteps = true, language = "en"))
    }

    @Test
    fun `float noise from the chart library is rounded away`() {
        val formatter = AxisAmountFormatter(wholeSteps = false, language = "cs")
        assertEquals("0,60 Kč", formatter.getFormattedValue(0.6000001f))
    }
}
