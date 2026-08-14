package com.odorik.odorikbuddy.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odorik.odorikbuddy.BuildConfig
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.repository.UpdateRepository
import com.odorik.odorikbuddy.model.AppUpdateInfo
import com.odorik.odorikbuddy.util.ErrorMessageUtil
import com.odorik.odorikbuddy.util.VersionUtils
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

        loadCachedUpdateInfo()
    }

    private fun loadCachedUpdateInfo() {
        updateRepository.getCachedUpdateInfo()?.let { cachedInfo ->
            _updateInfo.value = cachedInfo
        }
    }

    fun checkForUpdates() {

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


                    _error.value = ErrorMessageUtil.standardizeError(
                        exception,
                        context,
                        localeManager
                    )
                    _isLoading.value = false
                }
        }
    }

    fun isUpdateAvailable(): Boolean {
        val latestVersion = _updateInfo.value?.version
        return latestVersion?.let { VersionUtils.isNewer(it, BuildConfig.VERSION_NAME) } ?: false
    }
}