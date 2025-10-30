package com.odorik.odorikbuddy.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Sms
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.model.HistoryItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyItems by viewModel.history.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) }
            )
        }
    ) { padding ->
        if (historyItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(historyItems) { item ->
                    HistoryListItem(item = item)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun HistoryListItem(item: HistoryItem) {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
    val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

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
        
        val icon = if (item.length != null) Icons.Default.Call else Icons.Default.Sms
        Icon(imageVector = icon, contentDescription = if (item.length != null) stringResource(R.string.call_history) else stringResource(R.string.sms_history))
        
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = "${stringResource(R.string.from_history)} ${item.source_number}", fontWeight = FontWeight.Bold)
            Text(text = "${stringResource(R.string.to_history)} ${item.destination_number}")
            Text(text = formattedDate, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.currency_format, item.price),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
