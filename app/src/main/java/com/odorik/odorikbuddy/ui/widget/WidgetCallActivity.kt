package com.odorik.odorikbuddy.ui.widget

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
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.domain.usecase.CallUseCase
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
    lateinit var callUseCase: CallUseCase

    @Inject
    lateinit var themeManager: ThemeManager

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
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.widget_call_connecting),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {

                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {

            if (!callViewModel.isOneShotCallLoading.value) {
                handleTileAction(tileId)
            } else {
                finish()
            }
        }
    }

    private suspend fun handleTileAction(tileId: Int) {
        val tile = tileRepository.getTileById(tileId)
        if (tile == null) {
            showErrorAndFinish(getString(R.string.widget_error_tile_not_found))
            return
        }

        if (tile.callType == "CALLBACK") {
            handleCallback(tile)
        } else {
            handleOneShotCall(tile)
        }
    }

    private suspend fun handleCallback(tile: TileEntity) {
        try {

        val globalLineIdStr = appPreferences.getString("selected_line", null)
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
