package com.odorik.odorikbuddy.util

import android.content.Context
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.repository.AuthenticationException
import com.odorik.odorikbuddy.data.repository.CredentialsNotSetException
import java.io.IOException
import java.net.UnknownHostException

/**
 * Utility class for standardizing error messages across the app.
 * Converts raw exception messages into user-friendly, localized strings.
 */
object ErrorMessageUtil {

    /**
     * Pure classifier: maps known exception types to a string resource.
     * Returns null when the throwable needs the message-text fallback.
     */
    fun errorResFor(throwable: Throwable?): Int? = when (throwable) {
        is CredentialsNotSetException -> R.string.auth_credentials_not_set
        is AuthenticationException -> R.string.invalid_credentials
        is UnknownHostException -> R.string.error_host_unresolvable
        is IOException -> R.string.error_network_unreachable
        is OdorikApiException -> odorikCodeRes[throwable.code]
        else -> null
    }

    /** Odorik error codes of the 1.6.0 endpoints. Unknown codes fall back to the raw text. */
    private val odorikCodeRes = mapOf(
        "unauthorized" to R.string.error_odorik_unauthorized,
        "authentication_failed" to R.string.invalid_credentials,
        "low_balance" to R.string.error_odorik_low_balance,
        "invalid_number" to R.string.error_speed_dial_invalid_number,
        "invalid_shortcut" to R.string.error_speed_dial_invalid_shortcut,
        "name_too_long" to R.string.error_speed_dial_name_too_long,
        "shortcut_already_used" to R.string.error_speed_dial_shortcut_used,
        "speed_dials_full" to R.string.error_speed_dial_full,
        "change_in_progress" to R.string.error_sim_change_in_progress,
        "mobile_data_disabled" to R.string.error_sim_mobile_data_disabled
    )

    /**
     * Preferred entry point: type-based mapping first, then the legacy
     * message-text matching as fallback.
     */
    fun standardizeError(throwable: Throwable?, context: Context, localeManager: LocaleManager? = null): String {
        errorResFor(throwable)?.let {
            val localizedContext = localeManager?.createLocaleContext(context) ?: context
            return localizedContext.getString(it)
        }
        return standardizeError(throwable?.message, context, localeManager)
    }

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

            // User not logged in / credentials missing
            errorMessage.contains("User not logged in") ||
            errorMessage.contains("credentials missing") ||
            errorMessage.contains("User credentials are not set") -> {
                localizedContext.getString(R.string.auth_credentials_not_set)
            }

            // For any other errors, return the original message
            else -> errorMessage
        }
    }
}