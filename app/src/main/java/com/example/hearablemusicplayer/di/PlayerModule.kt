package com.example.hearablemusicplayer.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object PlayerModule {

    @UnstableApi
    @Provides
    @ServiceScoped
    fun provideExoPlayer(
        @ApplicationContext context: Context
    ): ExoPlayer {
        // 配置音频属性
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true) // true 表示自动处理音频焦点
            .setHandleAudioBecomingNoisy(true) // 处理耳机拔出
            .setWakeMode(C.WAKE_MODE_NETWORK) // 保持网络唤醒
            .build()
    }
}
