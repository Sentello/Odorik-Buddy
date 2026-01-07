package com.odorik.odorikbuddy.ui.sms

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.util.getResponsiveBodyLargeSize
import com.odorik.odorikbuddy.util.getResponsiveCardPadding
import com.odorik.odorikbuddy.util.getResponsiveMaxLines
import com.odorik.odorikbuddy.util.getResponsiveSpacing
import com.odorik.odorikbuddy.util.getResponsiveTitleLargeSize
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        }
    ) {
        Text(
            text = message,
            color = if (isError)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
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
    var expanded by remember { mutableStateOf(false) }
    var delayMode by remember { mutableStateOf("minutes") }
    var showDelayOptions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
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
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.fetchAllowedSenders()
        val (draftRecipient, draftMessage, draftSender) = viewModel.loadDraft()
        recipient = draftRecipient
        message = draftMessage
        selectedSender = draftSender
    }

    LaunchedEffect(allowedSenders) {
        if (allowedSenders.size == 1 && selectedSender == null) {
            selectedSender = allowedSenders.first()
        }
    }

    LaunchedEffect(sendResult) {
        if (sendResult?.startsWith("successfully") == true) {
            recipient = ""
            message = ""
            selectedSender = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sms_title)) },
                actions = {
                    IconButton(onClick = { showDelayOptions = true }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = stringResource(R.string.sms_delay_options_title)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.sendSms(recipient, message, selectedSender) },
                modifier = Modifier.imePadding()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send_sms)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = recipient,
                onValueChange = {
                    recipient = it
                    viewModel.saveDraft(it, message, selectedSender)
                },
                label = { Text(stringResource(R.string.recipient)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = {
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
                        }
                    ) {
                        Icon(
                            Icons.Default.Contacts,
                            contentDescription = stringResource(R.string.pick_contact)
                        )
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
                            viewModel.saveDraft(recipient, message, selectedSender)
                        })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            val minLines = 3

            OutlinedTextField(
                value = message,
                onValueChange = {
                    if (it.length <= 765) {
                        message = it
                        viewModel.saveDraft(recipient, it, selectedSender)
                    }
                },
                label = { Text(stringResource(R.string.message)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = minLines,
                maxLines = getResponsiveMaxLines(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                )
            )

            
            val charCount = message.length
            val segments = calculateSmsSegments(message)
            val countColor = when {
                segments <= 1 -> Color.Green
                segments <= 3 -> Color(0xFFFFA500) 
                else -> Color.Red
            }
            val isMultipart = segments > 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (isMultipart) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Multipart SMS info",
                        tint = countColor,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { showMultipartInfo = true }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "$charCount / 765${if (isMultipart) " ($segments)" else ""}",
                    color = countColor,
                    fontSize = getResponsiveBodyLargeSize() * 0.8f,
                    modifier = Modifier.semantics {
                        contentDescription = "$charCount characters, ${if (isMultipart) "$segments messages" else "1 message"}"
                    }
                )
            }

            if (error != null) ApiMessage(response = error!!, isError = true)
            if (sendResult != null) ApiMessage(response = sendResult!!, isError = false)

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
        }

        if (showDelayOptions) {
            ModalBottomSheet(
                onDismissRequest = { showDelayOptions = false },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = getResponsiveSpacing() * 2)
                ) {
                    DelayOptionsContent(
                        viewModel = viewModel,
                        delayMode = delayMode,
                        onDelayModeChange = { newMode -> delayMode = newMode },
                        showDatePicker = { showDatePicker = true }
                    )
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DelayOptionsContent(
    viewModel: SmsViewModel,
    delayMode: String,
    onDelayModeChange: (String) -> Unit,
    showDatePicker: () -> Unit
) {
    val delayed by viewModel.delayed.collectAsState()
    val delayedError by viewModel.delayedError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = getResponsiveCardPadding(), vertical = getResponsiveCardPadding()),
        verticalArrangement = Arrangement.spacedBy(getResponsiveSpacing())
    ) {
        Text(
            text = stringResource(R.string.sms_delay_options_title),
            fontSize = getResponsiveTitleLargeSize(),
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = getResponsiveCardPadding()/2),
            horizontalArrangement = Arrangement.Center
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                SegmentedButton(
                    selected = delayMode == "minutes",
                    onClick = { onDelayModeChange("minutes"); viewModel.onMinutesDelayedInputChange("") },
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                ) {
                    Text(
                        stringResource(R.string.sms_minutes),
                        fontSize = getResponsiveBodyLargeSize() * 0.9f
                    )
                }
                SegmentedButton(
                    selected = delayMode == "datetime",
                    onClick = { onDelayModeChange("datetime"); viewModel.setDateTimeDelayed("") },
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                ) {
                    Text(
                        stringResource(R.string.sms_specific_time),
                        fontSize = getResponsiveBodyLargeSize() * 0.9f
                    )
                }
            }
        }

        val selectDateTimeText = stringResource(R.string.sms_select_date_time)
        val displayDelayed = remember(delayed) {
            if (delayed.isBlank()) selectDateTimeText
            else try {
                val instant = Instant.parse(delayed)
                instant.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'"))
            } catch (e: Exception) { delayed }
        }

        if (delayMode == "minutes") {
            OutlinedTextField(
                value = delayed,
                onValueChange = { viewModel.onMinutesDelayedInputChange(it) },
                label = {
                    Text(
                        stringResource(R.string.sms_delay_minutes),
                        fontSize = getResponsiveBodyLargeSize() * 0.9f
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = delayedError != null,
                supportingText = {
                    delayedError?.let {
                        Text(
                            stringResource(it),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = getResponsiveBodyLargeSize() * 0.8f
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = getResponsiveBodyLargeSize() * 0.95f)
            )
        } else {
            OutlinedTextField(
                value = displayDelayed,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        stringResource(R.string.sms_scheduled_time_utc),
                        fontSize = getResponsiveBodyLargeSize() * 0.9f
                    )
                },
                trailingIcon = {
                    IconButton(onClick = showDatePicker) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Select date",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                isError = delayedError != null,
                supportingText = {
                    delayedError?.let {
                        Text(
                            stringResource(it),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = getResponsiveBodyLargeSize() * 0.8f
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = getResponsiveBodyLargeSize() * 0.95f)
            )
        }
    }
}
