package com.odorik.odorikbuddy.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

/**
 * Centralized helper for launching phone calls or the dialer.
 *
 * This reduces duplication between CallScreen (one-shot flow) and WidgetCallActivity.
 */
object PhoneCallLauncher {

    /**
     * Launches either ACTION_CALL (if direct calls enabled + permission granted)
     * or falls back to ACTION_DIAL.
     */
    fun launch(
        context: Context,
        phoneNumber: String,
        directCallsEnabled: Boolean
    ) {
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (directCallsEnabled && hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        }

        context.startActivity(intent)
    }
}
