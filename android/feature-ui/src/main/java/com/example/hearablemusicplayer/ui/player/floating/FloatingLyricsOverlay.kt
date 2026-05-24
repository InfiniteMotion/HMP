package com.example.hearablemusicplayer.ui.player.floating

import android.view.WindowManager
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearablemusicplayer.player.controller.MusicController
import com.hmp.domain.config.LyricsConfig
import com.hmp.domain.lyrics.LrcParser
import com.hmp.domain.lyrics.findCurrentLyricIndex
import com.hmp.domain.lyrics.LyricsComponent
import com.hmp.domain.setting.usecase.LyricsSettingsUseCase

@Composable
fun FloatingLyricsOverlay(
    musicController: MusicController,
    lyricsSettingsUseCase: LyricsSettingsUseCase,
    windowManager: WindowManager,
    initialLayoutParams: WindowManager.LayoutParams,
    onClose: () -> Unit
) {
    val overlayView = LocalView.current
    val displayMetrics = overlayView.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels

    val lyrics by musicController.currentMusicLyrics.collectAsState()
    val currentPosition by musicController.currentPosition.collectAsState()
    val isPlaying by musicController.isPlaying.collectAsState()

    var resolvedConfig by remember { mutableStateOf(LyricsConfig()) }
    var isLocked by remember { mutableStateOf(false) }
    var animTargetX by remember { mutableIntStateOf(initialLayoutParams.x) }

    LaunchedEffect(Unit) {
        resolvedConfig = lyricsSettingsUseCase.resolveConfig(LyricsComponent.FLOATING)
    }

    val animatedX by animateIntAsState(animTargetX, tween(250), label = "snapX")
    LaunchedEffect(animatedX) {
        initialLayoutParams.x = animatedX
        try { windowManager.updateViewLayout(overlayView, initialLayoutParams) } catch (_: Exception) {}
    }

    val lyricsText = lyrics
    val parsedLyrics = remember(lyricsText) {
        if (lyricsText != null) LrcParser.parse(lyricsText) else emptyList()
    }

    val currentIndex by remember(parsedLyrics, currentPosition) {
        derivedStateOf { findCurrentLyricIndex(parsedLyrics, currentPosition) }
    }

    val currentLine = parsedLyrics.getOrNull(currentIndex)

    val background = Color(0xE61A1A1A)
    val textColor = Color.White
    val dimTextColor = Color.White.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (!isLocked) Modifier.pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { _, d ->
                            initialLayoutParams.x += d.x.toInt()
                            initialLayoutParams.y += d.y.toInt()
                            try { windowManager.updateViewLayout(overlayView, initialLayoutParams) } catch (_: Exception) {}
                        },
                        onDragEnd = {
                            val cx = initialLayoutParams.x + initialLayoutParams.width / 2
                            animTargetX = if (cx < screenWidth / 2) 0 else screenWidth - initialLayoutParams.width
                        }
                    )
                } else Modifier
            )
    ) {
        // Top bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⋮⋮", color = dimTextColor, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { isLocked = !isLocked }, Modifier.size(24.dp)) {
                Text(if (isLocked) "🔒" else "🔓", fontSize = 10.sp)
            }
            IconButton(onClick = onClose, Modifier.size(24.dp)) {
                Text("✕", color = dimTextColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Current lyric line — centered
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (currentLine != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentLine.originalText,
                        color = textColor,
                        fontSize = resolvedConfig.currentTimeTextSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (resolvedConfig.displayMode != com.hmp.domain.config.DisplayMode.LANG1) {
                        currentLine.translatedText?.let { translated ->
                            Spacer(Modifier.size(4.dp))
                            Text(
                                text = translated,
                                color = dimTextColor,
                                fontSize = resolvedConfig.translatedTextSize.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                Text("♪", color = dimTextColor, fontSize = 24.sp)
            }
        }

        // Playing indicator
        if (isPlaying) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            ) {
                Text("●", color = Color.White.copy(alpha = 0.15f), fontSize = 6.sp)
            }
        }
    }
}
