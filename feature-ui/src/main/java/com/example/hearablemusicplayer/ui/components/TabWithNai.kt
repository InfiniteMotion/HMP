package com.example.hearablemusicplayer.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel

@OptIn(UnstableApi::class)
@Composable
fun CustomBottomNavBar(
    playControlViewModel: PlayControlViewModel,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = rememberBottomNavItems()
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    val haptic = rememberHapticFeedback()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                CustomNavItem(
                    isPlaying = isPlaying,
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = {
                        haptic.performClick()
                        onNavigate(item.route)
                    }
                )
            }
        }
    }
}

@Composable
fun CustomNavItem(
    isPlaying: Boolean,
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp)
        ) {
            if (item.label == "Player") {
                if (isPlaying) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(item.icon),
                        contentDescription = item.label,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }else {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(item.icon),
                        contentDescription = item.label,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(if (isSelected) item.iconSelected else item.icon),
                    contentDescription = item.label,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

