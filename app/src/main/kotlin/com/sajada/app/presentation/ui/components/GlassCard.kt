package com.sajada.app.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) {
        Color(0xFF242424).copy(alpha = 0.65f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.75f)
    }

    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.5f)
    }

    val shape = RoundedCornerShape(cornerRadius)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = backgroundColor,
            tonalElevation = elevation,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = backgroundColor,
            tonalElevation = elevation,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(content = content)
        }
    }
}
