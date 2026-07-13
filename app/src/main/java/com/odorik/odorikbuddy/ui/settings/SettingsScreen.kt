package com.odorik.odorikbuddy.ui.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.odorik.odorikbuddy.BuildConfig
import com.odorik.odorikbuddy.MainActivity
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.components.darkModeBorder
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes
import com.odorik.odorikbuddy.ui.navigation.SettingsRoutes
import com.odorik.odorikbuddy.ui.theme.SettingsAccent
import com.odorik.odorikbuddy.ui.theme.SettingsAccentLight
import com.odorik.odorikbuddy.util.getResponsiveBodyLargeSize
import com.odorik.odorikbuddy.util.getResponsiveBodyMediumSize
import com.odorik.odorikbuddy.util.getResponsiveBodySmallSize
import com.odorik.odorikbuddy.util.getResponsiveSpacing

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .darkModeBorder(RoundedCornerShape(20.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SettingsAccent.copy(alpha = 0.15f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = SettingsAccent
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
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
    val autoUpdateEnabled by viewModel.autoUpdateEnabled.collectAsState()
    val directCallsEnabled by viewModel.directCallsEnabled.collectAsState()

    val uriHandler = LocalUriHandler.current

    // Update information
    val updateInfo by updateViewModel.updateInfo.collectAsState()
    val isUpdateLoading by updateViewModel.isLoading.collectAsState()
    val updateError by updateViewModel.error.collectAsState()

    // Permission launcher for notifications
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, enable auto-update
            viewModel.setAutoUpdateEnabled(true)
            // Perform immediate update check to show notification if update is available
            viewModel.performImmediateUpdateCheck()
        }
    }

    // Permission launcher for direct calls
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, enable direct calls
            viewModel.setDirectCallsEnabled(true)
        }
    }



    Scaffold(
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "iconRotation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(20000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "iconRotation"
            )
            GradientHeader(
                title = stringResource(R.string.settings),
                iconVector = Icons.Default.Settings,
                backgroundBrush = Brush.verticalGradient(listOf(SettingsAccent.copy(alpha = 0.35f), Color.Transparent)),
                iconGradientBrush = Brush.linearGradient(listOf(SettingsAccent, SettingsAccentLight)),
                iconRotation = rotation
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = getResponsiveSpacing(),
                    end = getResponsiveSpacing(),
                    bottom = getResponsiveSpacing()
                )
            ) {
                // Account Section (Lines)
                item {
                    SettingsSection(
                        title = stringResource(R.string.section_account),
                        icon = Icons.Default.Call
                    ) {
                        Column {
                            lines.forEachIndexed { index, line ->
                                ListItem(
                                    headlineContent = { Text("${line.name} (${line.callerId})") },
                                    supportingContent = { Text(stringResource(R.string.line_id_label) + " " + line.id.toString()) },
                                    modifier = Modifier.clickable { viewModel.onLineSelected(line) }
                                )
                                if (index < lines.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Personal Section
                item {
                    SettingsSection(
                        title = stringResource(R.string.section_personal),
                        icon = Icons.Default.Person
                    ) {
                        Column {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.personal_phone_number)) },
                                supportingContent = {
                                    Text(
                                        if (currentPhoneNumber.isNotEmpty()) currentPhoneNumber
                                        else stringResource(R.string.personal_phone_number_description),
                                        color = if (currentPhoneNumber.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                modifier = Modifier.clickable { showPhoneNumberDialog = true }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.section_routing)) },
                                supportingContent = { Text(stringResource(R.string.routing_description)) },
                                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    internalNavController.navigate(SettingsRoutes.ROUTING_OPTIONS_SCREEN)
                                }
                            )
                        }
                    }
                }

                // Display Section
                item {
                    SettingsSection(
                        title = stringResource(R.string.section_display),
                        icon = Icons.Default.Brightness6
                    ) {
                        Column {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.dark_mode)) },
                                trailingContent = {
                                    Switch(
                                        checked = isDarkMode,
                                        onCheckedChange = { viewModel.setDarkMode(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = SettingsAccent,
                                            checkedTrackColor = SettingsAccentLight.copy(alpha = 0.5f)
                                        )
                                    )
                                },
                                modifier = Modifier.clickable { viewModel.setDarkMode(!isDarkMode) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            val languages = listOf(
                                stringResource(R.string.lang_english) to "en",
                                stringResource(R.string.lang_czech) to "cs"
                            )
                            val selectedLang = languages.find { it.second == language }?.first ?: ""
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.settings_language_label)) },
                                supportingContent = { Text(selectedLang, color = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.clickable { showLanguageDialog = true }
                            )
                        }
                    }
                }

                // Data Section
                item {
                    SettingsSection(
                        title = stringResource(R.string.section_data),
                        icon = Icons.Default.History
                    ) {
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
                            supportingContent = { Text(selectedPeriod, color = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.clickable { showHistoryPeriodDialog = true }
                        )
                    }
                }

                // App Section
                item {
                    SettingsSection(
                        title = stringResource(R.string.section_app),
                        icon = Icons.Default.Apps
                    ) {
                        Column {
                            // Auto-update
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.auto_update_checking)) },
                                supportingContent = { Text(stringResource(R.string.auto_update_checking_description)) },
                                trailingContent = {
                                    Switch(
                                        checked = autoUpdateEnabled,
                                        onCheckedChange = { enabled ->
                                            if (enabled) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                } else {
                                                    viewModel.setAutoUpdateEnabled(true)
                                                }
                                            } else {
                                                viewModel.setAutoUpdateEnabled(false)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = SettingsAccent,
                                            checkedTrackColor = SettingsAccentLight.copy(alpha = 0.5f)
                                        )
                                    )
                                },
                                modifier = Modifier.clickable {
                                    if (!autoUpdateEnabled) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            viewModel.setAutoUpdateEnabled(true)
                                        }
                                    } else {
                                        viewModel.setAutoUpdateEnabled(false)
                                    }
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            
                            // Direct calls
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.direct_calls)) },
                                supportingContent = { Text(stringResource(R.string.direct_calls_description)) },
                                trailingContent = {
                                    Switch(
                                        checked = directCallsEnabled,
                                        onCheckedChange = { enabled ->
                                            if (enabled) {
                                                callPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                                            } else {
                                                viewModel.setDirectCallsEnabled(false)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = SettingsAccent,
                                            checkedTrackColor = SettingsAccentLight.copy(alpha = 0.5f)
                                        )
                                    )
                                },
                                modifier = Modifier.clickable {
                                    if (!directCallsEnabled) {
                                        callPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
                                    } else {
                                        viewModel.setDirectCallsEnabled(false)
                                    }
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // About
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.about_app)) },
                                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                modifier = Modifier.clickable { showAboutDialog = true }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // Version / Update
                            val isUpdateAvailable = updateViewModel.isUpdateAvailable()
                            ListItem(
                                headlineContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.version_label))
                                        if (isUpdateAvailable) {
                                            Icon(
                                                Icons.Default.Update,
                                                contentDescription = stringResource(R.string.update_available),
                                                modifier = Modifier
                                                    .padding(start = 4.dp)
                                                    .size(16.dp),
                                                tint = SettingsAccent
                                            )
                                        }
                                    }
                                },
                                supportingContent = { Text(BuildConfig.VERSION_NAME) },
                                modifier = Modifier.clickable {
                                    showUpdateDialog = true
                                    updateViewModel.checkForUpdates()
                                }
                            )
                        }
                    }
                }

                // Logout Button
                item {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout, 
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.logout),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS --- //

    selectedLine?.let {
        LineInfoDialog(line = it, onDismiss = { viewModel.onDismissLineDialog() })
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.confirm_logout_title)) },
            text = { Text(stringResource(R.string.confirm_logout_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        outerNavController.navigate(NavigationRoutes.LOGIN) {
                            popUpTo(NavigationRoutes.MAIN) { inclusive = true }
                        }
                        showLogoutDialog = false
                    }
                ) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = language,
            onLanguageSelected = {
                viewModel.setLanguage(it)
                (context as? MainActivity)?.updateLocale(it)
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showHistoryPeriodDialog) {
        HistoryPeriodSelectionDialog(
            currentPeriod = historyPeriod,
            onPeriodSelected = { viewModel.setHistoryPeriod(it) },
            onDismiss = { showHistoryPeriodDialog = false }
        )
    }

    if (showPhoneNumberDialog) {
        PhoneNumberInputDialog(
            currentNumber = currentPhoneNumber,
            onNumberSaved = { phoneNumber ->
                viewModel.setPhoneNumber(phoneNumber)
                showPhoneNumberDialog = false
            },
            onDismiss = { showPhoneNumberDialog = false }
        )
    }
    
    // Update Dialog
    if (showUpdateDialog) {
        val isUpdateAvailable = updateViewModel.isUpdateAvailable()
        UpdateInfoDialog(
            updateInfo = updateInfo,
            isLoading = isUpdateLoading,
            error = updateError,
            isUpdateAvailable = isUpdateAvailable,
            onDismiss = { showUpdateDialog = false }
        )
    }
    
    // About Dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false },
            onOpenGitHub = { uriHandler.openUri("https://github.com/Sentello/Odorik-Buddy") },
            onOpenForum = { uriHandler.openUri("https://forum.odorik.cz/viewtopic.php?t=6042") }
        )
    }
}

@Composable
private fun LineInfoDialog(line: Line, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.line_info_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(getResponsiveSpacing()/2)) {
                Text(
                    "${stringResource(R.string.line_name_label)} ${line.name}", 
                    fontSize = getResponsiveBodyLargeSize()
                )
                Text(
                    "${stringResource(R.string.caller_id_label_settings)} ${line.callerId}", 
                    fontSize = getResponsiveBodyLargeSize()
                )
                line.publicNumber?.let {
                    Text(
                        "${stringResource(R.string.public_number_label)} ${it}", 
                        fontSize = getResponsiveBodyLargeSize()
                    )
                }

                var showPassword by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${stringResource(R.string.password_label_settings)} ${if (showPassword) line.sipPassword else "••••••••"}",
                        fontSize = getResponsiveBodyLargeSize()
                    )
                    IconButton(
                        onClick = { showPassword = !showPassword },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = getResponsiveSpacing()/2))
                Text(
                    stringResource(R.string.connected_devices_label),
                    fontSize = getResponsiveBodyMediumSize(),
                    fontWeight = FontWeight.SemiBold
                )
                if (line.connectedDevices.isEmpty()) {
                    Text(
                        stringResource(R.string.none), 
                        fontSize = getResponsiveBodyLargeSize()
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(getResponsiveSpacing()/3)) {
                        line.connectedDevices.forEach { device ->
                            val ipAddress = device.publicSocket.substringBefore(':')
                            Column {
                                Text(
                                    "• ${device.userAgent}", 
                                    fontSize = getResponsiveBodyLargeSize(),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "  IP: $ipAddress", 
                                    fontSize = getResponsiveBodySmallSize(), 
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
        stringResource(R.string.lang_czech) to "cs",
        stringResource(R.string.lang_english) to "en"
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
                    colors = androidx.compose.material3.ListItemDefaults.colors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    modifier = Modifier.clickable { onOpenGitHub() }
                )
                
                ListItem(
                    headlineContent = { Text(stringResource(R.string.discussion_forum)) },
                    leadingContent = { Icon(Icons.Default.Forum, contentDescription = null) },
                    colors = androidx.compose.material3.ListItemDefaults.colors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
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
