plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.kotlin.multiplatform.library")
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
        // 第 0 步：commonMain 仅 platform 接口，需 shared 领域类型 + coroutines StateFlow
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.kotlinx.coroutines)
        }

        androidMain.dependencies {
            implementation(project(":shared"))
            api(project(":android:core-player"))

            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)

            // 依赖替换（方案 §2.2 / C17）：androidx.compose → Compose Multiplatform 1.8.2（同包名，代码 import 不变）
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.ui.tooling.preview)
            implementation(libs.jetbrains.compose.animation)

            // lifecycle / navigation3 → KMP 分发版（包名保持 androidx.*）
            // navigation3-runtime 无 JetBrains 分发，用 google 坐标（本身即 KMP，JetBrains UI 亦依赖它）
            implementation(libs.jetbrains.lifecycle.viewmodel.compose)
            implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.jetbrains.navigation3.ui)

            implementation(libs.kotlinx.serialization.json)

            // media3 仅剩 @UnstableApi 注解使用，待第 3 步 PlaybackController 接线时剥离（C4/C12）
            implementation(libs.androidx.media3.common)

            // Coil2 → Coil3（KMP）
            implementation(libs.coil.compose.kmp)
            implementation(libs.coil.network.ktor3)

            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.haze)
            implementation(libs.haze.materials)

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
