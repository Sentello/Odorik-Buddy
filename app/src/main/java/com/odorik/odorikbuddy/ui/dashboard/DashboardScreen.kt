package com.odorik.odorikbuddy.ui.dashboard

import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.util.CurrencyFormatter
import com.odorik.odorikbuddy.util.getResponsiveCardPadding
import com.odorik.odorikbuddy.util.getResponsiveChartHeight
import com.odorik.odorikbuddy.util.getResponsiveHeadlineLargeSize
import com.odorik.odorikbuddy.util.getResponsivePadding
import com.odorik.odorikbuddy.util.getResponsiveSpacing
import com.odorik.odorikbuddy.util.getResponsiveTitleLargeSize
import java.text.SimpleDateFormat


class TwoDecimalValueFormatter : ValueFormatter() {
    private val symbols = java.text.DecimalFormatSymbols(java.util.Locale("cs", "CZ"))
    private val format = java.text.DecimalFormat("0.00", symbols)

    override fun getFormattedValue(value: Float): String {
        return if (value > 0) {
            format.format(value)
        } else {
            ""
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val creditState by viewModel.credit.collectAsState()
    val todaysSpending by viewModel.todaysSpending.collectAsState()
    val thisMonthsSpending by viewModel.thisMonthsSpending.collectAsState()
    val spendingChartData by viewModel.spendingChartData.collectAsState()
    val spendingChartAverage by viewModel.spendingChartAverage.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val error by viewModel.error.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()

    
    val context = LocalContext.current
    val currentLanguage = remember {
        val locale = context.resources.configuration.locales[0]
        locale.language
    }
    val currencyFormatter = remember { CurrencyFormatter(context) }

    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullRefreshState(isRefreshing, { viewModel.refresh() })

    LaunchedEffect(Unit) {
        viewModel.loadData(true)
    }

    
    val currentStartDate by viewModel.startDate.collectAsState()
    val currentEndDate by viewModel.endDate.collectAsState()
    
    LaunchedEffect(Unit) {
        snapshotFlow { 
            navController.currentBackStackEntry?.savedStateHandle?.get<Long>("startDate") to
            navController.currentBackStackEntry?.savedStateHandle?.get<Long>("endDate")
        }.collect { (startDateValue, endDateValue) ->
            if (startDateValue != null && endDateValue != null) {
                val newStartDate = java.time.LocalDate.ofEpochDay(startDateValue)
                val newEndDate = java.time.LocalDate.ofEpochDay(endDateValue)
                
                
                if (newStartDate != currentStartDate || newEndDate != currentEndDate) {
                    viewModel.updateDateRange(newStartDate, newEndDate)
                    
                    
                    navController.currentBackStackEntry?.savedStateHandle?.remove<Long>("startDate")
                    navController.currentBackStackEntry?.savedStateHandle?.remove<Long>("endDate")
                }
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                actionLabel = "Dismiss"
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard)) },
                windowInsets = WindowInsets.statusBars
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.systemBars)
                .pullRefresh(pullRefreshState)
        ) {
            val isCriticalError = !isInitialLoading && creditState is DashboardViewModel.UiState.Error

            if (isInitialLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (isCriticalError) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error icon",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (creditState as DashboardViewModel.UiState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadData(true) }) { 
                        Text("Retry")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(getResponsivePadding()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        val currentCreditState = creditState
                        if (currentCreditState is DashboardViewModel.UiState.Success) {
                            val balance = currentCreditState.data
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it })
                            ) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(getResponsiveCardPadding())) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.AccountBalanceWallet,
                                                contentDescription = "Balance icon",
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(getResponsiveSpacing()/2))
                                            Text(
                                                text = stringResource(R.string.balance),
                                                fontSize = getResponsiveTitleLargeSize(),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        val formattedBalance = currencyFormatter.formatCurrency(balance, currentLanguage)
                                        Text(
                                            text = formattedBalance,
                                            fontSize = getResponsiveHeadlineLargeSize(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.semantics {
                                                contentDescription = "Current balance: $formattedBalance"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(getResponsiveSpacing()))
                        SpendingSummary(todaysSpending, thisMonthsSpending, currentLanguage, currencyFormatter)
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(getResponsiveSpacing()))
                        SpendingChart(spendingChartData, spendingChartAverage, startDate, endDate, navController, viewModel, currentLanguage, currencyFormatter)
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

@Composable
fun SpendingSummary(
    todaysSpending: Double, 
    thisMonthsSpending: Double,
    language: String,
    currencyFormatter: CurrencyFormatter
) {
    val todaysLabel = currencyFormatter.formatCurrency(todaysSpending, language)
    val monthsLabel = currencyFormatter.formatCurrency(thisMonthsSpending, language)
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
        Column(modifier = Modifier.padding(getResponsiveCardPadding())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Spending summary icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(getResponsiveSpacing()/2))
                Text(
                    text = stringResource(R.string.spending_summary),
                    fontSize = getResponsiveTitleLargeSize(),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(getResponsiveSpacing()/2))
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
fun SpendingChart(
    spendingChartData: List<DashboardViewModel.ChartDay>,
    spendingChartAverage: Double,
    startDate: java.time.LocalDate,
    endDate: java.time.LocalDate,
    navController: NavController,
    viewModel: DashboardViewModel,
    language: String,
    currencyFormatter: CurrencyFormatter
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val averageText = stringResource(R.string.weekly_average, spendingChartAverage)

    val isDefaultRange = startDate.toEpochDay() == java.time.LocalDate.now().minusDays(6).toEpochDay() && endDate.toEpochDay() == java.time.LocalDate.now().toEpochDay()

    val title = if (isDefaultRange) {
        stringResource(R.string.last_7_days)
    } else {
        stringResource(R.string.custom_period)
    }

    val currentLocale = context.resources.configuration.locales[0]
    val dayFormat = SimpleDateFormat("EEE", currentLocale)
    val dateFormat = SimpleDateFormat("d.M.", currentLocale)
    val days = mutableListOf<String>()
    var currentDate = startDate
    while (!currentDate.isAfter(endDate)) {
        val date = java.util.Date.from(currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
        if (isDefaultRange) {
            days.add(dayFormat.format(date))
        } else {
            days.add(dateFormat.format(date))
        }
        currentDate = currentDate.plusDays(1)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                val valuesDesc = spendingChartData.joinToString(", ") { 
                    val formattedValue = currencyFormatter.formatCurrency(it.spending, language)
                    "${it.date}: $formattedValue"
                }
                val formattedAverage = currencyFormatter.formatCurrency(spendingChartAverage, language)
                contentDescription = "Weekly spending chart for last 7 days. $valuesDesc. Average: $formattedAverage"
                heading()
            }
    ) {
        Column(modifier = Modifier.padding(getResponsiveCardPadding())) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Weekly spending chart icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(getResponsiveSpacing()/2))
                Text(
                    text = title,
                    fontSize = getResponsiveTitleLargeSize(),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (!isDefaultRange) {
                    IconButton(onClick = { viewModel.resetDateRange() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.reset))
                    }
                }
                IconButton(onClick = { navController.navigate("date_range_picker") }) {
                    Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.select_date_range))
                }
            }
            Text(
                text = averageText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(getResponsiveSpacing()/2))
            if (spendingChartData.isNotEmpty()) {
                val maxSpending = spendingChartData.maxOfOrNull { it.spending } ?: 0.0
                if (maxSpending > 0) {
                    val chartHeight = getResponsiveChartHeight(maxSpending)
                    val chartKey = listOf(startDate, endDate, spendingChartData.size)
                    key(chartKey) {
                        AndroidView(
                            factory = { ctx ->
                                CombinedChart(ctx).apply {
                                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                                    xAxis.granularity = 1f
                                    xAxis.isGranularityEnabled = true
                                    axisRight.isEnabled = false
                                    description.isEnabled = false
                                    legend.isEnabled = true
                                    legend.textColor = secondaryColor
                                    setTouchEnabled(true)
                                    isDragEnabled = true
                                    setScaleEnabled(true)
                                    setPinchZoom(true)
                                    isHighlightPerTapEnabled = true
                                    isHighlightPerDragEnabled = true
                                    animateY(1000)
                                    setMaxHighlightDistance(Float.MAX_VALUE)
                                }
                            },
                            update = { chart ->
                                chart.xAxis.textColor = onSurfaceColor
                                chart.axisLeft.textColor = onSurfaceColor
                                val barEntries = spendingChartData.mapIndexed { index, day ->
                                    BarEntry(index.toFloat(), day.spending.toFloat())
                                }
                                val spendingLabel = chart.context.getString(R.string.chart_spending)
                                val barDataSet = BarDataSet(barEntries, spendingLabel).apply {
                                    color = primaryColor
                                    setGradientColor(primaryColor, primaryContainerColor)
                                    valueTypeface = Typeface.DEFAULT_BOLD
                                    valueTextSize = 10f  
                                    valueTextColor = primaryColor
                                    valueFormatter = TwoDecimalValueFormatter()
                                    setDrawValues(true)
                                }
                                val barData = BarData(barDataSet)

                                val lineEntries = listOf(
                                    Entry(-0.5f, spendingChartAverage.toFloat()), 
                                    *(0 until spendingChartData.size).map { index ->
                                        Entry(index.toFloat(), spendingChartAverage.toFloat())
                                    }.toTypedArray(),
                                    Entry((spendingChartData.size - 0.5f).toFloat(), spendingChartAverage.toFloat()) 
                                )
                                val averageLabel = chart.context.getString(R.string.chart_average)
                                val lineDataSet = LineDataSet(lineEntries, averageLabel).apply {
                                    color = secondaryColor
                                    lineWidth = 1.5f  
                                    setDrawCircles(false)
                                    setDrawValues(false)
                                    setDrawFilled(false)
                                    mode = LineDataSet.Mode.LINEAR
                                    isHighlightEnabled = false
                                }
                                val lineData = LineData(lineDataSet)

                                val combinedData = CombinedData()
                                combinedData.setData(barData)
                                combinedData.setData(lineData)
                                chart.data = combinedData

                                chart.xAxis.valueFormatter = IndexAxisValueFormatter(days)
                                chart.axisLeft.setAxisMaximum((maxSpending * 1.1).toFloat())
                                chart.axisLeft.setAxisMinimum(0f)

                                
                                chart.xAxis.axisMinimum = -0.5f
                                chart.xAxis.axisMaximum = (spendingChartData.size - 1).toFloat()

                                val marker = CustomMarkerView(chart.context, R.layout.chart_marker_view, days, spendingChartData)
                                chart.marker = marker

                                chart.invalidate()

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(chartHeight)
                                .semantics {
                                    role = Role.Image
                                    contentDescription = "Interactive bar chart of weekly spending with average line. Tap bars for details."
                                }
                        )
                    }
                } else {
                    val noSpendingDataText = stringResource(R.string.no_spending_data_available)
                    Text(
                        text = noSpendingDataText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.semantics {
                            contentDescription = noSpendingDataText
                        }
                    )
                }
            } else {
                val noDataText = stringResource(R.string.no_data_available)
                val noWeeklySpendingText = stringResource(R.string.no_weekly_spending_data_available)
                Text(
                    text = noDataText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        contentDescription = noWeeklySpendingText
                    }
                )
            }
        }
    }
}