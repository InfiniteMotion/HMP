package com.example.hearablemusicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.hearablemusicplayer.R

data class BottomNavItem(
    val route: String,
    val icon: Int,
    val icond: Int,
    val label: String
)

@Composable
fun rememberBottomNavItems(): List<BottomNavItem> {
    return remember {
        listOf(
            BottomNavItem(
                route = "home",
                icon = R.drawable.house,
                icond = R.drawable.house_fill,
                label = "Home"
            ),
            BottomNavItem(
                route = "gallery",
                icon = R.drawable.music,
                icond = R.drawable.music_fill,
                label = "Gallery"
            ),
            BottomNavItem(
                route = "player",
                icon = R.drawable.player_d,
                icond = R.drawable.player_d,
                label = "Player"
            ),
            BottomNavItem(
                route = "list",
                icon = R.drawable.square_grid_2x2,
                icond = R.drawable.square_fill_grid_2x2,
                label = "List"
            ),
            BottomNavItem(
                route = "user",
                icon = R.drawable.person,
                icond = R.drawable.person_filled_viewfinder,
                label = "User"
            )
        )
    }
}