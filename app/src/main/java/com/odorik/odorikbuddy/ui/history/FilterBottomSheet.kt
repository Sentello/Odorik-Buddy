package com.odorik.odorikbuddy.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.ui.components.GradientHeader
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents





@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterBottomSheet(
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
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(colors = listOf(ScreenAccents.History.main(), ScreenAccents.History.secondary())))
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        com.odorik.odorikbuddy.util.ConfigureBottomSheetWindow()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = LocalAppDimens.current.spacing * 2)
        ) {

            GradientHeader(
                title = stringResource(R.string.filter_history),
                iconVector = Icons.Default.FilterList,
                accent = ScreenAccents.History,
                iconSize = 22.dp,
                iconContainerSize = 40.dp,
                iconCornerRadius = 10.dp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = LocalAppDimens.current.cardPadding,
                        vertical = LocalAppDimens.current.cardPadding
                    )
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(LocalAppDimens.current.spacing)
            ) {

                ExposedDropdownMenuBox(
                    expanded = lineExpanded,
                    onExpandedChange = { lineExpanded = !lineExpanded }
                ) {
                    OutlinedTextField(
                        value = tempSelectedLine?.callerId ?: stringResource(R.string.all_lines),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.line_filter)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lineExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScreenAccents.History.main(),
                            focusedLabelColor = ScreenAccents.History.main()
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
                                text = { Text(line.callerId) },
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
                        focusedBorderColor = ScreenAccents.History.main(),
                        focusedLabelColor = ScreenAccents.History.main()
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
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScreenAccents.History.main(),
                            focusedLabelColor = ScreenAccents.History.main()
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
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScreenAccents.History.main(),
                            focusedLabelColor = ScreenAccents.History.main()
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
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScreenAccents.History.main()),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.apply_filters),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
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
                            modifier = Modifier.height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.clear_filters),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}





