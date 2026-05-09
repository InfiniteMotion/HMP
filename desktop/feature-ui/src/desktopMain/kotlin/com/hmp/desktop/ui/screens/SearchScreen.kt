package com.hmp.desktop.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.usecase.SearchMusicUseCase
import com.hmp.desktop.player.DesktopMusicController
import com.hmp.desktop.ui.components.musiclist.MusicList
import com.hmp.desktop.ui.components.musiclist.MusicListCallbacksAdapter
import com.hmp.desktop.ui.components.musiclist.defaultMusicListConfig
import com.hmp.desktop.ui.navigation.DesktopNavigator
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMusicClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchMusicUseCase: SearchMusicUseCase = koinInject()
    val controller: DesktopMusicController = koinInject()
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<MusicInfo>>(emptyList()) }

    val config = defaultMusicListConfig(
        callbacks = object : MusicListCallbacksAdapter() {
            override fun onItemClick(musicInfo: MusicInfo, index: Int) {
                controller.setPlaylist(results, index)
            }
        },
    )

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("搜索") },
            navigationIcon = {
                IconButton(onClick = { DesktopNavigator.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
        )

        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                if (newQuery.isNotBlank()) {
                    scope.launch {
                        results = searchMusicUseCase(newQuery)
                    }
                } else {
                    results = emptyList()
                }
            },
            label = { Text("搜索音乐...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        MusicList(
            musicInfoList = results,
            config = config,
            modifier = Modifier.weight(1f),
        )
    }
}
