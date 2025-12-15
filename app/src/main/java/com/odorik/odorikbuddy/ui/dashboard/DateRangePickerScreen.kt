package com.odorik.odorikbuddy.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.odorik.odorikbuddy.R
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerScreen(navController: NavController) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(6)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.select_date_range)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { showStartDatePicker = true }) {
                Text(stringResource(R.string.start_date, startDate.toString()))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showEndDatePicker = true }) {
                Text(stringResource(R.string.end_date, endDate.toString()))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Button(onClick = {
                    if (endDate.isBefore(startDate)) {
                        scope.launch {
                            snackbarHostState.showSnackbar(message = "End date cannot be before start date.")
                        }
                    } else {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("startDate", startDate.toEpochDay())
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("endDate", endDate.toEpochDay())
                        navController.popBackStack()
                    }
                }) {
                    Text(stringResource(R.string.apply))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    showStartDatePicker = false 
                    startDate = Instant.ofEpochMilli(datePickerState.selectedDateMillis ?: 0).atZone(ZoneId.systemDefault()).toLocalDate()
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    showEndDatePicker = false
                    endDate = Instant.ofEpochMilli(datePickerState.selectedDateMillis ?: 0).atZone(ZoneId.systemDefault()).toLocalDate()
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
