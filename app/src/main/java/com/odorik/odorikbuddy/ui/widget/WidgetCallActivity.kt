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
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.AppPreferences
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.ui.calls.CallViewModel
import com.odorik.odorikbuddy.ui.theme.OdorikBuddyTheme
import com.odorik.odorikbuddy.util.PhoneCallLauncher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WidgetCallActivity : ComponentActivity() {

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
                val isCallbackLoading by callViewModel.isCallbackLoading.collectAsStateWithLifecycle()
                val isOneShotLoading by callViewModel.isOneShotCallLoading.collectAsStateWithLifecycle()
                val oneShotResult by callViewModel.oneShotCallResult.collectAsStateWithLifecycle()
                val oneShotError by callViewModel.oneShotCallError.collectAsStateWithLifecycle()
                val callbackError by callViewModel.callbackError.collectAsStateWithLifecycle()


                LaunchedEffect(oneShotResult) {
                    if (oneShotResult.isNotEmpty()) {
                        PhoneCallLauncher.launch(
                            context = this@WidgetCallActivity,
                            phoneNumber = oneShotResult,
                            directCallsEnabled = appPreferences.directCallsEnabled
                        )
                        callViewModel.resetOneShotCallResult()
                        finish()
                    }
                }


                LaunchedEffect(Unit) {
                    callViewModel.widgetCallbackSucceeded.collect { recipient ->
                        Toast.makeText(
                            this@WidgetCallActivity,
                            getString(R.string.callback_success_notification, recipient),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

                LaunchedEffect(oneShotError, callbackError) {
                    val message = oneShotError?.takeIf { it.isNotEmpty() }
                        ?: callbackError?.takeIf { it.isNotEmpty() }
                    if (message != null) {

                        showErrorAndFinish(message)
                        callViewModel.resetOneShotCallError()
                        callViewModel.resetCallbackError()
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
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isCallbackLoading || isOneShotLoading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.widget_call_connecting),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }


        callViewModel.dispatchWidgetTileAction(tileId)
    }

    private fun showErrorAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
