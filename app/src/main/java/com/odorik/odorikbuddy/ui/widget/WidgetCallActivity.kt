package com.odorik.odorikbuddy.ui.widget

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.SecurePreferences
import com.odorik.odorikbuddy.data.local.ThemeManager
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.repository.TileRepository
import com.odorik.odorikbuddy.domain.usecase.CallUseCase
import com.odorik.odorikbuddy.domain.usecase.CreateRouteUseCase
import com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase
import com.odorik.odorikbuddy.domain.usecase.GetSharedPublicNumbersUseCase
import com.odorik.odorikbuddy.ui.theme.OdorikBuddyTheme
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
    lateinit var getLinesUseCase: GetLinesUseCase

    @Inject
    lateinit var securePreferences: SecurePreferences

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tileId = intent.getIntExtra("tile_id", -1)
        if (tileId == -1) {
            finish()
            return
        }

        setContent {
            OdorikBuddyTheme(themeManager = themeManager) {
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

        lifecycleScope.launch {
            handleTileAction(tileId)
        }
    }
    
    private suspend fun handleTileAction(tileId: Int) {
        val tile = tileRepository.getTileById(tileId)
        if (tile == null) {
            showErrorAndFinish("Tile not found")
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
             
            val globalLineIdStr = securePreferences.getString("selected_line", null)
            val targetLineIdStr = if (!tile.lineId.isNullOrBlank()) tile.lineId else globalLineIdStr
            
            val selectedLineInfo = if (targetLineIdStr != null) {
                lines.find { it.id.toString() == targetLineIdStr }
            } else null

            
            val result = if (selectedLineInfo != null) {
                createRouteUseCase.executeWithLineCredentials(
                    publicNumber = lastSharedNumber,
                    sourceNumber = sourceNumber,
                    ringingNumber = tile.recipient,
                    replaceBySource = true,
                    useCallerIdPrefix = tile.useLineAsCallerId,
                    lineId = selectedLineInfo.id.toString(),
                    sipPassword = selectedLineInfo.sip_password
                )
            } else {
                 createRouteUseCase.execute(
                    publicNumber = lastSharedNumber,
                    sourceNumber = sourceNumber,
                    ringingNumber = tile.recipient,
                    replaceBySource = true,
                    useCallerIdPrefix = tile.useLineAsCallerId
                )
            }

            if (result.isSuccess) {
                launchDialer(lastSharedNumber)
            } else {
                showErrorAndFinish(result.exceptionOrNull()?.message ?: "Unknown error")
            }

        } catch (e: Exception) {
            showErrorAndFinish(e.message ?: "Unknown error")
        }
    }

    private fun launchDialer(phoneNumber: String) {
        val directCallsEnabled = sharedPreferences.getBoolean("direct_calls_enabled", false)
        val hasCallPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (directCallsEnabled && hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        }
        startActivity(intent)
        finish()
    }

    private fun showErrorAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
