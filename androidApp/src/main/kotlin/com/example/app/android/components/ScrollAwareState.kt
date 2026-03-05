package com.example.app.android.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.abs

class ScrollAwareState(private val thresholdPx: Float) {
    // 0f = fully visible, -heightPx = fully hidden (slides up)
    var topBarOffsetPx by mutableFloatStateOf(0f)
        private set

    // 0f = fully visible, -heightPx = fully hidden (slides down, inverted in UI)
    var bottomBarOffsetPx by mutableFloatStateOf(0f)
        private set

    var topBarHeightPx by mutableFloatStateOf(0f)
    var bottomBarHeightPx by mutableFloatStateOf(0f)

    private var directionBuffer = 0f
    private var lastDirectionDown: Boolean? = null
    private var thresholdCleared = false

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.y // negative = scrolling down, positive = scrolling up
            if (abs(delta) < 0.5f) return Offset.Zero

            val isDown = delta < 0

            // Direction changed → reset buffer and threshold
            if (lastDirectionDown != null && lastDirectionDown != isDown) {
                directionBuffer = 0f
                // Skip dead zone if bars are mid-transition (not fully visible or fully hidden)
                val topAtRest = topBarOffsetPx == 0f || topBarOffsetPx == -topBarHeightPx
                val bottomAtRest = bottomBarOffsetPx == 0f || bottomBarOffsetPx == -bottomBarHeightPx
                thresholdCleared = !(topAtRest && bottomAtRest)
            }
            lastDirectionDown = isDown

            if (!thresholdCleared) {
                directionBuffer += delta
                if (abs(directionBuffer) < thresholdPx) return Offset.Zero
                // Threshold just crossed — use only the excess
                thresholdCleared = true
                val excess = abs(directionBuffer) - thresholdPx
                val effectiveDelta = if (isDown) -excess else excess
                topBarOffsetPx = (topBarOffsetPx + effectiveDelta).coerceIn(-topBarHeightPx, 0f)
                bottomBarOffsetPx = (bottomBarOffsetPx + effectiveDelta).coerceIn(-bottomBarHeightPx, 0f)
            } else {
                topBarOffsetPx = (topBarOffsetPx + delta).coerceIn(-topBarHeightPx, 0f)
                bottomBarOffsetPx = (bottomBarOffsetPx + delta).coerceIn(-bottomBarHeightPx, 0f)
            }

            return Offset.Zero
        }
    }

    fun reset() {
        topBarOffsetPx = 0f
        bottomBarOffsetPx = 0f
        directionBuffer = 0f
        lastDirectionDown = null
        thresholdCleared = false
    }
}

@Composable
fun rememberScrollAwareState(): ScrollAwareState {
    val density = LocalDensity.current.density
    return remember { ScrollAwareState(thresholdPx = 200f * density) }
}
