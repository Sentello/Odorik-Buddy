package com.odorik.odorikbuddy.ui.calls

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.theme.CallAccent
import com.odorik.odorikbuddy.ui.theme.CallAccentLight
import com.odorik.odorikbuddy.ui.theme.CallButton
import com.odorik.odorikbuddy.util.getResponsiveBodyLargeSize
import com.odorik.odorikbuddy.util.getResponsiveCardPadding
import com.odorik.odorikbuddy.util.getResponsivePadding
import com.odorik.odorikbuddy.util.getResponsiveSpacing
import com.odorik.odorikbuddy.util.getResponsiveTitleLargeSize
import kotlinx.coroutines.delay

private fun mapApiArgumentToStringId(apiArgument: String): Int {
    return when (apiArgument) {
        "caller" -> R.string.argument_caller
        "recipient" -> R.string.argument_recipient
        "line" -> R.string.argument_line
        else -> R.string.argument_unknown
    }
}

@Composable
private fun CallApiMessage(response: String, visible: Boolean) {
    val isError = response.startsWith("error")
    val message = when {
        response == "callback_ordered" -> stringResource(R.string.call_callback_ordered)
        response == "successfully_enqueued" -> stringResource(R.string.call_successfully_enqueued)
        response == "error callback_failed" -> stringResource(R.string.call_error_callback_failed)
        response.startsWith("error missing_argument") -> {
            val rawArgsString = response.substringAfter("error missing_argument ").trim()
            val rawArgsList = rawArgsString.split(',').map { it.trim() }
            val translatedArgs = rawArgsList.map { rawArg ->
                val stringId = mapApiArgumentToStringId(rawArg)
                stringResource(id = stringId)
            }
            val finalArgsString = translatedArgs.joinToString(", ")
            stringResource(R.string.call_error_missing_argument, finalArgsString)
        }
        response == "error invalid_delay_format" -> stringResource(R.string.call_error_invalid_delay_format)
        response == "error delayed_into_past" -> stringResource(R.string.call_error_delayed_into_past)
        response == "error invalid_line" -> stringResource(R.string.call_error_invalid_line)
        else -> if (response.isNotEmpty()) stringResource(R.string.call_unknown_error) else ""
    }
    
    if (message.isEmpty()) return
    
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

@Composable
private fun ErrorMessage(errorText: String, visible: Boolean) {
    AnimatedVisibility(
        visible = visible && errorText.isNotEmpty(),
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
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
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
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun GradientHeader(title: String) {
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
                            CallAccent.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(iconScale)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(CallAccent, CallAccentLight)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
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
        }
    }
}


enum class ContactField {
    CALLER_ID, RECIPIENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    viewModel: CallViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabOrder by viewModel.tabOrder.collectAsState()
    
    val tabItems = remember(tabOrder) {
        tabOrder.map { title ->
            when (title) {
                "callback_title" -> TabItem(
                    titleResId = R.string.callback_title,
                    title = "callback_title",
                    content = { CallbackTab(viewModel) }
                )
                "oneshot_call" -> TabItem(
                    titleResId = R.string.oneshot_call,
                    title = "oneshot_call",
                    content = { OneShotCallTab(viewModel) }
                )
                else -> TabItem(
                    titleResId = R.string.callback_title,
                    title = "callback_title",
                    content = { CallbackTab(viewModel) }
                )
            }
        }
    }
    
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            
            GradientHeader(title = stringResource(R.string.calls))
            
            
            DraggableTabs(
                tabItems = tabItems,
                selectedTabTitle = selectedTab,
                onTabSelected = { viewModel.updateSelectedTab(it) },
                onTabOrderChanged = { newOrder -> viewModel.updateTabOrder(newOrder) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallbackTab(
    viewModel: CallViewModel = hiltViewModel()
) {
    val callList by viewModel.callList.collectAsState()
    val callResult by viewModel.callResult.collectAsState()
    val lines by viewModel.lines.collectAsState()
    val callerId by viewModel.callerId.collectAsState()
    val recipient by viewModel.recipient.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedLine by viewModel.selectedLine.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var phoneNumbers by remember { mutableStateOf(emptyList<String>()) }
    var currentContactField by remember { mutableStateOf<ContactField?>(null) }
    var launcherToTrigger by remember { mutableStateOf<(() -> Unit)?>(null) }
    var contentVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let {
                val numbers = viewModel.getPhoneNumbersFromContact(context.contentResolver, it)
                if (numbers.size == 1) {
                    val number = numbers.first()
                    when (currentContactField) {
                        ContactField.CALLER_ID -> viewModel.updateCallerId(number)
                        ContactField.RECIPIENT -> viewModel.updateRecipient(number)
                        null -> {}
                    }
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
                launcherToTrigger?.invoke()
                launcherToTrigger = null
            }
        }
    )

    fun pickContact(field: ContactField) {
        currentContactField = field
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            contactPickerLauncher.launch(null)
        } else {
            launcherToTrigger = { contactPickerLauncher.launch(null) }
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.getCallList()
        viewModel.getLines()
        contentVisible = true
    }
    
    LaunchedEffect(callResult) {
        if (callResult.isNotEmpty() && !callResult.startsWith("error")) {
            delay(5000L)
            viewModel.resetCallResult()
        }
    }
    
    
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(getResponsivePadding())
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(400)) + 
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(400)
                    )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                
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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(getResponsiveSpacing())
                    ) {
                        
                        OutlinedTextField(
                            value = callerId,
                            onValueChange = { viewModel.updateCallerId(it) },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(start = 4.dp))
                                    Text(
                                        stringResource(R.string.caller_id),
                                        fontSize = getResponsiveBodyLargeSize()
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CallAccent,
                                focusedLabelColor = CallAccent
                            ),
                            trailingIcon = {
                                IconButton(onClick = { pickContact(ContactField.CALLER_ID) }) {
                                    Icon(
                                        Icons.Default.Contacts, 
                                        contentDescription = stringResource(R.string.pick_caller_id),
                                        tint = CallAccent
                                    )
                                }
                            }
                        )
                        
                        
                        OutlinedTextField(
                            value = recipient,
                            onValueChange = { viewModel.updateRecipient(it) },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(start = 4.dp))
                                    Text(
                                        stringResource(R.string.called_number),
                                        fontSize = getResponsiveBodyLargeSize()
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CallAccent,
                                focusedLabelColor = CallAccent
                            ),
                            trailingIcon = {
                                IconButton(onClick = { pickContact(ContactField.RECIPIENT) }) {
                                    Icon(
                                        Icons.Default.Contacts, 
                                        contentDescription = stringResource(R.string.pick_recipient),
                                        tint = CallAccent
                                    )
                                }
                            }
                        )
                        
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = lines.find { it.id == selectedLine }?.let { "${it.name} (${it.caller_id})" } ?: stringResource(R.string.select_line),
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.padding(start = 4.dp))
                                        Text(stringResource(R.string.line))
                                    }
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CallAccent,
                                    focusedLabelColor = CallAccent
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                lines.forEach { line ->
                                    DropdownMenuItem(
                                        text = { Text("${line.name} (${line.caller_id})") },
                                        onClick = {
                                            viewModel.updateSelectedLine(line.id)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        
                        Button(
                            onClick = {
                                selectedLine?.let { lineId ->
                                    viewModel.makeCall(callerId, recipient, lineId.toString())
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(buttonScale),
                            interactionSource = buttonInteractionSource,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CallButton
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.callback_call_button),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                
                if (callResult.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CallApiMessage(response = callResult, visible = true)
                }
                if (error?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ErrorMessage(errorText = error ?: "", visible = true)
                }
                
                
                if (callList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = stringResource(R.string.call_history_title),
                        fontSize = getResponsiveTitleLargeSize(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    callList.forEach { call ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(getResponsiveCardPadding())
                            ) {
                                CallHistoryRow(
                                    label = stringResource(R.string.from_label),
                                    value = call.callerId
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CallHistoryRow(
                                    label = stringResource(R.string.to_label),
                                    value = call.calledNumber
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CallHistoryRow(
                                    label = stringResource(R.string.duration_label),
                                    value = "${call.duration}s"
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CallHistoryRow(
                                    label = stringResource(R.string.cost_label),
                                    value = call.cost
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CallHistoryRow(
                                    label = stringResource(R.string.time_label),
                                    value = call.startTime
                                )
                            }
                        }
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
                        TextButton(
                            onClick = {
                                when (currentContactField) {
                                    ContactField.CALLER_ID -> viewModel.updateCallerId(number)
                                    ContactField.RECIPIENT -> viewModel.updateRecipient(number)
                                    else -> {}
                                }
                                showPhoneNumberDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(number, textAlign = TextAlign.Start)
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

@Composable
private fun CallHistoryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = getResponsiveBodyLargeSize(),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = getResponsiveBodyLargeSize(),
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneShotCallTab(
    callViewModel: CallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lines by callViewModel.lines.collectAsState()
    val recipient by callViewModel.recipient.collectAsState()
    val selectedLine by callViewModel.selectedLine.collectAsState()
    val oneShotCallResult by callViewModel.oneShotCallResult.collectAsState()
    val oneShotCallError by callViewModel.oneShotCallError.collectAsState()
    val isOneShotCallLoading by callViewModel.isOneShotCallLoading.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val directCallsEnabled = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getBoolean("direct_calls_enabled", false) }

    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var phoneNumbers by remember { mutableStateOf(emptyList<String>()) }
    var currentContactField by remember { mutableStateOf<ContactField?>(null) }
    var launcherToTrigger by remember { mutableStateOf<(() -> Unit)?>(null) }
    var contentVisible by remember { mutableStateOf(false) }
    
    val useCallerIdPrefix by callViewModel.useCallerIdPrefix.collectAsState()

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let {
                val numbers = callViewModel.getPhoneNumbersFromContact(context.contentResolver, it)
                if (numbers.size == 1) {
                    val number = numbers.first()
                    when (currentContactField) {
                        ContactField.CALLER_ID -> callViewModel.updateCallerId(number)
                        ContactField.RECIPIENT -> callViewModel.updateRecipient(number)
                        null -> {}
                    }
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
                launcherToTrigger?.invoke()
                launcherToTrigger = null
            }
        }
    )

    fun pickContact(field: ContactField) {
        currentContactField = field
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            contactPickerLauncher.launch(null)
        } else {
            launcherToTrigger = { contactPickerLauncher.launch(null) }
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    
    LaunchedEffect(Unit) {
        callViewModel.getLines()
        contentVisible = true
    }

    val previousResult = remember { mutableStateOf("") }
    val hasLaunchedDialer = remember { mutableStateOf(false) }
    
    LaunchedEffect(oneShotCallResult) {
        if (oneShotCallResult.isNotEmpty() && previousResult.value != oneShotCallResult) {
            previousResult.value = oneShotCallResult
            hasLaunchedDialer.value = false
        }
    }
    
    LaunchedEffect(oneShotCallResult, hasLaunchedDialer.value) {
        if (oneShotCallResult.isNotEmpty() && !hasLaunchedDialer.value) {
            try {
                val hasCallPermission = ContextCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
                
                val intent = if (directCallsEnabled && hasCallPermission) {
                    Intent(Intent.ACTION_CALL, Uri.parse("tel:$oneShotCallResult"))
                } else {
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$oneShotCallResult"))
                }
                
                context.startActivity(intent)
                hasLaunchedDialer.value = true
                callViewModel.resetOneShotCallResult()
            } catch (e: Exception) {
                hasLaunchedDialer.value = true
                callViewModel.resetOneShotCallResult()
            }
        }
    }
    
    
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(getResponsivePadding())
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(400)) + 
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(400)
                    )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                
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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(getResponsiveSpacing())
                    ) {
                        
                        OutlinedTextField(
                            value = recipient,
                            onValueChange = { callViewModel.updateRecipient(it) },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(start = 4.dp))
                                    Text(
                                        stringResource(R.string.called_number),
                                        fontSize = getResponsiveBodyLargeSize()
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CallAccent,
                                focusedLabelColor = CallAccent
                            ),
                            trailingIcon = {
                                IconButton(onClick = { pickContact(ContactField.RECIPIENT) }) {
                                    Icon(
                                        Icons.Default.Contacts, 
                                        contentDescription = stringResource(R.string.pick_recipient),
                                        tint = CallAccent
                                    )
                                }
                            }
                        )
                        
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = lines.find { it.id == selectedLine }?.let { "${it.name} (${it.caller_id})" } ?: stringResource(R.string.select_line),
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.padding(start = 4.dp))
                                        Text(stringResource(R.string.line))
                                    }
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CallAccent,
                                    focusedLabelColor = CallAccent
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                lines.forEach { line ->
                                    DropdownMenuItem(
                                        text = { Text("${line.name} (${line.caller_id})") },
                                        onClick = {
                                            callViewModel.updateSelectedLine(line.id)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { callViewModel.updateUseCallerIdPrefix(!useCallerIdPrefix) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = useCallerIdPrefix,
                                onCheckedChange = { callViewModel.updateUseCallerIdPrefix(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CallAccent
                                )
                            )
                            Text(
                                text = stringResource(R.string.use_line_number_as_caller_id),
                                fontSize = getResponsiveBodyLargeSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        
                        Button(
                            onClick = {
                                hasLaunchedDialer.value = false
                                callViewModel.makeOneShotCall(recipient, useCallerIdPrefix)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(buttonScale),
                            interactionSource = buttonInteractionSource,
                            enabled = !isOneShotCallLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CallButton
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            if (isOneShotCallLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.loading),
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.call),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                
                if (oneShotCallResult.isNotEmpty() && !isOneShotCallLoading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn(animationSpec = tween(300))
                    ) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.call_successfully_enqueued),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                if (oneShotCallError?.isNotEmpty() == true && !isOneShotCallLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ErrorMessage(errorText = oneShotCallError ?: "", visible = true)
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
                        TextButton(
                            onClick = {
                                when (currentContactField) {
                                    ContactField.CALLER_ID -> callViewModel.updateCallerId(number)
                                    ContactField.RECIPIENT -> callViewModel.updateRecipient(number)
                                    else -> {}
                                }
                                showPhoneNumberDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(number, textAlign = TextAlign.Start)
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