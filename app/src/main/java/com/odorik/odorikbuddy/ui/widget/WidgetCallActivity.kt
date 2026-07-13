package com.odorik.odorikbuddy.ui.widget

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.domain.usecase.CallUseCase
import com.odorik.odorikbuddy.domain.usecase.CreateRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.GetSharedPublicNumbersUseCase
import com.odorik.odorikbuddy.ui.calls.CallViewModel
import com.odorik.odorikbuddy.ui.theme.OdorikBuddyTheme
import com.odorik.odorikbuddy.util.PhoneCallLauncher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetCallActivity : ComponentActivity() {

    @Inject
    lateinit var tileRepository: TileRepository

    @Inject
    lateinit var createRouteUseCase: CreateRouteUseCase

    @Inject
    lateinit var callUseCase: CallUseCase

    @Inject
    lateinit var getSharedPublicNumbersUseCase: GetSharedPublicNumbersUseCase

    @Inject
    lateinit var securePreferences: SecurePreferences

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var appPreferences: AppPreferences

    private val callViewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tileId = intent.getIntExtra("tile_id", -1)
        if (tileId == -1) {
            finish()
            return
        }

        setContent {
            OdorikBuddyTheme(themeManager = themeManager) {
                val isLoading by callViewModel.isOneShotCallLoading.collectAsStateWithLifecycle()
                val oneShotResult by callViewModel.oneShotCallResult.collectAsStateWithLifecycle()
                val oneShotError by callViewModel.oneShotCallError.collectAsStateWithLifecycle()


                LaunchedEffect(oneShotResult, oneShotError) {
                    if (oneShotResult.isNotEmpty()) {
                        PhoneCallLauncher.launch(
                            context = this@WidgetCallActivity,
                            phoneNumber = oneShotResult,
                            directCallsEnabled = appPreferences.directCallsEnabled
                        )
                        callViewModel.resetOneShotCallResult()
                        finish()
                    } else if (!oneShotError.isNullOrEmpty()) {
                        showErrorAndFinish(oneShotError!!)
                        callViewModel.resetOneShotCallError()
            val globalCallerId = securePreferences.getString("caller_id", "") ?: ""
            val callerId = if (!tile.callerId.isNullOrBlank()) tile.callerId else globalCallerId


            val globalLine = securePreferences.getString("selected_line", null)
            val lineId = if (!tile.lineId.isNullOrBlank()) tile.lineId else globalLine

            if (callerId.isBlank()) {
                showErrorAndFinish(getString(R.string.callback_error_no_caller_id))
                return
            }

            val result = callUseCase.execute(
                callerId = callerId,
                recipient = tile.recipient,
                line = lineId ?: ""
            )

            result.onSuccess {
                Toast.makeText(this, getString(R.string.callback_success_notification, tile.recipient), Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {

                showErrorAndFinish(it.message ?: getString(R.string.widget_error_callback_failed))
            }

        } catch (e: Exception) {
             showErrorAndFinish(e.message ?: getString(R.string.widget_error_unknown))
        }
    }

    private suspend fun handleOneShotCall(tile: TileEntity) {

        val globalLineIdStr = securePreferences.getString("selected_line", null)
        val targetLineIdStr = if (!tile.lineId.isNullOrBlank()) tile.lineId else globalLineIdStr
        val targetLineId = targetLineIdStr?.toIntOrNull()


        callViewModel.makeOneShotCall(
            targetRecipient = tile.recipient,
            useLineAsCallerId = tile.useLineAsCallerId,
            selectedLineId = targetLineId
        )
    }

    private fun showErrorAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
