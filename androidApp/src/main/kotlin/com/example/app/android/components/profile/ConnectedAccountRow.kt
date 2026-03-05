package com.example.app.android.components.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.android.theme.AppTheme

@Composable
fun ConnectedAccountRow(
    icon: ImageVector,
    name: String,
    connected: Boolean,
    statusLabel: String,
    connectLabel: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = name,
            fontSize = 15.sp,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (connected) statusLabel else connectLabel,
            fontSize = 13.sp,
            fontWeight = if (connected) FontWeight.Medium else FontWeight.Normal,
            color = if (connected) colors.badgeText else colors.accent
        )
    }
}
