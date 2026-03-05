package com.example.app.android.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.app.android.R
import com.example.app.android.theme.AppTheme

@Composable
fun MyPostsScreen(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.my_posts_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        Text(
            text = stringResource(R.string.my_posts_empty),
            fontSize = 14.sp,
            color = colors.textSecondary
        )
    }
}
