# Add project specific ProGuard rules here.

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class com.example.hearablemusicplayer.ui.viewmodel.** { *; }

# Keep UI Event classes
-keep class com.example.hearablemusicplayer.ui.viewmodel.UiEvent { *; }
-keep class com.example.hearablemusicplayer.ui.viewmodel.UiEvent$* { *; }

# Keep Composable functions
-keep @androidx.compose.runtime.Composable class ** { *; }
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable *;
}

# Navigation Compose
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Palette
-keep class androidx.palette.** { *; }
-dontwarn androidx.palette.**
