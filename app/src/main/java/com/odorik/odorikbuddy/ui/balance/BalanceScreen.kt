package com.odorik.odorikbuddy.ui.balance

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BalanceScreen(viewModel: BalanceViewModel = viewModel()) {
    val balance by viewModel.balance.collectAsState()
    val error by viewModel.error.collectAsState()

    Column {
        Button(onClick = { viewModel.fetchBalance() }) { Text("Refresh Balance") }
        if (error != null) {
            Text("Error: $error", color = Color.Red)
        } else if (balance != null) {
            Text("Current Balance: $balance Kč")
        } else {
            Text("Loading...")
        }
    }
}
