package com.anujsinghdev.anujtodo.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable (triggerDismiss: () -> Unit) -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(50)
        showContent = true
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Animated background overlay
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                )
            }

            // Animated content
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(300)) +
                        scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ),
                exit = fadeOut(animationSpec = tween(200)) +
                        scaleOut(
                            targetScale = 0.8f,
                            animationSpec = tween(200)
                        )
            ) {
                content {
                    scope.launch {
                        showContent = false
                        delay(250)
                        onDismissRequest()
                    }
                }
            }
        }
    }
}
