package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.ui.components.BottomNavItem
import com.example.hearablemusicplayer.ui.components.rememberBottomNavItems

@Composable
fun CustomBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = rememberBottomNavItems()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            CustomNavItem(
                item = item,
                isSelected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

@Composable
fun CustomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(48.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                painter = painterResource(if (isSelected) item.icond else item.icon),
                contentDescription = item.label,
                tint = colorResource(R.color.black)
            )
        }
    }
}

