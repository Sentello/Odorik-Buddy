package com.odorik.odorikbuddy.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.odorik.odorikbuddy.BuildConfig
import com.odorik.odorikbuddy.MainActivity
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.AppTheme
import com.odorik.odorikbuddy.data.local.NavIconStyle
import com.odorik.odorikbuddy.data.local.ThemeMode
import com.odorik.odorikbuddy.ui.calls.TileSize
import com.odorik.odorikbuddy.ui.components.AccentIconChip
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.components.TransparentListItem
import com.odorik.odorikbuddy.ui.components.constrainedContentWidth
import com.odorik.odorikbuddy.ui.main.BottomNavItem
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes
import com.odorik.odorikbuddy.ui.navigation.SettingsRoutes
import com.odorik.odorikbuddy.ui.settings.SettingsViewModel.BackupUiEvent
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import com.odorik.odorikbuddy.util.findActivity
import com.odorik.odorikbuddy.util.openAppSettings
import com.odorik.odorikbuddy.util.openNotificationSettings

/**
 * A row of chips for a short either/or choice, shown directly in a settings row.
 * The row scrolls sideways if the labels don't fit (long words, large font).
 */
@Composable
private fun <T> ChipChoiceRow(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .selectableGroup()
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
                leadingIcon = if (selected == value) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                AccentIconChip(
                    icon = icon,
                    accent = ScreenAccents.Settings.main(),
                    size = 32.dp
                )
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
    val themeMode by viewModel.themeMode
    val appTheme by viewModel.appTheme
    val navIconStyle by viewModel.navIconStyle
    val language by viewModel.language.collectAsState()
    val historyPeriod by viewModel.historyPeriod.collectAsState()
    val updateViewModel: UpdateViewModel = hiltViewModel()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showColorThemeDialog by remember { mutableStateOf(false) }
    var showNavIconStyleDialog by remember { mutableStateOf(false) }
    var showHistoryPeriodDialog by remember { mutableStateOf(false) }
    var showPhoneNumberDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var deniedPermissionMessageRes by remember { mutableStateOf<Int?>(null) }
    val currentPhoneNumber by viewModel.phoneNumber.collectAsState()
    val autoUpdateEnabled by viewModel.autoUpdateEnabled.collectAsState()
    val directCallsEnabled by viewModel.directCallsEnabled.collectAsState()
    val tileSize by viewModel.tileSize.collectAsState()
    val simNumbers by viewModel.simNumbers.collectAsState()

    val uriHandler = LocalUriHandler.current

    // Update information
    val updateInfo by updateViewModel.updateInfo.collectAsState()
    val isUpdateLoading by updateViewModel.isLoading.collectAsState()
    val updateError by updateViewModel.error.collectAsState()

    // Covers both the Android 13 permission and notifications blocked for the app.
    // Re-read on every resume: the user may change it in the system settings.
    var notificationsAllowed by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Permission launcher for notifications. The check is already on by the time this runs:
    // without the permission a new version still shows on the Dashboard.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.performImmediateUpdateCheck()
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!isGranted && context.findActivity()?.let {
                !ActivityCompat.shouldShowRequestPermissionRationale(it, android.Manifest.permission.POST_NOTIFICATIONS)
            } == true
        ) {
            // Denied with no system dialog shown = permanently denied
            deniedPermissionMessageRes = R.string.notification_permission_denied_message
        }
    }

    val enableAutoUpdate = {
        viewModel.setAutoUpdateEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsAllowed) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.performImmediateUpdateCheck()
        }
    }

    // Permission launcher for direct calls
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setDirectCallsEnabled(true)
        } else if (context.findActivity()?.let {
                !ActivityCompat.shouldShowRequestPermissionRationale(it, android.Manifest.permission.CALL_PHONE)
            } == true
        ) {
            deniedPermissionMessageRes = R.string.call_permission_denied_message
        }
    }



    // --- Settings backup (export / import) ---
    val snackbarHostState = remember { SnackbarHostState() }
    val pendingImport by viewModel.pendingImport.collectAsState()
    val backupBusy by viewModel.backupBusy.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::exportBackup) }

    // "*/*": files received over Bluetooth often carry a wrong MIME type; the content is validated.
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::loadImport) }

    LaunchedEffect(Unit) {
        viewModel.backupEvents.collect { event ->
            when (event) {
                is BackupUiEvent.Exported -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.backup_exported, event.tileCount),
                        actionLabel = context.getString(R.string.backup_share),
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, event.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(
                                Intent.createChooser(send, context.getString(R.string.backup_share))
                            )
                        } catch (e: ActivityNotFoundException) {
                            // Nothing can receive the file; the saved copy is still there.
                        }
                    }
                }
                BackupUiEvent.ExportFailed ->
                    snackbarHostState.showSnackbar(context.getString(R.string.backup_export_failed))
                is BackupUiEvent.ImportFailed ->
                    snackbarHostState.showSnackbar(context.getString(event.messageRes))
                is BackupUiEvent.Imported -> {
                    val report = event.report
                    val tilesImported = report.tilesImported
                    // What was imported, then what the user must turn on again by hand.
                    val hasHints = report.directCallsSkipped
                    val message = buildList {
                        add(
                            if (tilesImported == null) context.getString(R.string.backup_imported_settings_only)
                            else context.getString(R.string.backup_imported, tilesImported)
                        )
                        if (report.directCallsSkipped) add(context.getString(R.string.backup_direct_calls_skipped))
                    }.joinToString("\n")
                    val language = report.languageToApply
                    if (language != null) {
                        // Switching the language recreates the activity and would drop a
                        // snackbar, so confirm with a toast and switch last.
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        viewModel.setLanguage(language)
                        (context as? MainActivity)?.updateLocale(language)
                    } else {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = if (hasHints) SnackbarDuration.Long else SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val gearRotation = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                gearRotation.animateTo(
                    360f,
                    animationSpec = tween(900, easing = FastOutSlowInEasing)
                )
            }
            GradientHeader(
                title = stringResource(R.string.settings),
                iconVector = Icons.Default.Settings,
                accent = ScreenAccents.Settings,
                iconRotation = gearRotation.value
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .constrainedContentWidth(),
                contentPadding = PaddingValues(
                    start = LocalAppDimens.current.spacing,
                    end = LocalAppDimens.current.spacing,
                    bottom = LocalAppDimens.current.spacing
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
                                TransparentListItem(
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
                            TransparentListItem(
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
                            TransparentListItem(
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

                // Calling Section (speed dials)
                item {
                    SettingsSection(
                        title = stringResource(R.string.section_calling),
                        icon = Icons.Default.Dialpad
                    ) {
                        TransparentListItem(
                            headlineContent = { Text(stringResource(R.string.speed_dials_title)) },
                            supportingContent = { Text(stringResource(R.string.speed_dials_description)) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable {
                                internalNavController.navigate(SettingsRoutes.SPEED_DIALS_SCREEN)
                            }
                        )
                    }
                }

                // SIM card section — only for accounts that have an Odorik SIM
                if (simNumbers.isNotEmpty()) {
                    item {
                        SettingsSection(
                            title = stringResource(R.string.sim_section_title),
                            icon = Icons.Default.SimCard
                        ) {
                            Column {
                                simNumbers.forEachIndexed { index, number ->
                                    TransparentListItem(
                                        headlineContent = { Text(com.odorik.odorikbuddy.util.PhoneNumberUtils.formatForDisplay(number)) },
                                        supportingContent = { Text(stringResource(R.string.sim_item_description)) },
                                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                        modifier = Modifier.clickable {
                                            internalNavController.navigate(SettingsRoutes.simScreen(number))
                                        }
                                    )
                                    if (index < simNumbers.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                }
                            }
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
                            // Short choices are chips right in the row: one tap, and the current
                            // value is visible without opening a dialog.
                            TransparentListItem(
                                headlineContent = { Text(stringResource(R.string.settings_appearance)) },
                                supportingContent = {
                                    ChipChoiceRow(
                                        options = listOf(
                                            ThemeMode.SYSTEM to stringResource(R.string.theme_mode_system),
                                            ThemeMode.LIGHT to stringResource(R.string.theme_mode_light),
                                            ThemeMode.DARK to stringResource(R.string.theme_mode_dark)
                                        ),
                                        selected = themeMode,
                                        onSelect = { viewModel.setThemeMode(it) }
                                    )
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            TransparentListItem(
                                headlineContent = { Text(stringResource(R.string.settings_color_theme)) },
                                supportingContent = {
                                    val colorThemeLabel = when (appTheme) {
                                        AppTheme.STANDARD -> stringResource(R.string.app_theme_standard)
                                        AppTheme.MATERIAL_YOU -> stringResource(R.string.app_theme_material_you)
                                        AppTheme.ODORIK -> stringResource(R.string.app_theme_odorik)
                                    }
                                    Text(colorThemeLabel, color = MaterialTheme.colorScheme.primary)
                                },
                                modifier = Modifier.clickable { showColorThemeDialog = true }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            TransparentListItem(
                                headlineContent = { Text(stringResource(R.string.settings_nav_icon_style)) },
                                supportingContent = {
                                    Text(
                                        stringResource(navIconStyleLabel(navIconStyle)),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.clickable { showNavIconStyleDialog = true }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            // Tile size on the Calls screen (same three steps as the widget text size)
                            TransparentListItem(
                                headlineContent = { Text(stringResource(R.string.tile_size)) },
                                supportingContent = {
                                    Column {
                                        Text(stringResource(R.string.tile_size_description))
                                        ChipChoiceRow(
                                            options = listOf(
                                                TileSize.NORMAL to stringResource(R.string.widget_config_normal),
                                                TileSize.LARGE to stringResource(R.string.widget_config_large),
                                                TileSize.EXTRA_LARGE to stringResource(R.string.widget_config_extra_large)
                                            ),
                                            selected = tileSize,
                                            onSelect = { viewModel.setTileSize(it) }
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            TransparentListItem(
                                headlineContent = { Text(stringResource(R.string.settings_language_label)) },
                                supportingContent = {
                                    ChipChoiceRow(
                                        options = listOf(
                                            // Czech first: Odorik is mainly used in Czechia.
                                            "cs" to stringResource(R.string.lang_czech),
                                            "en" to stringResource(R.string.lang_english)
                                        ),
                                        selected = language,
                                        onSelect = {
                                            if (it != language) {
                                                viewModel.setLanguage(it)
                                                (context as? MainActivity)?.updateLocale(it)
                                            }
                                        }
                                    )
                                }
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
                        Column {
                            TransparentListItem(
                                headlineContent = { Text(stringResource(R.string.history_period_label)) },
                                supportingContent = { Text(selectedPeriod, color = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.clickable { showHistoryPeriodDialog = true }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            val backupFileName = stringResource(
                                R.string.backup_file_name,
                                java.time.LocalDate.now().toString()
                            )
                            TransparentListItem(
                                headlineContent = { Text(stringResource(R.string.backup_export)) },
                                supportingContent = { Text(stringResource(R.string.backup_export_description)) },
                                trailingContent = {
                                    if (backupBusy) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Upload, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.clickable(enabled = !backupBusy) {
                                    exportLauncher.launch(backupFileName)
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            TransparentListItem(
                                headlineContent = { Text(stringResource(R.string.backup_import)) },
                                supportingContent = { Text(stringResource(R.string.backup_import_description)) },
                                trailingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                                modifier = Modifier.clickable(enabled = !backupBusy) {
                                    importLauncher.launch(arrayOf("*/*"))
                                }
                            )
                        }
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
                            TransparentListItem(
                                headlineContent = { Text(stringResource(R.string.auto_update_checking)) },
                                supportingContent = {
                                    Column {
                                        Text(stringResource(R.string.auto_update_checking_description))
                                        if (autoUpdateEnabled && !notificationsAllowed) {
                                            Text(
                                                stringResource(R.string.update_notifications_off_hint),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .clickable { context.openNotificationSettings() }
                                            )
                                        }
                                    }
                                },
                                trailingContent = {
                                    Switch(
                                        checked = autoUpdateEnabled,
                                        onCheckedChange = { enabled ->
                                            if (enabled) {
                                                enableAutoUpdate()
                                            } else {
                                                viewModel.setAutoUpdateEnabled(false)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = ScreenAccents.Settings.main(),
                                            checkedTrackColor = ScreenAccents.Settings.secondary().copy(alpha = 0.5f)
                                        )
                                    )
                                },
                                modifier = Modifier.clickable {
                                    if (!autoUpdateEnabled) {
                                        enableAutoUpdate()
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
                            TransparentListItem(
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
                                            checkedThumbColor = ScreenAccents.Settings.main(),
                                            checkedTrackColor = ScreenAccents.Settings.secondary().copy(alpha = 0.5f)
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
                            TransparentListItem(
                                headlineContent = { Text(stringResource(R.string.about_app)) },
                                supportingContent = {
                                    Text(
                                        stringResource(R.string.about_app_summary),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                modifier = Modifier.clickable { showAboutDialog = true }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // Version / Update
                            val isUpdateAvailable = updateViewModel.isUpdateAvailable()
                            TransparentListItem(
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
                                                tint = ScreenAccents.Settings.main()
                                            )
                                        }
                                    }
                                },
                                supportingContent = {
                                    Text(
                                        BuildConfig.VERSION_NAME,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
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

    if (showColorThemeDialog) {
        val options = buildList {
            add(AppTheme.STANDARD to (stringResource(R.string.app_theme_standard) to null as String?))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(
                    AppTheme.MATERIAL_YOU to (
                        stringResource(R.string.app_theme_material_you) to
                            stringResource(R.string.app_theme_material_you_subtitle)
                    )
                )
            }
            add(
                AppTheme.ODORIK to (
                    stringResource(R.string.app_theme_odorik) to
                        stringResource(R.string.app_theme_odorik_subtitle)
                )
            )
        }
        AlertDialog(
            onDismissRequest = { showColorThemeDialog = false },
            title = { Text(stringResource(R.string.settings_color_theme)) },
            text = {
                Column(Modifier.selectableGroup()) {
                    options.forEach { (theme, labels) ->
                        val (label, subtitle) = labels
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = appTheme == theme,
                                    onClick = {
                                        viewModel.setAppTheme(theme)
                                        showColorThemeDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = appTheme == theme, onClick = null)
                            Column(Modifier.padding(start = 8.dp)) {
                                Text(label)
                                if (subtitle != null) {
                                    Text(
                                        subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorThemeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showNavIconStyleDialog) {
        AlertDialog(
            onDismissRequest = { showNavIconStyleDialog = false },
            title = { Text(stringResource(R.string.settings_nav_icon_style)) },
            text = {
                Column(Modifier.selectableGroup()) {
                    NavIconStyle.entries.forEach { style ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = navIconStyle == style,
                                    onClick = {
                                        viewModel.setNavIconStyle(style)
                                        showNavIconStyleDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = navIconStyle == style, onClick = null)
                            Column(Modifier.padding(start = 8.dp)) {
                                Text(stringResource(navIconStyleLabel(style)))
                                // Preview of the bar: the first tab as selected, the rest as not.
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BottomNavItem.all.forEachIndexed { i, item ->
                                        Icon(
                                            item.icons.icon(selected = i == 0, style = style),
                                            contentDescription = null,
                                            tint = if (i == 0) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNavIconStyleDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // --- DIALOGS --- //

    pendingImport?.let { pending ->
        BackupImportDialog(
            preview = pending.preview,
            onConfirm = viewModel::confirmImport,
            onDismiss = viewModel::cancelImport
        )
    }

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

    deniedPermissionMessageRes?.let { messageRes ->
        AlertDialog(
            onDismissRequest = { deniedPermissionMessageRes = null },
            title = { Text(stringResource(R.string.permission_required_title)) },
            text = { Text(stringResource(messageRes)) },
            confirmButton = {
                TextButton(onClick = {
                    deniedPermissionMessageRes = null
                    context.openAppSettings()
                }) { Text(stringResource(R.string.open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { deniedPermissionMessageRes = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
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
            onOpenForum = { uriHandler.openUri("https://forum.odorik.cz/viewtopic.php?t=6042") },
            onOpenWebsite = { uriHandler.openUri("https://www.odorik.cz") }
        )
    }
}

private fun navIconStyleLabel(style: NavIconStyle): Int = when (style) {
    NavIconStyle.CLASSIC -> R.string.nav_icon_style_classic
    NavIconStyle.ROUNDED -> R.string.nav_icon_style_rounded
    NavIconStyle.TWO_TONE -> R.string.nav_icon_style_two_tone
}
