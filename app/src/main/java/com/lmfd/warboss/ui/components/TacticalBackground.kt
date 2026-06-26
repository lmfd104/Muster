package com.lmfd.warboss.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TacticalBackground(
    modifier: Modifier = Modifier,
    dotColor: Color = Color(0xFF1F1F1F),
    spacing: Dp = 24.dp,
) {
    Canvas(modifier.fillMaxSize()) {
        val spacingPx = spacing.toPx()
        var y = 0f
        while (y <= size.height + spacingPx) {
            var x = 0f
            while (x <= size.width + spacingPx) {
                drawCircle(color = dotColor, radius = 1.3f, center = Offset(x, y))
                x += spacingPx
            }
            y += spacingPx
        }
    }
}
