package com.odorik.odorikbuddy.ui.dashboard

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Y-axis scale of the spending chart: a round step from the 1–2–5 series (…, 0,20, 0,50,
 * 1, 2, 5, 10, …) and a top value that is a whole number of steps, so the labels read
 * 0 / 1 / 2 / 3 / 4 Kč instead of the chart library's 0,8 / 1,6 / 2,4.
 */
internal data class SpendingAxisScale(val step: Double, val max: Double) {
    /** Labels from 0 to [max], one per step. */
    val labelCount: Int get() = (max / step).roundToInt() + 1

    /** Whole koruny steps get whole-number labels ("2 Kč"); others always two decimals ("0,50 Kč"). */
    val wholeSteps: Boolean get() = step >= 1.0
}

/** Scale that fits [maxValue] (already including headroom) in at most [maxSteps] round steps. */
internal fun spendingAxisScale(maxValue: Double, maxSteps: Int = 4): SpendingAxisScale {
    if (maxValue <= 0.0 || maxValue.isNaN()) return SpendingAxisScale(step = 1.0, max = 1.0)
    val raw = maxValue / maxSteps
    val magnitude = 10.0.pow(floor(log10(raw)))
    val step = listOf(1.0, 2.0, 5.0, 10.0).map { it * magnitude }.first { it >= raw * (1 - 1e-9) }
    val steps = ceil(maxValue / step - 1e-9).coerceAtLeast(1.0)
    return SpendingAxisScale(step = step, max = steps * step)
}

/** "2 Kč" / "0,50 Kč" in Czech, "2 CZK" / "0.50 CZK" in English; zero is "0 Kč". */
internal fun formatAxisAmount(value: Double, wholeSteps: Boolean, language: String): String {
    val locale = if (language == "en") Locale.US else Locale.Builder().setLanguage("cs").setRegion("CZ").build()
    val symbol = if (language == "en") "CZK" else "Kč"
    val digits = if (wholeSteps || value == 0.0) 0 else 2
    val format = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = digits
        maximumFractionDigits = digits
    }
    return "${format.format(value)} $symbol"
}

internal class AxisAmountFormatter(
    private val wholeSteps: Boolean,
    private val language: String
) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String =
        // The library hands back float sums of the step (0.6000001); round to haléře first.
        formatAxisAmount((value * 100).roundToInt() / 100.0, wholeSteps, language)
}
