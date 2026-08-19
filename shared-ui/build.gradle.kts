plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.kotlin.multiplatform.library")
    // Res.* 访问器生成（资源 A1 配套，方案 §6/映射表 §5）：带入的组件依赖与下方手动依赖同坐标同版本，自动去重
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    // AGP 9 内置 Kotlin 支持的新式 KMP 库 DSL（与 :shared 模块同模式）
    android {
        namespace = "com.hearablemusic.player.ui"
        compileSdk { version = release(36) }
        // res 支持（R 类生成）：AGP 9 KMP 库插件默认关闭，需显式 opt-in（kotlinlang.org AGP 9 迁移指南）
        androidResources {
            enable = true
        }
        // AGP 9 KMP DSL：属性赋值形式（lambda 形式的 version 字段要 MinSdkVersion 类型，不可用 Int）
        minSdk = 33
    }

    sourceSets {
        // 第 1 步：设计系统+主题已迁 commonMain，compose 基础库随之上移（androidMain 自动继承）
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.kotlinx.coroutines)
            // Res 访问器运行时（composeResources 配套，与 :desktop:feature-ui 同用法）
            implementation(compose.components.resources)

            // 依赖替换（方案 §2.2 / C17）：androidx.compose → Compose Multiplatform（同包名，代码 import 不变）
            // 版本 1.9.3：AGP 9.0 要求 CMP ≥1.9.3（官方兼容矩阵）；material3 独立版本见 libs.versions.toml
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.animation)

            // navigation3（KMP）：第 1 步空 NavHost 壳（AppRoot）在 commonMain
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.jetbrains.navigation3.ui)

            // nav3 NavKey @Serializable 序列化支持（AppRoot 占位路由）
            implementation(libs.kotlinx.serialization.json)

            // 第 2a 步：Tab 壳组件（BottomFusionBar/TabPageIndicator/AlbumCover/HazeIntensity）迁入 commonMain
            implementation(libs.coil.compose.kmp)        // Coil3 AsyncImage（KMP）
            implementation(libs.haze)                    // 底部融合栏毛玻璃（CMP 兼容库）
            implementation(libs.haze.materials)
        }

        androidMain.dependencies {
            implementation(project(":shared"))
            api(project(":android:core-player"))

            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)

            implementation(libs.jetbrains.compose.ui.tooling.preview)

            // lifecycle / navigation3 → KMP 分发版（包名保持 androidx.*）
            // navigation3-runtime 无 JetBrains 分发，用 google 坐标（本身即 KMP，JetBrains UI 亦依赖它）
            // nav3 runtime/ui 已上移 commonMain（AppRoot 空壳）；此处仅留旧 UI 所需 lifecycle decorator
            implementation(libs.jetbrains.lifecycle.viewmodel.compose)
            implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)

            // media3 仅剩 @UnstableApi 注解使用，待第 3 步 PlaybackController 接线时剥离（C4/C12）
            implementation(libs.androidx.media3.common)

            // Android 端网络图片加载注册（coil3 核心 API 已上移 commonMain）
            implementation(libs.coil.network.ktor3)

            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Material3 Adaptive → CMP 分发版
            implementation(libs.jetbrains.material3.adaptive)
            implementation(libs.jetbrains.material3.adaptive.layout)
            implementation(libs.jetbrains.material3.adaptive.navigation)

            // 原为 debugImplementation，KMP 源集无 debug 变体，并入 androidMain
            implementation(libs.jetbrains.compose.ui.tooling)
        }

        androidUnitTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// composeResources 资源访问器：与 desktop 旧层风格一致（<包名>.generated.resources），public 供 app 壳/desktop 后续引用
compose.resources {
    publicResClass = true
    packageOfResClass = "com.hearablemusic.player.ui.generated.resources"
}
