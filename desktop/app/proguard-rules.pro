# ╔══════════════════════════════════════════════════════════════════╗
# ║  HMP Desktop — ProGuard / R8 Configuration                   ║
# ║  Keep rules for Compose Desktop, Koin, Room, Coil, Ktor, etc ║
# ╚══════════════════════════════════════════════════════════════════╝

# ── Compose Desktop / Skiko ──────────────────────────────────────
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }

# ── Koin DI ──────────────────────────────────────────────────────
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module { *; }
-keepclassmembers class * {
    @org.koin.** <fields>;
}

# ── Kotlin Coroutines ───────────────────────────────────────────
-keep class kotlinx.coroutines.** { *; }

# ── DataStore Preferences ───────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── Room Database ────────────────────────────────────────────────
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase$Callback { *; }
-keep class * extends androidx.room.RoomDatabase$DestructiveMigrationCallback { *; }

# ── Coil 3 Image Loading ────────────────────────────────────────
-keep class coil3.** { *; }
-keep class coil3.compose.** { *; }

# ── Ktor HTTP Client ────────────────────────────────────────────
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ── kotlinx.serialization ────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class com.hmp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Jaudiotagger (music tag parsing) ─────────────────────────────
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**

# ── Haze (frosted glass effects) ────────────────────────────────
-keep class dev.chrisbanes.haze.** { *; }

# ── App domain / data / player models ────────────────────────────
-keep class com.hmp.domain.** { *; }
-keep class com.hmp.data.** { *; }
-keep class com.hmp.desktop.player.** { *; }
-keep class com.hmp.desktop.SingleInstanceGuard { *; }
-keep class com.hmp.desktop.SystemTrayManager { *; }
-keep class com.hmp.di.** { *; }

# ── AWT / Swing (system tray, file dialog) ──────────────────────
-keep class java.awt.** { *; }
-keep class javax.swing.** { *; }
-keep class javax.sound.** { *; }
-dontwarn java.awt.**
-dontwarn javax.swing.**

# ── General warnings to suppress ─────────────────────────────────
-dontwarn sun.misc.Unsafe
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**
