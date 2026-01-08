# Hearable Music Player 架构优化方案

## 📋 优化任务清单

### 高优先级（立即执行）
1. **Repository 职责拆分**
   - [ ] 在 `core-domain` 定义 `IMusicRepository`、`IPlaylistRepository`、`IUserRepository` 接口
   - [ ] 在 `core-data` 实现各 Repository 接口，拆分原 `MusicRepository` 的职责
   - [ ] 更新 Hilt 注入配置，绑定接口与实现
   - [ ] 重构所有 Use Case，依赖接口而非具体实现

2. **播放器服务解耦**
   - [ ] 定义 `PlaybackState` 数据类和 `PlaybackEvent` 密封类
   - [ ] 实现 `PlaybackStateManager` 作为状态发布-订阅中心
   - [ ] 重构 `MusicPlayService`，通过状态管理器发布播放状态
   - [ ] 简化 `PlayControlViewModel`，通过状态管理器订阅状态和发送事件
   - [ ] 更新 `MainActivity` 生命周期管理逻辑

3. **统一错误处理机制**
   - [ ] 定义 `MusicPlayerException` 基类和 `ErrorCode` 枚举
   - [ ] 实现各业务场景的具体异常类
   - [ ] 重构所有 Repository 和 Use Case，使用统一异常体系
   - [ ] 实现全局错误拦截和处理逻辑

4. **资源管理优化**
   - [ ] 实现 `AudioEffectManager` 的统一释放机制
   - [ ] 建立统一的图片加载和缓存策略
   - [ ] 实现定时器和延迟任务的统一管理

5. **可测试性提升**
   - [ ] 移除 `PlayControlViewModel` 中的 `Context` 依赖
   - [ ] 为 `MusicRepository` 设计可测试的接口抽象
   - [ ] 实现 Service 绑定逻辑的测试替身

## 一、概述

本文档基于对项目架构的深入分析，提出系统性的优化建议方案。优化目标涵盖模块职责边界、依赖解耦、错误处理统一、性能提升及可测试性增强等核心维度。每项优化均提供具体的问题描述、改进方案、实施步骤及验收标准，确保方案可执行、可追踪、可验证。

当前项目采用多模块 Clean Architecture 架构，技术栈选型合理，但在实际代码层面存在职责边界模糊、依赖关系复杂、错误处理不统一等问题。本方案按优先级分为三个阶段实施，建议在功能迭代中逐步推进，每次重大调整均需建立完善的测试覆盖。

## 二、问题诊断

### 2.1 模块职责边界问题

**问题描述**：`core-data` 模块承担了过多的数据层职责，同时存在模型定义冗余问题。`AiProviderType` 在 `core-data` 和 `core-domain` 两个模块中均有定义，违反 DRY 原则且模糊了领域层与数据层的边界。此外，`MusicRepository` 构造函数包含 13 个参数，同时处理音乐扫描、数据库操作、AI 集成、播放列表管理、用户设置等多种不相关业务逻辑，严重违反单一职责原则。

**影响范围**：代码维护困难、新功能开发效率降低、单元测试设置复杂、模块耦合度高。

**严重程度**：高。

### 2.2 播放器服务耦合问题

**问题描述**：`MainActivity` 与 `MusicPlayService` 通过 `ServiceConnection` 直接绑定，生命周期管理复杂且易出错。`PlayControlViewModel` 中存在大量与播放控制相关的状态管理逻辑，这些逻辑本应由领域层或播放器层处理。当前设计导致测试困难，因为 ViewModel 和 Service 直接交互，需要模拟复杂的绑定过程。

**影响范围**：播放功能稳定性、测试覆盖、代码复用性。

**严重程度**：高。

### 2.3 错误处理不统一问题

**问题描述**：项目使用自定义 `Result` 类包装操作结果，但部分代码仍直接抛出异常。ViewModel 中对错误状态的处理不一致，有的通过 `_errorMessage.value` 设置，有的直接让异常传播。错误类型缺乏统一分类，UI 层难以根据错误类型提供有意义的用户反馈。

**影响范围**：用户体验一致性、错误恢复策略实施、调试效率。

**严重程度**：中。

### 2.4 资源管理潜在泄漏问题

**问题描述**：`AudioEffectManager` 采用单例模式管理音频效果对象，但释放调用散落在多处，容易遗漏。图片加载和资源管理目前没有统一方案。定时器和延迟任务管理不够统一。

**影响范围**：内存占用、应用稳定性、电池续航。

**严重程度**：中。

### 2.5 可测试性不足问题

**问题描述**：`PlayControlViewModel` 构造函数包含 `Context` 等 Android 框架依赖，单元测试需要 Mock 大量对象。`MusicRepository` 测试需要模拟数据库和文件系统。Service 的绑定逻辑测试困难。

**影响范围**：测试覆盖、代码质量、重构安全。

**严重程度**：中。

## 三、优化方案

### 3.1 Repository 职责拆分

**目标**：将臃肿的 `MusicRepository` 拆分为多个专注的 Repository，降低耦合度，提升可维护性和可测试性。

#### 3.1.1 实施方案

**步骤一：定义 Repository 接口**

在 `core-domain` 模块中定义接口，明确各 Repository 的职责边界：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/repository/IMusicRepository.kt
package com.example.hearablemusicplayer.domain.repository

import com.example.hearablemusicplayer.data.repository.Result
import kotlinx.coroutines.flow.Flow

/**
 * 音乐元数据仓储接口
 * 负责音乐的扫描、查询、更新等元数据操作
 */
interface IMusicRepository {
    suspend fun loadMusicFromDevice(): Result<Unit>
    fun isScanning(): Flow<Boolean>
    suspend fun getAllMusic(): List<MusicInfo>
    suspend fun getMusicById(id: Long): MusicInfo?
    suspend fun searchMusic(query: String): List<MusicInfo>
    fun getScanProgress(): Flow<ScanProgress>
}

// core-domain/src/main/java/com/example/hearablemusicplayer/domain/repository/IPlaylistRepository.kt
package com.example.hearablemusicplayer.domain.repository

import com.example.hearablemusicplayer.data.database.MusicInfo
import kotlinx.coroutines.flow.Flow

/**
 * 播放列表仓储接口
 * 负责播放列表的完整生命周期管理
 */
interface IPlaylistRepository {
    suspend fun createPlaylist(name: String): Long
    suspend fun removePlaylist(name: String)
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo>
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>>
    suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String)
    suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long)
    suspend fun resetPlaylistItems(playlistId: Long, playlist: List<MusicInfo>)
}

// core-domain/src/main/java/com/example/hearablemusicplayer/domain/repository/IUserRepository.kt
package com.example.hearablemusicplayer.domain.repository

/**
 * 用户数据仓储接口
 * 负责用户信息、偏好设置等数据管理
 */
interface IUserRepository {
    suspend fun saveUserInfo(userInfo: UserInfo)
    suspend fun getUserInfo(): UserInfo?
    suspend fun updateListeningDuration(duration: Long)
    suspend fun getTotalListeningDuration(): Long
}
```

**步骤二：实现具体 Repository**

在 `core-data` 模块中实现各接口：

```kotlin
// core-data/src/main/java/com/example/hearablemusicplayer/data/repository/MusicRepository.kt
package com.example.hearablemusicplayer.data.repository

import com.example.hearablemusicplayer.domain.repository.IMusicRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val musicDao: MusicDao,
    private val musicExtraDao: MusicExtraDao,
    private val musicAllDao: MusicAllDao,
    private val musicScanner: MusicScanner,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) : IMusicRepository {

    private val _scanProgress = MutableStateFlow(ScanProgress())
    override fun getScanProgress(): Flow<ScanProgress> = _scanProgress

    override suspend fun loadMusicFromDevice(): Result<Unit> {
        return try {
            _scanProgress.value = ScanProgress(isScanning = true)
            musicScanner.scan(_scanProgress)
            _scanProgress.value = ScanProgress(isScanning = false)
            Result.Success(Unit)
        } catch (e: Exception) {
            _scanProgress.value = ScanProgress(isScanning = false)
            Result.Error(e)
        }
    }

    // ... 其他方法实现
}

// core-data/src/main/java/com/example/hearablemusicplayer/data/repository/PlaylistRepository.kt
package com.example.hearablemusicplayer.data.repository

import com.example.hearablemusicplayer.domain.repository.IPlaylistRepository
import com.example.hearablemusicplayer.data.database.PlaylistDao
import com.example.hearablemusicplayer.data.database.PlaylistItemDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val playlistItemDao: PlaylistItemDao
) : IPlaylistRepository {

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insert(Playlist(name = name))
    }

    // ... 其他方法实现
}
```

**步骤三：更新 Hilt 注入配置**

```kotlin
// core-data/src/main/java/com/example/hearablemusicplayer/data/di/RepositoryModule.kt
package com.example.hearablemusicplayer.data.di

import com.example.hearablemusicplayer.data.repository.MusicRepository
import com.example.hearablemusicplayer.data.repository.PlaylistRepository
import com.example.hearablemusicplayer.data.repository.UserRepository
import com.example.hearablemusicplayer.domain.repository.IMusicRepository
import com.example.hearablemusicplayer.domain.repository.IPlaylistRepository
import com.example.hearablemusicplayer.domain.repository.IUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindIMusicRepository(
        musicRepository: MusicRepository
    ): IMusicRepository

    @Binds
    @Singleton
    abstract fun bindIPlaylistRepository(
        playlistRepository: PlaylistRepository
    ): IPlaylistRepository

    @Binds
    @Singleton
    abstract fun bindIUserRepository(
        userRepository: UserRepository
    ): IUserRepository
}
```

**步骤四：更新 Use Case 依赖**

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/usecase/music/LoadMusicFromDeviceUseCase.kt
class LoadMusicFromDeviceUseCase @Inject constructor(
    private val musicRepository: IMusicRepository  // 改为接口依赖
) {
    suspend operator fun invoke(): Result<Unit> {
        return musicRepository.loadMusicFromDevice()
    }
}
```

#### 3.1.2 验收标准

- [ ] 所有 Repository 依赖接口而非具体实现
- [ ] MusicRepository 构造函数参数减少至 5 个以内
- [ ] 每个 Repository 职责单一，不超过 200 行核心代码
- [ ] 单元测试可使用 Mock 替代真实 Repository
- [ ] 现有功能测试用例全部通过

#### 3.1.3 预估工时

4-6 小时（含测试编写）。

### 3.2 播放器服务解耦

**目标**：采用发布-订阅模式解耦 Activity/ViewModel 与 Service 的直接依赖，提升可测试性和代码可维护性。

#### 3.2.1 实施方案

**步骤一：定义播放状态协议**

```kotlin
// core-player/src/main/java/com/example/hearablemusicplayer/player/model/PlaybackState.kt
package com.example.hearablemusicplayer.player.model

import com.example.hearablemusicplayer.data.database.Music

/**
 * 播放状态协议
 * 统一管理所有播放相关状态的发布与订阅
 */
data class PlaybackState(
    val currentMusic: Music? = null,
    val isPlaying: Boolean = false,
    val playbackPosition: Long = 0L,
    val playbackDuration: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isBuffering: Boolean = false,
    val audioSessionId: Int = 0
)

enum class RepeatMode {
    OFF, ONE, ALL
}

// core-player/src/main/java/com/example/hearablemusicplayer/player/model/PlaybackEvent.kt
sealed class PlaybackEvent {
    data class Play(val music: Music) : PlaybackEvent()
    data class Pause(val music: Music) : PlaybackEvent()
    data class Resume(val music: Music) : PlaybackEvent()
    data class SeekTo(val position: Long) : PlaybackEvent()
    data class SkipNext(val music: Music) : PlaybackEvent()
    data class SkipPrevious(val music: Music) : PlaybackEvent()
    data class SetPlaybackMode(val mode: RepeatMode, val shuffle: Boolean) : PlaybackEvent()
    data class PlaybackCompleted(val music: Music) : PlaybackEvent()
    data class Error(val exception: Throwable) : PlaybackEvent()
}
```

**步骤二：创建 PlaybackStateManager**

```kotlin
// core-player/src/main/java/com/example/hearablemusicplayer/player/PlaybackStateManager.kt
package com.example.hearablemusicplayer.player

import com.example.hearablemusicplayer.player.model.PlaybackEvent
import com.example.hearablemusicplayer.player.model.PlaybackState
import com.example.hearablemusicplayer.player.model.RepeatMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 播放状态管理器
 * 负责播放状态的发布与订阅，采用发布-订阅模式解耦组件依赖
 */
@Singleton
class PlaybackStateManager @Inject constructor() {

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlaybackEvent>()
    val events: SharedFlow<PlaybackEvent> = _events.asSharedFlow()

    private val connectionState = MutableStateFlow<ServiceConnectionState>(ServiceConnectionState.Disconnected)

    fun updateState(reducer: PlaybackState.() -> PlaybackState) {
        _state.value = _state.value.reducer()
    }

    fun publishEvent(event: PlaybackEvent) {
        kotlinx.coroutines.GlobalScope.launch {
            _events.emit(event)
        }
    }

    fun updateConnectionState(state: ServiceConnectionState) {
        connectionState.value = state
    }

    fun isConnected(): Boolean = connectionState.value is ServiceConnectionState.Connected
}

sealed class ServiceConnectionState {
    object Disconnected : ServiceConnectionState()
    object Connecting : ServiceConnectionState()
    data class Connected(val service: MusicPlayService?) : ServiceConnectionState()
    data class Error(val message: String) : ServiceConnectionState()
}
```

**步骤三：重构 MusicPlayService**

```kotlin
// core-player/src/main/java/com/example/hearablemusicplayer/player/service/MusicPlayService.kt
@UnstableApi
@AndroidEntryPoint
class MusicPlayService : Service(), PlayControl {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var playbackStateManager: PlaybackStateManager

    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeMediaSession()
        playbackStateManager.updateConnectionState(
            ServiceConnectionState.Connected(this)
        )
    }

    private fun initializePlayer() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                playbackStateManager.updateState {
                    copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        playbackDuration = exoPlayer.duration
                    )
                }
                if (playbackState == Player.STATE_READY) {
                    playbackStateManager.updateState {
                        copy(audioSessionId = exoPlayer.audioSessionId)
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playbackStateManager.updateState { copy(isPlaying = isPlaying) }
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder {
        return MusicPlayServiceBinder(this)
    }

    inner class MusicPlayServiceBinder : Binder() {
        fun getService(): MusicPlayService = this@MusicPlayService
    }
}
```

**步骤四：简化 PlayControlViewModel**

```kotlin
// feature-ui/src/main/java/com/example/hearablemusicplayer/ui/viewmodel/PlayControlViewModel.kt
@HiltViewModel
@UnstableApi
class PlayControlViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val playbackStateManager: PlaybackStateManager,  // 注入状态管理器
    private val playbackHistoryUseCase: PlaybackHistoryUseCase,
    private val timerUseCase: TimerUseCase
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackStateManager.state

    init {
        observePlaybackEvents()
    }

    private fun observePlaybackEvents() {
        viewModelScope.launch {
            playbackStateManager.events.collect { event ->
                when (event) {
                    is PlaybackEvent.PlaybackCompleted -> handlePlaybackCompleted(event)
                    is PlaybackEvent.Error -> handleError(event.exception)
                    else -> { /* 其他事件处理 */ }
                }
            }
        }
    }

    fun playOrResume() {
        if (playbackState.value.currentMusic != null) {
            playbackStateManager.publishEvent(PlaybackEvent.Resume(
                playbackState.value.currentMusic!!
            ))
        } else {
            // 首次播放逻辑
            viewModelScope.launch {
                playCurrentTrack("AutoPlay")
            }
        }
    }

    fun pauseMusic() {
        playbackStateManager.publishEvent(PlaybackEvent.Pause(
            playbackState.value.currentMusic!!
        ))
    }

    private fun handlePlaybackCompleted(event: PlaybackEvent.PlaybackCompleted) {
        viewModelScope.launch {
            playbackHistoryUseCase.recordListeningDuration(
                System.currentTimeMillis() - playStartTime
            )
            playStartTime = 0L
        }
    }

    private fun handleError(exception: Throwable) {
        _errorMessage.value = exception.message ?: "播放错误"
    }
}
```

**步骤五：更新 MainActivity**

```kotlin
// app/src/main/java/com/example/hearablemusicplayer/MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val musicViewModel by viewModels<MusicViewModel>()
    private val playControlViewModel by viewModels<PlayControlViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 延迟绑定 Service，不在 onCreate 中立即绑定
        val intent = Intent(this, MusicPlayService::class.java)
        startService(intent)
    }

    override fun onStart() {
        super.onStart()
        // 在 onStart 时绑定，确保生命周期正确
        bindService(
            Intent(this, MusicPlayService::class.java),
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                    val service = (binder as? MusicPlayService.MusicPlayServiceBinder)?.getService()
                    service?.let {
                        playbackStateManager.updateConnectionState(
                            ServiceConnectionState.Connected(it)
                        )
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    playbackStateManager.updateConnectionState(
                        ServiceConnectionState.Disconnected
                    )
                }
            },
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        // 解绑但不停止服务
        unbindService(connection)
    }
}
```

#### 3.2.2 验收标准

- [ ] ViewModel 中不再直接持有 Service 引用
- [ ] 播放状态通过 StateFlow 订阅获取
- [ ] 支持多客户端同时订阅播放状态
- [ ] Service 生命周期独立于 Activity
- [ ] 单元测试可独立测试 ViewModel 逻辑，无需绑定 Service

#### 3.2.3 预估工时

8-10 小时（含测试编写）。

### 3.3 统一错误处理机制

**目标**：建立统一的异常层次结构和错误处理策略，提升代码健壮性和用户体验一致性。

#### 3.3.1 实施方案

**步骤一：定义异常体系**

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/exception/MusicPlayerException.kt
package com.example.hearablemusicplayer.domain.exception

/**
 * 应用异常基类
 * 所有应用内异常均继承此类，便于统一处理
 */
abstract class MusicPlayerException(
    message: String,
    cause: Throwable? = null,
    val errorCode: ErrorCode
) : Exception(message, cause)

/**
 * 错误代码枚举
 * 定义所有可能的错误类型，便于 UI 层根据错误类型显示相应提示
 */
enum class ErrorCode {
    // 音乐扫描相关
    SCAN_PERMISSION_DENIED,
    SCAN_IO_ERROR,
    SCAN_NO_MUSIC_FOUND,

    // 播放相关
    PLAYBACK_FILE_NOT_FOUND,
    PLAYBACK_DECODE_ERROR,
    PLAYBACK_SERVICE_ERROR,

    // 数据库相关
    DATABASE_INSERT_ERROR,
    DATABASE_QUERY_ERROR,
    DATABASE_UPDATE_ERROR,

    // AI 服务相关
    AI_API_KEY_MISSING,
    AI_API_ERROR,
    AI_TIMEOUT,

    // 网络相关
    NETWORK_UNAVAILABLE,
    NETWORK_TIMEOUT,

    // 通用
    UNKNOWN_ERROR
}

// core-domain/src/main/java/com/example/hearablemusicplayer/domain/exception/MusicScanException.kt
class MusicScanException(
    cause: Throwable,
    errorCode: ErrorCode = ErrorCode.SCAN_IO_ERROR
) : MusicPlayerException("音乐扫描失败: ${cause.message}", cause, errorCode)

// core-domain/src/main/java/com/example/hearablemusicplayer/domain/exception/PlaybackException.kt
class PlaybackException(
    val musicPath: String,
    cause: Throwable,
    errorCode: ErrorCode = ErrorCode.PLAYBACK_FILE_NOT_FOUND
) : MusicPlayerException("播放失败: ${cause.message}", cause, errorCode)

// core-domain/src/main/java/com/com/example/hearablemusicplayer/domain/exception/AIServiceException.kt
class AIServiceException(
    val provider: String,
    cause: Throwable,
    errorCode: ErrorCode = ErrorCode.AI_API_ERROR
) : MusicPlayerException("AI 服务错误: ${cause.message}", cause, errorCode)
```

**步骤二：统一 Result 封装**

```kotlin
// core-data/src/main/java/com/example/hearablemusicplayer/data/repository/Result.kt
package com.example.hearablemusicplayer.data.repository

import com.example.hearablemusicplayer.domain.exception.ErrorCode
import com.example.hearablemusicplayer.domain.exception.MusicPlayerException

/**
 * 操作结果封装
 * 统一处理成功、失败、加载中三种状态
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(
        val exception: MusicPlayerException
    ) : Result<Nothing>()
    object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: MusicPlayerException): Result<Nothing> = Error(exception)
        fun loading(): Result<Nothing> = Loading

        /**
         * 安全的 suspend 函数包装器
         * 自动捕获异常并转换为 Result
         */
        suspend fun <T> safeCall(
            errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR,
            block: suspend () -> T
        ): Result<T> {
            return try {
                Success(block())
            } catch (e: MusicPlayerException) {
                Error(e)
            } catch (e: Exception) {
                Error(MusicPlayerException(
                    e.message ?: "未知错误",
                    e,
                    errorCode
                ))
            }
        }
    }
}
```

**步骤三：创建统一的错误处理组件**

```kotlin
// feature-ui/src/main/java/com/example/hearablemusicplayer/ui/components/ErrorHandler.kt
package com.example.hearablemusicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.hearablemusicplayer.domain.exception.ErrorCode
import com.example.hearablemusicplayer.domain.exception.MusicPlayerException

/**
 * 错误处理组件
 * 统一监听错误状态并显示相应的用户提示
 */
@Composable
fun ErrorHandler(
    error: MusicPlayerException?,
    onErrorConsumed: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    LaunchedEffect(error) {
        error?.let {
            showErrorSnackbar(it, onRetry)
            onErrorConsumed()
        }
    }
}

private fun showErrorSnackbar(
    error: MusicPlayerException,
    onRetry: (() -> Unit)?
) {
    val message = when (error.errorCode) {
        ErrorCode.SCAN_PERMISSION_DENIED -> "请授予存储权限以扫描音乐文件"
        ErrorCode.SCAN_IO_ERROR -> "读取音乐文件时出错，请重试"
        ErrorCode.SCAN_NO_MUSIC_FOUND -> "未找到音乐文件"
        ErrorCode.PLAYBACK_FILE_NOT_FOUND -> "音乐文件不存在或已删除"
        ErrorCode.PLAYBACK_DECODE_ERROR -> "无法播放此音乐文件"
        ErrorCode.NETWORK_UNAVAILABLE -> "网络连接不可用"
        ErrorCode.NETWORK_TIMEOUT -> "网络连接超时，请检查网络设置"
        ErrorCode.AI_API_KEY_MISSING -> "请配置 AI 服务商 API 密钥"
        ErrorCode.AI_API_ERROR -> "AI 服务暂时不可用，请稍后重试"
        else -> error.message ?: "发生未知错误"
    }

    // 实际项目中通过 SnackbarHostState 显示
    // 这里展示错误处理逻辑
}
```

**步骤四：更新 Repository 使用统一错误处理**

```kotlin
// core-data/src/main/java/com/example/hearablemusicplayer/data/repository/MusicRepository.kt
class MusicRepository @Inject constructor(
    private val musicDao: MusicDao,
    private val musicScanner: MusicScanner,
    @ApplicationContext private val context: Context
) {
    suspend fun loadMusicFromDevice(): Result<Unit> {
        return Result.safeCall(ErrorCode.SCAN_IO_ERROR) {
            if (!hasStoragePermission()) {
                throw MusicScanException(
                    SecurityException("Storage permission denied"),
                    ErrorCode.SCAN_PERMISSION_DENIED
                )
            }
            musicScanner.scan()
            Result.Success(Unit)
        }.flatMap { it }
    }
}
```

#### 3.3.2 验收标准

- [ ] 所有业务异常继承自 MusicPlayerException
- [ ] Repository 层统一返回 Result 封装
- [ ] UI 层通过 ErrorHandler 组件统一处理错误
- [ ] 错误消息对用户友好，提供操作建议
- [ ] 单元测试覆盖错误处理逻辑

#### 3.3.3 预估工时

3-4 小时。

### 3.4 资源管理优化

**目标**：建立统一的资源管理机制，防止内存泄漏，优化资源使用效率。

#### 3.4.1 实施方案

**步骤一：创建可关闭的资源管理接口**

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/util/CloseableResource.kt
package com.example.hearablemusicplayer.domain.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 可关闭资源接口
 * 统一管理需要释放的资源，实现自动释放
 */
interface CloseableResource {
    fun release()
}

/**
 * 定时任务管理器
 * 统一管理应用内所有定时任务，防止资源泄漏
 */
class TimerManager {

    private val timers = mutableMapOf<String, TimerTask>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun schedule(
        key: String,
        delayMillis: Long,
        action: suspend () -> Unit
    ): Job {
        cancel(key)
        val task = TimerTask(scope, key, delayMillis, action)
        timers[key] = task
        task.start()
        return task.job
    }

    fun cancel(key: String) {
        timers.remove(key)?.cancel()
    }

    fun cancelAll() {
        timers.values.forEach { it.cancel() }
        timers.clear()
    }

    private class TimerTask(
        private val scope: CoroutineScope,
        private val key: String,
        private val delayMillis: Long,
        private val action: suspend () -> Unit
    ) {
        val job: Job = scope.launch {
            delay(delayMillis)
            action()
        }

        fun cancel() {
            job.cancel()
        }
    }
}

/**
 * 音效资源管理器
 * 使用 Closeable 接口确保资源正确释放
 */
class AudioEffectResourceManager : CloseableResource {

    private val equalizer: Equalizer? = null
    private val bassBoost: BassBoost? = null
    private val virtualizer: Virtualizer? = null
    private val presetReverb: PresetReverb? = null
    private var isReleased = false

    override fun release() {
        if (isReleased) return
        isReleased = true

        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            presetReverb?.release()
        } catch (e: Exception) {
            // 记录日志但不影响其他资源释放
        }
    }

    fun use(block: (AudioEffectResourceManager) -> Unit) {
        try {
            block(this)
        } finally {
            release()
        }
    }
}
```

**步骤二：集成 Lifecycle 自动释放**

```kotlin
// feature-ui/src/main/java/com/example/hearablemusicplayer/ui/util/ResourceLifecycleObserver.kt
package com.example.hearablemusicplayer.ui.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 资源生命周期观察者
 * 自动在 ViewModel 或 Composable 生命周期结束时释放资源
 */
class ResourceLifecycleObserver(
    private vararg val resources: AutoCloseable
) : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                resources.forEach { it.close() }
            }
            else -> { /* 其他事件不处理 */ }
        }
    }

    companion object {
        /**
         * 为 Composable 注册资源生命周期观察者
         */
        @Composable
        fun Register(
            lifecycleOwner: LifecycleOwner,
            vararg resources: AutoCloseable
        ) {
            DisposableEffect(lifecycleOwner) {
                val observer = ResourceLifecycleObserver(*resources)
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
        }
    }
}

// 使用示例
@Composable
fun PlayerScreen(
    viewModel: PlayControlViewModel = hiltViewModel()
) {
    val audioEffectManager = remember { AudioEffectResourceManager() }

    ResourceLifecycleObserver(
        lifecycleOwner = LocalLifecycleOwner.current,
        audioEffectManager
    )

    // ... 页面内容
}
```

**步骤三：优化 AudioEffectManager**

```kotlin
// core-player/src/main/java/com/example/hearablemusicplayer/player/AudioEffectManager.kt
class AudioEffectManager : CloseableResource {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null
    private var audioSessionId: Int = 0
    private var isInitialized = false

    @Synchronized
    override fun release() {
        if (!isInitialized) return
        isInitialized = false

        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            presetReverb?.release()

            equalizer = null
            bassBoost = null
            virtualizer = null
            presetReverb = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio effects", e)
        }
    }

    @Synchronized
    fun initialize(audioSessionId: Int): Boolean {
        if (isInitialized && this.audioSessionId == audioSessionId) {
            return true
        }

        release()
        this.audioSessionId = audioSessionId

        try {
            // 初始化逻辑...
            isInitialized = true
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioEffectManager", e)
            return false
        }
    }

    /**
     * 使用音效管理器，自动释放
     */
    inline fun <T> use(block: (AudioEffectManager) -> T): T {
        try {
            return block(this)
        } finally {
            release()
        }
    }
}
```

#### 3.4.2 验收标准

- [ ] AudioEffectManager 实现 Closeable 接口
- [ ] 所有定时任务通过 TimerManager 管理
- [ ] 资源在组件销毁时自动释放
- [ ] 无内存泄漏（通过 LeakCanary 或 Android Studio Profiler 验证）
- [ ] 音频效果对象在不再使用时及时释放

#### 3.4.3 预估工时

2-3 小时。

### 3.5 模块拆分（可选优化）

**目标**：将臃肿的 `core-data` 模块拆分为更细粒度的模块，降低模块间耦合度，提升编译效率。

#### 3.5.1 实施方案

**模块拆分策略**：

```
current:
  ├── core-data/          (网络、数据库、Repository、模型)
  ├── core-domain/        (Use Case、领域模型)
  ├── core-player/        (播放器核心)
  └── feature-ui/         (UI组件)

target:
  ├── core-data/
  │   ├── core-database/  (Room 数据库、DAO)
  │   ├── core-network/   (Retrofit、API)
  │   └── core-repository/(Repository 实现)
  ├── core-ai/            (AI 服务集成)
  ├── core-domain/
  ├── core-player/
  └── feature-ui/
```

**步骤一：创建新模块目录结构**

```
d:\MyFile\HMP\core-network\
    ├── build.gradle.kts
    ├── src\main\AndroidManifest.xml
    └── java\com\example\hearablemusicplayer\network\
        ├── DeepSeekAPI.kt
        ├── MultiProviderApiAdapter.kt
        └── NetworkModule.kt

d:\MyFile\HMP\core-ai\
    ├── build.gradle.kts
    ├── src\main\AndroidManifest.xml
    └── java\com\example\hearablemusicplayer\ai\
        ├── AiProvider.kt
        ├── DeepSeekProvider.kt
        └── AiProviderFactory.kt
```

**步骤二：更新 settings.gradle.kts**

```kotlin
// settings.gradle.kts
include(
    ":app",
    ":core-data",
    ":core-domain",
    ":core-player",
    ":feature-ui",
    ":core-network",     // 新增
    ":core-ai"           // 新增
)
```

**步骤三：更新模块依赖**

```kotlin
// core-ai/build.gradle.kts
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":core-network"))
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
}

// core-network/build.gradle.kts
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":core-domain"))
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
}
```

#### 3.5.2 验收标准

- [ ] 新模块成功创建并编译通过
- [ ] 原有功能保持正常
- [ ] 模块间依赖关系清晰
- [ ] 编译速度有明显提升

#### 3.5.3 预估工时

6-8 小时（包含模块配置和依赖调整）。

### 3.6 领域层增强

**目标**：完善 Use Case 层封装，将散落在 ViewModel 和 Repository 中的业务逻辑统一到领域层。

#### 3.6.1 实施方案

**步骤一：创建 PlaybackUseCase**

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/usecase/playback/PlaybackUseCase.kt
class PlaybackUseCase @Inject constructor(
    private val playbackStateManager: PlaybackStateManager,
    private val playbackHistoryUseCase: PlaybackHistoryUseCase,
    private val playlistUseCase: ManagePlaylistUseCase
) {
    /**
     * 播放音乐
     */
    suspend fun play(music: Music, source: String = "Unknown"): Result<Unit> {
        return try {
            playbackStateManager.publishEvent(PlaybackEvent.Play(music))
            playbackHistoryUseCase.recordPlaybackStart(music.id, source)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(PlaybackException(music.path, e))
        }
    }

    /**
     * 暂停播放
     */
    suspend fun pause(): Result<Unit> {
        return try {
            val currentState = playbackStateManager.state.value
            val duration = System.currentTimeMillis() - playStartTime
            if (duration > 0) {
                playbackHistoryUseCase.recordListeningDuration(duration)
            }
            playbackStateManager.publishEvent(PlaybackEvent.Pause(
                currentState.currentMusic!!
            ))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(PlaybackException("", e))
        }
    }

    /**
     * 切换播放状态
     */
    suspend fun togglePlayPause(): Result<Unit> {
        val currentState = playbackStateManager.state.value
        return if (currentState.isPlaying) {
            pause()
        } else {
            if (currentState.currentMusic != null) {
                playbackStateManager.publishEvent(PlaybackEvent.Resume(
                    currentState.currentMusic!!
                ))
                Result.Success(Unit)
            } else {
                Result.Error(PlaybackException("", IllegalStateException("No music selected")))
            }
        }
    }

    /**
     * 播放下一首
     */
    suspend fun skipNext(): Result<Unit> {
        return try {
            val currentState = playbackStateManager.state.value
            val nextMusic = playlistUseCase.getNextTrack(
                currentState.currentMusic?.id,
                currentState.shuffleEnabled,
                currentState.repeatMode
            )
            nextMusic?.let {
                play(it, "SkipNext")
            } ?: Result.Error(PlaybackException("", IllegalStateException("No next track")))
        } catch (e: Exception) {
            Result.Error(PlaybackException("", e))
        }
    }

    /**
     * 播放上一首
     */
    suspend fun skipPrevious(): Result<Unit> {
        return try {
            val currentState = playbackStateManager.state.value
            val prevMusic = playlistUseCase.getPreviousTrack(
                currentState.currentMusic?.id,
                currentState.shuffleEnabled,
                currentState.repeatMode
            )
            prevMusic?.let {
                play(it, "SkipPrevious")
            } ?: Result.Error(PlaybackException("", IllegalStateException("No previous track")))
        } catch (e: Exception) {
            Result.Error(PlaybackException("", e))
        }
    }

    companion object {
        private var playStartTime: Long = 0L
    }
}
```

**步骤二：更新 PlayControlViewModel**

```kotlin
// feature-ui/src/main/java/com/example/hearablemusicplayer/ui/viewmodel/PlayControlViewModel.kt
@HiltViewModel
class PlayControlViewModel @Inject constructor(
    private val playbackUseCase: PlaybackUseCase,  // 注入 PlaybackUseCase
    private val timerUseCase: TimerUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackStateManager.state

    fun playOrResume() {
        viewModelScope.launch {
            when (val result = playbackUseCase.togglePlayPause()) {
                is Result.Success -> { /* 状态已通过 PlaybackStateManager 更新 */ }
                is Result.Error -> {
                    _errorMessage.value = result.exception.message
                }
                is Result.Loading -> { /* 显示加载状态 */ }
            }
        }
    }

    fun pauseMusic() {
        viewModelScope.launch {
            playbackUseCase.pause()
        }
    }

    fun skipNext() {
        viewModelScope.launch {
            playbackUseCase.skipNext()
        }
    }

    fun skipPrevious() {
        viewModelScope.launch {
            playbackUseCase.skipPrevious()
        }
    }
}
```

#### 3.6.2 验收标准

- [ ] 所有播放控制逻辑通过 PlaybackUseCase 执行
- [ ] ViewModel 代码量减少 30% 以上
- [ ] 业务逻辑与 UI 逻辑分离清晰
- [ ] PlaybackUseCase 可独立测试

#### 3.6.3 预估工时

4-5 小时。

## 四、实施路线图

### 4.1 第一阶段：基础优化（高优先级）

**目标**：修复最影响代码质量和稳定性的问题，为后续优化奠定基础。

| 序号 | 优化项 | 预估工时 | 依赖项 | 验收标准 |
|------|--------|----------|--------|----------|
| 1.1 | Repository 职责拆分 | 4-6h | 无 | 所有 Repository 依赖接口 |
| 1.2 | 统一错误处理机制 | 3-4h | 1.1 | 异常继承体系建立 |
| 1.3 | 资源管理优化 | 2-3h | 无 | 无内存泄漏 |

**阶段验收**：所有单元测试通过，功能测试通过，无回归问题。

### 4.2 第二阶段：核心重构（中优先级）

**目标**：重构核心架构，提升代码可维护性和可测试性。

| 序号 | 优化项 | 预估工时 | 依赖项 | 验收标准 |
|------|--------|----------|--------|----------|
| 2.1 | 播放器服务解耦 | 8-10h | 1.1, 1.2 | ViewModel 不直接依赖 Service |
| 2.2 | 领域层增强 | 4-5h | 1.1 | PlaybackUseCase 完整实现 |

**阶段验收**：播放器功能测试通过，多客户端订阅测试通过，UI 测试通过。

### 4.3 第三阶段：架构演进（低优先级）

**目标**：优化模块结构，提升开发效率和编译速度。

| 序号 | 优化项 | 预估工时 | 依赖项 | 验收标准 |
|------|--------|----------|--------|----------|
| 3.1 | 模块拆分 | 6-8h | 1.1 | 模块编译通过 |
| 3.2 | 导航架构优化 | 4-6h | 无 | 导航逻辑解耦 |

**阶段验收**：项目编译速度提升，模块依赖关系清晰。

## 五、风险评估与应对

### 5.1 技术风险

**风险一：重构引入回归问题**

**可能性**：中

**影响**：高

**应对措施**：
- 建立完整的自动化测试覆盖后再进行重构
- 采用渐进式重构，每次只修改一个小部分
- 每日构建验证，确保问题及时发现
- 保留原有代码的备份分支

**风险二：编译时间增加**

**可能性**：低

**影响**：中

**应对措施**：
- 使用 Gradle 构建缓存
- 合理配置模块依赖，避免循环依赖
- 对不常变动的模块使用静态依赖

### 5.2 进度风险

**风险一：实际工时超出预估**

**可能性**：中

**影响**：中

**应对措施**：
- 将大任务拆分为更小的子任务
- 预留 20% 的缓冲时间
- 定期评估进度，及时调整计划
- 优先完成核心功能优化

### 5.3 质量风险

**风险三：测试覆盖不足**

**可能性**：高

**影响**：中

**应对措施**：
- 将测试编写纳入每个任务的验收标准
- 使用代码覆盖率工具监控测试质量
- 建立 CI 流水线强制执行测试

## 六、验收标准总览

### 6.1 代码质量标准

- [ ] 代码重复率低于 3%
- [ ] 圈复杂度平均低于 10
- [ ] 类、方法注释覆盖率超过 80%
- [ ] 关键业务逻辑单元测试覆盖率达到 90%

### 6.2 架构一致性标准

- [ ] 所有 Repository 依赖接口
- [ ] Use Case访问 Android 框架类
- [ ] ViewModel 不包含业务逻辑
- 不直接 [ ] 组件间通信通过协议或 StateFlow

### 6.3 性能标准

- [ ] 启动时间低于 2 秒
- [ ] 内存占用稳定在 200MB 以内
- [ ] 无内存泄漏（LeakCanary 检测通过）
- [ ] 数据库查询平均响应时间低于 100ms

### 6.4 可测试性标准

- [ ] 单元测试可独立运行，无需 Android 框架
- [ ] ViewModel 测试可使用 Mock Repository
- [ ] Service 测试可使用 Fake 实现
- [ ] 集成测试覆盖核心用户场景

## 七、附录

### 7.1 相关文档

- [README.md](README.md)：项目总体介绍和功能说明
- [ROADMAP.md](ROADMAP.md)：项目演进路线和版本规划
- [DEVELOP.md](DEVELOP.md)：开发环境配置指南

### 7.2 参考资料

- [Android 官方架构指南](https://developer.android.com/topic/libraries/architecture)
- [Hilt 官方文档](https://dagger.dev/hilt/)
- [Kotlin 协程最佳实践](https://kotlinlang.org/docs/coroutines-basics.html)
- [Room 数据库指南](https://developer.android.com/training/data-storage/room)

### 7.3 变更记录

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|----------|------|
| 1.0 | 2026-01-08 | 初版优化方案 | WLYB |

---

**文档版本**：1.0
**最后更新**：2026-01-08
**维护者**：WLYB
