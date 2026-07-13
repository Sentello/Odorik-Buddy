package com.odorik.odorikbuddy.data.local

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getPreferredLanguage(): String {
        return LanguagePreferences.getPreferredLanguage(context)
    }

    fun setPreferredLanguage(lang: String) {
        LanguagePreferences.setPreferredLanguage(context, lang)
    }


    fun applyLocale(lang: String) {
        setPreferredLanguage(lang)

        val localeList = LocaleListCompat.forLanguageTags(lang)
        AppCompatDelegate.setApplicationLocales(localeList)
    }


    fun createLocaleContext(base: Context): Context {
        val lang = LanguagePreferences.getPreferredLanguage(base)
        val localeList = LocaleListCompat.forLanguageTags(lang)
        val platformLocales = localeList.unwrap() as? android.os.LocaleList ?: android.os.LocaleList.getEmptyLocaleList()
        return base.createConfigurationContext(
            base.resources.configuration.apply {
                setLocales(platformLocales)
            }
        )
    }
}