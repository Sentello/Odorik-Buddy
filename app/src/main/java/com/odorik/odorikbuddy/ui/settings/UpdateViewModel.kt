package com.odorik.odorikbuddy.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.BuildConfig
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.repository.UpdateRepository
import com.odorik.odorikbuddy.model.AppUpdateInfo
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val localeManager: LocaleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        // Load cached update info on initialization
        loadCachedUpdateInfo()
    }

    private fun loadCachedUpdateInfo() {
        updateRepository.getCachedUpdateInfo()?.let { cachedInfo ->
            _updateInfo.value = cachedInfo
        }
    }

    fun checkForUpdates() {
        // Avoid parallel requests
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            updateRepository.getAppUpdateInfo()
                .onSuccess { appUpdateInfo ->
                    _updateInfo.value = appUpdateInfo
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    // Optionally keep previous _updateInfo to avoid losing last known good state
                    // Map to a safe, user-visible message
                    _error.value = ErrorMessageUtil.standardizeError(
                        exception.message ?: context.getString(R.string.error_checking_for_updates),
                        context,
                        localeManager
                    )
                    _isLoading.value = false
                }
        }
    }

    fun isUpdateAvailable(): Boolean {
        val latestVersion = _updateInfo.value?.version
        // BuildConfig.VERSION_NAME is non-null; only guard on latestVersion
        return latestVersion?.let { compareVersions(it, BuildConfig.VERSION_NAME) > 0 } ?: false
    }

    private fun compareVersions(version1: String, version2: String): Int {
        val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until maxOf(v1Parts.size, v2Parts.size)) {
            val part1 = if (i < v1Parts.size) v1Parts[i] else 0
            val part2 = if (i < v2Parts.size) v2Parts[i] else 0
            
            if (part1 != part2) {
                return part1.compareTo(part2)
            }
        }
        
        return 0
    }
}