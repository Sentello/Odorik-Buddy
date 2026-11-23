package com.odorik.odorikbuddy.ui.main

import androidx.lifecycle.ViewModel
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes
import com.odorik.odorikbuddy.worker.UpdateWorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val updateWorkManager: UpdateWorkManager
) : ViewModel() {

    private val LAST_SCREEN_KEY = "last_screen"
    private val KEY_UPDATE_PROMPT_ANSWERED = "update_prompt_answered"

    private val _askAboutUpdates = MutableStateFlow(shouldAskAboutUpdates())
    /** True until the user has answered the one-time "Tell you about new versions?" prompt. */
    val askAboutUpdates: StateFlow<Boolean> = _askAboutUpdates.asStateFlow()

    fun getLastScreen(): String {
        return appPreferences.getString(LAST_SCREEN_KEY, NavigationRoutes.DASHBOARD) ?: NavigationRoutes.DASHBOARD
    }

    fun saveLastScreen(route: String) {
        appPreferences.saveString(LAST_SCREEN_KEY, route)
    }

    /**
     * The app is sideloaded, so this check is the only way users hear about new versions.
     * Ask once — also users updating from an older version whose checks are still off.
     */
    private fun shouldAskAboutUpdates(): Boolean {
        if (appPreferences.getString(KEY_UPDATE_PROMPT_ANSWERED) == "true") return false
        if (updateWorkManager.isAutoUpdateEnabled()) {
            // Already on (set in Settings or restored from a backup): nothing to ask.
            appPreferences.saveString(KEY_UPDATE_PROMPT_ANSWERED, "true")
            return false
        }
        return true
    }

    /** [enable] = the user said yes. Either way the prompt never shows again. */
    fun answerUpdatePrompt(enable: Boolean) {
        appPreferences.saveString(KEY_UPDATE_PROMPT_ANSWERED, "true")
        _askAboutUpdates.value = false
        if (enable) updateWorkManager.setAutoUpdateEnabled(true)
    }

    fun checkForUpdateNow() = updateWorkManager.performImmediateUpdateCheck()
}
