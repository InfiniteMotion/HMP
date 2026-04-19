package com.example.hearablemusicplayer.ui.common.dialogs.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hearablemusicplayer.ui.common.dialogs.controller.DialogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DialogManagerViewModel @Inject constructor(
    val dialogManager: DialogManager
) : ViewModel()