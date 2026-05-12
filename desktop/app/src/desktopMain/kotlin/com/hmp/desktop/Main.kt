package com.hmp.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hmp.desktop.player.DesktopMusicController
import com.hmp.desktop.ui.common.design.theme.ThemeExtensionManager
import com.hmp.desktop.ui.common.pages.MainScreen
import com.hmp.desktop.DwmHelper
import com.sun.jna.platform.win32.WinDef
import kotlinx.coroutines.CompletableDeferred
import org.koin.core.context.GlobalContext
import java.awt.EventQueue
import java.io.File
import kotlin.concurrent.thread

fun main() {
    // HiDPI scaling: must be set before any AWT/Compose class is loaded
    System.setProperty("sun.java2d.dpiaware", "true")
    System.setProperty("sun.java2d.scaling.enabled", "false")
    System.setProperty("sun.java2d.uiScale", "1")
    System.setProperty("skiko.renderApi", "OPENGL")
    System.setProperty("awt.useSystemAAFontSettings", "on")

    // Single-instance guard: exit immediately if another instance is running
    if (!SingleInstanceGuard.tryAcquire()) {
        println("HMP is already running. Exiting.")
        return
    }

    // Register Koin modules (lazy — no instances created yet)
    HmpDesktopApplication.init()

    // Pre-warm heavy dependencies on a background thread.
    // This triggers Room database creation, DataStore, and core UseCase resolution
    // so Compose first frame is not blocked by I/O.
    val appReady = CompletableDeferred<DesktopMusicController>()
    thread(name = "hmp-prewarm", isDaemon = true) {
        val controller = GlobalContext.get().get<DesktopMusicController>()
        appReady.complete(controller)
    }

    application {
        val state = rememberWindowState(
            size = DpSize(1200.dp, 800.dp),
            position = WindowPosition(Alignment.Center)
        )

        val backHandler = remember { mutableStateOf<(() -> Unit)?>(null) }

        // Live system dark mode state — updated by DwmHelper registry watcher
        var systemIsDark by remember { mutableStateOf(DwmHelper.isSystemDark()) }

        // MusicController is null until background pre-warm completes
        var musicController by remember { mutableStateOf<DesktopMusicController?>(null) }

        LaunchedEffect(Unit) {
            musicController = appReady.await()
        }

        Window(
            onCloseRequest = {
                musicController?.release()
                SystemTrayManager.dispose()
                SingleInstanceGuard.release()
                exitApplication()
            },
            state = state,
            undecorated = true,
            transparent = true,
            icon = painterResource("icon.png"),
            onKeyEvent = { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.Escape -> {
                            backHandler.value?.invoke()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
        ) {
            // Extract HWND and set up system theme watcher
            DisposableEffect(Unit) {
                val awtWindow = java.awt.Window.getWindows().firstOrNull()
                if (awtWindow != null) {
                    val hwnd = getHwndFromAwt(awtWindow)
                    if (hwnd != null) {
                        DwmHelper.setWindowHandle(hwnd)
                    }
                }
                val disposeWatcher = DwmHelper.watchSystemTheme { isDark ->
                    systemIsDark = isDark
                }
                onDispose { disposeWatcher() }
            }

            // Clip entire window content to rounded corners.
            // transparent = true makes the window background transparent,
            // so the clipped shape defines the visible window boundary.
            val cornerRadius = 20.dp
            val staticColorScheme = ThemeExtensionManager.getColorScheme(systemIsDark)
            MaterialTheme(colorScheme = staticColorScheme) {
                val shape = RoundedCornerShape(cornerRadius)
                Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (musicController == null) {
                        // Lightweight loading screen shown immediately while deps initialize
                        LoadingScreen()
                    } else {
                        val controller = musicController!!

                        DisposableEffect(Unit) {
                            onDispose { controller.release() }
                        }

                        // Initialize system tray with playback controls
                        LaunchedEffect(Unit) {
                            val window = java.awt.Window.getWindows().firstOrNull()
                            SystemTrayManager.init(
                                onPlayPause = { controller.togglePlayPause() },
                                onNext = { controller.playNext() },
                                onPrev = { controller.playPrevious() },
                                onShowWindow = {
                                    EventQueue.invokeLater {
                                        window?.let {
                                            it.isVisible = true
                                            it.toFront()
                                            it.repaint()
                                        }
                                    }
                                },
                                onExit = {
                                    controller.release()
                                    SystemTrayManager.dispose()
                                    SingleInstanceGuard.release()
                                    exitApplication()
                                }
                            )
                        }

                        // Update tray tooltip when current song changes
                        val currentMusic by controller.currentPlayingMusic.collectAsState()
                        LaunchedEffect(currentMusic) {
                            currentMusic?.let { music ->
                                val title = music.music.title.ifBlank { File(music.music.path).name }
                                val artist = music.music.artist.ifBlank { "Unknown Artist" }
                                SystemTrayManager.updateTooltip("$title - $artist")
                            }
                        }

                        // MainScreen fills entire window — background extends behind title bar
                        MainScreen(
                            onBackHandlerReady = { handler -> backHandler.value = handler },
                            systemIsDark = systemIsDark
                        )
                    }

                    // Title bar overlays on top
                    CustomTitleBar(
                        isDarkTheme = systemIsDark,
                        onMinimize = {
                            val awtWindow = java.awt.Window.getWindows().firstOrNull()
                            awtWindow?.let { WindowHelper.minimizeAwt(it) }
                        },
                        onClose = {
                            musicController?.release()
                            SystemTrayManager.dispose()
                            SingleInstanceGuard.release()
                            exitApplication()
                        }
                    )
                }
            }
            }
        }
    }

    // Cleanup on normal exit (after application{} returns)
    SingleInstanceGuard.release()
}

/**
 * Extract the native HWND from an AWT Window via its peer.
 * Compose Desktop creates an AWT Window under the hood; the peer holds the HWND.
 */
private fun getHwndFromAwt(window: java.awt.Window): WinDef.HWND? {
    return try {
        // Access the peer field via reflection (package-private in java.awt.Component)
        val peerField = java.awt.Component::class.java.getDeclaredField("peer")
        peerField.isAccessible = true
        val peer = peerField.get(window) ?: return null

        // The hwnd field lives on WComponentPeer in the class hierarchy
        val hwndPtr = findFieldValue(peer, "hwnd") as? Long
        if (hwndPtr != null) {
            return WinDef.HWND(com.sun.jna.Pointer(hwndPtr))
        }
        println("[Main] Could not find hwnd field on peer")
        null
    } catch (e: Throwable) {
        println("[Main] Could not extract HWND from AWT: ${e.message}")
        null
    }
}

private fun findFieldValue(obj: Any, fieldName: String): Any? {
    var cls: Class<*>? = obj.javaClass
    while (cls != null) {
        try {
            val field = cls.getDeclaredField(fieldName)
            field.isAccessible = true
            return field.get(obj)
        } catch (_: NoSuchFieldException) {
            cls = cls.superclass
        }
    }
    return null
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "HMP",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
