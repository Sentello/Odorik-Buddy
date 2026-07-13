package com.odorik.odorikbuddy

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import com.odorik.odorikbuddy.worker.UpdateWorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class OdorikBuddyApplication : Application(), androidx.work.Configuration.Provider {

    @Inject
    lateinit var updateWorkManager: UpdateWorkManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: androidx.work.Configuration
        get() = androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun attachBaseContext(base: Context) {
        // Lightweight attachBaseContext for the very first launch.
        // Main locale handling is done via AppCompatDelegate in LocaleManager.
        val lang = LanguagePreferences.getPreferredLanguage(base)
        val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(lang)
        val config = Configuration(base.resources.configuration)
        val platformLocales = localeList.unwrap() as? android.os.LocaleList ?: android.os.LocaleList.getEmptyLocaleList()
        config.setLocales(platformLocales)
        super.attachBaseContext(base.createConfigurationContext(config))
    }

    override fun onCreate() {
        super.onCreate()
        // Schedule update checking work if enabled
        updateWorkManager.scheduleUpdateCheck()
    }
}