package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
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
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel

@Composable
fun CustomBottomNavBar(
    playControlViewModel: PlayControlViewModel,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = rememberBottomNavItems()
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    // 添加顶部阴影线条 + 背景区分
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                CustomNavItem(
                    isPlaying = isPlaying,
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) }
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
                    )
                }
            }else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(if (isSelected) item.iconSelected else item.icon),
                    contentDescription = item.label,
                )
            }
        }
    }
}

