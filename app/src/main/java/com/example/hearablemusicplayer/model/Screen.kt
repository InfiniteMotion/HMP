package com.example.hearablemusicplayer.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.hearablemusicplayer.R

data class BottomNavItem(
    val route: String,
    val icon: Int,
    val label: String
)

@Composable
fun rememberBottomNavItems(): List<BottomNavItem> {
    return remember {
        listOf(
            BottomNavItem(
                route = "home",
                icon = R.drawable.home_d,
                label = "Home"
            ),
            BottomNavItem(
                route = "gallery",
                icon = R.drawable.gallery_d,
                label = "Gallery"
            ),
            BottomNavItem(
                route = "player",
                icon = R.drawable.player_d,
                label = "Player"
            ),
            BottomNavItem(
                route = "list",
                icon = R.drawable.list_d,
                label = "List"
            ),
            BottomNavItem(
                route = "user",
                icon = R.drawable.user_d,
                label = "User"
            )
        )
    }
}