package com.odorik.odorikbuddy.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.odorik.odorikbuddy.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val credit by viewModel.credit.collectAsState()
    val userInfo by viewModel.userInfo.collectAsState()
    val todaysSpending by viewModel.todaysSpending.collectAsState()
    val thisMonthsSpending by viewModel.thisMonthsSpending.collectAsState()
    val weeklySpending by viewModel.weeklySpending.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard)) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = stringResource(R.string.balance), style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = credit?.let { "%.2f Kč".format(it) } ?: stringResource(R.string.loading),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                if (error != null) {
                    Text(stringResource(R.string.error, error!!), color = MaterialTheme.colorScheme.error)
                } else {
                    SpendingSummary(todaysSpending, thisMonthsSpending)
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SpendingChart(weeklySpending)
            }
            item {
                userInfo?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = stringResource(R.string.user_info), style = MaterialTheme.typography.titleLarge)
                            Text(text = "${stringResource(R.string.name_label)} ${it.name}")
                            Text(text = "${stringResource(R.string.email_label)} ${it.email}")
                            Text(text = "${stringResource(R.string.phone_label)} ${it.phoneNumber}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpendingSummary(todaysSpending: Double, thisMonthsSpending: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.spending_summary), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.todays_spending))
                Text(text = stringResource(R.string.currency_format, todaysSpending), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.this_months_spending))
                Text(text = "%.2f Kč".format(thisMonthsSpending), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SpendingChart(weeklySpending: List<Double>) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales.get(0)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.last_7_days), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxSpending = weeklySpending.maxOrNull() ?: 1.0
                val today = Calendar.getInstance()

                for (i in 6 downTo 0) {
                    val spending = weeklySpending.getOrElse(6 - i) { 0.0 }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = "%.2f".format(spending),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp) 
                        )
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(200.dp * (spending / maxSpending).toFloat())
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp)) 
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val dayFormat = SimpleDateFormat("EEE", locale)
                for (i in 6 downTo 0) {
                    val today = Calendar.getInstance()
                    val day = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
                    Text(text = dayFormat.format(day.time), modifier = Modifier.width(30.dp), style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center))
                }
            }
        }
    }
}