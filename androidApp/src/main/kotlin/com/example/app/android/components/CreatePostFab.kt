package com.example.app.android.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.app.android.R
import com.example.app.android.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun CreatePostFab(
    onClick: () -> Unit,
    scrollAwareState: ScrollAwareState,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val density = LocalDensity.current

    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = colors.accent,
        contentColor = colors.onAccent,
        modifier = modifier
            .padding(end = 20.dp)
            .offset {
                IntOffset(
                    0,
                    (-scrollAwareState.bottomBarHeightPx - with(density) { 16.dp.toPx() }).roundToInt()
                        + (-scrollAwareState.bottomBarOffsetPx).roundToInt()
                )
            }
            .size(56.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.new_post),
            modifier = Modifier.size(24.dp)
        )
    }
}
