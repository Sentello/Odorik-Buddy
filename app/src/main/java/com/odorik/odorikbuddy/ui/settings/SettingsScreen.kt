package com.odorik.odorikbuddy.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.util.Log
import com.odorik.odorikbuddy.BuildConfig
import com.odorik.odorikbuddy.MainActivity
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.settings.SettingsViewModel
import android.content.res.Configuration
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController
) {
    Log.d("SettingsScreen", "SettingsScreen Composable entered")
    val lines = viewModel.lines.collectAsState()
    Log.d("SettingsScreen", "Lines size: ${lines.value.size}")
    val lineInfo = viewModel.lineInfo.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    
    val isDarkMode by viewModel.isDarkMode
    val language by viewModel.language.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getLines()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween 
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize() 
        ) {
            item {
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.dark_mode))
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }, 
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    )
                }
            }
            
            item {
                val context = LocalContext.current
                var expanded by remember { mutableStateOf(false) }
                val languages = listOf(
                    stringResource(R.string.lang_english) to "en",
                    stringResource(R.string.lang_czech) to "cs"
                )
                val currentLangCode = context.resources.configuration.locales.get(0).language
                val selectedLang = languages.find { it.second == currentLangCode }?.first ?: stringResource(R.string.lang_english)

                LaunchedEffect(currentLangCode) {
                    if (language != currentLangCode) {
                        viewModel.setLanguage(currentLangCode)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.settings_language_label))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedLang,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .wrapContentWidth()
                                .widthIn(max = 150.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors() 
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            languages.forEach { (display, code) ->
                                DropdownMenuItem(
                                    text = { Text(display) },
                                    onClick = {
                                        viewModel.setLanguage(code)
                                        (context as? MainActivity)?.updateLocale(code)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            items(lines.value) { line ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            viewModel.getLineInfo(line.id); showDialog = true
                        },
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "${stringResource(R.string.caller_id_label)} ${line.caller_id}")
                        Text(text = "${stringResource(R.string.line_id_label)} ${line.id}")
                    }
                }
            }
            item {
                
                Log.d("SettingsScreen", "Logout Button Composable rendered")
                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.logout))
                }
            }
            
            item {
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant 
                )
            }
        }
    }

    if (showDialog) {
        lineInfo.value?.let {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Line Info") },
                text = {
                    Column {
                        Text(text = "Line name: ${it.lineName}")
                        Text(text = "Caller ID: ${it.callerId}")
                        Text(text = "Password: ${it.password}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }
    }
}