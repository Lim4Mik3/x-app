package com.example.app.android.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.android.R
import com.example.app.android.network.ApiClient
import com.example.app.android.network.SignalKeysCache
import com.example.app.android.network.models.PostSignals
import com.example.app.android.network.models.SignalGroup
import com.example.app.android.network.models.SignalKey
import com.example.app.android.network.models.SignalPair
import com.example.app.android.network.models.formatCompactNumber
import com.example.app.android.theme.AppTheme
import kotlinx.coroutines.launch

private val positiveColor = Color(0xFF33B380)
private val negativeColor = Color(0xFFD95959)

private val emojiMap = mapOf(
    "saw_it" to "\uD83D\uDC40",
    "i_confirm" to "\u2705",
    "is_recurring" to "\uD83D\uDD01",
    "didnt_see" to "\uD83E\uDD37",
    "not_true" to "\uD83D\uDEAB",
    "exaggerated" to "\uD83D\uDE24",
    "solidarity" to "\uD83E\uDD1D",
    "outrage" to "\uD83D\uDE21",
    "fear" to "\uD83D\uDE28",
    "relief" to "\uD83D\uDE2E\u200D\uD83D\uDCA8",
    "gratitude" to "\uD83D\uDE4F",
    "still_happening" to "\u231B",
    "resolved" to "\uD83D\uDD27",
    "me_too" to "\uD83D\uDE4B",
    "urgent" to "\uD83D\uDD25",
    "helpful" to "\uD83D\uDCA1",
    "funny" to "\uD83D\uDE02",
    "sad" to "\uD83D\uDE22",
    "love" to "\u2764\uFE0F",
    "danger" to "\u26A0\uFE0F",
    "safe" to "\uD83D\uDEE1\uFE0F"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalSheet(
    postId: String,
    typeKey: String,
    onSignalsUpdated: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var signalKeys by remember { mutableStateOf<List<SignalKey>?>(null) }
    var signalGroups by remember { mutableStateOf<List<SignalGroup>>(emptyList()) }
    var postSignals by remember { mutableStateOf<PostSignals?>(null) }
    var selectedKeys by remember { mutableStateOf(setOf<String>()) }
    var isLoading by remember { mutableStateOf(true) }
    var unsupportedType by remember { mutableStateOf(false) }

    // Remember initial state for change detection
    var initialKeys by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(postId) {
        isLoading = true
        val keys = SignalKeysCache.getKeys(typeKey)
        if (keys != null) {
            signalKeys = keys
            signalGroups = SignalGroup.groupFromKeys(keys)
            ApiClient.getPostSignals(postId).onSuccess { ps ->
                postSignals = ps
                selectedKeys = ps.mySignals.toSet()
                initialKeys = ps.mySignals.toSet()
            }
        } else {
            unsupportedType = true
        }
        isLoading = false
    }

    fun syncAndDismiss() {
        val hasChanges = selectedKeys != initialKeys
        if (hasChanges) {
            // Optimistic count
            val oldKeys = initialKeys
            val added = selectedKeys.subtract(oldKeys).size
            val removed = oldKeys.subtract(selectedKeys).size
            val currentTotal = postSignals?.signals?.values?.sum() ?: 0
            val previousTotal = currentTotal
            val optimisticTotal = maxOf(currentTotal + added - removed, 0)
            onSignalsUpdated(optimisticTotal)

            val keys = selectedKeys.toList()
            scope.launch {
                ApiClient.syncSignals(postId, keys).fold(
                    onSuccess = { response ->
                        val serverTotal = response.signals.values.sum()
                        onSignalsUpdated(serverTotal)
                    },
                    onFailure = {
                        onSignalsUpdated(previousTotal)
                        ToastManager.show(context.getString(R.string.toast_signal_error))
                    }
                )
            }
        }
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = { syncAndDismiss() },
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 16.dp)
                    .size(width = 36.dp, height = 5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.divider)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 34.dp)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = context.getString(R.string.signal_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = context.getString(R.string.signal_subtitle),
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    lineHeight = 18.sp
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    }
                }
                unsupportedType -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = context.getString(R.string.signal_unsupported),
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        signalGroups.forEach { group ->
                            SignalGroupSection(
                                group = group,
                                selectedKeys = selectedKeys,
                                signalCounts = postSignals?.signals ?: emptyMap(),
                                context = context,
                                onToggle = { signal, opposite ->
                                    selectedKeys = if (signal.key in selectedKeys) {
                                        selectedKeys - signal.key
                                    } else {
                                        val newSet = selectedKeys + signal.key
                                        if (signal.key != opposite.key) newSet - opposite.key else newSet
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalGroupSection(
    group: SignalGroup,
    selectedKeys: Set<String>,
    signalCounts: Map<String, Int>,
    context: android.content.Context,
    onToggle: (SignalKey, SignalKey) -> Unit
) {
    val colors = AppTheme.colors
    val localizedLabel = when (group.category) {
        "verification" -> context.getString(R.string.signal_section_verification)
        "reaction" -> context.getString(R.string.signal_section_reaction)
        else -> group.label
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Category header with lines
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f).height(0.5.dp).background(colors.divider))
            Text(
                text = localizedLabel.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Box(Modifier.weight(1f).height(0.5.dp).background(colors.divider))
        }

        // Signal pairs
        group.pairs.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    SignalItem(
                        signal = pair.left,
                        isPositive = true,
                        isSelected = pair.left.key in selectedKeys,
                        count = signalCounts[pair.left.key] ?: 0,
                        onClick = { onToggle(pair.left, pair.right) }
                    )
                }
                if (pair.left.key != pair.right.key) {
                    Box(Modifier.weight(1f)) {
                        SignalItem(
                            signal = pair.right,
                            isPositive = false,
                            isSelected = pair.right.key in selectedKeys,
                            count = signalCounts[pair.right.key] ?: 0,
                            onClick = { onToggle(pair.right, pair.left) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalItem(
    signal: SignalKey,
    isPositive: Boolean,
    isSelected: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    val tintColor = if (isPositive) positiveColor else negativeColor
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Background circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) tintColor.copy(alpha = 0.25f) else colors.divider.copy(alpha = 0.3f))
            )

            // Emoji
            Text(
                text = emojiMap[signal.key] ?: "\uD83D\uDCCC",
                fontSize = 24.sp,
                modifier = Modifier.scale(scale)
            )

            // Count badge
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) tintColor else colors.divider)
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatCompactNumber(count),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = signal.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) tintColor else colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
