package com.odorik.odorikbuddy.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.odorik.odorikbuddy.BuildConfig
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.model.Line
import com.odorik.odorikbuddy.ui.components.TransparentListItem
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens

@Composable
internal fun LineInfoDialog(line: Line, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.line_info_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LocalAppDimens.current.spacing/2)) {
                Text(
                    "${stringResource(R.string.line_name_label)} ${line.name}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "${stringResource(R.string.caller_id_label_settings)} ${line.callerId}",
                    style = MaterialTheme.typography.bodyLarge
                )
                line.publicNumber?.let {
                    Text(
                        "${stringResource(R.string.public_number_label)} ${it}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                var showPassword by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${stringResource(R.string.password_label_settings)} ${if (showPassword) line.sipPassword else "••••••••"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    IconButton(
                        onClick = { showPassword = !showPassword },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) stringResource(R.string.a11y_hide_password) else stringResource(R.string.a11y_show_password),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = LocalAppDimens.current.spacing/2))
                Text(
                    stringResource(R.string.connected_devices_label),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (line.connectedDevices.isEmpty()) {
                    Text(
                        stringResource(R.string.none),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(LocalAppDimens.current.spacing/3)) {
                        line.connectedDevices.forEach { device ->
                            val ipAddress = device.publicSocket.substringBefore(':')
                            Column {
                                Text(
                                    "• ${device.userAgent}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "  IP: $ipAddress",
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
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = LocalAppDimens.current.spacing)
                )
                OutlinedTextField(
                    value = phoneNumberInput,
                    onValueChange = { phoneNumberInput = it },
                    label = {
                        Text(
                            stringResource(R.string.personal_phone_number),
                            style = MaterialTheme.typography.bodyLarge
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
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = LocalAppDimens.current.spacing/2)
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
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = LocalAppDimens.current.spacing/2)
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
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "${stringResource(R.string.latest_version)}: ${updateInfo.version}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (isUpdateAvailable) {
                            Text(
                                stringResource(R.string.update_available),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = LocalAppDimens.current.spacing/2)
                            )
                            Text(
                                updateInfo.message,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = LocalAppDimens.current.spacing/2)
                            )
                        } else {
                            Text(
                                stringResource(R.string.up_to_date),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = LocalAppDimens.current.spacing/2)
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
    onOpenForum: () -> Unit,
    onOpenWebsite: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.about_app)) },
        text = {
            Column {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LocalAppDimens.current.spacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_odorik_logo),
                        contentDescription = stringResource(R.string.a11y_odorik_logo),
                        modifier = Modifier
                            .height(48.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.version_label) + " " + BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }


                Text(
                    text = stringResource(R.string.about_app_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = LocalAppDimens.current.spacing / 2)
                )
                Text(
                    text = stringResource(R.string.about_fan_app),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = LocalAppDimens.current.spacing)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(bottom = LocalAppDimens.current.spacing / 2),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                TransparentListItem(
                    headlineContent = { Text(stringResource(R.string.odorik_website)) },
                    leadingContent = {
                        Icon(Icons.Default.Language, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onOpenWebsite() }
                )

                TransparentListItem(
                    headlineContent = { Text(stringResource(R.string.discussion_forum)) },
                    leadingContent = { Icon(Icons.Default.Forum, contentDescription = null) },
                    modifier = Modifier.clickable { onOpenForum() }
                )

                TransparentListItem(
                    headlineContent = { Text(stringResource(R.string.github_repository)) },
                    leadingContent = { Icon(Icons.Default.Code, contentDescription = null) },
                    modifier = Modifier.clickable { onOpenGitHub() }
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
