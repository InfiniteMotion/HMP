package com.example.hearablemusicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi

class MusicNotificationReceiver : BroadcastReceiver() {
    @OptIn(UnstableApi::class)
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val serviceIntent = Intent(context, MusicPlayService::class.java).apply {
            this.action = action
        }
        Log.d("NotificationReceiver", "接收到操作: $action")
        context.startService(serviceIntent)
    }
}
