package com.odorik.odorikbuddy.ui.calls

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.odorik.odorikbuddy.ui.theme.CallAccent
import com.odorik.odorikbuddy.ui.theme.CallAccentLight
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
    val initialDraggingIndex = remember { mutableStateOf(-1) }
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
                .height(56.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val tabWidth = size.width / currentTabOrder.size
                                val tabIndex = (offset.x / tabWidth).toInt()
                                if (tabIndex in currentTabOrder.indices) {
                                    isDragging.value = true
                                    draggingIndex.value = tabIndex
                                    initialDraggingIndex.value = tabIndex
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset.value += dragAmount.x
                                
                                val currentDraggingIdx = draggingIndex.value
                                val initialIdx = initialDraggingIndex.value
                                if (currentDraggingIdx >= 0 && initialIdx >= 0) {
                                    val tabWidth = size.width / currentTabOrder.size
                                    val newIndex = ((dragOffset.value + initialIdx * tabWidth) / tabWidth).roundToInt()
                                        .coerceIn(0, currentTabOrder.size - 1)
                                    
                                    if (newIndex != currentDraggingIdx) {
                                        val newOrder = currentTabOrder.toMutableList()
                                        val draggedTitle = newOrder.removeAt(currentDraggingIdx)
                                        newOrder.add(newIndex, draggedTitle)
                                        currentTabOrder = newOrder
                                        draggingIndex.value = newIndex
                                    }
                                }
                            },
                            onDragEnd = {
                                isDragging.value = false
                                draggingIndex.value = -1
                                initialDraggingIndex.value = -1
                                dragOffset.value = 0f
                                val viewModelOrder = tabItems.map { it.title }
                                if (currentTabOrder != viewModelOrder) {
                                    onTabOrderChanged(currentTabOrder)
                                }
                            },
                            onDragCancel = {
                                isDragging.value = false
                                draggingIndex.value = -1
                                initialDraggingIndex.value = -1
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
        
        
        val selectedIndex = currentTabOrder.indexOf(selectedTabTitle)
        var swipeOffset by remember { mutableStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(currentTabOrder, selectedIndex) {
                    detectHorizontalDragGestures(
                        onDragStart = { swipeOffset = 0f },
                        onDragEnd = {
                            val threshold = 100f 
                            if (swipeOffset > threshold) {
                                
                                if (selectedIndex > 0) {
                                    onTabSelected(currentTabOrder[selectedIndex - 1])
                                }
                            } else if (swipeOffset < -threshold) {
                                
                                if (selectedIndex >= 0 && selectedIndex < currentTabOrder.size - 1) {
                                    onTabSelected(currentTabOrder[selectedIndex + 1])
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            swipeOffset += dragAmount
                        }
                    )
                }
        ) {
            AnimatedContent(
                targetState = selectedTabTitle,
                transitionSpec = {
                    val oldIndex = currentTabOrder.indexOf(initialState)
                    val newIndex = currentTabOrder.indexOf(targetState)
                    if (newIndex > oldIndex) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "TabContentAnimation"
            ) { targetTitle ->
                val item = tabItems.find { it.title == targetTitle }
                item?.content?.invoke()
            }
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
    
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tabScale"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "tabTextColor"
    )
    
    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(CallAccent, CallAccentLight)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.Transparent)
                    )
                }
            )
            .clickable { onTabSelected(tabTitle) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            
            if (isDragging) {
                Spacer(modifier = Modifier.height(2.dp))
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    modifier = Modifier.size(14.dp),
                    tint = textColor
                )
            }
        }
    }
}