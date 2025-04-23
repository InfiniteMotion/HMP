package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.database.myClass.PlaybackMode
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel

@Composable
fun GalleryShiftButton() {
    var isLeftSelected by remember { mutableStateOf(true) } // 默认选中左边图标
    val lColor = if (isLeftSelected) {
        colorResource(R.color.white)
    } else {
        colorResource(R.color.black)
    }
    val rColor = if (isLeftSelected) {
        colorResource(R.color.black)
    } else {
        colorResource(R.color.white)
    }

    Box(
        modifier = Modifier
            .border(
                width = 2.dp, // 边框宽度
                color = Color.Black, // 边框颜色
                shape = RoundedCornerShape(32.dp) // 边框形状，可以自定义为圆形或其他形状
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier
        ) {
            // 左边图标
            IconButton(
                onClick = {isLeftSelected = true},
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (isLeftSelected) colorResource(R.color.HDRed) else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.externaldrive),
                    contentDescription = "File Button",
                    modifier = Modifier.size(24.dp),
                    tint = lColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 右边图标
            IconButton(
                onClick = {isLeftSelected = false},
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (!isLeftSelected) colorResource(R.color.HDRed) else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.icloud),
                    contentDescription = "Cloud Button",
                    modifier = Modifier.size(24.dp),
                    tint = rColor
                )
            }
        }
    }
}

@Composable
fun PlayControlButtonOne(
    navController: NavController,
    playControlViewModel: PlayControlViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = {},
            modifier = Modifier
                .size(32.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.slider_vertical_3),
                contentDescription = "select Button",
                modifier = Modifier.size(24.dp),
            )
        }
        IconButton(
            onClick = {
                playControlViewModel.addAllToPlaylistInOrder()
                navController.navigate("player")
            },
            modifier = Modifier
                .size(32.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.order_play),
                contentDescription = "order play Button",
                modifier = Modifier.size(24.dp),
            )
        }
        IconButton(
            onClick = {
                playControlViewModel.addAllToPlaylistByShuffle()
                navController.navigate("player")
            },
            modifier = Modifier
                .size(32.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = "shuffle play Button",
                modifier = Modifier.size(24.dp),
            )
        }
        IconButton(
            onClick = {

            },
            modifier = Modifier
                .size(32.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.lightbulb),
                contentDescription = "self play Button",
                modifier = Modifier.size(24.dp),
            )
        }
    }
}