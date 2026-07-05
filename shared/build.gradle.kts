plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.serialization)
    kotlin("native.cocoapods")
}

kotlin {
    android {
        namespace = "com.hmp.shared"
        compileSdk { version = release(36) }
    }

    jvm("desktop")

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xexpect-actual-classes"))
    }

    cocoapods {
        summary = "HMP shared module"
        homepage = "https://github.com/hmp/shared"
        version = "1.0.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../ios/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.ktor.core)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.logging)
            implementation(libs.kotlinx.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.ktor.okhttp)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.jaudiotagger)
            implementation("com.belerweb:pinyin4j:2.5.1")
        }
        iosMain.dependencies {
            implementation(libs.ktor.darwin)
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.java)
                implementation(libs.jaudiotagger)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

// Copy shared icons to iOS project bundle resources
val copyIconsToIos by tasks.registering(Copy::class) {
    from("$projectDir/src/commonMain/resources/icons")
    into("$projectDir/../ios/HMP/HMP/icons")
}

tasks.matching { it.name == "compileKotlinIosSimulatorArm64" }.configureEach {
    finalizedBy(copyIconsToIos)
}
tasks.matching { it.name == "compileKotlinIosArm64" }.configureEach {
    finalizedBy(copyIconsToIos)
}
tasks.matching { it.name == "compileKotlinIosX64" }.configureEach {
    finalizedBy(copyIconsToIos)
}
