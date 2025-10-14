package com.odorik.odorikbuddy.data.local

import android.content.Context
import android.content.SharedPreferences

object LanguagePreferences {

    private const val PREF_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "language"

    fun getPreferredLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lang = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        android.util.Log.d("LanguagePrefs", "Loaded language from prefs: $lang")
        return lang
    }

    fun setPreferredLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, lang).commit()
        android.util.Log.d("LanguagePrefs", "Saved language to prefs: $lang")
    }
}