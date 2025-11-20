package com.example.hearablemusicplayer.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.BackButton
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel

@Composable
fun SearchScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
){
    var visible by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchResults by musicViewModel.searchResults.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        visible = true
        musicViewModel.searchMusic(searchQuery)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.align(Alignment.CenterStart)
                    ){
                        BackButton(navController)
                    }
                    Text(
                        "搜索",
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        musicViewModel.searchMusic(it) // 调用 ViewModel 的搜索方法
                    },
                    label = { Text("搜索您的音乐") },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.magnifyingglass), contentDescription = "搜索") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent, // 聚焦时下划线颜色
                        unfocusedIndicatorColor = Color.Transparent, // 未聚焦时下划线颜色
                        disabledIndicatorColor = Color.Transparent // 禁用时下划线颜色
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 32.dp)
                )
                MusicList(searchResults,playControlViewModel,navController)
            }
        }
    }
}
