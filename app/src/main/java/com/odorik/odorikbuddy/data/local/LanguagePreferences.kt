package com.odorik.odorikbuddy.data.local

import android.content.Context

object LanguagePreferences {

    private const val PREF_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "language"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getPreferredLanguage(context: Context): String {
        return prefs(context).getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun setPreferredLanguage(context: Context, lang: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, lang).apply()
    }

    /**
     * [base] with the app's chosen language applied. Every Activity (and the Application)
     * must wrap its base context with this in attachBaseContext; otherwise a screen follows
     * the phone's language instead of the app's (seen on the widget configuration screens).
     */
    fun wrapContext(base: Context): Context {
        val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(getPreferredLanguage(base))
        val config = android.content.res.Configuration(base.resources.configuration)
        val platformLocales = localeList.unwrap() as? android.os.LocaleList
            ?: android.os.LocaleList.getEmptyLocaleList()
        config.setLocales(platformLocales)
        return base.createConfigurationContext(config)
    }
}