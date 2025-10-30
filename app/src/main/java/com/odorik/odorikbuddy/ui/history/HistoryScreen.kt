package com.odorik.odorikbuddy.ui.history

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.model.HistoryItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyItems by viewModel.history.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(isRefreshing, { viewModel.fetchHistory() })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            if (historyItems.isEmpty() && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(historyItems) { item ->
                        HistoryListItem(item = item)
                        HorizontalDivider()
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryListItem(item: HistoryItem) {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
    val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val formattedDate = try {
        val date = inputFormat.parse(item.date)
        date?.let { outputFormat.format(it) } ?: item.date
    } catch (e: Exception) {
        item.date 
    }

    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isCall = item.length != null
        if (isCall) {
            val icon = when {
                item.status == "missed" -> Icons.Default.PhoneMissed
                item.direction == "in" -> Icons.Default.PhoneCallback
                item.direction == "out" -> Icons.Default.PhoneForwarded
                else -> Icons.Default.PhoneCallback 
            }
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.call_history)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Sms,
                contentDescription = stringResource(R.string.sms_history)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${stringResource(R.string.from_history)} ${item.source_number}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(item.source_number))
                        Toast.makeText(context, context.getString(R.string.number_coppied, item.source_number), Toast.LENGTH_SHORT).show()
                    }
                )
            )
            Text(
                text = "${stringResource(R.string.to_history)} ${item.destination_number}",
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(item.destination_number))
                        Toast.makeText(context, context.getString(R.string.number_coppied, item.destination_number), Toast.LENGTH_SHORT).show()
                    }
                )
            )
            Row {
                Text(text = formattedDate, style = MaterialTheme.typography.bodySmall)
                item.length?.let {
                    if (it > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatDuration(it),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.currency_format, item.price),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return when {
        minutes > 0 && remainingSeconds > 0 -> "${minutes}m ${remainingSeconds}s"
        minutes > 0 -> "${minutes}m"
        else -> "${remainingSeconds}s"
    }
}
