package com.anujsinghdev.anujtodo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// macOS Folder Blue Colors
val MacFolderLightBlue = Color(0xFF7AA7F3) // Lighter top/tab
val MacFolderDarkBlue = Color(0xFF4B8EF7)  // Main body gradient start
val MacFolderDeepBlue = Color(0xFF2E7CF6)  // Main body gradient end

@Composable
fun MacosFolderIcon(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val width = this.size.width
        val height = this.size.height

        // 1. Draw the Back Tab (Top part)
        // It's a rounded rect that peeks out from the top left
        val tabHeight = height * 0.15f
        val tabWidth = width * 0.4f

        drawRoundRect(
            color = MacFolderLightBlue,
            topLeft = Offset(0f, height * 0.1f),
            size = Size(tabWidth, height * 0.5f), // Extend down so it connects
            cornerRadius = CornerRadius(width * 0.05f, width * 0.05f)
        )

        // 2. Draw the Back Paper/Sheet (Optional: White sheet inside)
        // Usually hidden or just a subtle white line, skipping for simple flat look

        // 3. Draw the Main Folder Body (Front)
        // This is the main blue rectangle with gradients
        val bodyTop = height * 0.25f
        val bodyHeight = height * 0.75f

        val gradientBrush = Brush.verticalGradient(
            colors = listOf(MacFolderDarkBlue, MacFolderDeepBlue),
            startY = bodyTop,
            endY = height
        )

        // Using a Path for slightly more control over corners if needed,
        // but RoundRect is sufficient for the classic rounded look.
        drawRoundRect(
            brush = gradientBrush,
            topLeft = Offset(0f, bodyTop),
            size = Size(width, bodyHeight),
            cornerRadius = CornerRadius(width * 0.08f, width * 0.08f)
        )

        // 4. Highlight/Shine (Optional for 3D effect)
        // A subtle gradient overlay on the top edge of the body
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                startY = bodyTop,
                endY = bodyTop + (bodyHeight * 0.3f)
            ),
            topLeft = Offset(0f, bodyTop),
            size = Size(width, bodyHeight * 0.3f),
            cornerRadius = CornerRadius(width * 0.08f, width * 0.08f)
        )
    }
}