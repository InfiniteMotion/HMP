package com.example.hearablemusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hearablemusicplayer.ui.controller.DialogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DialogManagerViewModel @Inject constructor(
    val dialogManager: DialogManager
) : ViewModel()