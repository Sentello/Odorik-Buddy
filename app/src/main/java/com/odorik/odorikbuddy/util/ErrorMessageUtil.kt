package com.odorik.odorikbuddy.util

import android.content.Context
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LocaleManager


object ErrorMessageUtil {


    fun standardizeError(errorMessage: String?, context: Context, localeManager: LocaleManager? = null): String {
        if (errorMessage.isNullOrEmpty()) {
            val localizedContext = localeManager?.createLocaleContext(context) ?: context
            return localizedContext.getString(R.string.unknown_error)
        }


        val localizedContext = localeManager?.createLocaleContext(context) ?: context

        return when {

            errorMessage.contains("Unable to resolve host") ||
            errorMessage.contains("No address associated with hostname") ||
            errorMessage.contains("www.odorik.cz") -> {
                localizedContext.getString(R.string.error_host_unresolvable)
            }


            errorMessage.contains("No internet") ||
            errorMessage.contains("Network is unreachable") ||
            errorMessage.contains("Connection refused") ||
            errorMessage.contains("Timeout") -> {
                localizedContext.getString(R.string.error_network_unreachable)
            }


            errorMessage.contains("Invalid credentials") ||
            errorMessage.contains("authentication_failed") -> {
                localizedContext.getString(R.string.invalid_credentials)
            }


            errorMessage.contains("User not logged in") ||
            errorMessage.contains("credentials missing") -> {
                localizedContext.getString(R.string.auth_credentials_not_set)
            }


            else -> errorMessage
        }
    }
}