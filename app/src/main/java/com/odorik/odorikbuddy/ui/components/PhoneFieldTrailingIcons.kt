package com.odorik.odorikbuddy.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.odorik.odorikbuddy.R

@Composable
fun PhoneFieldTrailingIcons(
    value: String,
    onClear: () -> Unit,
    onPickContact: () -> Unit,
    pickContactDescription: String,
    contactTint: Color = LocalContentColor.current
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (value.isNotEmpty()) {
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_number))
            }
        }
        IconButton(onClick = onPickContact) {
            Icon(Icons.Default.Contacts, contentDescription = pickContactDescription, tint = contactTint)
        }
    }
}
