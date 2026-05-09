package com.example.hearablemusicplayer.ui.common.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TabPageIndicator(
    modifier: Modifier = Modifier,
    currentPage: Int,
    totalPages: Int = 4,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            val barColor by animateColorAsState(
                targetValue = if (isSelected) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 200)
            )
            val barWidth = 32.dp
            val barHeight = if (isSelected) 3.dp else 2.dp

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(barHeight)
                    .padding(horizontal = 4.dp)
                    .background(
                        color = barColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}