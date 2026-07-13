package com.odorik.odorikbuddy.util

import android.content.Context
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Currency formatter that adapts to the app's language settings
 * - Czech (cs): Uses comma as decimal separator and "Kč" symbol
 * - English (en): Uses dot as decimal separator and "CZK" symbol
 */
@Singleton
class CurrencyFormatter @Inject constructor(
    private val context: Context
) {
    
    /**
     * Formats a monetary value based on the current app language
     * @param amount The amount to format
     * @param language The app language code (e.g., "cs", "en")
     * @return Formatted currency string
     */
    fun formatCurrency(amount: Double, language: String): String {
        return when (language) {
            "cs" -> {
                // Czech locale: comma as decimal separator, "Kč" symbol
                val czechFormat = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("cs").setRegion("CZ").build())
                czechFormat.maximumFractionDigits = 2
                czechFormat.minimumFractionDigits = if (amount == 0.0) 0 else 2
                "${czechFormat.format(amount)} Kč"
            }
            "en" -> {
                // English locale: dot as decimal separator, "CZK" symbol
                val englishFormat = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("US").build())
                englishFormat.maximumFractionDigits = 2
                englishFormat.minimumFractionDigits = if (amount == 0.0) 0 else 2
                "${englishFormat.format(amount)} CZK"
            }
            else -> {
                // Default to Czech format for unknown languages
                val defaultFormat = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("cs").setRegion("CZ").build())
                defaultFormat.maximumFractionDigits = 2
                defaultFormat.minimumFractionDigits = if (amount == 0.0) 0 else 2
                "${defaultFormat.format(amount)} Kč"
            }
        }
    }
    
    /**
     * Gets the currency symbol for the given language
     * @param language The app language code
     * @return Currency symbol
     */
    fun getCurrencySymbol(language: String): String {
        return when (language) {
            "cs" -> "Kč"
            "en" -> "CZK"
            else -> "Kč"
        }
    }
    
    /**
     * Checks if the given language uses comma as decimal separator
     * @param language The app language code
     * @return true if uses comma separator, false if uses dot separator
     */
    fun usesCommaSeparator(language: String): Boolean {
        return language == "cs"
    }
}