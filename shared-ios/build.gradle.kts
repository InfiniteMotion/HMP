plugins {
    alias(libs.plugins.kotlin.multiplatform)
    // compose 插件：聚合 shared-ui 的 Compose klib 与 composeResources 处理
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    kotlin("native.cocoapods")
}

/**
 * iOS 聚合框架（方向 A Phase 1 / A1）。
 *
 * 为什么需要聚合：app 同时链接 shared（静态）与 shared-ui（静态）时，
 * 两个静态框架各内联一份 Kotlin/Native 运行时与传递库（stdlib/okio/kotlinx 等），
 * app 链接期 duplicate symbol；把 shared-ui 改为动态框架又会把 shared 的代码复制进
 * dylib（Koin 全局/类身份分裂，KoinApplication has not been started）。
 * 故按「单一框架」原则由本模块把 shared + shared-ui 两个 klib 链接为同一个框架，
 * 所有代码只出现一次；Swift 侧 import sharedIos 即可。
 *
 * 依赖方向：只含 iOS targets（iosArm64 + iosSimulatorArm64；navigation3-ui 无
 * iosX64 构件，见 shared-ui 注释）。
 */
kotlin {
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "HMP unified iOS framework (shared + shared-ui)"
        homepage = "https://github.com/hmp/shared-ios"
        version = "1.0.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../ios/Podfile")
        framework {
            baseName = "sharedIos"
            isStatic = true
            // 导出依赖模块的公开 API 面：框架头文件需包含 shared/shared-ui 的类型与顶层函数
            // （K/N 默认只导出本编译单元的 API，依赖 klib 需显式 export 才能出现在 ObjC 头中）
            export(project(":shared"))
            export(project(":shared-ui"))
        }
    }

    sourceSets {
        iosMain.dependencies {
            // api：export(project(...)) 要求依赖为 API 依赖（依赖的类型/顶层函数进入框架头）
            api(project(":shared"))
            api(project(":shared-ui"))
        }
    }
}
