package com.odorik.odorikbuddy.util

import android.content.Context
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LocaleManager

/**
 * Utility class for standardizing error messages across the app.
 * Converts raw exception messages into user-friendly, localized strings.
 */
object ErrorMessageUtil {

    /**
     * Standardizes error messages by mapping raw exceptions to user-friendly localized strings.
     *
     * @param errorMessage The raw error message from an exception
     * @param context Android context for accessing string resources
     * @param localeManager Optional LocaleManager to ensure proper localization
     * @return A user-friendly, localized error message
     */
    fun standardizeError(errorMessage: String?, context: Context, localeManager: LocaleManager? = null): String {
        if (errorMessage.isNullOrEmpty()) {
            val localizedContext = localeManager?.createLocaleContext(context) ?: context
            return localizedContext.getString(R.string.unknown_error)
        }

        // Use localized context for string resources
        val localizedContext = localeManager?.createLocaleContext(context) ?: context

        return when {
            // Network resolution errors (DNS, host unreachable)
            errorMessage.contains("Unable to resolve host") ||
            errorMessage.contains("No address associated with hostname") ||
            errorMessage.contains("www.odorik.cz") -> {
                localizedContext.getString(R.string.error_host_unresolvable)
            }

            // General network connectivity errors
            errorMessage.contains("No internet") ||
            errorMessage.contains("Network is unreachable") ||
            errorMessage.contains("Connection refused") ||
            errorMessage.contains("Timeout") -> {
                localizedContext.getString(R.string.error_network_unreachable)
            }

            // Authentication errors
            errorMessage.contains("Invalid credentials") ||
            errorMessage.contains("authentication_failed") -> {
                localizedContext.getString(R.string.invalid_credentials)
            }

            // User not logged in
            errorMessage.contains("User not logged in") ||
            errorMessage.contains("credentials missing") -> {
                localizedContext.getString(R.string.auth_credentials_not_set)
            }

            // For any other errors, return the original message
            else -> errorMessage
        }
    }
}