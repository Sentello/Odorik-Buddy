package com.odorik.odorikbuddy.ui.calls

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import com.odorik.odorikbuddy.util.PhoneCallLauncher


fun mapApiArgumentToStringId(apiArgument: String): Int {
    return when (apiArgument) {
        "caller" -> R.string.argument_caller
        "recipient" -> R.string.argument_recipient
        "line" -> R.string.argument_line
        else -> R.string.argument_unknown
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    viewModel: CallViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabOrder by viewModel.tabOrder.collectAsState()
    val context = LocalContext.current
    val directCallsEnabled = viewModel.directCallsEnabled


    LaunchedEffect(Unit) {
        viewModel.dialerLaunchRequest.collect { phoneNumber ->
            try {

                kotlinx.coroutines.delay(1200L)

                PhoneCallLauncher.launch(
                    context = context,
                    phoneNumber = phoneNumber,
                    directCallsEnabled = directCallsEnabled
                )

                viewModel.resetOneShotCallResult()
            } catch (e: Exception) {
                viewModel.resetOneShotCallResult()
            }
        }
    }


    val tabItems = remember(tabOrder) {
        tabOrder.map { title ->
            when (title) {
                "callback_title" -> TabItem(
                    titleResId = R.string.callback_title,
                    title = "callback_title",
                    content = { CallbackTab(viewModel) }
                )
                "oneshot_call" -> TabItem(
                    titleResId = R.string.oneshot_call,
                    title = "oneshot_call",
                    content = { OneShotCallTab(viewModel) }
                )
                "tiles_title" -> TabItem(
                    titleResId = R.string.tiles_title,
                    title = "tiles_title",
                    content = { TilesTab(viewModel) }
                )
                else -> TabItem(
                    titleResId = R.string.callback_title,
                    title = "callback_title",
                    content = { CallbackTab(viewModel) }
                )
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

            GradientHeader(title = stringResource(R.string.calls), iconVector = Icons.Default.Call, accent = ScreenAccents.Calls)


            DraggableTabs(
                tabItems = tabItems,
                selectedTabTitle = selectedTab,
                onTabSelected = { viewModel.updateSelectedTab(it) },
                onTabOrderChanged = { newOrder -> viewModel.updateTabOrder(newOrder) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

