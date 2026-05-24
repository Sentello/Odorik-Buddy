package com.odorik.odorikbuddy

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import com.odorik.odorikbuddy.worker.UpdateWorkManager
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
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
        val lang = LanguagePreferences.getPreferredLanguage(base)
        val locale = Locale.forLanguageTag(lang)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
    }

    override fun onCreate() {
        super.onCreate()

        updateWorkManager.scheduleUpdateCheck()
    }
}