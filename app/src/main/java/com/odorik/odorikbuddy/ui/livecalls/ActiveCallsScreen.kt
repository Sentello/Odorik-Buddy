package com.odorik.odorikbuddy.ui.livecalls

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.model.ActiveCall
import com.odorik.odorikbuddy.ui.components.AccentIconChip
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents

/** All calls in progress, each with its own hang-up button. Opened from the Dashboard card. */
@Composable
fun ActiveCallsScreen(
    navController: NavController,
    viewModel: LiveCallsViewModel = hiltViewModel()
) {
    LiveCallsLifecycle(viewModel)
    val calls by viewModel.calls.collectAsState()
    // Read so the rows recompose (caller instead of own number) once own numbers are known.
    viewModel.ownNumbersLoaded.collectAsState().value
    var pendingHangUp by remember { mutableStateOf<ActiveCall?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.hangUpEvent.collect { ok ->
            snackbarHostState.showSnackbar(
                context.getString(if (ok) R.string.live_call_ended else R.string.live_call_hang_up_failed)
            )
        }
    }

    Scaffold(
        topBar = {
            GradientHeader(
                title = stringResource(R.string.live_calls_screen_title),
                iconVector = Icons.Default.Call,
                accent = ScreenAccents.Dashboard,
                onBackClick = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(LocalAppDimens.current.screenPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.live_calls_auto_refresh),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (calls.isEmpty()) {
                EmptyLiveCalls()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(calls, key = { it.id }) { call ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(LocalAppDimens.current.cardPadding),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                LiveCallRow(
                                    call = call,
                                    name = viewModel.displayName(call),
                                    subtitle = viewModel.subtitle(call)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    call.line?.let { line ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                stringResource(R.string.live_call_line, line.toString()),
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    HangUpButton(onClick = { pendingHangUp = call })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingHangUp?.let { call ->
        HangUpConfirmDialog(
            name = viewModel.displayName(call),
            onConfirm = {
                viewModel.hangUp(call.id)
                pendingHangUp = null
            },
            onDismiss = { pendingHangUp = null }
        )
    }
}

@Composable
private fun EmptyLiveCalls() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            AccentIconChip(icon = Icons.Default.Call, accent = ScreenAccents.Dashboard.main(), size = 72.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.live_calls_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.live_calls_empty_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
