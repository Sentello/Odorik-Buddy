package com.odorik.odorikbuddy.ui.history

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.history.HistoryViewModel.HistoryDisplayItem
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import com.odorik.odorikbuddy.util.ApiDates
import com.odorik.odorikbuddy.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Locale





internal fun getFormatters(): Pair<SimpleDateFormat, SimpleDateFormat> {
    val currentLocale = Locale.getDefault()
    val timeFormat = SimpleDateFormat("HH:mm:ss", currentLocale)
    val fullFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", currentLocale)
    return Pair(timeFormat, fullFormat)
}

internal fun parseApiDate(isoDate: String): java.util.Date? {
    return ApiDates.parse(isoDate)?.let { java.util.Date.from(it) }
}

internal fun formatRelativeTime(isoDate: String, context: android.content.Context): String {
    val date = parseApiDate(isoDate) ?: return isoDate

    val (timeFormat, fullFormat) = getFormatters()

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

internal fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return when {
        minutes > 0 && remainingSeconds > 0 -> "${minutes}m ${remainingSeconds}s"
        minutes > 0 -> "${minutes}m"
        else -> "${remainingSeconds}s"
    }
}

internal fun getNetworkColor(destinationName: String?): androidx.compose.ui.graphics.Color? {
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
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(isRefreshing, { viewModel.fetchHistory(isRefresh = true) })

    val context = LocalContext.current
    val currentLanguage = remember {
        context.resources.configuration.locales[0].language
    }
    val currencyFormatter = remember { CurrencyFormatter(context) }

    var showFilterSheet by remember { mutableStateOf(false) }
    var filtersApplied by remember { mutableStateOf(false) }
    var wasRefreshing by remember { mutableStateOf(false) }


    LaunchedEffect(isRefreshing) {
        if (wasRefreshing && !isRefreshing && historyItems.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
        wasRefreshing = isRefreshing
    }


    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.loadContacts(context.contentResolver)
            }
        }
    )

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

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            GradientHeader(
                title = stringResource(R.string.history_title),
                iconVector = Icons.Default.History,
                accent = ScreenAccents.History,
                onActionClick = { showFilterSheet = true },
                actionIcon = Icons.Default.FilterList,
                actionContentDescription = stringResource(R.string.filter_history),
                actionTint = ScreenAccents.History.main()
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
                    currencyFormatter = currencyFormatter,
                    listState = listState
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
    currencyFormatter: CurrencyFormatter,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    val hasError = error != null

    when {
        isRefreshing && historyItems.isEmpty() && !hasError -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(LocalAppDimens.current.cardPadding)
            ) {
                com.odorik.odorikbuddy.ui.components.HistoryListSkeleton()
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

            val horizontalPadding = LocalAppDimens.current.cardPadding
            val bottomPadding = LocalAppDimens.current.spacing

            val dayGroups = remember(historyItems) {
                val groups = linkedMapOf<String, MutableList<HistoryDisplayItem>>()
                for (displayItem in historyItems) {
                    val date = parseApiDate(displayItem.item.date)
                    val key = if (date != null) {
                        val cal = java.util.Calendar.getInstance().apply { time = date }
                        "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
                    } else {
                        "unknown"
                    }
                    groups.getOrPut(key) { mutableListOf() }.add(displayItem)
                }
                groups.values.toList()
            }
            val todayLabel = stringResource(R.string.today)
            val yesterdayLabel = stringResource(R.string.yesterday)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = bottomPadding
                )
            ) {
                dayGroups.forEach { group ->
                    val first = group.firstOrNull()
                    val date = first?.let { parseApiDate(it.item.date) }
                    val headerLabel = if (date != null) {
                        val cal = java.util.Calendar.getInstance()
                        val todayStart = cal.apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        val yesterdayStart = cal.timeInMillis
                        when {
                            date.time >= todayStart -> todayLabel
                            date.time >= yesterdayStart -> yesterdayLabel
                            else -> java.text.SimpleDateFormat("d. M. yyyy", java.util.Locale.getDefault()).format(date)
                        }
                    } else null

                    if (headerLabel != null) {
                        item(key = "header_${first?.item?.id}") {
                            Text(
                                text = headerLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                        }
                    }
                    items(
                        count = group.size,
                        key = { idx ->
                            val displayItem = group[idx]
                            displayItem.item.id + if (displayItem.isChild) "_child" else ""
                        }
                    ) { idx ->
                        HistoryListItem(
                            displayItem = group[idx],
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
            modifier = Modifier,
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
                colors = ButtonDefaults.buttonColors(containerColor = ScreenAccents.History.main())
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
            colors = ButtonDefaults.buttonColors(containerColor = ScreenAccents.History.main())
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}





