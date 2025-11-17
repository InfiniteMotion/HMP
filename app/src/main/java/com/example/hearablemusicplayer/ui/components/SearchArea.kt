package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.viewmodel.MusicViewModel


@Composable
fun SearchArea(
    viewModel: MusicViewModel
){
    // 搜索框内容
    var searchQuery by rememberSaveable { mutableStateOf("") }
    // 搜索结果
    val searchResults by viewModel.searchResults.collectAsState(initial = emptyList())
    TextField(
        value = searchQuery,
        onValueChange = {
            searchQuery = it
            viewModel.searchMusic(it) // 调用 ViewModel 的搜索方法
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
        modifier = Modifier.height(56.dp)
            .fillMaxWidth()
    )
}