package com.odorik.odorikbuddy

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import com.odorik.odorikbuddy.worker.UpdateWorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import java.util.Locale

@HiltAndroidApp
class OdorikBuddyApplication : Application() {

    @Inject
    lateinit var updateWorkManager: UpdateWorkManager

    override fun attachBaseContext(base: Context) {
        val lang = LanguagePreferences.getPreferredLanguage(base)
        val locale = Locale(lang)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
    }

    override fun onCreate() {
        super.onCreate()
        
        updateWorkManager.scheduleUpdateCheck()
    }
}