package com.anujsinghdev.anujtodo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElasticSwipeToDismiss(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onComplete: (() -> Unit)? = null, // Changed from onArchive to onComplete
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    // Define the threshold (e.g., 25% of the screen or fixed dp)
    val actionThreshold = 56.dp

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (onComplete != null) {
                        onComplete()
                        true
                    } else false
                }
                else -> false
            }
        },
        positionalThreshold = { with(density) { actionThreshold.toPx() } }
    )

    // Track if we have passed the threshold to trigger the "elastic" effect
    var willDismiss by remember { mutableStateOf(false) }

    // Animate Icon Scale (The Elastic Effect)
    val iconScale by animateFloatAsState(
        targetValue = if (willDismiss) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "IconScale"
    )

    // Safety check for offset reading
    LaunchedEffect(state) {
        snapshotFlow {
            try {
                state.requireOffset()
            } catch (e: Exception) {
                0f // Default to 0 if not initialized yet
            }
        }
            .collect { offsetVal ->
                val offset = abs(offsetVal)
                val thresholdPx = with(density) { actionThreshold.toPx() }

                val newWillDismiss = offset > thresholdPx

                if (newWillDismiss != willDismiss) {
                    willDismiss = newWillDismiss
                    if (willDismiss) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            }
    }

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = onComplete != null,
        backgroundContent = {
            val direction = state.dismissDirection
            val color by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF5252) // Red for Delete
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Green for Complete
                    else -> Color.Transparent
                }, label = "BgColor"
            )

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }

            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Outlined.Check // Check Icon
                else -> Icons.Outlined.Delete
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.scale(iconScale) // Apply the bounce
                )
            }
        },
        content = { content() }
    )
}
