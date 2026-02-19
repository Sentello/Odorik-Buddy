package com.odorik.odorikbuddy.ui.history

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.model.HistoryItem
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.history.HistoryViewModel.HistoryDisplayItem
import com.odorik.odorikbuddy.ui.theme.HistoryAccent
import com.odorik.odorikbuddy.ui.theme.HistoryAccentLight
import com.odorik.odorikbuddy.util.CurrencyFormatter
import com.odorik.odorikbuddy.util.getResponsiveCardPadding
import com.odorik.odorikbuddy.util.getResponsiveSpacing
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone





private fun parseApiDate(isoDate: String): java.util.Date? {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
    return try {
        inputFormat.parse(isoDate)
    } catch (_: Exception) {
        null
    }
}

private fun formatRelativeTime(isoDate: String, context: android.content.Context): String {
    val date = parseApiDate(isoDate) ?: return isoDate
    
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val fullFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    
    val calendar = java.util.Calendar.getInstance()
    val todayStart = calendar.apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
    val yesterdayStart = calendar.timeInMillis
    
    return when {
        date.time >= todayStart -> context.getString(R.string.today) + " " + timeFormat.format(date)
        date.time >= yesterdayStart -> context.getString(R.string.yesterday) + " " + timeFormat.format(date)
        else -> fullFormat.format(date)
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

private fun getNetworkColor(destinationName: String?): androidx.compose.ui.graphics.Color? {
    return when {
        destinationName == null -> null
        destinationName.contains("mobil", ignoreCase = true) -> androidx.compose.ui.graphics.Color(0xFF2196F3) 
        destinationName.contains("pevná", ignoreCase = true) -> androidx.compose.ui.graphics.Color(0xFF4CAF50) 
        destinationName.contains("SMS", ignoreCase = true) -> androidx.compose.ui.graphics.Color(0xFF9C27B0) 
        destinationName.contains("800", ignoreCase = true) -> androidx.compose.ui.graphics.Color(0xFF009688) 
        else -> androidx.compose.ui.graphics.Color(0xFF757575) 
    }
}





@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyItems by viewModel.filteredHistory.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val lines by viewModel.lines.collectAsState()
    val selectedLine by viewModel.selectedLine.collectAsState()
    val filterNumber by viewModel.filterNumber.collectAsState()
    val eventTypeFilter by viewModel.eventTypeFilter.collectAsState()
    val eventDirectionFilter by viewModel.eventDirectionFilter.collectAsState()
    val pullRefreshState = rememberPullRefreshState(isRefreshing, { viewModel.fetchHistory(isRefresh = true) })
    
    val context = LocalContext.current
    val currentLanguage = remember {
        context.resources.configuration.locales[0].language
    }
    val currencyFormatter = remember { CurrencyFormatter(context) }
    
    var showFilterSheet by remember { mutableStateOf(false) }
    var filtersApplied by remember { mutableStateOf(false) }
    
    
    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.loadContacts(context.contentResolver)
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.refreshIfStale()
    }

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) -> {
                viewModel.loadContacts(context.contentResolver)
            }
            else -> {
                readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            
            GradientHeader(
                title = stringResource(R.string.history_title),
                iconVector = Icons.Default.History,
                backgroundBrush = Brush.verticalGradient(
                    colors = listOf(
                        HistoryAccent.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                iconGradientBrush = Brush.linearGradient(
                    colors = listOf(HistoryAccent, HistoryAccentLight)
                ),
                onActionClick = { showFilterSheet = true },
                actionIcon = Icons.Default.FilterList,
                actionContentDescription = stringResource(R.string.filter_history),
                actionTint = HistoryAccent
            )
            
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                LaunchedEffect(filterNumber, selectedLine, filtersApplied) {
                    if (!filtersApplied) {
                        if (filterNumber.isEmpty() && selectedLine == null) {
                            filtersApplied = true
                        } else if (filterNumber.isNotEmpty() || selectedLine != null) {
                            filterNumber.takeIf { it.isNotEmpty() }?.let { viewModel.setFilterNumber(it) }
                            selectedLine?.let { viewModel.setSelectedLine(it) }
                            filtersApplied = true
                        }
                    }
                }

                
                HistoryContent(
                    historyItems = historyItems,
                    isRefreshing = isRefreshing,
                    error = error,
                    viewModel = viewModel,
                    language = currentLanguage,
                    currencyFormatter = currencyFormatter
                )
                
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
        
        
        if (showFilterSheet) {
            FilterBottomSheet(
                lines = lines,
                selectedLine = selectedLine,
                filterNumber = filterNumber,
                eventTypeFilter = eventTypeFilter,
                eventDirectionFilter = eventDirectionFilter,
                viewModel = viewModel,
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}





@Composable
private fun HistoryContent(
    historyItems: List<HistoryDisplayItem>,
    isRefreshing: Boolean,
    error: String?,
    viewModel: HistoryViewModel,
    language: String,
    currencyFormatter: CurrencyFormatter
) {
    val hasError = error != null

    when {
        isRefreshing && historyItems.isEmpty() && !hasError -> {
            
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = HistoryAccent)
            }
        }
        hasError -> {
            
            ErrorState(
                error = error!!,
                onRetry = { viewModel.fetchHistory(isRefresh = true) },
                onClose = { viewModel.clearError() }
            )
        }
        historyItems.isEmpty() -> {
            
            EmptyState(onRetry = { viewModel.fetchHistory(isRefresh = true) })
        }
        else -> {
            
            val horizontalPadding = getResponsiveCardPadding()
            val bottomPadding = getResponsiveSpacing()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = bottomPadding
                )
            ) {
                itemsIndexed(historyItems) { index, displayItem ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300, delayMillis = index * 30)) +
                                slideInVertically(
                                    initialOffsetY = { it / 4 },
                                    animationSpec = tween(300, delayMillis = index * 30)
                                )
                    ) {
                        HistoryListItem(
                            displayItem = displayItem,
                            viewModel = viewModel,
                            language = language,
                            currencyFormatter = currencyFormatter
                        )
                    }
                }
            }
        }
    }
}





@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.error_loading_history),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.close))
            }
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = HistoryAccent)
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}





@Composable
private fun EmptyState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_history_found),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.pull_to_refresh_or_change_period_in_settings),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = HistoryAccent)
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    lines: List<com.odorik.odorikbuddy.data.model.Line>,
    selectedLine: com.odorik.odorikbuddy.data.model.Line?,
    filterNumber: String,
    eventTypeFilter: String,
    eventDirectionFilter: String,
    viewModel: HistoryViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var lineExpanded by remember { mutableStateOf(false) }
    var tempSelectedLine by remember { mutableStateOf(selectedLine) }
    var tempFilterNumber by remember { mutableStateOf(filterNumber) }
    var tempEventTypeFilter by remember { mutableStateOf(eventTypeFilter) }
    var tempEventDirectionFilter by remember { mutableStateOf(eventDirectionFilter) }
    var eventTypeExpanded by remember { mutableStateOf(false) }
    var eventDirectionExpanded by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = getResponsiveSpacing() * 5)
        ) {
            
            GradientHeader(
                title = stringResource(R.string.filter_history),
                iconVector = Icons.Default.FilterList,
                backgroundBrush = Brush.verticalGradient(
                    colors = listOf(
                        HistoryAccent.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                iconGradientBrush = Brush.linearGradient(
                    colors = listOf(HistoryAccent, HistoryAccentLight)
                ),
                iconSize = 20.dp,
                iconContainerSize = 36.dp,
                iconCornerRadius = 10.dp
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = getResponsiveCardPadding()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                
                ExposedDropdownMenuBox(
                    expanded = lineExpanded,
                    onExpandedChange = { lineExpanded = !lineExpanded }
                ) {
                    OutlinedTextField(
                        value = tempSelectedLine?.caller_id ?: stringResource(R.string.all_lines),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.line_filter)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lineExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HistoryAccent,
                            focusedLabelColor = HistoryAccent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = lineExpanded,
                        onDismissRequest = { lineExpanded = false }
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_lines)) },
                            onClick = {
                                tempSelectedLine = null
                                lineExpanded = false
                            }
                        )
                        lines.forEach { line ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(line.caller_id) },
                                onClick = {
                                    tempSelectedLine = line
                                    lineExpanded = false
                                }
                            )
                        }
                    }
                }
                
                
                OutlinedTextField(
                    value = tempFilterNumber,
                    onValueChange = { tempFilterNumber = it },
                    label = { Text(stringResource(R.string.number_filter)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HistoryAccent,
                        focusedLabelColor = HistoryAccent
                    )
                )
                
                
                ExposedDropdownMenuBox(
                    expanded = eventTypeExpanded,
                    onExpandedChange = { eventTypeExpanded = !eventTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = when (tempEventTypeFilter) {
                            "call" -> stringResource(R.string.event_type_call)
                            "sms" -> stringResource(R.string.event_type_sms)
                            else -> stringResource(R.string.event_type_all)
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.event_type_filter)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventTypeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HistoryAccent,
                            focusedLabelColor = HistoryAccent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = eventTypeExpanded,
                        onDismissRequest = { eventTypeExpanded = false }
                    ) {
                        listOf("all" to R.string.event_type_all, "call" to R.string.event_type_call, "sms" to R.string.event_type_sms).forEach { (value, resId) ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(resId)) },
                                onClick = {
                                    tempEventTypeFilter = value
                                    eventTypeExpanded = false
                                }
                            )
                        }
                    }
                }
                
                
                ExposedDropdownMenuBox(
                    expanded = eventDirectionExpanded,
                    onExpandedChange = { eventDirectionExpanded = !eventDirectionExpanded }
                ) {
                    OutlinedTextField(
                        value = when (tempEventDirectionFilter) {
                            "incoming" -> stringResource(R.string.event_direction_incoming)
                            "outgoing" -> stringResource(R.string.event_direction_outgoing)
                            else -> stringResource(R.string.event_direction_all)
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.event_direction_filter)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventDirectionExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HistoryAccent,
                            focusedLabelColor = HistoryAccent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = eventDirectionExpanded,
                        onDismissRequest = { eventDirectionExpanded = false }
                    ) {
                        listOf("all" to R.string.event_direction_all, "incoming" to R.string.event_direction_incoming, "outgoing" to R.string.event_direction_outgoing).forEach { (value, resId) ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(resId)) },
                                onClick = {
                                    tempEventDirectionFilter = value
                                    eventDirectionExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val lineChanged = tempSelectedLine != selectedLine
                            val numberChanged = tempFilterNumber != filterNumber
                            val eventTypeChanged = tempEventTypeFilter != eventTypeFilter
                            val eventDirectionChanged = tempEventDirectionFilter != eventDirectionFilter
                            
                            if (lineChanged || numberChanged || eventTypeChanged || eventDirectionChanged) {
                                viewModel.setSelectedLine(tempSelectedLine)
                                viewModel.setFilterNumber(tempFilterNumber)
                                viewModel.setEventTypeFilter(tempEventTypeFilter)
                                viewModel.setEventDirectionFilter(tempEventDirectionFilter)
                            } else {
                                viewModel.refreshData()
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HistoryAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.apply_filters))
                    }
                    
                    if (selectedLine != null || filterNumber.isNotEmpty() || eventTypeFilter != "all" || eventDirectionFilter != "all") {
                        Button(
                            onClick = {
                                tempSelectedLine = null
                                tempFilterNumber = ""
                                tempEventTypeFilter = "all"
                                tempEventDirectionFilter = "all"
                                viewModel.setSelectedLine(null)
                                viewModel.setFilterNumber("")
                                viewModel.setEventTypeFilter("all")
                                viewModel.setEventDirectionFilter("all")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.clear_filters))
                        }
                    }
                }
            }
        }
    }
}





@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryListItem(
    displayItem: HistoryDisplayItem,
    viewModel: HistoryViewModel,
    language: String,
    currencyFormatter: CurrencyFormatter
) {
    val item = displayItem.item
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    val statusColor = when {
        item.status == "missed" -> MaterialTheme.colorScheme.error
        item.direction == "in" -> MaterialTheme.colorScheme.primary
        item.direction == "out" -> MaterialTheme.colorScheme.secondary
        item.direction == "redirected" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = {  },
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(item.source_number))
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.number_coppied, item.source_number),
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (displayItem.isChild) 1.dp else 2.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (displayItem.isChild) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                statusColor,
                                statusColor.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
                    .align(Alignment.CenterVertically)
            ) {
                
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                
                if (displayItem.isChild) {
                    ChildConnector()
                }
                
                Row(
                    modifier = Modifier
                        .padding(
                            start = if (displayItem.isChild) 28.dp else 12.dp,
                            top = if (displayItem.isChild) 4.dp else getResponsiveCardPadding(),
                            end = getResponsiveCardPadding(),
                            bottom = getResponsiveCardPadding()
                        )
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    EventIcon(item = item, statusColor = statusColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    
                    ItemDetails(
                        item = item,
                        displayItem = displayItem,
                        viewModel = viewModel,
                        clipboardManager = clipboardManager,
                        context = context,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    
                    PriceAndDuration(
                        item = item,
                        displayItem = displayItem,
                        currencyFormatter = currencyFormatter,
                        language = language
                    )
                }
            }
        }
    }
}





@Composable
private fun ChildConnector() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Spacer(
            modifier = Modifier
                .width(2.dp)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.redirected_to),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusIndicator(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}

@Composable
private fun EventIcon(
    item: HistoryItem,
    statusColor: Color
) {
    val isCall = item.length != null
    val icon: ImageVector
    val backgroundColor: Color
    
    if (isCall) {
        icon = when {
            item.status == "missed" -> Icons.AutoMirrored.Filled.PhoneMissed
            item.direction == "in" -> Icons.AutoMirrored.Filled.PhoneCallback
            item.direction == "redirected" -> Icons.AutoMirrored.Filled.PhoneForwarded
            item.direction == "out" -> Icons.Filled.Call
            else -> Icons.AutoMirrored.Filled.PhoneCallback
        }
        backgroundColor = statusColor
    } else {
        icon = Icons.Default.Sms
        backgroundColor = HistoryAccent
    }
    
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isCall) stringResource(R.string.call_history) else stringResource(R.string.sms_history),
            tint = backgroundColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemDetails(
    item: HistoryItem,
    displayItem: HistoryDisplayItem,
    viewModel: HistoryViewModel,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    val sourceDisplayName = viewModel.getContactName(item.source_number)
    val contactName = viewModel.getContactName(item.destination_number)
    val destinationDisplayName = if (item.destination_name != null) {
        if (contactName != item.destination_number) {
            "$contactName (${item.destination_name})"
        } else {
            "${item.destination_number} (${item.destination_name})"
        }
    } else {
        contactName
    }
    
    
    val relativeTime = formatRelativeTime(item.date, context)
    
    
    val networkColor = getNetworkColor(item.destination_name)
    
    Column(modifier = modifier) {
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${stringResource(R.string.from_history)} $sourceDisplayName",
                fontWeight = if (displayItem.isChild) FontWeight.Normal else FontWeight.Bold,
                fontSize = if (displayItem.isChild) 14.sp else 16.sp,
                color = if (displayItem.isChild) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false)
            )
            
            if (item.recording != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.recording_available),
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        
        
        Text(
            text = "${stringResource(R.string.to_history)} $destinationDisplayName",
            fontSize = if (displayItem.isChild) 12.sp else 14.sp,
            color = if (displayItem.isChild) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.combinedClickable(
                onClick = {  },
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(item.destination_number))
                    Toast.makeText(
                        context,
                        context.getString(R.string.number_coppied, item.destination_number),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        )
        
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            
            if (networkColor != null && item.destination_name != null) {
                val networkLabelResId = when {
                    item.destination_name.contains("mobil", ignoreCase = true) -> R.string.network_mobile
                    item.destination_name.contains("pevná", ignoreCase = true) -> R.string.network_landline
                    item.destination_name.contains("SMS", ignoreCase = true) -> R.string.network_sms
                    item.destination_name.contains("800", ignoreCase = true) -> R.string.network_toll_free
                    else -> null
                }
                if (networkLabelResId != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(networkColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(networkLabelResId),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = networkColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            
            
            Text(
                text = relativeTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PriceAndDuration(
    item: HistoryItem,
    displayItem: HistoryDisplayItem,
    currencyFormatter: CurrencyFormatter,
    language: String
) {
    val formattedPrice = currencyFormatter.formatCurrency(item.price, language)
    
    Column(horizontalAlignment = Alignment.End) {
        
        Text(
            text = formattedPrice,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.End
        )
        
        
        if (item.price_per_minute != null && item.price_per_minute > 0) {
            Text(
                text = currencyFormatter.formatCurrency(item.price_per_minute, language) + "/min",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
        
        when {
            item.length != null && item.length > 0 -> {
                DurationIndicator(
                    length = item.length,
                    isChild = displayItem.isChild
                )
            }
            item.ringing_length != null && item.ringing_length > 0 && item.status == "missed" -> {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(((item.ringing_length / 10f).coerceAtMost(20f)).coerceAtLeast(6f).dp)
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.rang_duration, item.ringing_length),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationIndicator(length: Int, isChild: Boolean) {
    Row(
        modifier = Modifier.padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(6.dp)
                .width(((length / 60f).coerceAtMost(30f)).coerceAtLeast(6f).dp)
                .background(
                    if (isChild) {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    },
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formatDuration(length),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isChild) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
        )
    }
}
