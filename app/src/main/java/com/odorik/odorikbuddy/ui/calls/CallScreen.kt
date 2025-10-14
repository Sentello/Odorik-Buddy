package com.odorik.odorikbuddy.ui.calls

import android.Manifest
import android.content.pm.PackageManager
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    viewModel: CallViewModel = hiltViewModel()
) {
    val callList = viewModel.callList.collectAsState()
    val callResult = viewModel.callResult.collectAsState()
    val lines by viewModel.lines.collectAsState()
    var callerId by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var selectedLine by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val callerIdLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            contactUri?.let {
                viewModel.handleContactSelection(context.contentResolver, it) { phoneNumber ->
                    callerId = phoneNumber
                }
            }
        }
    )

    val recipientLauncher = rememberLauncherForActivityResult(
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
                
            } else {
                
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = callerId,
            onValueChange = { callerId = it },
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
                    Icon(Icons.Default.Contacts, contentDescription = "Pick Caller ID")
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
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
        Text(text = callResult.value)
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
}