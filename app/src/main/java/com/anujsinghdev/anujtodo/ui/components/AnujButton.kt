package com.anujsinghdev.anujtodo.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Colors (Blue Theme) ---
val AnujBlue500 = Color(0xFF3B82F6)
val AnujIndigo700 = Color(0xFF4338CA)
val AnujRed500 = Color(0xFFEF4444)
val AnujSky400 = Color(0xFF38BDF8)
val AnujSky500 = Color(0xFF0EA5E9)
val AnujSky600 = Color(0xFF0284C7)
val AnujSlate50 = Color(0xFFF8FAFC)
val AnujZinc800 = Color(0xFF27272A)

@Composable
fun AnujButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    // 0 = Idle, 1 = Loading
    var state by remember { mutableIntStateOf(0) }

    val handleOnClick = {
        if (state == 0) {
            onClick()
            state = 1
            scope.launch {
                delay(2000)
                state = 0
            }
        }
    }

    // Animation values
    val animatable = remember { Animatable(.1f) }
    LaunchedEffect(state) {
        if (state == 1) {
            while (true) {
                animatable.animateTo(.4f, animationSpec = tween(500))
                animatable.animateTo(.94f, animationSpec = tween(500))
            }
        } else {
            animatable.animateTo(.5f, animationSpec = tween(900))
        }
    }

    val color = remember { Animatable(AnujSky600) }
    LaunchedEffect(state) {
        if (state == 1) {
            while (true) {
                color.animateTo(AnujBlue500, animationSpec = tween(500))
                color.animateTo(AnujSky400, animationSpec = tween(500))
            }
        } else {
            color.animateTo(AnujSky500, animationSpec = tween(900))
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = handleOnClick
            )
            .anujMeshGradient(
                points = listOf(
                    listOf(
                        Offset(0f, 0f) to AnujZinc800,
                        Offset(.5f, 0f) to AnujZinc800,
                        Offset(1f, 0f) to AnujZinc800,
                    ),
                    listOf(
                        Offset(0f, .5f) to AnujIndigo700,
                        Offset(.5f, animatable.value) to AnujIndigo700,
                        Offset(1f, .5f) to AnujIndigo700,
                    ),
                    listOf(
                        Offset(0f, 1f) to color.value,
                        Offset(.5f, 1f) to color.value,
                        Offset(1f, 1f) to color.value,
                    ),
                ),
                resolutionX = 64,
            )
            .animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                )
            )
    ) {
        AnimatedContent(
            targetState = state,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .defaultMinSize(minHeight = 24.dp)
                .align(Alignment.Center),
            transitionSpec = {
                slideInVertically(initialOffsetY = { -it }) + fadeIn() togetherWith
                        slideOutVertically(targetOffsetY = { it }) + fadeOut() using SizeTransform(
                    clip = false,
                    sizeAnimationSpec = { _, _ -> spring(stiffness = Spring.StiffnessHigh) }
                )
            },
            label = "ButtonContent"
        ) { targetState ->
            if (targetState == 1) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AnujSlate50,
                    strokeWidth = 3.dp,
                    strokeCap = StrokeCap.Round,
                )
            } else {
                Text(
                    text = text,
                    color = AnujSlate50,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

// --- Custom Mesh Modifier Logic ---
fun Modifier.anujMeshGradient(
    points: List<List<Pair<Offset, Color>>>,
    resolutionX: Int = 1,
): Modifier = this.drawBehind {
    val resolutionY = resolutionX
    val vertices = mutableListOf<Float>()
    val colors = mutableListOf<Int>()
    val indices = mutableListOf<Short>()

    for (y in 0..resolutionY) {
        for (x in 0..resolutionX) {
            val u = x.toFloat() / resolutionX
            val v = y.toFloat() / resolutionY

            val p = getInterpolatedPoint(u, v, points, size)
            vertices.add(p.x)
            vertices.add(p.y)

            val c = getInterpolatedColor(u, v, points)
            colors.add(c.toArgb())
        }
    }

    for (y in 0 until resolutionY) {
        for (x in 0 until resolutionX) {
            val i = (y * (resolutionX + 1) + x).toShort()
            val j = (i + 1).toShort()
            val k = ((y + 1) * (resolutionX + 1) + x).toShort()
            val l = (k + 1).toShort()

            indices.add(i); indices.add(k); indices.add(j)
            indices.add(j); indices.add(k); indices.add(l)
        }
    }

    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawVertices(
            android.graphics.Canvas.VertexMode.TRIANGLES,
            vertices.size, vertices.toFloatArray(), 0, null, 0,
            colors.toIntArray(), 0, indices.toShortArray(), 0, indices.size,
            android.graphics.Paint()
        )
    }
}

@SuppressLint("RestrictedApi")
private fun getInterpolatedPoint(
    u: Float, v: Float, points: List<List<Pair<Offset, Color>>>, size: androidx.compose.ui.geometry.Size
): Offset {
    val row = (points.size - 1) * v
    val col = (points[0].size - 1) * u
    val r1 = row.toInt()
    val c1 = col.toInt()
    val r2 = (r1 + 1).coerceAtMost(points.size - 1)
    val c2 = (c1 + 1).coerceAtMost(points[0].size - 1)
    val rFrac = row - r1
    val cFrac = col - c1

    val p00 = points[r1][c1].first.scale(size)
    val p10 = points[r1][c2].first.scale(size)
    val p01 = points[r2][c1].first.scale(size)
    val p11 = points[r2][c2].first.scale(size)

    val top = lerp(p00, p10, cFrac)
    val bottom = lerp(p01, p11, cFrac)
    return lerp(top, bottom, rFrac)
}

private fun getInterpolatedColor(
    u: Float, v: Float, points: List<List<Pair<Offset, Color>>>
): Color {
    val row = (points.size - 1) * v
    val col = (points[0].size - 1) * u
    val r1 = row.toInt()
    val c1 = col.toInt()
    val r2 = (r1 + 1).coerceAtMost(points.size - 1)
    val c2 = (c1 + 1).coerceAtMost(points[0].size - 1)
    val rFrac = row - r1
    val cFrac = col - c1

    val c00 = points[r1][c1].second
    val c10 = points[r1][c2].second
    val c01 = points[r2][c1].second
    val c11 = points[r2][c2].second

    val top = lerp(c00, c10, cFrac)
    val bottom = lerp(c01, c11, cFrac)
    return lerp(top, bottom, rFrac)
}

private fun Offset.scale(size: androidx.compose.ui.geometry.Size): Offset {
    return Offset(x * size.width, y * size.height)
}