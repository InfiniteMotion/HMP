# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Compose rules
-keepclassmembers class * extends androidx.compose.runtime.Composable {
    *;
}
-keep class androidx.compose.** { *; }

# Koin rules
-keep class org.koin.** { *; }

# Media3 rules
-keep class androidx.media3.** { *; }

# Kotlin serialization
-keep class kotlinx.serialization.** { *; }

# Room rules
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Music info classes
-keep class com.hearablemusic.player.domain.music.** { *; }

# Navigation3 rules
-keep class androidx.navigation3.** { *; }

# Preserve annotation classes
-keepattributes *Annotation*

# Preserve generic signatures
-keepattributes Signature

# Preserve exceptions
-keepattributes Exceptions

# Preserve inner classes
-keepattributes InnerClasses

# Preserve enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve Kotlin metadata
-keep class kotlin.Metadata { *; }

# Preserve Parcelable implementations
-keep class * implements android.os.Parcelable { *; }

# Preserve Serializable implementations
-keep class * implements java.io.Serializable { *; }