package com.odorik.odorikbuddy.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.components.TransparentListItem
import com.odorik.odorikbuddy.ui.navigation.SettingsRoutes
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutingOptionsScreen(
    internalNavController: NavHostController
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            GradientHeader(
                title = stringResource(R.string.section_routing),
                iconVector = Icons.Default.Settings,
                accent = ScreenAccents.Settings,
                onBackClick = { internalNavController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = LocalAppDimens.current.spacing),
            verticalArrangement = Arrangement.spacedBy(LocalAppDimens.current.spacing/4),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LocalAppDimens.current.cardPadding, vertical = LocalAppDimens.current.spacing/2),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column {

                        TransparentListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.shared_numbers),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.shared_number),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = null,
                                    tint = ScreenAccents.Settings.main()
                                )
                            },
                            modifier = Modifier.clickable { internalNavController.navigate(SettingsRoutes.ROUTES_SCREEN) }
                        )

                        HorizontalDivider(
                             modifier = Modifier.padding(horizontal = 16.dp),
                             color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )


                        TransparentListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.own_numbers),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.own_numbers_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                             leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = ScreenAccents.Settings.main()
                                )
                            },
                            modifier = Modifier.clickable { internalNavController.navigate(SettingsRoutes.OWN_NUMBERS_SCREEN) }
                        )
                    }
                }
            }
        }
    }
}