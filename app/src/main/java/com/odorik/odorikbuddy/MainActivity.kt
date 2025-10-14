package com.odorik.odorikbuddy

import android.content.res.Configuration
import android.os.Bundle
import java.util.Locale
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.AndroidEntryPoint

import com.odorik.odorikbuddy.ui.navigation.AppNavigation
import com.odorik.odorikbuddy.ui.theme.OdorikBuddyTheme
import com.odorik.odorikbuddy.ui.calls.CallViewModel
import androidx.activity.viewModels

import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var localeManager: LocaleManager

    private val callViewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lang = LanguagePreferences.getPreferredLanguage(this)
        android.util.Log.d("MainActivity", "Explicitly loading lang: $lang")
        val locale = Locale(lang)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        android.util.Log.d("MainActivity", "Current locale after explicit update: ${resources.configuration.locales[0].language}")

        setContent {
            val isDarkMode = themeManager.isDarkMode.value

            OdorikBuddyTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        AppNavigation()

                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                    }
                    android.util.Log.d("MainActivity", "Locale after setContent: ${resources.configuration.locales[0].language}")
                }
            }
        }
    }

    fun updateLocale(lang: String) {
        localeManager.setPreferredLanguage(lang)
        val locale = Locale(lang)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }
}
