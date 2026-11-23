package com.odorik.odorikbuddy.ui.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.livecalls.HangUpButton
import com.odorik.odorikbuddy.ui.livecalls.LiveCallDuration
import com.odorik.odorikbuddy.ui.livecalls.LiveDot
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents

/** Live progress of a callback ordered from the Callback tab: Ordered → Ringing → Connected. */
@Composable
fun CallbackStatusCard(
    status: CallViewModel.CallbackStatus,
    onHangUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ringingReached = status is CallViewModel.CallbackStatus.Ringing ||
        status is CallViewModel.CallbackStatus.Connected ||
        status is CallViewModel.CallbackStatus.Ended
    val connectedReached = status is CallViewModel.CallbackStatus.Connected ||
        status is CallViewModel.CallbackStatus.Ended
    val ended = status is CallViewModel.CallbackStatus.Ended

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
                if (!ended) LiveDot() else Spacer(Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(if (ended) R.string.callback_status_ended else R.string.callback_status_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (status is CallViewModel.CallbackStatus.Connected) LiveCallDuration(status.call)
            }

            StatusStep(stringResource(R.string.callback_step_ordered), done = true, current = false)
            StatusStep(
                stringResource(R.string.callback_step_ringing),
                done = connectedReached,
                current = ringingReached && !connectedReached
            )
            StatusStep(
                stringResource(R.string.callback_step_connected),
                done = ended,
                current = connectedReached && !ended
            )

            if (!ended && status !is CallViewModel.CallbackStatus.Waiting) {
                HangUpButton(
                    onClick = onHangUp,
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.callback_hang_up)
                )
            }
        }
    }
}

@Composable
private fun StatusStep(label: String, done: Boolean, current: Boolean) {
    val green = ScreenAccents.CounterGreen.main()
    Row(verticalAlignment = Alignment.CenterVertically) {
        val circle = Modifier
            .size(24.dp)
            .clip(CircleShape)
        when {
            done -> Box(circle.background(green.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = green, modifier = Modifier.size(16.dp))
            }
            current -> Box(circle.background(green), contentAlignment = Alignment.Center) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
            }
            else -> Box(circle.border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (current) FontWeight.Medium else FontWeight.Normal,
            color = if (done || current) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
