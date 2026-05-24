package com.odorik.odorikbuddy.util

import android.content.Context
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CurrencyFormatter @Inject constructor(
    private val context: Context
) {


    fun formatCurrency(amount: Double, language: String): String {
        return when (language) {
            "cs" -> {

                val czechFormat = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("cs").setRegion("CZ").build())
                czechFormat.maximumFractionDigits = 2
                czechFormat.minimumFractionDigits = if (amount == 0.0) 0 else 2
                "${czechFormat.format(amount)} Kč"
            }
            "en" -> {

                val englishFormat = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("US").build())
                englishFormat.maximumFractionDigits = 2
                englishFormat.minimumFractionDigits = if (amount == 0.0) 0 else 2
                "${englishFormat.format(amount)} CZK"
            }
            else -> {

                val defaultFormat = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("cs").setRegion("CZ").build())
                defaultFormat.maximumFractionDigits = 2
                defaultFormat.minimumFractionDigits = if (amount == 0.0) 0 else 2
                "${defaultFormat.format(amount)} Kč"
            }
        }
    }


    fun getCurrencySymbol(language: String): String {
        return when (language) {
            "cs" -> "Kč"
            "en" -> "CZK"
            else -> "Kč"
        }
    }


    fun usesCommaSeparator(language: String): Boolean {
        return language == "cs"
    }
}