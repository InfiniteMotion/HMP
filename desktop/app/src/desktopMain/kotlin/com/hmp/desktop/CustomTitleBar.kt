package com.hmp.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmp.desktop.ui.common.components.TITLE_BAR_HEIGHT

/**
 * Custom window title bar for undecorated desktop window.
 * Provides window dragging via WindowDraggableArea and minimize/close controls.
 *
 * Maximize is intentionally omitted due to a known Compose Multiplatform bug
 * (JetBrains/compose-multiplatform#3625).
 *
 * @param isDarkTheme Whether the app is in dark theme — affects button styling
 * @param onMinimize Called to minimize the window
 * @param onClose Called when the close button is clicked
 */
@Composable
fun CustomTitleBar(
    isDarkTheme: Boolean,
    onMinimize: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TITLE_BAR_HEIGHT)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: App title
        Text(
            text = "HMP",
            modifier = Modifier.padding(start = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )

        // Center: Draggable area — uses java.awt.MouseInfo for screen-absolute
        // mouse position. This avoids the Compose coordinate feedback loop that
        // causes jitter when using detectDragGestures.
        Box(
            modifier = Modifier
                .weight(1f)
                .height(TITLE_BAR_HEIGHT)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitPointerEvent(PointerEventPass.Initial)
                            if (!down.changes.any { it.pressed }) continue
                            down.changes.first().consume()

                            val awtWindow = java.awt.Window.getWindows().firstOrNull() ?: continue
                            val startMouse = java.awt.MouseInfo.getPointerInfo().location
                            val startWin = awtWindow.locationOnScreen

                            while (true) {
                                val move = awaitPointerEvent(PointerEventPass.Initial)
                                if (!move.changes.any { it.pressed }) break
                                move.changes.first().consume()

                                val mouse = java.awt.MouseInfo.getPointerInfo().location
                                awtWindow.location = java.awt.Point(
                                    startWin.x + mouse.x - startMouse.x,
                                    startWin.y + mouse.y - startMouse.y
                                )
                            }
                        }
                    }
                }
        )

        // Right: Window control buttons
        val closeInteraction = remember { MutableInteractionSource() }
        val closeHovered by closeInteraction.collectIsHoveredAsState()

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minimize button
            WindowControlButton(
                symbol = "─",
                isDarkTheme = isDarkTheme,
                onClick = onMinimize
            )

            // Close button — red highlight on hover
            WindowControlButton(
                symbol = "✕",
                isDarkTheme = isDarkTheme,
                isClose = true,
                interactionSource = closeInteraction,
                hoveredOverride = closeHovered,
                onClick = onClose
            )
        }
    }
}

@Composable
private fun WindowControlButton(
    symbol: String,
    isDarkTheme: Boolean,
    isClose: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    hoveredOverride: Boolean? = null,
    onClick: () -> Unit
) {
    val isHovered = hoveredOverride ?: interactionSource.collectIsHoveredAsState().value
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = when {
        isClose && isHovered -> Color(0xFFC42B1C)
        isClose && isPressed -> Color(0xFFB22419)
        isHovered -> if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f)
        isPressed -> if (isDarkTheme) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    val textColor = when {
        isClose && (isHovered || isPressed) -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(46.dp, TITLE_BAR_HEIGHT)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = textColor,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )
    }
}
