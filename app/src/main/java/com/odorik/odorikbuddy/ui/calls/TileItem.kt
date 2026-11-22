package com.odorik.odorikbuddy.ui.calls

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.local.entity.TileCallType
import com.odorik.odorikbuddy.data.local.entity.TileEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TileItem(
    tile: TileEntity,
    contactName: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isEditMode: Boolean = false,
    isCalling: Boolean = false,
    size: TileSize = TileSize.NORMAL,
    singleColumn: Boolean = false
) {
    val isSystemDark = com.odorik.odorikbuddy.ui.theme.LocalIsAppDark.current
    val containerColor = TileColorHelper.resolveColor(tile.color, isSystemDark) ?: MaterialTheme.colorScheme.surfaceContainerLow
    
    val isCustomColor = tile.color != null
    val explicitText = TileColorHelper.readableTextColor(tile.textColor, containerColor)
    val titleColor = if (explicitText != null) {
        explicitText
    } else if (isCustomColor) {
        if (isSystemDark) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    val bodyColor = if (explicitText != null) {
        explicitText.copy(alpha = 0.7f)
    } else if (isCustomColor) {
        if (isSystemDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val primaryColor = if (explicitText != null) {
        explicitText.copy(alpha = 0.9f)
    } else if (isCustomColor) {
         if (isSystemDark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f)
    } else {
         MaterialTheme.colorScheme.primary
    }

    val iconBgColor = if (explicitText != null) {
         explicitText.copy(alpha = 0.1f)
    } else if (isCustomColor) {
        if (isSystemDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    }
    
    val iconTint = if (explicitText != null) {
        explicitText
    } else if (isCustomColor) {
         if (isSystemDark) Color.White else Color.Black.copy(alpha = 0.7f)
    } else {
         MaterialTheme.colorScheme.primary
    }

    val monogram = remember(contactName, tile.label) {
        val source = contactName.ifBlank { tile.label }
        val initials = source.trim().split(Regex("\\s+"))
            .mapNotNull { word -> word.firstOrNull { it.isLetter() }?.uppercaseChar() }
        when {
            initials.size >= 2 -> "${initials[0]}${initials[1]}"
            initials.size == 1 -> initials[0].toString()
            else -> null
        }
    }

    val titleStyle = when (size) {
        TileSize.NORMAL -> MaterialTheme.typography.titleMedium
        TileSize.LARGE -> MaterialTheme.typography.titleLarge
        TileSize.EXTRA_LARGE -> MaterialTheme.typography.headlineSmall
    }
    val nameStyle = when (size) {
        TileSize.NORMAL -> MaterialTheme.typography.bodyMedium
        TileSize.LARGE -> MaterialTheme.typography.bodyLarge
        TileSize.EXTRA_LARGE -> MaterialTheme.typography.titleMedium
    }
    val numberStyle = when (size) {
        TileSize.NORMAL -> MaterialTheme.typography.bodySmall
        TileSize.LARGE -> MaterialTheme.typography.bodyMedium
        TileSize.EXTRA_LARGE -> MaterialTheme.typography.bodyLarge
    }
    val callTypeStyle = when (size) {
        TileSize.NORMAL -> MaterialTheme.typography.labelSmall
        TileSize.LARGE -> MaterialTheme.typography.labelMedium
        TileSize.EXTRA_LARGE -> MaterialTheme.typography.labelLarge
    }
    val monogramStyle = if (size == TileSize.NORMAL) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium
    val titleMaxLines = size.titleMaxLines
    val badgeSize = when (size) {
        TileSize.NORMAL -> 36.dp
        TileSize.LARGE -> 44.dp
        TileSize.EXTRA_LARGE -> 48.dp
    }

    val badge = @Composable {
         Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(iconBgColor)
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center
        ) {
            if (isCalling) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = iconTint
                )
            } else if (monogram != null) {
                Text(
                    text = monogram,
                    style = monogramStyle,
                    fontWeight = FontWeight.Bold,
                    color = iconTint
                )
            } else {
                Icon(
                    imageVector = if (tile.callType == TileCallType.CALLBACK) Icons.Default.Call else Icons.AutoMirrored.Filled.PhoneForwarded,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = iconTint
                )
            }
        }
    }

    val editButtons = @Composable {
        Row {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_tile),
                    tint = bodyColor
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = if (isCustomColor && isSystemDark) 0.9f else 0.75f)
                )
            }
        }
    }

    val texts = @Composable {
        val callTypeText = if (tile.callType == TileCallType.CALLBACK) stringResource(R.string.call_type_callback) else stringResource(R.string.call_type_oneshot)

        if (tile.label.isNotEmpty()) {
            Text(
                text = tile.label,
                style = titleStyle,
                fontWeight = FontWeight.Bold,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = callTypeText,
                style = callTypeStyle,
                color = primaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contactName,
                style = nameStyle,
                color = bodyColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = callTypeText,
                style = callTypeStyle,
                color = primaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contactName,
                style = titleStyle,
                fontWeight = FontWeight.Bold,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                color = titleColor
            )
            if (contactName != tile.recipient) {
                Text(
                    text = tile.recipient,
                    style = numberStyle,
                    color = bodyColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                when (size) {
                    TileSize.NORMAL -> Modifier.height(160.dp)
                    TileSize.LARGE -> Modifier.heightIn(min = 88.dp)
                    TileSize.EXTRA_LARGE -> Modifier.heightIn(min = 128.dp)
                }
            )
            .then(if (isEditMode) Modifier.scale(0.97f) else Modifier)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onEdit
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .then(if (size == TileSize.NORMAL) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                .padding(if (size == TileSize.NORMAL) 12.dp else 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (size == TileSize.LARGE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    badge()
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) { texts() }
                    if (isEditMode) editButtons()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    badge()
                    if (isEditMode) editButtons()
                }

                Column(
                    modifier = if (size == TileSize.NORMAL) Modifier.weight(1f) else Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) { texts() }
            }

            if (isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onMoveUp) {
                        Icon(
                            imageVector = if (singleColumn) Icons.Default.KeyboardArrowUp else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(if (singleColumn) R.string.move_up else R.string.move_tile_earlier),
                            tint = bodyColor
                        )
                    }
                    IconButton(onClick = onMoveDown) {
                        Icon(
                            imageVector = if (singleColumn) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(if (singleColumn) R.string.move_down else R.string.move_tile_later),
                            tint = bodyColor
                        )
                    }
                }
            }
        }
    }
}
