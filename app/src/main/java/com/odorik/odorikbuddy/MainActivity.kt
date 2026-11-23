package com.odorik.odorikbuddy

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.ThemeManager
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
        // The main locale switching is handled via AppCompatDelegate; this covers first launch.
        super.attachBaseContext(LanguagePreferences.wrapContext(newBase))
    }

    fun updateLocale(lang: String) {
        localeManager.applyLocale(lang)
        recreate()
    }
}
