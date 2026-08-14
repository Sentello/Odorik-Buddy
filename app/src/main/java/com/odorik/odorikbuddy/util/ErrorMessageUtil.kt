package com.odorik.odorikbuddy.util

import android.content.Context
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LocaleManager
import com.odorik.odorikbuddy.data.repository.AuthenticationException
import com.odorik.odorikbuddy.data.repository.CredentialsNotSetException
import java.io.IOException
import java.net.UnknownHostException


object ErrorMessageUtil {


    fun errorResFor(throwable: Throwable?): Int? = when (throwable) {
        is CredentialsNotSetException -> R.string.auth_credentials_not_set
        is AuthenticationException -> R.string.invalid_credentials
        is UnknownHostException -> R.string.error_host_unresolvable
        is IOException -> R.string.error_network_unreachable
        else -> null
    }


    fun standardizeError(throwable: Throwable?, context: Context, localeManager: LocaleManager? = null): String {
        errorResFor(throwable)?.let {
            val localizedContext = localeManager?.createLocaleContext(context) ?: context
            return localizedContext.getString(it)
        }
        return standardizeError(throwable?.message, context, localeManager)
    }


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
            errorMessage.contains("credentials missing") ||
            errorMessage.contains("User credentials are not set") -> {
                localizedContext.getString(R.string.auth_credentials_not_set)
            }


            else -> errorMessage
        }
    }
}