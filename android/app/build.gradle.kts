import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.hearablemusicplayer"
    compileSdk {
        version = release(36)
    }

    // 统一签名配置 - 解决不同环境编译APK无法无缝安装的问题
    signingConfigs {
        create("unified") {
            val ksFile = file("hmp-unified-key.jks")
            val ksPassword = System.getenv("KEYSTORE_PASSWORD") ?: providers.gradleProperty("KEYSTORE_PASSWORD").getOrElse("hmp123456")
            val kAlias = System.getenv("KEY_ALIAS") ?: "hmpkey"
            val kPassword = System.getenv("KEY_PASSWORD") ?: providers.gradleProperty("KEY_PASSWORD").getOrElse("hmp123456")
            storeFile = ksFile
            storePassword = ksPassword
            keyAlias = kAlias
            keyPassword = kPassword
        }
    }

    defaultConfig {
        applicationId = "com.example.hearablemusicplayer"
        minSdk = 33
        targetSdk = 36
        versionCode = project.findProperty("hmp.versionCode")?.toString()?.toIntOrNull() ?: 51000
        versionName = project.findProperty("hmp.versionName")?.toString() ?: "5.10.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 内置 AI API Key（免费体验 / 付费模式共用）
        // 本地开发: 在 gradle.properties 中填写（不提交 Git）
        // CI 构建: GitHub Actions secrets 自动注入
        val builtInEndpoint = providers.gradleProperty("BUILT_IN_AI_ENDPOINT").getOrElse("https://api.deepseek.com/v1")
        val builtInKey = providers.gradleProperty("BUILT_IN_AI_API_KEY").getOrElse("")
        val builtInModel = providers.gradleProperty("BUILT_IN_AI_MODEL").getOrElse("deepseek-chat")
        buildConfigField("String", "BUILT_IN_AI_ENDPOINT", "\"$builtInEndpoint\"")
        buildConfigField("String", "BUILT_IN_AI_API_KEY", "\"$builtInKey\"")
        buildConfigField("String", "BUILT_IN_AI_MODEL", "\"$builtInModel\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("unified")
        }
        debug {
            signingConfig = signingConfigs.getByName("unified")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(project(":shared"))
    implementation(project(":android:feature-ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.media3.common)

    implementation(libs.koin.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}