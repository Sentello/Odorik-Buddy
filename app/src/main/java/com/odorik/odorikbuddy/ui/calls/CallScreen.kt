package com.odorik.odorikbuddy.ui.calls

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R

@Composable
private fun CallApiMessage(response: String) {
    val message = when {
        response == "callback_ordered" -> stringResource(R.string.call_callback_ordered)
        response == "successfully_enqueued" -> stringResource(R.string.call_successfully_enqueued)
        response == "error callback_failed" -> stringResource(R.string.call_error_callback_failed)
        response.startsWith("error missing_argument") -> {
            val args = response.substringAfter("error missing_argument ").trim()
            stringResource(R.string.call_error_missing_argument, args)
        }
        response == "error invalid_delay_format" -> stringResource(R.string.call_error_invalid_delay_format)
        response == "error delayed_into_past" -> stringResource(R.string.call_error_delayed_into_past)
        response == "error invalid_line" -> stringResource(R.string.call_error_invalid_line)
        else -> if (response.isNotEmpty()) stringResource(R.string.call_unknown_error) else ""
    }
    if (message.isNotEmpty()) {
        Text(text = message, color = if (message.startsWith("Error") || message.startsWith("Chyba")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
    }
}

private fun getPhoneNumbersFromContact(contentResolver: ContentResolver, contactUri: Uri): List<String> {
    val numbers = mutableListOf<String>()
    contentResolver.query(contactUri, arrayOf(ContactsContract.Contacts._ID), null, null, null)?.use { contactCursor ->
        if (contactCursor.moveToFirst()) {
            val contactId = contactCursor.getString(contactCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val phoneSelection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
            val phoneSelectionArgs = arrayOf(contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                phoneProjection,
                phoneSelection,
                phoneSelectionArgs,
                null
            )?.use { phoneCursor ->
                while (phoneCursor.moveToNext()) {
                    var number = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    number = number.replace(Regex("[^0-9+]"), "") 
                    if (number.isNotBlank()) {
                        numbers.add(number)
                    }
                }
            }
        }
    }
    return numbers
}

enum class ContactField {
    CALLER_ID, RECIPIENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    viewModel: CallViewModel = hiltViewModel()
) {
    val callList = viewModel.callList.collectAsState()
    val callResult = viewModel.callResult.collectAsState()
    val lines by viewModel.lines.collectAsState()
    val callerId by viewModel.callerId.collectAsState()
    val recipient by viewModel.recipient.collectAsState()
    var selectedLine by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    
    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var phoneNumbers by remember { mutableStateOf(emptyList<String>()) }
    var currentContactField by remember { mutableStateOf<ContactField?>(null) }

    val context = LocalContext.current

    val callerIdLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let {
                val numbers = getPhoneNumbersFromContact(context.contentResolver, it)
                if (numbers.size == 1) {
                    viewModel.updateCallerId(numbers.first())
                } else if (numbers.size > 1) {
                    phoneNumbers = numbers
                    currentContactField = ContactField.CALLER_ID
                    showPhoneNumberDialog = true
                }
            }
        }
    )

    val recipientLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let {
                val numbers = getPhoneNumbersFromContact(context.contentResolver, it)
                if (numbers.size == 1) {
                    viewModel.updateRecipient(numbers.first())
                } else if (numbers.size > 1) {
                    phoneNumbers = numbers
                    currentContactField = ContactField.RECIPIENT
                    showPhoneNumberDialog = true
                }
            }
        }
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                
            }
            else {
                
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.getCallList()
        viewModel.getLines()
    }

    LaunchedEffect(lines) {
        Log.d("CallScreen", "Lines updated: $lines")
        if (selectedLine == null && lines.isNotEmpty()) {
            selectedLine = lines.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.callback_title)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = callerId,
                onValueChange = { viewModel.updateCallerId(it) },
                label = { Text(stringResource(R.string.caller_id)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_CONTACTS
                            ) -> {
                                callerIdLauncher.launch(null)
                            }
                            else -> {
                                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Contacts, contentDescription = stringResource(R.string.pick_caller_id))
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = recipient,
                onValueChange = { viewModel.updateRecipient(it) },
                label = { Text(stringResource(R.string.called_number)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_CONTACTS
                            ) -> {
                                recipientLauncher.launch(null)
                            }
                            else -> {
                                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Contacts, contentDescription = "Pick Recipient")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = lines.find { it.id == selectedLine }?.caller_id ?: stringResource(R.string.select_line),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.line)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    lines.forEach { line ->
                        DropdownMenuItem(text = { Text(line.caller_id) }, onClick = {
                            selectedLine = line.id
                            expanded = false
                        })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (selectedLine != null) {
                        viewModel.makeCall(callerId, recipient, selectedLine!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.call))
            }
            Spacer(modifier = Modifier.height(16.dp))
            CallApiMessage(response = callResult.value)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(callList.value) { call ->
                    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "${stringResource(R.string.from_label)} ${call.callerId}")
                            Text(text = "${stringResource(R.string.to_label)} ${call.calledNumber}")
                            Text(text = "${stringResource(R.string.duration_label)} ${call.duration}s")
                            Text(text = "${stringResource(R.string.cost_label)} ${call.cost}")
                            Text(text = "${stringResource(R.string.time_label)} ${call.startTime}")
                        }
                    }
                }
            }
        }

        
        if (showPhoneNumberDialog) {
            AlertDialog(
                onDismissRequest = { showPhoneNumberDialog = false },
                title = {
                    Text(
                        stringResource(
                            when (currentContactField) {
                                ContactField.CALLER_ID -> R.string.pick_caller_id_number
                                ContactField.RECIPIENT -> R.string.pick_recipient_number
                                else -> R.string.choose_phone_number
                            }
                        )
                    )
                },
                text = {
                    LazyColumn {
                        items(phoneNumbers) { number ->
                            TextButton(onClick = {
                                when (currentContactField) {
                                    ContactField.CALLER_ID -> viewModel.updateCallerId(number)
                                    ContactField.RECIPIENT -> viewModel.updateRecipient(number)
                                    else -> {}
                                }
                                showPhoneNumberDialog = false
                            }) {
                                Text(number)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPhoneNumberDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}