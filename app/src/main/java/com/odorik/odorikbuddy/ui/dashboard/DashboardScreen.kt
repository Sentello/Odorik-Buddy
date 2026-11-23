package com.odorik.odorikbuddy.ui.dashboard

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.renderer.CombinedChartRenderer
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.LanguagePreferences
import com.odorik.odorikbuddy.model.AppUpdateInfo
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.components.constrainedContentWidth
import com.odorik.odorikbuddy.ui.navigation.NavigationRoutes
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import com.odorik.odorikbuddy.ui.theme.TabularNumbers
import com.odorik.odorikbuddy.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters


class TwoDecimalValueFormatter(locale: java.util.Locale) : ValueFormatter() {
    private val symbols = java.text.DecimalFormatSymbols(locale)
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
    viewModel: DashboardViewModel = hiltViewModel(),
    liveCallsViewModel: com.odorik.odorikbuddy.ui.livecalls.LiveCallsViewModel = hiltViewModel()
) {
    com.odorik.odorikbuddy.ui.livecalls.LiveCallsLifecycle(liveCallsViewModel)
    val activeCalls by liveCallsViewModel.calls.collectAsState()
    val ownNumbersLoaded by liveCallsViewModel.ownNumbersLoaded.collectAsState()
    var pendingHangUp by remember { mutableStateOf<com.odorik.odorikbuddy.data.model.ActiveCall?>(null) }
    val creditState by viewModel.credit.collectAsState()
    val todaysSpending by viewModel.todaysSpending.collectAsState()
    val selectedPeriodSpending by viewModel.selectedPeriodSpending.collectAsState()
    val spendingChartData by viewModel.spendingChartData.collectAsState()
    val spendingChartAverage by viewModel.spendingChartAverage.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val error by viewModel.error.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()
    val updateBanner by viewModel.updateBanner.collectAsState()
    
    val hasSpendingData by viewModel.hasSpendingData.collectAsState()
    // Replace the whole page only when there is nothing to show: balance failed and no
    // spending data (fresh or cached). Otherwise the balance error stays in its own card.
    val isCriticalError = !isInitialLoading && creditState is DashboardViewModel.UiState.Error && !hasSpendingData

    // After a call ends Odorik bills it within seconds: refresh balance and spending then,
    // instead of showing the pre-call numbers until the user pulls to refresh.
    var hadActiveCall by remember { mutableStateOf(false) }
    LaunchedEffect(activeCalls.isEmpty()) {
        if (activeCalls.isNotEmpty()) {
            hadActiveCall = true
        } else if (hadActiveCall) {
            hadActiveCall = false
            kotlinx.coroutines.delay(4_000)
            viewModel.refresh()
        }
    }

    // Context and language setup for currency formatting
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
    
    // Auto-retry when in critical error state


    // Observe navigation results reliably using LaunchedEffect keys
    val navStartDate = navController.currentBackStackEntry?.savedStateHandle?.get<Long>("startDate")
    val navEndDate = navController.currentBackStackEntry?.savedStateHandle?.get<Long>("endDate")

    LaunchedEffect(navStartDate, navEndDate) {
        if (navStartDate != null && navEndDate != null) {
            val newStartDate = java.time.LocalDate.ofEpochDay(navStartDate)
            val newEndDate = java.time.LocalDate.ofEpochDay(navEndDate)

            // Only update if the range actually changed
            if (newStartDate != startDate || newEndDate != endDate) {
                viewModel.updateDateRange(newStartDate, newEndDate)
            }

            // Clear immediately after processing to prevent re-triggers
            navController.currentBackStackEntry?.savedStateHandle?.remove<Long>("startDate")
            navController.currentBackStackEntry?.savedStateHandle?.remove<Long>("endDate")
        }
    }

    LaunchedEffect(Unit) {
        liveCallsViewModel.hangUpEvent.collect { ok ->
            snackbarHostState.showSnackbar(
                context.getString(if (ok) R.string.live_call_ended else R.string.live_call_hang_up_failed)
            )
        }
    }

    pendingHangUp?.let { call ->
        com.odorik.odorikbuddy.ui.livecalls.HangUpConfirmDialog(
            name = liveCallsViewModel.displayName(call),
            onConfirm = {
                liveCallsViewModel.hangUp(call.id)
                pendingHangUp = null
            },
            onDismiss = { pendingHangUp = null }
        )
    }

    LaunchedEffect(error) {
        // Only show snackbar if NOT in critical error state
        if (!isCriticalError && error != null) {
            snackbarHostState.showSnackbar(
                message = error!!,
                actionLabel = context.getString(R.string.dismiss)
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            GradientHeader(
                title = stringResource(R.string.dashboard),
                iconVector = Icons.Default.Dashboard,
                accent = ScreenAccents.Dashboard
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
                .pullRefresh(pullRefreshState)
        ) {
            if (isInitialLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(LocalAppDimens.current.screenPadding)
                ) {
                    com.odorik.odorikbuddy.ui.components.DashboardSkeleton()
                }
            } else if (isCriticalError) {
                DashboardErrorState(
                    error = (creditState as DashboardViewModel.UiState.Error).message,
                    onRetry = { viewModel.loadData(true) }
                )
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(LocalAppDimens.current.screenPadding)
                ) {
                    // Calculate chart height dynamically.
                    // Total non-chart overhead: balance card (~110dp) + spending summary (~140dp)
                    // + chart card internal elements (~128dp) + spacers & padding (~52dp) ≈ 430dp
                    val chartOverhead = 430.dp
                    val dynamicChartHeight = (maxHeight - chartOverhead).coerceAtLeast(180.dp)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .constrainedContentWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.animation.AnimatedVisibility(visible = activeCalls.isNotEmpty()) {
                            Column {
                                com.odorik.odorikbuddy.ui.livecalls.LiveCallsCard(
                                    calls = activeCalls,
                                    displayName = { call -> ownNumbersLoaded.let { liveCallsViewModel.displayName(call) } },
                                    subtitle = { call -> ownNumbersLoaded.let { liveCallsViewModel.subtitle(call) } },
                                    onShowAll = { navController.navigate(NavigationRoutes.ACTIVE_CALLS) },
                                    onHangUp = { pendingHangUp = it }
                                )
                                Spacer(modifier = Modifier.height(LocalAppDimens.current.spacing))
                            }
                        }

                        updateBanner?.let { info ->
                            UpdateBanner(info = info, onDismiss = { viewModel.dismissUpdateBanner() })
                            Spacer(modifier = Modifier.height(LocalAppDimens.current.spacing))
                        }

                        val currentCreditState = creditState
                        if (currentCreditState is DashboardViewModel.UiState.Success) {
                            val balance = currentCreditState.data
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                            ) {
                                    Column(modifier = Modifier.padding(LocalAppDimens.current.cardPadding)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(ScreenAccents.Dashboard.wash().copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.AccountBalanceWallet,
                                                    contentDescription = stringResource(R.string.a11y_balance_icon),
                                                    tint = ScreenAccents.Dashboard.main(),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(LocalAppDimens.current.spacing))
                                            Text(
                                                text = stringResource(R.string.balance),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(LocalAppDimens.current.spacing / 2))
                                        val formattedBalance = currencyFormatter.formatCurrency(balance, currentLanguage)
                                        val balanceDescription = stringResource(R.string.a11y_current_balance, formattedBalance)
                                        Text(
                                            text = formattedBalance,
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontFeatureSettings = "tnum",
                                                fontWeight = FontWeight.SemiBold,
                                                color = ScreenAccents.Dashboard.main()
                                            ),
                                            modifier = Modifier.semantics {
                                                contentDescription = balanceDescription
                                            }
                                        )
                                    }
                                }
                        } else if (currentCreditState is DashboardViewModel.UiState.Error) {
                            BalanceErrorCard(message = currentCreditState.message, onRetry = { viewModel.refresh() })
                        }

                        Spacer(modifier = Modifier.height(LocalAppDimens.current.spacing))
                        SpendingSummary(todaysSpending, selectedPeriodSpending, currentLanguage, currencyFormatter)

                        Spacer(modifier = Modifier.height(LocalAppDimens.current.spacing))
                        SpendingChart(spendingChartData, spendingChartAverage, startDate, endDate, navController, viewModel, currentLanguage, currencyFormatter, dynamicChartHeight)
                    }
                }
            }
            
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = ScreenAccents.Dashboard.main()
            )
        }
    }
}

@Composable
fun SpendingSummary(
    todaysSpending: Double,
    selectedPeriodSpending: Double,
    language: String,
    currencyFormatter: CurrencyFormatter
) {
    val todaysLabel = currencyFormatter.formatCurrency(todaysSpending, language)
    val periodLabel = currencyFormatter.formatCurrency(selectedPeriodSpending, language)
    val summaryDesc = "Spending summary: Today's $todaysLabel, selected period $periodLabel"
    val todaysDesc = "Today's spending: $todaysLabel"
    val periodDesc = "Spending for selected period: $periodLabel"

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = summaryDesc
                heading()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(LocalAppDimens.current.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ScreenAccents.Dashboard.wash().copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = stringResource(R.string.a11y_spending_summary_icon),
                        tint = ScreenAccents.Dashboard.main(),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(LocalAppDimens.current.spacing))
                Text(
                    text = stringResource(R.string.spending_summary),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(LocalAppDimens.current.spacing))
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
                    style = LocalTextStyle.current.merge(TabularNumbers),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = periodDesc
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.spending_for_selected_period))
                Text(
                    text = periodLabel,
                    style = LocalTextStyle.current.merge(TabularNumbers),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** Periods longer than this many days are drawn as one bar per week. */
private const val WEEKLY_BARS_FROM_DAYS = 31

@Composable
fun SpendingChart(
    spendingChartData: List<DashboardViewModel.ChartDay>,
    spendingChartAverage: Double,
    startDate: java.time.LocalDate,
    endDate: java.time.LocalDate,
    navController: NavController,
    viewModel: DashboardViewModel,
    language: String,
    currencyFormatter: CurrencyFormatter,
    chartHeight: Dp = 220.dp
) {
    val context = LocalContext.current
    val primaryColor = ScreenAccents.Dashboard.main().toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val primaryContainerColor = ScreenAccents.Dashboard.secondary().toArgb()
    // Days without calls count as 0, so this is an average per day — formatted like the balance.
    val averageText = stringResource(
        R.string.weekly_average,
        currencyFormatter.formatCurrency(spendingChartAverage, language)
    )

    val now = LocalDate.now()
    val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sunday = monday.plusDays(6)
    val isDefaultRange = startDate == monday && endDate == sunday

    val title = if (isDefaultRange) {
        stringResource(R.string.this_week)
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

    // Above a month, one bar per day is a thin needle; sum each Monday–Sunday into one bar.
    // The tap marker then shows the week's range ("3.9.–9.9.") and the dashed line the weekly average.
    val weekly = spendingChartData.size > WEEKLY_BARS_FROM_DAYS
    val bars: List<DashboardViewModel.ChartDay>
    val barLabels: List<String>
    if (weekly) {
        val weeks = linkedMapOf<LocalDate, MutableList<Int>>()
        spendingChartData.indices.forEach { i ->
            val day = startDate.plusDays(i.toLong())
            weeks.getOrPut(day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))) { mutableListOf() }.add(i)
        }
        fun short(day: LocalDate) = dateFormat.format(
            java.util.Date.from(day.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
        )
        bars = weeks.values.map { idx ->
            val first = startDate.plusDays(idx.first().toLong())
            val last = startDate.plusDays(idx.last().toLong())
            DashboardViewModel.ChartDay("${short(first)}–${short(last)}", idx.sumOf { spendingChartData[it].spending })
        }
        barLabels = weeks.values.map { idx -> short(startDate.plusDays(idx.first().toLong())) }
    } else {
        bars = spendingChartData
        barLabels = days
    }
    val markerLabels = if (weekly) bars.map { it.date } else days
    val barAverage = if (weekly) spendingChartAverage * 7 else spendingChartAverage

    val valuesDesc = bars.joinToString(", ") {
        val formattedValue = currencyFormatter.formatCurrency(it.spending, language)
        "${it.date}: $formattedValue"
    }
    val formattedAverage = currencyFormatter.formatCurrency(spendingChartAverage, language)
    val chartDescription = stringResource(R.string.a11y_weekly_chart, valuesDesc, formattedAverage)
    val interactiveChartDescription = stringResource(R.string.a11y_interactive_chart)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = chartDescription
                heading()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(LocalAppDimens.current.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ScreenAccents.Dashboard.wash().copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.a11y_weekly_chart_icon),
                        tint = ScreenAccents.Dashboard.main(),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(LocalAppDimens.current.spacing))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (!isDefaultRange) {
                    IconButton(onClick = { viewModel.resetDateRange() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.reset), tint = ScreenAccents.Dashboard.main())
                    }
                }
                IconButton(onClick = {
                    // The picker opens on the range the dashboard shows (it reads these keys
                    // from the previous back-stack entry; the result comes back as before).
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set(PICKER_INITIAL_START, startDate.toEpochDay())
                        set(PICKER_INITIAL_END, endDate.toEpochDay())
                    }
                    navController.navigate(com.odorik.odorikbuddy.ui.navigation.NavigationRoutes.DATE_RANGE_PICKER)
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.select_date_range), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = averageText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(LocalAppDimens.current.spacing))
            if (spendingChartData.isNotEmpty()) {
                val maxSpending = maxOf(bars.maxOfOrNull { it.spending } ?: 0.0, barAverage)
                if (maxSpending > 0) {

                    val chartKey = listOf(startDate, endDate, bars.size)
                    key(chartKey) {
                        AndroidView(
                            factory = { ctx ->
                                CombinedChart(ctx).apply {
                                    // X-Axis styling
                                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                                    xAxis.granularity = 1f
                                    xAxis.isGranularityEnabled = true
                                    xAxis.setDrawAxisLine(false)
                                    xAxis.setDrawGridLines(false)
                                    xAxis.yOffset = 8f

                                    // Y-Axis styling
                                    axisLeft.setDrawAxisLine(false)
                                    axisLeft.setDrawGridLines(true)
                                    axisLeft.gridColor = android.graphics.Color.parseColor("#22888888")
                                    axisLeft.gridLineWidth = 0.5f
                                    axisLeft.enableGridDashedLine(8f, 4f, 0f)
                                    axisLeft.setLabelCount(5, false)
                                    axisRight.isEnabled = false

                                    // General chart styling
                                    description.isEnabled = false
                                    legend.isEnabled = true
                                    legend.textColor = secondaryColor
                                    legend.yOffset = 8f
                                    setExtraOffsets(4f, 8f, 4f, 20f)

                                    // Interaction
                                    setTouchEnabled(true)
                                    isDragEnabled = true
                                    setScaleEnabled(true)
                                    // Zooming the amounts would break the round axis steps;
                                    // only the days (X) zoom.
                                    isScaleYEnabled = false
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
                                chart.legend.textColor = onSurfaceColor

                                val barEntries = bars.mapIndexed { index, day ->
                                    BarEntry(index.toFloat(), day.spending.toFloat())
                                }
                                val spendingLabel = chart.context.getString(R.string.chart_spending)
                                val barDataSet = BarDataSet(barEntries, spendingLabel).apply {
                                    color = primaryColor
                                    setGradientColor(primaryColor, primaryContainerColor)
                                    valueTypeface = Typeface.DEFAULT_BOLD
                                    valueTextSize = 11f
                                    valueTextColor = primaryColor
                                    valueFormatter = TwoDecimalValueFormatter(currentLocale)
                                    // Above ~10 bars the amounts run into each other; longer
                                    // periods show the value in the tap marker instead.
                                    setDrawValues(bars.size <= 10)
                                    highLightColor = primaryColor
                                    highLightAlpha = 40
                                }
                                val barData = BarData(barDataSet)
                                barData.barWidth = 0.6f

                                val lineEntries = listOf(
                                    Entry(-0.5f, barAverage.toFloat()),
                                    *(0 until bars.size).map { index ->
                                        Entry(index.toFloat(), barAverage.toFloat())
                                    }.toTypedArray(),
                                    Entry((bars.size - 0.5f).toFloat(), barAverage.toFloat())
                                )
                                val averageLabel = chart.context.getString(
                                    if (weekly) R.string.chart_average_week else R.string.chart_average
                                )
                                val lineDataSet = LineDataSet(lineEntries, averageLabel).apply {
                                    color = android.graphics.Color.parseColor("#99F59E0B")
                                    lineWidth = 2f
                                    enableDashedLine(10f, 5f, 0f)
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

                                // Swap bar sub-renderer inside CombinedChartRenderer for rounded bars
                                val combinedRenderer = chart.renderer as? CombinedChartRenderer
                                combinedRenderer?.let { cr ->
                                    val subRenderers = cr.subRenderers.toMutableList()
                                    val barIndex = subRenderers.indexOfFirst { it is BarChartRenderer }
                                    if (barIndex >= 0) {
                                        val roundedRenderer = RoundedBarChartRenderer(
                                            chart, chart.animator, chart.viewPortHandler, 16f
                                        )
                                        roundedRenderer.initBuffers()
                                        subRenderers[barIndex] = roundedRenderer
                                        cr.subRenderers = subRenderers
                                    }
                                }

                                chart.xAxis.valueFormatter = IndexAxisValueFormatter(barLabels)
                                // Round steps in koruny (0 / 1 / 2 / 3 / 4 Kč), not the library's 0,8 / 1,6.
                                // 15 % headroom keeps the amounts drawn above the bars inside the chart.
                                val scale = spendingAxisScale(maxSpending * 1.15)
                                chart.axisLeft.axisMinimum = 0f
                                chart.axisLeft.axisMaximum = scale.max.toFloat()
                                chart.axisLeft.granularity = scale.step.toFloat()
                                chart.axisLeft.setLabelCount(scale.labelCount, true)
                                chart.axisLeft.valueFormatter = AxisAmountFormatter(scale.wholeSteps, language)

                                chart.xAxis.axisMinimum = -0.5f
                                chart.xAxis.axisMaximum = (bars.size - 0.5f)

                                val marker = CustomMarkerView(chart.context, R.layout.chart_marker_view, markerLabels, bars)
                                chart.marker = marker

                                chart.invalidate()

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(chartHeight)
                                .semantics {
                                    role = Role.Image
                                    contentDescription = interactiveChartDescription
                                }
                        )
                    }
                } else {
                    EmptyChartMessage()
                }
            } else {
                EmptyChartMessage()
            }
        }
    }
}



@Composable
private fun EmptyChartMessage() {
    val emptyText = stringResource(R.string.no_spending_in_period)
    Text(
        text = emptyText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .semantics { contentDescription = emptyText }
    )
}

@Composable
fun DashboardErrorState(
    error: String,
    onRetry: () -> Unit
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
                    text = stringResource(R.string.error_loading_dashboard),
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
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = ScreenAccents.Dashboard.main())
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}
/** Keys the dashboard sets before opening the date-range picker, so it starts on the shown range. */
internal const val PICKER_INITIAL_START = "pickerInitialStart"
internal const val PICKER_INITIAL_END = "pickerInitialEnd"

/** Balance failed but spending is shown: the error stays here instead of replacing the page. */
@Composable
private fun BalanceErrorCard(message: String, onRetry: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(LocalAppDimens.current.cardPadding)) {
            Text(
                text = stringResource(R.string.balance),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(LocalAppDimens.current.spacing / 2))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            androidx.compose.material3.TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}

/**
 * "Version X is available" card from the cached update feed. Needs no notification
 * permission, so it is the fallback when notifications are off or were dismissed.
 */
@Composable
private fun UpdateBanner(info: AppUpdateInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val language = remember { LanguagePreferences.getPreferredLanguage(context) }
    val message = info.localizedMessage(language)
    val downloadUrl = info.safeDownloadUrl()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                start = LocalAppDimens.current.cardPadding,
                end = LocalAppDimens.current.cardPadding / 2,
                top = LocalAppDimens.current.cardPadding / 2,
                bottom = if (downloadUrl != null) 0.dp else LocalAppDimens.current.cardPadding
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Update,
                    contentDescription = null,
                    tint = ScreenAccents.Dashboard.main(),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(LocalAppDimens.current.spacing))
                Text(
                    text = stringResource(R.string.update_banner_title, info.version),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.update_banner_dismiss))
                }
            }
            if (message.isNotBlank()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = LocalAppDimens.current.cardPadding / 2)
                )
            }
            if (downloadUrl != null) {
                val cannotOpenUrl = stringResource(R.string.cannot_open_url)
                androidx.compose.material3.TextButton(
                    onClick = {
                        try {
                            uriHandler.openUri(downloadUrl)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, cannotOpenUrl, android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.download_update))
                }
            }
        }
    }
}
