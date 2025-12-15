package com.odorik.odorikbuddy.data.local

import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val KEY_LANGUAGE = "language"

    fun getPreferredLanguage(): String {
        return LanguagePreferences.getPreferredLanguage(context)
    }

    fun setPreferredLanguage(lang: String) {
        LanguagePreferences.setPreferredLanguage(context, lang)
    }

    fun createLocaleContext(base: Context): Context {
        val lang = LanguagePreferences.getPreferredLanguage(base)
        val locale = Locale(lang)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }

    
    fun updateLocale(context: Context, lang: String): Context {
        setPreferredLanguage(lang)
        val locale = Locale(lang)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

