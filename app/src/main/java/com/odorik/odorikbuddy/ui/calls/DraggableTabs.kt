package com.odorik.odorikbuddy.ui.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


data class TabItem(
    val titleResId: Int,
    val title: String,
    val content: @Composable () -> Unit
)


@Composable
fun DraggableTabs(
    tabItems: List<TabItem>,
    selectedTabTitle: String,
    onTabSelected: (String) -> Unit,
    onTabOrderChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    
    var currentTabOrder by remember { mutableStateOf<List<String>>(emptyList()) }
    val isDragging = remember { mutableStateOf(false) }
    val draggingIndex = remember { mutableStateOf(-1) }
    val dragOffset = remember { mutableStateOf(0f) }
    
    
    LaunchedEffect(Unit) {
        currentTabOrder = tabItems.map { it.title }
    }

    
    LaunchedEffect(tabItems) {
        val viewModelOrder = tabItems.map { it.title }
        if (currentTabOrder.isEmpty() || currentTabOrder.size != viewModelOrder.size) {
            currentTabOrder = viewModelOrder
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                
                                val tabWidth = size.width / currentTabOrder.size
                                val tabIndex = (offset.x / tabWidth).toInt()
                                if (tabIndex in currentTabOrder.indices) {
                                    isDragging.value = true
                                    draggingIndex.value = tabIndex
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset.value += dragAmount.x
                                
                                val currentDraggingIndex = draggingIndex.value
                                if (currentDraggingIndex >= 0) {
                                    val tabWidth = size.width / currentTabOrder.size
                                    val newIndex = ((dragOffset.value + currentDraggingIndex * tabWidth) / tabWidth).roundToInt()
                                        .coerceIn(0, currentTabOrder.size - 1)
                                    
                                    if (newIndex != currentDraggingIndex) {
                                        
                                        val newOrder = currentTabOrder.toMutableList()
                                        val draggedTitle = newOrder.removeAt(currentDraggingIndex)
                                        newOrder.add(newIndex, draggedTitle)
                                        currentTabOrder = newOrder
                                        draggingIndex.value = newIndex
                                    }
                                }
                            },
                            onDragEnd = {
                                isDragging.value = false
                                draggingIndex.value = -1
                                dragOffset.value = 0f
                                
                                val viewModelOrder = tabItems.map { it.title }
                                if (currentTabOrder != viewModelOrder) {
                                    onTabOrderChanged(currentTabOrder)
                                }
                            },
                            onDragCancel = {
                                isDragging.value = false
                                draggingIndex.value = -1
                                dragOffset.value = 0f
                                
                                currentTabOrder = tabItems.map { it.title }
                            }
                        )
                    }
            ) {
                currentTabOrder.forEachIndexed { index, title ->
                    val item = tabItems.find { it.title == title }
                    item?.let { tabItem ->
                        DraggableTab(
                            titleResId = tabItem.titleResId,
                            isSelected = title == selectedTabTitle,
                            isDragging = isDragging.value && draggingIndex.value == index,
                            tabTitle = title,
                            onTabSelected = onTabSelected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        
        val selectedItem = tabItems.firstOrNull { it.title == selectedTabTitle }
        if (selectedItem != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                selectedItem.content()
            }
        } else {
            
            tabItems.firstOrNull()?.content?.invoke()
        }
    }
}


@Composable
fun DraggableTab(
    titleResId: Int,
    isSelected: Boolean,
    isDragging: Boolean,
    tabTitle: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val title = stringResource(titleResId)
    
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                }
            )
            .padding(horizontal = 16.dp)
            .clickable { onTabSelected(tabTitle) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (isSelected) {
                    androidx.compose.ui.text.font.FontWeight.Bold
                } else {
                    androidx.compose.ui.text.font.FontWeight.Normal
                }
            )
            
            if (isDragging) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}