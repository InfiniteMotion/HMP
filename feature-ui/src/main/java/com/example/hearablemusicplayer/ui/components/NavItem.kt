package com.example.hearablemusicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.hearablemusicplayer.ui.R

import com.example.hearablemusicplayer.ui.util.Routes

data class BottomNavItem(
    val route: Any,
    val icon: Int,
    val iconSelected: Int,
    val label: String
)

@Composable
fun rememberBottomNavItems(): List<BottomNavItem> {
    return remember {
        listOf(
            BottomNavItem(
                route = Routes.Home,
                icon = R.drawable.house,
                iconSelected = R.drawable.house_fill,
                label = "Home"
            ),
            BottomNavItem(
                route = Routes.Gallery,
                icon = R.drawable.music,
                iconSelected = R.drawable.music_fill,
                label = "Gallery"
            ),
            BottomNavItem(
                route = Routes.Player,
                icon = R.drawable.player_d,
                iconSelected = R.drawable.player_d,
                label = "Player"
            ),
            BottomNavItem(
                route = Routes.List,
                icon = R.drawable.square_grid_2x2,
                iconSelected = R.drawable.square_fill_grid_2x2,
                label = "List"
            ),
            BottomNavItem(
                route = Routes.User,
                icon = R.drawable.person,
                iconSelected = R.drawable.person_filled_viewfinder,
                label = "User"
            )
        )
    }
}
