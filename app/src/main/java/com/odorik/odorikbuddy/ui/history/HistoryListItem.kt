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
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.model.HistoryItem
import com.odorik.odorikbuddy.ui.history.HistoryViewModel.HistoryDisplayItem
import com.odorik.odorikbuddy.ui.theme.ScreenAccents
import com.odorik.odorikbuddy.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Locale

private val CardCorner = 18.dp

/**
 * One row of a day's card in History. Rows of the same day share one card: the first row
 * rounds the top corners, the last one the bottom corners, and a thin divider separates rows.
 *
 * A redirected leg right under its call is drawn as a short indented line ([asLeg]:
 * "Redirected to: …"); a leg shown without its call (filtered out, or outside the loaded
 * period) gets a full row, so it still says who called and when.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryListItem(
    displayItem: HistoryDisplayItem,
    language: String,
    currencyFormatter: CurrencyFormatter,
    lineNames: Map<Int, String>,
    isFirst: Boolean,
    isLast: Boolean,
    showDivider: Boolean,
    asLeg: Boolean
) {
    val item = displayItem.item
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val (_, copyNumber) = counterpart(displayItem, lineNames)

    val shape = RoundedCornerShape(
        topStart = if (isFirst) CardCorner else 0.dp,
        topEnd = if (isFirst) CardCorner else 0.dp,
        bottomStart = if (isLast) CardCorner else 0.dp,
        bottomEnd = if (isLast) CardCorner else 0.dp
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .combinedClickable(
                onClick = { },
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(copyNumber))
                    Toast.makeText(
                        context,
                        context.getString(R.string.number_coppied, copyNumber),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            .padding(top = if (isFirst) 4.dp else 0.dp, bottom = if (isLast) 4.dp else 0.dp)
    ) {
        if (asLeg) {
            RedirectedLegRow(displayItem, lineNames, currencyFormatter, language)
        } else {
            CallRow(displayItem, lineNames, currencyFormatter, language)
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

// ============================================================================
// ROWS
// ============================================================================

@Composable
private fun CallRow(
    displayItem: HistoryDisplayItem,
    lineNames: Map<Int, String>,
    currencyFormatter: CurrencyFormatter,
    language: String
) {
    val item = displayItem.item
    val statusColor = statusColor(item)
    val (name, _) = counterpart(displayItem, lineNames)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EventIcon(item = item, statusColor = statusColor)
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = supportingText(item, lineNames),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!item.isSms) NetworkBadge(item.destinationName)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        PriceAndDuration(item = item, currencyFormatter = currencyFormatter, language = language)
    }
}

@Composable
private fun RedirectedLegRow(
    displayItem: HistoryDisplayItem,
    lineNames: Map<Int, String>,
    currencyFormatter: CurrencyFormatter,
    language: String
) {
    val item = displayItem.item
    val color = statusColor(item)
    val (name, _) = counterpart(displayItem, lineNames)
    val label = stringResource(R.string.redirected_to)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val trailing = when {
        item.status == "missed" && (item.ringingLength ?: 0) > 0 ->
            stringResource(R.string.rang_duration, item.ringingLength ?: 0)
        else -> listOfNotNull(
            item.price.takeIf { it > 0 }?.let { currencyFormatter.formatCurrency(it, language) },
            item.length?.takeIf { it > 0 }?.let(::formatDuration)
        ).joinToString(" · ")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 16.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.SubdirectoryArrowRight,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = buildAnnotatedString {
                append("$label ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurface)) { append(name) }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // Two lines: with a price on the right a long number would otherwise be cut off.
            // It wraps at the space after the label, so the number stays in one piece.
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (trailing.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ============================================================================
// PARTS
// ============================================================================

@Composable
private fun statusColor(item: HistoryItem): Color = when {
    item.status == "missed" -> MaterialTheme.colorScheme.error
    item.isSms -> ScreenAccents.History.main()
    item.direction == "in" -> ScreenAccents.CallIncoming.main()
    item.direction == "out" -> ScreenAccents.CallOutgoing.main()
    item.direction == "redirected" -> ScreenAccents.CallRedirected.main()
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

/**
 * The other party of an event: who called for incoming ones, who was called (or got the SMS)
 * otherwise. Returns the name to show and the number to copy. Own lines (`*760490`) show the
 * line's name.
 */
private fun counterpart(displayItem: HistoryDisplayItem, lineNames: Map<Int, String>): Pair<String, String> {
    val item = displayItem.item
    val (number, contact) = if (item.isIncoming) {
        item.sourceNumber to displayItem.sourceContactName
    } else {
        item.destinationNumber to displayItem.destinationContactName
    }
    val ownLine = number.removePrefix("*").takeIf { number.startsWith("*") }?.toIntOrNull()?.let(lineNames::get)
    // The resolver hands back the number itself when no contact matches.
    val contactName = contact.takeIf { it.isNotEmpty() && it != number }
    return (contactName ?: ownLine ?: number) to number
}

/** "from line kladno · 19:16", "to line kladno · 20:16" or "SMS · from line kladno · 21:05". */
@Composable
private fun supportingText(item: HistoryItem, lineNames: Map<Int, String>): String {
    val lineName = item.line?.let(lineNames::get)?.takeIf { it.isNotBlank() }
    val lineText = lineName?.let {
        stringResource(if (item.isIncoming) R.string.history_line_in else R.string.history_line_out, it)
    }
    val time = parseApiDate(item.date)?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) }
    return listOfNotNull(
        if (item.isSms) stringResource(R.string.network_sms) else null,
        lineText,
        time
    ).joinToString(" · ")
}

@Composable
private fun NetworkBadge(destinationName: String?) {
    val color = getNetworkColor(destinationName) ?: return
    val labelRes = when {
        destinationName == null -> null
        destinationName.contains("mobil", ignoreCase = true) -> R.string.network_mobile
        destinationName.contains("pevná", ignoreCase = true) -> R.string.network_landline
        destinationName.contains("800", ignoreCase = true) -> R.string.network_toll_free
        else -> null
    } ?: return
    Spacer(modifier = Modifier.width(8.dp))
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        // Compact line height so a badge does not make its row taller than the others.
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 13.sp),
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
internal fun EventIcon(
    item: HistoryItem,
    statusColor: Color
) {
    val icon: ImageVector = when {
        item.isSms -> Icons.Default.Sms
        item.status == "missed" -> Icons.AutoMirrored.Filled.PhoneMissed
        item.direction == "in" -> Icons.AutoMirrored.Filled.PhoneCallback
        item.direction == "redirected" -> Icons.AutoMirrored.Filled.PhoneForwarded
        else -> Icons.Filled.Call
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(statusColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (item.isSms) stringResource(R.string.sms_history) else stringResource(R.string.call_history),
            tint = statusColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PriceAndDuration(
    item: HistoryItem,
    currencyFormatter: CurrencyFormatter,
    language: String
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = currencyFormatter.formatCurrency(item.price, language),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum")
        )
        if (item.pricePerMinute != null && item.pricePerMinute > 0) {
            Text(
                text = currencyFormatter.formatCurrency(item.pricePerMinute, language) + "/min",
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        when {
            item.length != null && item.length > 0 -> Text(
                text = formatDuration(item.length),
                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            item.status == "missed" && item.ringingLength != null && item.ringingLength > 0 -> Text(
                text = stringResource(R.string.rang_duration, item.ringingLength),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
