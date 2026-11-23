package com.odorik.odorikbuddy.ui.livecalls

import android.provider.Settings
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.model.ActiveCall
import com.odorik.odorikbuddy.ui.components.AccentIconChip
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import com.odorik.odorikbuddy.util.ApiDates
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

/** Dashboard card: shown only while at least one call is in progress. */
@Composable
fun LiveCallsCard(
    calls: List<ActiveCall>,
    displayName: (ActiveCall) -> String,
    subtitle: (ActiveCall) -> String?,
    onShowAll: () -> Unit,
    onHangUp: (ActiveCall) -> Unit,
    modifier: Modifier = Modifier
) {
    val first = calls.firstOrNull() ?: return
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(LocalAppDimens.current.cardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveDot()
                Spacer(Modifier.width(10.dp))
                Text(
                    text = pluralStringResource(R.plurals.live_calls_title, calls.size, calls.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onShowAll) {
                    Text(stringResource(R.string.live_calls_show))
                }
            }
            LiveCallRow(call = first, name = displayName(first), subtitle = subtitle(first))
            HangUpButton(onClick = { onHangUp(first) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun LiveDot(modifier: Modifier = Modifier) {
    val green = ScreenAccents.CounterGreen.main()
    Box(modifier = modifier.size(18.dp), contentAlignment = Alignment.Center) {
        if (systemAnimationsEnabled()) {
            val transition = rememberInfiniteTransition(label = "liveDot")
            val pulse = remember {
                infiniteRepeatable(
                    animation = tween<Float>(durationMillis = 700, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                )
            }
            val scale by transition.animateFloat(
                initialValue = 1f,
                targetValue = 1.6f,
                animationSpec = pulse,
                label = "liveDotScale"
            )
            val haloAlpha by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.05f,
                animationSpec = pulse,
                label = "liveDotAlpha"
            )
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = haloAlpha
                    }
                    .background(green, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(green.copy(alpha = 0.2f), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(green, CircleShape)
        )
    }
}

/** True unless the system animator duration scale is 0 (reduced motion). */
@Composable
private fun systemAnimationsEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        runCatching {
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        }.getOrDefault(true)
    }
}

@Composable
fun LiveCallRow(call: ActiveCall, name: String, subtitle: String?, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AccentIconChip(
            icon = Icons.Default.Call,
            accent = ScreenAccents.Dashboard.main(),
            size = 40.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        LiveCallDuration(call)
    }
}

/** "Ringing…" until answered, then a locally ticking mm:ss / h:mm:ss. */
@Composable
fun LiveCallDuration(call: ActiveCall, modifier: Modifier = Modifier) {
    val answered = remember(call.answerDate) { call.answerDate?.let(ApiDates::parse) }
    if (answered == null) {
        Text(
            text = stringResource(R.string.live_call_ringing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
        return
    }
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(answered) {
        while (true) {
            now = Instant.now()
            delay(1_000)
        }
    }
    Text(
        text = formatElapsed(Duration.between(answered, now)),
        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

internal fun formatElapsed(duration: Duration): String {
    val total = duration.seconds.coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

@Composable
fun HangUpButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.live_call_hang_up)
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun HangUpConfirmDialog(name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CallEnd, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(stringResource(R.string.live_call_hang_up_title)) },
        text = { Text(stringResource(R.string.live_call_hang_up_text, name)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.live_call_hang_up), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** Starts polling while the screen is visible (ON_START) and stops when it isn't. */
@Composable
fun LiveCallsLifecycle(viewModel: LiveCallsViewModel) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START -> viewModel.start()
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> viewModel.stop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stop()
        }
    }
}
