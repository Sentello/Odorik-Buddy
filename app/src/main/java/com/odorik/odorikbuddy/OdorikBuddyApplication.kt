package com.odorik.odorikbuddy

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class OdorikBuddyApplication : Application() {

    override fun attachBaseContext(base: Context) {
        val lang = LanguagePreferences.getPreferredLanguage(base)
        android.util.Log.d("AppLocale", "attachBaseContext: Loaded lang from prefs: $lang")
        val locale = Locale(lang)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
        android.util.Log.d("AppLocale", "attachBaseContext: Set locale to $lang")
    }

    override fun onCreate() {
        super.onCreate()
    }
}