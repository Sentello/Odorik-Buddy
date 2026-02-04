package com.odorik.odorikbuddy.ui.sms

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.theme.CounterGreen
import com.odorik.odorikbuddy.ui.theme.CounterOrange
import com.odorik.odorikbuddy.ui.theme.CounterRed
import com.odorik.odorikbuddy.ui.theme.SmsAccent
import com.odorik.odorikbuddy.ui.theme.SmsAccentLight
import com.odorik.odorikbuddy.ui.theme.SmsSend
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
private fun ApiMessage(response: String, isError: Boolean, visible: Boolean) {
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

        
        response == stringResource(R.string.error_network_unreachable) -> stringResource(R.string.error_network_unreachable)
        response == stringResource(R.string.error_host_unresolvable) -> stringResource(R.string.error_host_unresolvable)

        
        response == stringResource(R.string.auth_credentials_not_set) -> stringResource(R.string.auth_credentials_not_set)
        response == stringResource(R.string.user_or_password_not_set) -> stringResource(R.string.user_or_password_not_set)

        
        response.startsWith("HTTP error:") -> {
            val code = response.substringAfter("HTTP error: ").trim()
            stringResource(R.string.error_unknown) + " (HTTP $code)"
        }

        
        else -> stringResource(R.string.sms_unknown_error)
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(250)
        ) + fadeOut(animationSpec = tween(200))
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isError) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = if (isError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
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

@Composable
private fun CircularCharacterCounter(
    charCount: Int,
    maxChars: Int = 765,
    segments: Int,
    onClick: () -> Unit
) {
    val progress = (charCount.toFloat() / maxChars).coerceIn(0f, 1f)
    
    val targetColor = when {
        segments <= 1 -> CounterGreen
        segments <= 3 -> CounterOrange
        else -> CounterRed
    }
    
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300),
        label = "counterColor"
    )
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "counterProgress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "$charCount characters, ${if (segments > 1) "$segments messages" else "1 message"}"
            }
    ) {
        
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(48.dp),
            color = animatedColor.copy(alpha = 0.2f),
            strokeWidth = 4.dp,
            trackColor = Color.Transparent
        )
        
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(48.dp),
            color = animatedColor,
            strokeWidth = 4.dp,
            trackColor = Color.Transparent
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = charCount.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
            if (segments > 1) {
                Text(
                    text = "×$segments",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = animatedColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun GradientHeader(
    title: String,
    onDelayClick: () -> Unit
) {
    var iconVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        iconVisible = true
    }
    
    val iconScale by animateFloatAsState(
        targetValue = if (iconVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SmsAccent.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .scale(iconScale)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(SmsAccent, SmsAccentLight)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontSize = getResponsiveTitleLargeSize(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDelayClick) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = stringResource(R.string.sms_delay_options_title),
                        tint = SmsAccent
                    )
                }
            }
        }
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
    var contentVisible by remember { mutableStateOf(false) }

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
        contentVisible = true
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
    
    
    val fabInteractionSource = remember { MutableInteractionSource() }
    val isFabPressed by fabInteractionSource.collectIsPressedAsState()
    val fabScale by animateFloatAsState(
        targetValue = if (isFabPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "fabScale"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.sendSms(recipient, message, selectedSender) },
                modifier = Modifier
                    .imePadding()
                    .scale(fabScale),
                interactionSource = fabInteractionSource,
                containerColor = SmsSend,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                ),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send_sms),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            
            GradientHeader(
                title = stringResource(R.string.sms_title),
                onDelayClick = { showDelayOptions = true }
            )
            
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(animationSpec = tween(400)) + 
                        slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(400)
                        )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            
                            OutlinedTextField(
                                value = recipient,
                                onValueChange = {
                                    recipient = it
                                    viewModel.saveDraft(it, message, selectedSender)
                                },
                                label = { Text(stringResource(R.string.recipient)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SmsAccent,
                                    focusedLabelColor = SmsAccent
                                ),
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
                                            contentDescription = stringResource(R.string.pick_contact),
                                            tint = SmsAccent
                                        )
                                    }
                                }
                            )

                            Spacer(Modifier.height(12.dp))

                            
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
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SmsAccent,
                                        focusedLabelColor = SmsAccent
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    allowedSenders.forEach { sender ->
                                        DropdownMenuItem(
                                            text = { Text(sender) },
                                            onClick = {
                                                selectedSender = sender
                                                expanded = false
                                                viewModel.saveDraft(recipient, message, selectedSender)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            val minLines = 3
                            val charCount = message.length
                            val segments = calculateSmsSegments(message)

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
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SmsAccent,
                                    focusedLabelColor = SmsAccent
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (segments > 1) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CounterOrange.copy(alpha = 0.1f))
                                            .clickable { showMultipartInfo = true }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Multipart SMS info",
                                            tint = CounterOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.multipart_sms_title),
                                            fontSize = 12.sp,
                                            color = CounterOrange,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                
                                CircularCharacterCounter(
                                    charCount = charCount,
                                    segments = segments,
                                    onClick = { showMultipartInfo = true }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    
                    if (error != null) {
                        ApiMessage(response = error!!, isError = true, visible = true)
                    }
                    if (sendResult != null) {
                        ApiMessage(response = sendResult!!, isError = false, visible = true)
                    }
                    
                    
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
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

        
        if (showDelayOptions) {
            ModalBottomSheet(
                onDismissRequest = { showDelayOptions = false },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(SmsAccent, SmsAccentLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.sms_delay_options_title),
                fontSize = getResponsiveTitleLargeSize(),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        
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
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.sms_minutes),
                        fontSize = getResponsiveBodyLargeSize() * 0.9f
                    )
                }
                SegmentedButton(
                    selected = delayMode == "datetime",
                    onClick = { onDelayModeChange("datetime"); viewModel.setDateTimeDelayed("") },
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
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
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SmsAccent,
                    focusedLabelColor = SmsAccent
                ),
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
                            modifier = Modifier.size(20.dp),
                            tint = SmsAccent
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
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SmsAccent,
                    focusedLabelColor = SmsAccent
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = getResponsiveBodyLargeSize() * 0.95f)
            )
        }
    }
}
