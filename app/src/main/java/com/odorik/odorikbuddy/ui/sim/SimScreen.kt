package com.odorik.odorikbuddy.ui.sim

import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.model.SimCard
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.components.SkeletonCard
import com.odorik.odorikbuddy.ui.components.constrainedContentWidth
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import com.odorik.odorikbuddy.util.PhoneNumberUtils

private enum class SimDialog { FUP, SUSPEND, REACTIVATE }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SimScreen(
    internalNavController: NavHostController,
    viewModel: SimViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var dialog by remember { mutableStateOf<SimDialog?>(null) }
    val pullRefreshState = rememberPullRefreshState(state.isRefreshing, viewModel::refresh)

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            GradientHeader(
                title = stringResource(R.string.sim_screen_title),
                iconVector = Icons.Default.SimCard,
                accent = ScreenAccents.Settings,
                onBackClick = { internalNavController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState, enabled = state.card != null)
        ) {
            val card = state.card
            when {
                state.isLoading -> Column(
                    modifier = Modifier.padding(LocalAppDimens.current.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SkeletonCard(height = 80.dp)
                    SkeletonCard(height = 200.dp)
                    SkeletonCard(height = 180.dp)
                }

                card == null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.loadError.orEmpty(), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::load) { Text(stringResource(R.string.retry)) }
                }

                else -> SimContent(
                    number = viewModel.number,
                    card = card,
                    state = state,
                    onSwitch = viewModel::setSwitch,
                    onAskDialog = { dialog = it }
                )
            }

            PullRefreshIndicator(
                refreshing = state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = ScreenAccents.Settings.main()
            )
        }
    }

    when (dialog) {
        SimDialog.FUP -> ConfirmDialog(
            title = stringResource(R.string.sim_fup_title),
            text = stringResource(R.string.sim_fup_text),
            confirm = stringResource(R.string.sim_fup_confirm),
            destructive = false,
            onConfirm = { dialog = null; viewModel.restartData() },
            onDismiss = { dialog = null }
        )
        SimDialog.SUSPEND -> ConfirmDialog(
            title = stringResource(R.string.sim_suspend_title),
            text = stringResource(R.string.sim_suspend_text),
            confirm = stringResource(R.string.sim_suspend_confirm),
            destructive = true,
            onConfirm = { dialog = null; viewModel.setSuspended(true) },
            onDismiss = { dialog = null }
        )
        SimDialog.REACTIVATE -> ConfirmDialog(
            title = stringResource(R.string.sim_reactivate_title),
            text = stringResource(R.string.sim_reactivate_text),
            confirm = stringResource(R.string.sim_reactivate),
            destructive = false,
            onConfirm = { dialog = null; viewModel.setSuspended(false) },
            onDismiss = { dialog = null }
        )
        null -> {}
    }
}

@Composable
private fun SimContent(
    number: String,
    card: SimCard,
    state: SimUiState,
    onSwitch: (String, Boolean) -> Unit,
    onAskDialog: (SimDialog) -> Unit
) {
    val locked = card.hasChangesInProgress || state.busy != null
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .constrainedContentWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (card.hasChangesInProgress) {
            item { Note(stringResource(R.string.sim_change_in_progress)) }
        }

        // State
        item {
            SimCardBox {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        PhoneNumberUtils.formatForDisplay(card.phoneNumber ?: number),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    StateChip(card)
                }
            }
        }

        // Data usage
        item {
            SimCardBox {
                val context = LocalContext.current
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        stringResource(R.string.sim_data_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    card.dataPackage?.takeIf { it.isNotBlank() && it != "none" }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(8.dp))
                val used = state.usedThisMonth
                if (used == null) {
                    Text(stringResource(R.string.sim_data_unknown), style = MaterialTheme.typography.bodyMedium)
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            Formatter.formatShortFileSize(context, used),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        state.limit?.let { limit ->
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.sim_data_of_limit, Formatter.formatShortFileSize(context, limit)),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                    state.limit?.takeIf { it > 0 }?.let { limit ->
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (used.toFloat() / limit).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = ScreenAccents.Settings.main(),
                            strokeCap = StrokeCap.Round,
                            drawStopIndicator = {}
                        )
                    }
                    if (state.daily.any { it > 0 }) {
                        Spacer(Modifier.height(12.dp))
                        DailyBars(state.daily)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.sim_data_daily),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (card.mobileData != false) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onAskDialog(SimDialog.FUP) },
                        enabled = !locked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.busy == "fup") {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.sim_fup_reset))
                    }
                }
            }
        }

        // Switches
        item {
            SimCardBox(padded = false) {
                SwitchRow(stringResource(R.string.sim_mobile_data), null, card.mobileData, "mobile_data", state.busy, locked, onSwitch)
                Divider()
                SwitchRow(stringResource(R.string.sim_lte), stringResource(R.string.sim_lte_hint), card.lte, "lte", state.busy, locked, onSwitch)
                Divider()
                SwitchRow(
                    stringResource(R.string.sim_missed_calls_sms),
                    stringResource(R.string.sim_missed_calls_sms_hint),
                    card.missedCallsRegister, "missed_calls_register", state.busy, locked, onSwitch
                )
                Divider()
                InfoRow(stringResource(R.string.sim_roaming), card.roaming)
            }
        }

        // Packages (read-only)
        item {
            SimCardBox(padded = false) {
                InfoRow(
                    stringResource(R.string.sim_data_package),
                    packageLabel(card.dataPackage),
                    card.dataPackageNextMonth?.takeIf { it != card.dataPackage }
                        ?.let { stringResource(R.string.sim_next_month, packageLabel(it).orEmpty()) }
                )
                Divider()
                InfoRow(
                    stringResource(R.string.sim_voice_package),
                    packageLabel(card.voicePackage),
                    card.voicePackageNextMonth?.takeIf { it != card.voicePackage }
                        ?.let { stringResource(R.string.sim_next_month, packageLabel(it).orEmpty()) }
                )
            }
        }

        // Suspend / reactivate
        item {
            if (card.isSuspended) {
                Button(
                    onClick = { onAskDialog(SimDialog.REACTIVATE) },
                    enabled = !locked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScreenAccents.Settings.main(), contentColor = Color.White)
                ) { Text(stringResource(R.string.sim_reactivate)) }
            } else if (card.isActive) {
                OutlinedButton(
                    onClick = { onAskDialog(SimDialog.SUSPEND) },
                    enabled = !locked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.sim_suspend))
                }
            }
        }
    }
}

/** Odorik's "none" → "None"/"Žádný"; other package names are shown as they come. */
@Composable
private fun packageLabel(name: String?): String? =
    if (name.equals("none", ignoreCase = true)) stringResource(R.string.sim_package_none) else name

@Composable
private fun SimCardBox(padded: Boolean = true, content: @Composable () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = if (padded) Modifier.padding(LocalAppDimens.current.cardPadding) else Modifier) {
            content()
        }
    }
}

@Composable
private fun Divider() = HorizontalDivider(
    modifier = Modifier.padding(horizontal = 16.dp),
    color = MaterialTheme.colorScheme.outlineVariant
)

@Composable
private fun StateChip(card: SimCard) {
    val (text, color) = when {
        card.isActive -> stringResource(R.string.sim_state_active) to ScreenAccents.CounterGreen.main()
        card.isSuspended -> stringResource(R.string.sim_state_suspended) to ScreenAccents.CounterOrange.main()
        else -> (card.state ?: "?") to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
        Text(
            text,
            color = color,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DailyBars(daily: List<Long>) {
    val max = (daily.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val accent = ScreenAccents.Settings.main()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        daily.forEachIndexed { index, bytes ->
            val fraction = (bytes.toFloat() / max).coerceIn(0.04f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (index == daily.lastIndex) accent else accent.copy(alpha = 0.35f))
            )
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    hint: String?,
    value: Boolean?,
    key: String,
    busy: String?,
    locked: Boolean,
    onSwitch: (String, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            hint?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        when {
            busy == key -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            // Unknown value from Odorik: show nothing rather than a guess.
            value == null -> Text("–", style = MaterialTheme.typography.bodyLarge)
            else -> Switch(
                checked = value,
                onCheckedChange = { onSwitch(key, it) },
                enabled = !locked,
                colors = SwitchDefaults.colors(checkedTrackColor = ScreenAccents.Settings.main())
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?, extra: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            extra?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            value?.takeIf { it.isNotBlank() } ?: "–",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Note(text: String) {
    Surface(
        color = ScreenAccents.CounterOrange.main().copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = ScreenAccents.CounterOrange.main(), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirm: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirm, color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
