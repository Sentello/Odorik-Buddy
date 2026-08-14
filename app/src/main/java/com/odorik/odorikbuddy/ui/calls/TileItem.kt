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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CardDefaults
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
    isEditMode: Boolean = false
) {
    val isSystemDark = com.odorik.odorikbuddy.ui.theme.LocalIsAppDark.current
    val containerColor = TileColorHelper.resolveColor(tile.color, isSystemDark) ?: MaterialTheme.colorScheme.surfaceContainerLow

    val isCustomColor = tile.color != null


    val titleColor = if (tile.textColor != null) {
        Color(tile.textColor)
    } else if (isCustomColor) {
        if (isSystemDark) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val bodyColor = if (tile.textColor != null) {
        Color(tile.textColor).copy(alpha = 0.7f)
    } else if (isCustomColor) {
        if (isSystemDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val primaryColor = if (tile.textColor != null) {
        Color(tile.textColor).copy(alpha = 0.9f)
    } else if (isCustomColor) {
         if (isSystemDark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f)
    } else {
         MaterialTheme.colorScheme.primary
    }

    val iconBgColor = if (tile.textColor != null) {
         Color(tile.textColor).copy(alpha = 0.1f)
    } else if (isCustomColor) {
        if (isSystemDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    }

    val iconTint = if (tile.textColor != null) {
        Color(tile.textColor)
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

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
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
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                 Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBgColor)


                        .clearAndSetSemantics {},
                    contentAlignment = Alignment.Center
                ) {
                    if (monogram != null) {
                        Text(
                            text = monogram,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = iconTint
                        )
                    } else {
                        Icon(
                            imageVector = if (tile.callType == "CALLBACK") Icons.Default.Call else Icons.AutoMirrored.Filled.PhoneForwarded,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = iconTint
                        )
                    }
                }

                if (isEditMode) {
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
            }


            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val callTypeText = if (tile.callType == "CALLBACK") stringResource(R.string.call_type_callback) else stringResource(R.string.call_type_oneshot)

                if (tile.label.isNotEmpty()) {
                    Text(
                        text = tile.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = callTypeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = bodyColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = callTypeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = titleColor
                    )
                    if (contactName != tile.recipient) {
                        Text(
                            text = tile.recipient,
                            style = MaterialTheme.typography.bodySmall,
                            color = bodyColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onMoveUp) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.move_up),
                            tint = bodyColor
                        )
                    }
                    IconButton(onClick = onMoveDown) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.move_down),
                            tint = bodyColor
                        )
                    }
                }
            }
        }
    }
}

