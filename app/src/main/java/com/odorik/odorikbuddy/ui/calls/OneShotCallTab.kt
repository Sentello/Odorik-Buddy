package com.odorik.odorikbuddy.ui.calls

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.SimCard
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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneShotCallTab(
    callViewModel: CallViewModel
) {
    val context = LocalContext.current
    val lines by callViewModel.lines.collectAsState()
    val recipient by callViewModel.oneShotRecipient.collectAsState()
    val recipientContactName by callViewModel.oneShotRecipientContactName.collectAsState()
    val selectedLine by callViewModel.selectedLine.collectAsState()
    val oneShotCallResult by callViewModel.oneShotCallResult.collectAsState()
    val oneShotCallError by callViewModel.oneShotCallError.collectAsState()
    val error by callViewModel.error.collectAsState()
    val isOneShotCallLoading by callViewModel.isOneShotCallLoading.collectAsState()
    var expanded by remember { mutableStateOf(false) }

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
                        ContactField.RECIPIENT -> callViewModel.updateOneShotRecipient(number)
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
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            callViewModel.loadContacts(context.contentResolver)
        }
        contentVisible = true
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
            .padding(LocalAppDimens.current.screenPadding)
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
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(LocalAppDimens.current.spacing)
                    ) {

                        OutlinedTextField(
                            value = recipient,
                            onValueChange = { callViewModel.updateOneShotRecipient(it) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.PhoneForwarded,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(start = 4.dp))
                                    Text(
                                        text = if (recipientContactName != null) "${stringResource(R.string.called_number)} • $recipientContactName" else stringResource(R.string.called_number),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ScreenAccents.Calls.main(),
                                focusedLabelColor = ScreenAccents.Calls.main()
                            ),
                            trailingIcon = {
                                IconButton(onClick = { pickContact(ContactField.RECIPIENT) }) {
                                    Icon(
                                        Icons.Default.Contacts,
                                        contentDescription = stringResource(R.string.pick_recipient),
                                        tint = ScreenAccents.Calls.main()
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
                                value = lines.find { it.id == selectedLine }?.let { "${it.name} (${it.callerId})" } ?: stringResource(R.string.select_line),
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SimCard,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.padding(start = 4.dp))
                                        Text(
                                            text = stringResource(R.string.line),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ScreenAccents.Calls.main(),
                                    focusedLabelColor = ScreenAccents.Calls.main()
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                lines.forEach { line ->
                                    DropdownMenuItem(
                                        text = { Text("${line.name} (${line.callerId})") },
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
                                    checkedColor = ScreenAccents.Calls.main()
                                )
                            )
                            Text(
                                text = stringResource(R.string.use_line_number_as_caller_id),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))


                        Button(
                            onClick = {
                                callViewModel.makeOneShotCall(recipient, useCallerIdPrefix)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(buttonScale),
                            interactionSource = buttonInteractionSource,
                            enabled = !isOneShotCallLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ScreenAccents.Calls.main()
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


                val activeError = if (!oneShotCallError.isNullOrEmpty()) {
                    oneShotCallError
                } else {
                    error
                }

                if (!activeError.isNullOrEmpty() && !isOneShotCallLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ErrorMessage(errorText = activeError, visible = true)
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
                                    ContactField.RECIPIENT -> callViewModel.updateOneShotRecipient(number)
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
