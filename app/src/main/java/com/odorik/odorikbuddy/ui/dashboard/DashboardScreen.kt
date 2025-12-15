package com.odorik.odorikbuddy.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.toArgb
import android.graphics.Typeface
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.odorik.odorikbuddy.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.material3.ExtendedFloatingActionButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val creditState by viewModel.credit.collectAsState()
    val todaysSpending by viewModel.todaysSpending.collectAsState()
    val thisMonthsSpending by viewModel.thisMonthsSpending.collectAsState()
    val weeklySpending by viewModel.weeklySpending.collectAsState()
    val error by viewModel.error.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()

    val pullRefreshState = rememberPullRefreshState(isRefreshing, { viewModel.refresh() })

    LaunchedEffect(Unit) {
        viewModel.loadData(true) 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard)) },
                windowInsets = WindowInsets.statusBars
            )
        },
    ) { padding ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.systemBars)
                .pullRefresh(pullRefreshState)
        ) {
            val configuration = LocalConfiguration.current
            val horizontalPadding = when {
                configuration.screenWidthDp < 600 -> 16.dp
                else -> 24.dp
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    val currentCreditState = creditState
                    when (currentCreditState) {
                        is DashboardViewModel.UiState.Loading -> {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.loading),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        is DashboardViewModel.UiState.Success -> {
                            val balance = currentCreditState.data
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it })
                            ) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.AccountBalanceWallet,
                                                contentDescription = "Balance icon",
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.balance),
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        }
                                        Text(
                                            text = "%.2f Kč".format(balance),
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.semantics {
                                                contentDescription = "Current balance: %.2f Kč".format(balance)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        is DashboardViewModel.UiState.Error -> {
                            val errorMsg = currentCreditState.message
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error icon",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = errorMsg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { viewModel.refresh() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(
                        visible = error == null,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it })
                    ) {
                        SpendingSummary(todaysSpending, thisMonthsSpending)
                    }
                    (error?.let { err ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error icon",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.error, err),
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.semantics {
                                        contentDescription = "Error: $err"
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.refresh() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    })
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SpendingChart(weeklySpending)
                }
            }
            
            val currentCredit = creditState
            if (isInitialLoading && currentCredit is DashboardViewModel.UiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun SpendingSummary(todaysSpending: Double, thisMonthsSpending: Double) {
    val todaysLabel = stringResource(R.string.currency_format, todaysSpending)
    val monthsLabel = "%.2f Kč".format(thisMonthsSpending)
    val summaryDesc = "Spending summary: Today's $todaysLabel, this month's $monthsLabel"
    val todaysDesc = "Today's spending: $todaysLabel"
    val monthsDesc = "This month's spending: $monthsLabel"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = summaryDesc
                heading()
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Spending summary icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.spending_summary),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = todaysDesc
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.todays_spending))
                Text(
                    text = todaysLabel,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = monthsDesc
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.this_months_spending))
                Text(
                    text = monthsLabel,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SpendingChart(weeklySpending: List<Double>) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales.get(0)
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Weekly spending chart for last 7 days. Values: ${weeklySpending.joinToString(", ") { "%.2f Kč".format(it) }} from oldest to newest."
                heading()
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Weekly spending chart icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.last_7_days),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (weeklySpending.isNotEmpty()) {
                val maxSpending = weeklySpending.maxOrNull() ?: 0.0
                if (maxSpending > 0) {
                    AndroidView(
                        factory = { ctx ->
                            BarChart(ctx).apply {
                                
                                val entries = (0 until 7).map { index ->
                                    val spending = weeklySpending.getOrNull(index) ?: 0.0
                                    BarEntry(index.toFloat(), spending.toFloat())
                                }
                                val dataSet = BarDataSet(entries, "Spending").apply {
                                    color = primaryColor
                                    valueTypeface = Typeface.DEFAULT_BOLD
                                    valueTextSize = 12f
                                    valueTextColor = primaryColor
                                }
                                data = BarData(dataSet)

                                
                                val dayFormat = SimpleDateFormat("EEE", locale)
                                val days = (6 downTo 0).map { i ->
                                    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                                }.map { dayFormat.format(it.time) }
                                xAxis.valueFormatter = IndexAxisValueFormatter(days)
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                xAxis.granularity = 1f
                                xAxis.isGranularityEnabled = true

                                
                                axisLeft.setAxisMaximum(maxSpending.toFloat())
                                axisLeft.setAxisMinimum(0f)
                                axisRight.isEnabled = false

                                
                                description.isEnabled = false
                                legend.isEnabled = false

                                
                                animateY(1000)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .semantics {
                                role = Role.Image
                                contentDescription = "Bar chart of weekly spending. Tap for details."
                            }
                    )
                } else {
                    Text(
                        text = "No spending data available",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.semantics {
                            contentDescription = "No spending data available for the week"
                        }
                    )
                }
            } else {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        contentDescription = "No weekly spending data available"
                    }
                )
            }
        }
    }
}