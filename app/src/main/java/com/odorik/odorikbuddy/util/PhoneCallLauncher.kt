package com.odorik.odorikbuddy.util

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.odorik.odorikbuddy.R


object PhoneCallLauncher {


    fun launch(
        context: Context,
        phoneNumber: String,
        directCallsEnabled: Boolean
    ) {
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED


        val uri = Uri.fromParts("tel", phoneNumber, null)
        val intent = if (directCallsEnabled && hasCallPermission) {
            Intent(Intent.ACTION_CALL, uri)
        } else {
            Intent(Intent.ACTION_DIAL, uri)
        }
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_no_phone_app, Toast.LENGTH_LONG).show()
        }
    }
}
