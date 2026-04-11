package com.littlegrow.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.softShadow(
    elevation: Dp = 12.dp,
    shape: Shape = Shapes.large,
    color: Color = Color(0xFF825600).copy(alpha = 0.05f)
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = color,
    spotColor = color
)
