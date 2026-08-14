package com.odorik.odorikbuddy.ui.history

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.model.HistoryItem
import com.odorik.odorikbuddy.ui.history.HistoryViewModel.HistoryDisplayItem
import com.odorik.odorikbuddy.ui.theme.LocalAppDimens
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import com.odorik.odorikbuddy.util.CurrencyFormatter





@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryListItem(
    displayItem: HistoryDisplayItem,
    language: String,
    currencyFormatter: CurrencyFormatter
) {
    val item = displayItem.item
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val statusColor = when {
        item.status == "missed" -> MaterialTheme.colorScheme.error
        item.direction == "in" -> ScreenAccents.CallIncoming.main()
        item.direction == "out" -> ScreenAccents.CallOutgoing.main()
        item.direction == "redirected" -> ScreenAccents.CallRedirected.main()
        else -> MaterialTheme.colorScheme.onSurface
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = {  },
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(item.sourceNumber))
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.number_coppied, item.sourceNumber),
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (displayItem.isChild) 1.dp else 2.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (displayItem.isChild) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                statusColor,
                                statusColor.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
                    .align(Alignment.CenterVertically)
            ) {

                Spacer(modifier = Modifier.width(4.dp))
            }

            Column(modifier = Modifier.weight(1f)) {

                if (displayItem.isChild) {
                    ChildConnector()
                }

                Row(
                    modifier = Modifier
                        .padding(
                            start = if (displayItem.isChild) 28.dp else 12.dp,
                            top = if (displayItem.isChild) 4.dp else LocalAppDimens.current.cardPadding,
                            end = LocalAppDimens.current.cardPadding,
                            bottom = LocalAppDimens.current.cardPadding
                        )
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    EventIcon(item = item, statusColor = statusColor)
                    Spacer(modifier = Modifier.width(12.dp))


                    ItemDetails(
                        item = item,
                        displayItem = displayItem,
                        clipboardManager = clipboardManager,
                        context = context,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))


                    PriceAndDuration(
                        item = item,
                        displayItem = displayItem,
                        currencyFormatter = currencyFormatter,
                        language = language
                    )
                }
            }
        }
    }
}





@Composable
internal fun ChildConnector() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Spacer(
            modifier = Modifier
                .width(2.dp)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.redirected_to),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun EventIcon(
    item: HistoryItem,
    statusColor: Color
) {
    val isCall = item.length != null
    val icon: ImageVector
    val backgroundColor: Color

    if (isCall) {
        icon = when {
            item.status == "missed" -> Icons.AutoMirrored.Filled.PhoneMissed
            item.direction == "in" -> Icons.AutoMirrored.Filled.PhoneCallback
            item.direction == "redirected" -> Icons.AutoMirrored.Filled.PhoneForwarded
            item.direction == "out" -> Icons.Filled.Call
            else -> Icons.AutoMirrored.Filled.PhoneCallback
        }
        backgroundColor = statusColor
    } else {
        icon = Icons.Default.Sms
        backgroundColor = ScreenAccents.History.main()
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isCall) stringResource(R.string.call_history) else stringResource(R.string.sms_history),
            tint = backgroundColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ItemDetails(
    item: HistoryItem,
    displayItem: HistoryDisplayItem,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    val sourceDisplayName = displayItem.sourceContactName.ifEmpty { item.sourceNumber }
    val contactName = displayItem.destinationContactName.ifEmpty { item.destinationNumber }
    val destinationDisplayName = if (item.destinationName != null) {
        if (contactName != item.destinationNumber) {
            "$contactName (${item.destinationName})"
        } else {
            "${item.destinationNumber} (${item.destinationName})"
        }
    } else {
        contactName
    }


    val relativeTime = formatRelativeTime(item.date, context)


    val networkColor = getNetworkColor(item.destinationName)

    Column(modifier = modifier) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${stringResource(R.string.from_history)} $sourceDisplayName",
                fontWeight = if (displayItem.isChild) FontWeight.Normal else FontWeight.Bold,
                fontSize = if (displayItem.isChild) 14.sp else 16.sp,
                color = if (displayItem.isChild) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false)
            )

            if (item.recording != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.recording_available),
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }


        Text(
            text = "${stringResource(R.string.to_history)} $destinationDisplayName",
            fontSize = if (displayItem.isChild) 12.sp else 14.sp,
            color = if (displayItem.isChild) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.combinedClickable(
                onClick = {  },
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(item.destinationNumber))
                    Toast.makeText(
                        context,
                        context.getString(R.string.number_coppied, item.destinationNumber),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        )


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {

            if (networkColor != null && item.destinationName != null) {
                val networkLabelResId = when {
                    item.destinationName.contains("mobil", ignoreCase = true) -> R.string.network_mobile
                    item.destinationName.contains("pevná", ignoreCase = true) -> R.string.network_landline
                    item.destinationName.contains("SMS", ignoreCase = true) -> R.string.network_sms
                    item.destinationName.contains("800", ignoreCase = true) -> R.string.network_toll_free
                    else -> null
                }
                if (networkLabelResId != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(networkColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(networkLabelResId),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = networkColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }


            Text(
                text = relativeTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun PriceAndDuration(
    item: HistoryItem,
    displayItem: HistoryDisplayItem,
    currencyFormatter: CurrencyFormatter,
    language: String
) {
    val formattedPrice = currencyFormatter.formatCurrency(item.price, language)

    Column(horizontalAlignment = Alignment.End) {

        Text(
            text = formattedPrice,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum")
        )


        if (item.pricePerMinute != null && item.pricePerMinute > 0) {
            Text(
                text = currencyFormatter.formatCurrency(item.pricePerMinute, language) + "/min",
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }

        when {
            item.length != null && item.length > 0 -> {
                DurationIndicator(
                    length = item.length,
                    isChild = displayItem.isChild
                )
            }
            item.ringingLength != null && item.ringingLength > 0 && item.status == "missed" -> {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(((item.ringingLength / 10f).coerceAtMost(20f)).coerceAtLeast(6f).dp)
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.rang_duration, item.ringingLength),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
internal fun DurationIndicator(length: Int, isChild: Boolean) {
    Row(
        modifier = Modifier.padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(6.dp)
                .width(((length / 60f).coerceAtMost(30f)).coerceAtLeast(6f).dp)
                .background(
                    if (isChild) {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    },
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formatDuration(length),
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.Bold,
            color = if (isChild) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
        )
    }
}
