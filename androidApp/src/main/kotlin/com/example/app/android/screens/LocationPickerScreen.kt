package com.example.app.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.app.android.R
import com.example.app.android.theme.AppTheme
import com.example.app.android.components.SafeScreen

@Composable
fun LocationPickerScreen(
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    val colors = AppTheme.colors
    SafeScreen(backgroundColor = colors.destructive) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.select_location),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onDestructive,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = colors.onDestructive.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.onDestructive.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            LocationOption("Osasco", colors.onDestructive, onLocationSelected)
            LocationOption("São Paulo", colors.onDestructive, onLocationSelected)
            LocationOption("Barueri", colors.onDestructive, onLocationSelected)
            LocationOption("Carapicuíba", colors.onDestructive, onLocationSelected)
        }
    }
}

@Composable
private fun LocationOption(
    name: String,
    textColor: androidx.compose.ui.graphics.Color,
    onSelect: (String) -> Unit
) {
    Text(
        text = name,
        fontSize = 16.sp,
        color = textColor,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(name) }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    )
}
