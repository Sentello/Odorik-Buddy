package com.odorik.odorikbuddy.ui.sms

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.ceil
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState

@Composable
private fun ApiMessage(response: String, isError: Boolean) {
    val message = when {
        response.startsWith("successfully_sent") -> {
            val credit = response.substringAfter("successfully_sent ").trim()
            stringResource(R.string.sms_successfully_sent, credit)
        }
        response == "successfully_enqueued" -> stringResource(R.string.sms_successfully_enqueued)
        response.startsWith("error missing_argument") -> {
            val args = response.substringAfter("error missing_argument ").trim()
            stringResource(R.string.sms_error_missing_argument, args)
        }
        response == "error empty_message" -> stringResource(R.string.sms_error_empty_message)
        response == "error forbidden_sender" -> stringResource(R.string.sms_error_forbidden_sender)
        response == "error unsupported_recipient" -> stringResource(R.string.sms_error_unsupported_recipient)
        response == "error low_balance" -> stringResource(R.string.sms_error_low_balance)
        response == "error gateway_failed" -> stringResource(R.string.sms_error_gateway_failed)
        response == "error invalid_delay_format" -> stringResource(R.string.sms_error_invalid_delay_format)
        response == "error delayed_into_past" -> stringResource(R.string.sms_error_delayed_into_past)
        else -> stringResource(R.string.sms_unknown_error)
    }
    Text(text = message, color = if (isError) Color.Red else Color.Green)
}


private fun calculateSmsSegments(message: String): Int {
    val isUnicode = message.any { it.code > 127 } 
    val singleLimit = if (isUnicode) 70 else 160
    val multiLimit = if (isUnicode) 67 else 153
    val charCount = message.length
    if (charCount == 0) return 0
    return if (charCount <= singleLimit) {
        1
    } else {
        ceil(charCount.toDouble() / multiLimit).toInt()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsScreen(viewModel: SmsViewModel = hiltViewModel()) {
    val allowedSenders by viewModel.allowedSenders.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    val error by viewModel.error.collectAsState()

    var recipient by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedSender by remember { mutableStateOf<String?>(null) }
    val delayed by viewModel.delayed.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var delayMode by remember { mutableStateOf("minutes") } 
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() + 86400000L,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis()
            }
        }
    )
    val timePickerState = rememberTimePickerState(initialHour = 0, initialMinute = 0, is24Hour = true) 

    
    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var phoneNumbers by remember { mutableStateOf(emptyList<String>()) }
    var showMultipartInfo by remember { mutableStateOf(false) }

    val delayedError by viewModel.delayedError.collectAsState()

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let {
                val numbers = viewModel.getPhoneNumbersFromContact(context.contentResolver, it)
                if (numbers.size == 1) {
                    recipient = numbers.first()
                } else if (numbers.size > 1) {
                    phoneNumbers = numbers
                    showPhoneNumberDialog = true
                }
                
            }
        }
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                launcher.launch(null)
            } else {
                
            }
        }
    )

    LaunchedEffect(Unit) { viewModel.fetchAllowedSenders() }

    LaunchedEffect(allowedSenders) {
        if (allowedSenders.size == 1 && selectedSender == null) {
            selectedSender = allowedSenders.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sms_title)) }
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
                value = recipient,
                onValueChange = { recipient = it },
                label = { Text(stringResource(R.string.recipient)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_CONTACTS
                            ) -> {
                                launcher.launch(null)
                            }
                            else -> {
                                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Contacts, contentDescription = stringResource(R.string.pick_contact))
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSender ?: stringResource(R.string.select_sender),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.sender)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    allowedSenders.forEach { sender ->
                        DropdownMenuItem(text = { Text(sender) }, onClick = {
                            selectedSender = sender
                            expanded = false
                        })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            val charCount = message.length
            val segments = calculateSmsSegments(message)
            val countColor = when {
                segments <= 1 -> Color.Green
                segments <= 3 -> Color(0xFFFFA500) 
                else -> Color.Red
            }
            val isMultipart = segments > 1

            OutlinedTextField(
                value = message,
                onValueChange = { newValue ->
                    if (newValue.length <= 765) {
                        message = newValue
                    }
                },
                label = { Text(stringResource(R.string.message)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                supportingText = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$charCount / 765${if (isMultipart) " ($segments messages)" else ""}",
                            color = countColor,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .semantics {
                                    contentDescription = "$charCount characters, ${if (isMultipart) "$segments messages" else "1 message"}"
                                }
                                .animateContentSize()
                        )
                        if (isMultipart) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Multipart SMS info",
                                tint = countColor,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { showMultipartInfo = true }
                            )
                        }
                    }
                }
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                SingleChoiceSegmentedButtonRow( 
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp) 
                ) {
                    SegmentedButton(
                        selected = delayMode == "minutes",
                        onClick = { delayMode = "minutes"; viewModel.onMinutesDelayedInputChange("") },
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                        modifier = Modifier.semantics { contentDescription = "Switch to minutes delay" }
                    ) {
                        Text(stringResource(R.string.sms_minutes))
                    }
                    SegmentedButton(
                        selected = delayMode == "datetime",
                        onClick = { delayMode = "datetime"; viewModel.setDateTimeDelayed("") },
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier.semantics { contentDescription = "Switch to specific time" }
                    ) {
                        Text(stringResource(R.string.sms_specific_time))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            
            val selectDateTimeText = stringResource(R.string.sms_select_date_time)
            val displayDelayed = remember(delayed) {
                if (delayed.isBlank()) {
                    selectDateTimeText
                } else {
                    try {
                        val instant = Instant.parse(delayed)
                        instant.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'"))
                    } catch (e: Exception) {
                        delayed
                    }
                }
            }

            if (delayMode == "minutes") {
                OutlinedTextField(
                    value = delayed,
                    onValueChange = { newValue ->
                        viewModel.onMinutesDelayedInputChange(newValue)
                    },
                    label = { Text(stringResource(R.string.sms_delay_minutes)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = delayedError != null,
                    supportingText = { delayedError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
            } else { 
                OutlinedTextField(
                    value = displayDelayed,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.sms_scheduled_time_utc)) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select date and time")
                        }
                    },
                    isError = delayedError != null,
                    supportingText = { delayedError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.sendSms(recipient, message, selectedSender) },
                
                enabled = recipient.isNotBlank() && message.isNotBlank() && delayedError == null,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.send_sms)) }
            if (error != null) ApiMessage(response = error!!, isError = true)
            if (sendResult != null) ApiMessage(response = sendResult!!, isError = false)
        }

        
        if (showPhoneNumberDialog) {
            AlertDialog(
                onDismissRequest = { showPhoneNumberDialog = false },
                title = { Text(stringResource(R.string.choose_phone_number)) },
                text = {
                    LazyColumn {
                        items(phoneNumbers) {
                            TextButton(onClick = {
                                recipient = it
                                showPhoneNumberDialog = false
                            }) {
                                Text(it)
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

        if (showMultipartInfo) {
            AlertDialog(
                onDismissRequest = { showMultipartInfo = false },
                title = { Text(stringResource(R.string.multipart_sms_title)) },
                text = { Text(stringResource(R.string.multipart_sms_message)) },
                confirmButton = {
                    TextButton(onClick = { showMultipartInfo = false }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        showTimePicker = true 
                    }) { Text(stringResource(R.string.sms_next)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text(stringResource(R.string.sms_select_time)) },
                text = {
                    TimePicker(state = timePickerState)
                },
                confirmButton = {
                    TextButton(onClick = {
                        showTimePicker = false
                        
                        val selectedDateMillis = datePickerState.selectedDateMillis ?: return@TextButton
                        val selectedDate = Instant.ofEpochMilli(selectedDateMillis)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        val localDateTime = LocalDateTime.of(selectedDate, selectedTime)
                        val zonedUtc = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"))

                        viewModel.setDateTimeDelayed(zonedUtc.format(DateTimeFormatter.ISO_INSTANT)) 
                    }) { Text(stringResource(R.string.ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }
    }
}