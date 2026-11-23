package com.odorik.odorikbuddy.ui.calls

// Imports for Contact Picker and other functionalities
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.entity.TileCallType
import com.odorik.odorikbuddy.data.local.entity.TileEntity
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.ui.components.PhoneFieldTrailingIcons
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import com.odorik.odorikbuddy.util.PhoneNumberUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTileDialog(
    tile: TileEntity?,
    lines: List<Line>,
    onDismiss: () -> Unit,
    onSave: (TileData) -> Unit,
    callViewModel: CallViewModel
) {
    val context = LocalContext.current
    // rememberSaveable: what was typed survives a rotation or a trip to the contact picker
    var label by rememberSaveable { mutableStateOf(tile?.label ?: "") }
    var recipient by rememberSaveable { mutableStateOf(tile?.recipient ?: "") }
    
    val tabOrder by callViewModel.tabOrder.collectAsState()
    val linesError by callViewModel.linesError.collectAsState()
    
    // Determine sort order for Call Type dropdown based on Tab Order
    val callTypeOptions = remember(tabOrder) {
        val callbackIndex = tabOrder.indexOf("callback_title").takeIf { it >= 0 } ?: Int.MAX_VALUE
        val oneshotIndex = tabOrder.indexOf("oneshot_call").takeIf { it >= 0 } ?: Int.MAX_VALUE
        
        if (oneshotIndex < callbackIndex) {
            listOf(TileCallType.ONESHOT to R.string.call_type_oneshot, TileCallType.CALLBACK to R.string.call_type_callback)
        } else {
            listOf(TileCallType.CALLBACK to R.string.call_type_callback, TileCallType.ONESHOT to R.string.call_type_oneshot)
        }
    }

    var callType by rememberSaveable { mutableStateOf(tile?.callType ?: callTypeOptions.first().first) }
    var selectedLineId by rememberSaveable { mutableStateOf(tile?.lineId) }
    var callerId by rememberSaveable { mutableStateOf(tile?.callerId ?: "") } 
    var useLineAsCallerId by rememberSaveable { mutableStateOf(tile?.useLineAsCallerId ?: false) }
    var selectedColor by rememberSaveable { mutableStateOf(tile?.color) }
    var selectedTextColor by rememberSaveable { mutableStateOf(tile?.textColor) }

    var contactFieldToUpdate by rememberSaveable { mutableStateOf<ContactField?>(null) }
    
    var lineDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    
    // Set by the first Save attempt; from then on each empty required field is marked.
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val recipientMissing = recipient.isBlank()
    val lineMissing = selectedLineId == null
    val callerIdMissing = callType == TileCallType.CALLBACK && callerId.isBlank()
    val requiredError: @Composable () -> Unit = { Text(stringResource(R.string.error_field_required)) }
    
    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var phoneNumbers by remember { mutableStateOf(emptyList<String>()) }
    
    // With a single line there is nothing to choose, so fill it in.
    LaunchedEffect(lines) {
        if (selectedLineId == null && lines.size == 1) {
            selectedLineId = lines.first().id.toString()
        }
    }

    val contactScope = rememberCoroutineScope()
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let { uri ->
                contactScope.launch {
                    val numbers = callViewModel.getPhoneNumbersFromContact(context.contentResolver, uri)
                    if (numbers.isNotEmpty()) {
                        if (numbers.size == 1) {
                            val number = numbers.first()
                            if (contactFieldToUpdate == ContactField.RECIPIENT) {
                                recipient = number
                            } else if (contactFieldToUpdate == ContactField.CALLER_ID) {
                                callerId = number
                            }
                        } else {
                            phoneNumbers = numbers
                            showPhoneNumberDialog = true
                        }
                    }
                }
            }
        }
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                contactPickerLauncher.launch(null)
            }
        }
    )

    fun pickContact(field: ContactField) {
        contactFieldToUpdate = field
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            contactPickerLauncher.launch(null)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    
    val colors = TileColorHelper.allBaseColors
    val textColors = TileColorHelper.textColors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tile == null) stringResource(R.string.add_tile) else stringResource(R.string.edit_tile)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Scrolls, so nothing is cut off on a small screen, with a large font
                    // or while the keyboard is open.
                    .verticalScroll(rememberScrollState())
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Call Type
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = if (callType == TileCallType.CALLBACK) stringResource(R.string.call_type_callback) else stringResource(R.string.call_type_oneshot),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.call_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        callTypeOptions.forEach { (typeKey, labelRes) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(labelRes)) },
                                onClick = { callType = typeKey; typeDropdownExpanded = false }
                            )
                        }
                    }
                }

                // Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.label_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                val recipientField = @Composable {
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = PhoneNumberUtils.sanitizeDialInput(it) },
                        label = { Text(stringResource(R.string.called_number)) },
                        isError = showErrors && recipientMissing,
                        supportingText = if (showErrors && recipientMissing) requiredError else null,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        trailingIcon = {
                            PhoneFieldTrailingIcons(
                                value = recipient,
                                onClear = { recipient = "" },
                                onPickContact = { pickContact(ContactField.RECIPIENT) },
                                pickContactDescription = stringResource(R.string.pick_contact),
                                contactTint = ScreenAccents.Calls.main()
                            )
                        }
                    )
                }

                val lineField = @Composable {
                    ExposedDropdownMenuBox(
                        expanded = lineDropdownExpanded,
                        onExpandedChange = { lineDropdownExpanded = !lineDropdownExpanded }
                    ) {
                        OutlinedTextField(
                             value = lines.find { it.id.toString() == selectedLineId }?.let { "${it.name} (${it.callerId})" } ?: stringResource(R.string.select_line),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.line)) },
                            isError = showErrors && lineMissing,
                            supportingText = if (showErrors && lineMissing) requiredError else null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lineDropdownExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = lineDropdownExpanded,
                            onDismissRequest = { lineDropdownExpanded = false }
                        ) {
                            lines.forEach { line ->
                                DropdownMenuItem(
                                    text = { Text("${line.name} (${line.callerId})") },
                                    onClick = { selectedLineId = line.id.toString(); lineDropdownExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Without lines the tile can't be saved, so say why and offer a retry
                val linesNotLoaded = @Composable {
                    if (lines.isEmpty() && linesError != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.lines_not_loaded),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { callViewModel.getLines() },
                                colors = ButtonDefaults.textButtonColors(contentColor = ScreenAccents.Calls.main())
                            ) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                if (callType == TileCallType.CALLBACK) {
                    OutlinedTextField(
                        value = callerId,
                        onValueChange = { callerId = PhoneNumberUtils.sanitizeDialInput(it) },
                        label = { Text(stringResource(R.string.caller_id)) },
                        isError = showErrors && callerIdMissing,
                        supportingText = if (showErrors && callerIdMissing) requiredError else null,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        trailingIcon = {
                            PhoneFieldTrailingIcons(
                                value = callerId,
                                onClear = { callerId = "" },
                                onPickContact = { pickContact(ContactField.CALLER_ID) },
                                pickContactDescription = stringResource(R.string.pick_contact),
                                contactTint = ScreenAccents.Calls.main()
                            )
                        }
                    )
                    recipientField()
                    lineField()
                    linesNotLoaded()
                } else {
                    recipientField()
                    lineField()
                    linesNotLoaded()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { useLineAsCallerId = !useLineAsCallerId }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useLineAsCallerId,
                            onCheckedChange = { useLineAsCallerId = it }
                        )
                        Text(stringResource(R.string.use_line_number_as_caller_id))
                    }
                }
                
                // Background Color Selection
                Text(stringResource(R.string.color), style = MaterialTheme.typography.bodySmall)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                         // Default / Transparent Option
                         Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(2.dp, if (selectedColor == null) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable { selectedColor = null }
                        )
                    }
                    
                    items(colors) { color ->
                        val displayColor = TileColorHelper.resolveColor(
                            color,
                            com.odorik.odorikbuddy.ui.theme.LocalIsAppDark.current
                        )!!
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(displayColor)
                                .border(2.dp, if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable { selectedColor = color }
                        )
                    }
                }

                // Text Color Selection
                Text(stringResource(R.string.text_color), style = MaterialTheme.typography.bodySmall)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                         // Default Option
                         Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(2.dp, if (selectedTextColor == null) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable { selectedTextColor = null }
                        )
                    }
                    
                    items(textColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(2.dp, if (selectedTextColor == color) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable { selectedTextColor = color }
                        )
                    }
                }

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (recipientMissing || lineMissing || callerIdMissing) {
                        // Mark the empty fields instead of saving
                        showErrors = true
                    } else {
                        onSave(
                            TileData(
                                label = label,
                                recipient = recipient,
                                callType = callType,
                                lineId = selectedLineId,
                                callerId = callerId.ifEmpty { null },
                                useLineAsCallerId = useLineAsCallerId,
                                color = selectedColor,
                                textColor = selectedTextColor
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScreenAccents.Calls.main())
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ScreenAccents.Calls.main())
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
    
    if (showPhoneNumberDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneNumberDialog = false },
            title = { Text(stringResource(R.string.choose_phone_number)) },
            text = {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(phoneNumbers) { number ->
                        TextButton(
                            onClick = {
                                if (contactFieldToUpdate == ContactField.RECIPIENT) {
                                    recipient = number
                                } else if (contactFieldToUpdate == ContactField.CALLER_ID) {
                                    callerId = number
                                }
                                showPhoneNumberDialog = false
                                contactFieldToUpdate = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = ScreenAccents.Calls.main())
                        ) {
                            Text(number, textAlign = TextAlign.Start)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showPhoneNumberDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = ScreenAccents.Calls.main())
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
