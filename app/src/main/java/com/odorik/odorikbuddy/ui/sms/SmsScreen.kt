package com.odorik.odorikbuddy.ui.sms

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsScreen(viewModel: SmsViewModel = hiltViewModel()) {
    val allowedSenders by viewModel.allowedSenders.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    val error by viewModel.error.collectAsState()

    var recipient by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedSender by remember { mutableStateOf<String?>(null) }
    var delayed by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let {
                viewModel.handleContactSelection(context.contentResolver, it) { phoneNumber ->
                    recipient = phoneNumber
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

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    Icon(Icons.Default.Contacts, contentDescription = "Pick Contact")
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
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(stringResource(R.string.message)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = delayed,
            onValueChange = { delayed = it },
            label = { Text(stringResource(R.string.delayed)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.sendSms(recipient, message, selectedSender, delayed.takeIf { it.isNotBlank() }) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.send_sms)) }
        if (error != null) Text(stringResource(R.string.error, error!!), color = Color.Red)
        if (sendResult != null) Text(stringResource(R.string.result, sendResult!!), color = Color.Green)
    }
}