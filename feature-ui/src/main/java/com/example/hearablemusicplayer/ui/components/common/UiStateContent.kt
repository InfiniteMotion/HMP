package com.example.hearablemusicplayer.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.hearablemusicplayer.ui.util.UiState

@Composable
fun <T> UiStateContent(
    state: UiState<T>,
    modifier: Modifier = Modifier,
    onLoading: @Composable () -> Unit = { DefaultLoading() },
    onError: @Composable (String) -> Unit = { message -> DefaultError(message) },
    onEmpty: @Composable () -> Unit = { DefaultEmpty() },
    onSuccess: @Composable (T) -> Unit
) {
    when (state) {
        is UiState.Idle -> onLoading()
        is UiState.Loading -> onLoading()
        is UiState.Error -> onError(state.message)
        is UiState.Empty -> onEmpty()
        is UiState.Success -> onSuccess(state.data)
    }
}
