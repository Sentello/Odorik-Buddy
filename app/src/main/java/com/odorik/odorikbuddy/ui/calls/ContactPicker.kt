package com.odorik.odorikbuddy.ui.calls

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

data class Contact(
    val name: String,
    val phoneNumber: String
)

@Composable
fun ContactPicker(
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val cursor: Cursor? = context.contentResolver.query(
                    uri,
                    null,
                    null,
                    null,
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val name = it.getString(
                            it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                        )
                        val id = it.getString(
                            it.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                        )
                        val phoneCursor: Cursor? = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(id),
                            null
                        )
                        phoneCursor?.use {
                            if (it.moveToFirst()) {
                                val phoneNumber = it.getString(
                                    it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                )
                                val contact = Contact(name, phoneNumber)
                                selectedContact = contact
                                onContactSelected(contact)
                            }
                        }
                    }
                }
            }
        }
    }

    Column(modifier = modifier) {
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = ContactsContract.Contacts.CONTENT_TYPE
                }
                launcher.launch(intent)
            }
        ) {
            Text(if (selectedContact != null) "Change Contact" else "Pick Contact")
        }
        selectedContact?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Selected: ${it.name} (${it.phoneNumber})")
        }
    }
}