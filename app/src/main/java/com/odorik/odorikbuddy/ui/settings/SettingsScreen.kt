package com.odorik.odorikbuddy.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.odorik.odorikbuddy.BuildConfig
import com.odorik.odorikbuddy.MainActivity
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes
import com.odorik.odorikbuddy.ui.navigation.SettingsRoutes
import com.odorik.odorikbuddy.util.getResponsiveBodyLargeSize
import com.odorik.odorikbuddy.util.getResponsiveCardPadding
import com.odorik.odorikbuddy.util.getResponsiveSpacing

@Composable
private fun SettingsHeader(title: String) {
    Text(
        text = title,
        fontSize = getResponsiveBodyLargeSize(),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = getResponsiveCardPadding(), vertical = getResponsiveSpacing()/2)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    outerNavController: NavController,
    internalNavController: NavHostController
) {
    val context = LocalContext.current
    val lines by viewModel.lines.collectAsState()
    val selectedLine by viewModel.selectedLine.collectAsState()
    val isDarkMode by viewModel.isDarkMode
    val language by viewModel.language.collectAsState()
    val historyPeriod by viewModel.historyPeriod.collectAsState()
    val updateViewModel: UpdateViewModel = hiltViewModel()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showHistoryPeriodDialog by remember { mutableStateOf(false) }
    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val currentPhoneNumber by viewModel.phoneNumber.collectAsState()

    val uriHandler = LocalUriHandler.current
    
    
    val updateInfo by updateViewModel.updateInfo.collectAsState()
    val isUpdateLoading by updateViewModel.isLoading.collectAsState()
    val updateError by updateViewModel.error.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.getLines()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings)) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = getResponsiveSpacing())
        ) {
            item { SettingsHeader(stringResource(R.string.section_display)) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.dark_mode)) },
                    leadingContent = { Icon(Icons.Default.Brightness6, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) }
                        )
                    },
                    modifier = Modifier.clickable { viewModel.setDarkMode(!isDarkMode) }
                )
            }
            item {
                val languages = listOf(
                    stringResource(R.string.lang_english) to "en",
                    stringResource(R.string.lang_czech) to "cs"
                )
                val selectedLang = languages.find { it.second == language }?.first ?: ""
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_language_label)) },
                    supportingContent = { Text(selectedLang) },
                    leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                    modifier = Modifier.clickable { showLanguageDialog = true }
                )
            }

            item { SettingsHeader(stringResource(R.string.section_data)) }
            item {
                val periods = listOf(
                    7 to stringResource(R.string.history_period_7),
                    30 to stringResource(R.string.history_period_30),
                    90 to stringResource(R.string.history_period_90),
                    180 to stringResource(R.string.history_period_180),
                    365 to stringResource(R.string.history_period_365)
                )
                val selectedPeriod = periods.find { it.first == historyPeriod }?.second ?: ""
                ListItem(
                    headlineContent = { Text(stringResource(R.string.history_period_label)) },
                    supportingContent = { Text(selectedPeriod) },
                    leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                    modifier = Modifier.clickable { showHistoryPeriodDialog = true }
                )
            }

            item { SettingsHeader(stringResource(R.string.section_personal)) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.personal_phone_number)) },
                    supportingContent = {
                        Text(
                            if (currentPhoneNumber.isNotEmpty()) currentPhoneNumber
                            else stringResource(R.string.personal_phone_number_description)
                        )
                    },
                    leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                    modifier = Modifier.clickable {
                        
                            Column {
                                Text(
                                    "• ${device.userAgent}", 
                                    fontSize = getResponsiveBodyLargeSize(),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "  IP: $ipAddress", 
                                    fontSize = getResponsiveBodyLargeSize() * 0.85f, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
        }
    )
}

@Composable
fun PhoneNumberInputDialog(
    currentNumber: String,
    onNumberSaved: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var phoneNumberInput by remember { mutableStateOf(currentNumber) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.personal_phone_number)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.personal_phone_number_description),
                    fontSize = getResponsiveBodyLargeSize(),
                    modifier = Modifier.padding(bottom = getResponsiveSpacing())
                )
                OutlinedTextField(
                    value = phoneNumberInput,
                    onValueChange = { phoneNumberInput = it },
                    label = { 
                        Text(
                            stringResource(R.string.personal_phone_number),
                            fontSize = getResponsiveBodyLargeSize()
                        ) 
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onNumberSaved(phoneNumberInput) }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        stringResource(R.string.lang_english) to "en",
        stringResource(R.string.lang_czech) to "cs"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language_label)) },
        text = {
            Column(Modifier.selectableGroup()) {
                languages.forEach { (display, code) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (code == currentLanguage),
                                onClick = {
                                    onLanguageSelected(code)
                                    onDismiss()
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (code == currentLanguage),
                            onClick = null
                        )
                        Text(
                            text = display,
                            fontSize = getResponsiveBodyLargeSize(),
                            modifier = Modifier.padding(start = getResponsiveSpacing()/2)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun HistoryPeriodSelectionDialog(
    currentPeriod: Int,
    onPeriodSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val periods = listOf(
        7 to stringResource(R.string.history_period_7),
        30 to stringResource(R.string.history_period_30),
        90 to stringResource(R.string.history_period_90),
        180 to stringResource(R.string.history_period_180),
        365 to stringResource(R.string.history_period_365)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_period_label)) },
        text = {
            Column(Modifier.selectableGroup()) {
                periods.forEach { (days, display) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (days == currentPeriod),
                                onClick = {
                                    onPeriodSelected(days)
                                    onDismiss()
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (days == currentPeriod),
                            onClick = null
                        )
                        Text(
                            text = display,
                            fontSize = getResponsiveBodyLargeSize(),
                            modifier = Modifier.padding(start = getResponsiveSpacing()/2)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun UpdateInfoDialog(
    updateInfo: com.odorik.odorikbuddy.model.AppUpdateInfo?,
    isLoading: Boolean,
    error: String?,
    isUpdateAvailable: Boolean,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    
    
    val cannotOpenUrlString = stringResource(R.string.cannot_open_url)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.app_update)) },
        text = {
            Column {
                if (isLoading) {
                    Text(stringResource(R.string.checking_for_updates))
                } else if (error != null) {
                    Text("${stringResource(R.string.error_checking_for_updates)}: $error")
                } else if (updateInfo != null) {
                    Column {
                        Text(
                            "${stringResource(R.string.current_version)}: ${BuildConfig.VERSION_NAME}",
                            fontSize = getResponsiveBodyLargeSize()
                        )
                        Text(
                            "${stringResource(R.string.latest_version)}: ${updateInfo.version}",
                            fontSize = getResponsiveBodyLargeSize()
                        )
                        
                        if (isUpdateAvailable) {
                            Text(
                                stringResource(R.string.update_available),
                                fontSize = getResponsiveBodyLargeSize(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = getResponsiveSpacing()/2)
                            )
                            Text(
                                updateInfo.message,
                                fontSize = getResponsiveBodyLargeSize(),
                                modifier = Modifier.padding(top = getResponsiveSpacing()/2)
                            )
                        } else {
                            Text(
                                stringResource(R.string.up_to_date),
                                fontSize = getResponsiveBodyLargeSize(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = getResponsiveSpacing()/2)
                            )
                        }
                    }
                } else {
                    Text(stringResource(R.string.no_update_info))
                }
            }
        },
        confirmButton = {
            if (updateInfo != null && isUpdateAvailable) {
                TextButton(
                    onClick = { 
                        try {
                            uriHandler.openUri(updateInfo.downloadUrl)
                        } catch (e: Exception) {
                            
                            android.widget.Toast.makeText(
                                context, 
                                cannotOpenUrlString, 
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                ) {
                    Text(stringResource(R.string.download_update))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.ok))
                }
            }
        },
        dismissButton = if (updateInfo != null && isUpdateAvailable) {
            null
        } else {
            {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    )
    
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    onOpenGitHub: () -> Unit,
    onOpenForum: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.about_app)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.about_app_description),
                    fontSize = getResponsiveBodyLargeSize(),
                    modifier = Modifier.padding(bottom = getResponsiveSpacing()/2)
                )
                
                ListItem(
                    headlineContent = { Text(stringResource(R.string.github_repository)) },
                    leadingContent = { Icon(Icons.Default.Code, contentDescription = null) },
                    modifier = Modifier.clickable { onOpenGitHub() }
                )
                
                ListItem(
                    headlineContent = { Text(stringResource(R.string.discussion_forum)) },
                    leadingContent = { Icon(Icons.Default.Forum, contentDescription = null) },
                    modifier = Modifier.clickable { onOpenForum() }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}
