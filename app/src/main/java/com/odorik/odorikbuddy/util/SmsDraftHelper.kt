package com.odorik.odorikbuddy.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SmsDraftHelper @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sms_draft_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val RECIPIENT_KEY = "draft_recipient"
        private const val MESSAGE_KEY = "draft_message"
        private const val SENDER_KEY = "draft_sender"
    }

    fun saveDraft(recipient: String, message: String, sender: String?) {
        prefs.edit()
            .putString(RECIPIENT_KEY, recipient)
            .putString(MESSAGE_KEY, message)
            .putString(SENDER_KEY, sender)
            .apply()
    }

    fun loadDraft(): Triple<String, String, String?> {
        val recipient = prefs.getString(RECIPIENT_KEY, "") ?: ""
        val message = prefs.getString(MESSAGE_KEY, "") ?: ""
        val sender = prefs.getString(SENDER_KEY, null)
        return Triple(recipient, message, sender)
    }

    fun clearDraft() {
        prefs.edit()
            .remove(MESSAGE_KEY)
            .apply()
    }
}