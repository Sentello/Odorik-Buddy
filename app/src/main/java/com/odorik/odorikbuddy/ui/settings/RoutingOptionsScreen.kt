package com.odorik.odorikbuddy.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.navigation.SettingsRoutes
import com.odorik.odorikbuddy.util.getResponsiveBodyLargeSize
import com.odorik.odorikbuddy.util.getResponsiveSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutingOptionsScreen(
    internalNavController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.section_routing)) },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.clickable {
                            internalNavController.popBackStack()
                        }
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = getResponsiveSpacing()),
            verticalArrangement = Arrangement.spacedBy(getResponsiveSpacing()/4),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.shared_numbers),
                            fontSize = getResponsiveBodyLargeSize(),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(R.string.shared_number),
                            fontSize = getResponsiveBodyLargeSize() * 0.85f,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable { internalNavController.navigate(SettingsRoutes.ROUTES_SCREEN) }
                )
            }

            
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.own_numbers),
                            fontSize = getResponsiveBodyLargeSize(),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(R.string.own_numbers_description),
                            fontSize = getResponsiveBodyLargeSize() * 0.85f,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable { internalNavController.navigate(SettingsRoutes.OWN_NUMBERS_SCREEN) }
                )
            }
        }
    }
}