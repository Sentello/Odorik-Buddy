package com.odorik.odorikbuddy

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.ui.calls.CallViewModel
import com.odorik.odorikbuddy.ui.navigation.AppNavigation
import com.odorik.odorikbuddy.ui.theme.OdorikBuddyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var localeManager: LocaleManager

    private val callViewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Locale configuration is now handled in attachBaseContext

        setContent {
            OdorikBuddyTheme(themeManager = themeManager) {
                Surface(
                    modifier = Modifier.fillMaxSize(), 
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Lightweight attachBaseContext for the very first launch.
        // The main locale switching is now handled via AppCompatDelegate.
        val lang = LanguagePreferences.getPreferredLanguage(newBase)
        val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(lang)
        val config = Configuration(newBase.resources.configuration)
        val platformLocales = localeList.unwrap() as? android.os.LocaleList ?: android.os.LocaleList.getEmptyLocaleList()
        config.setLocales(platformLocales)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    fun updateLocale(lang: String) {
        localeManager.applyLocale(lang)
        recreate()
    }
}
