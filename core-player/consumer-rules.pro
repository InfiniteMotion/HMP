# Consumer ProGuard rules for core-player module
# These rules will be automatically applied to consumers of this library

# 确保Media3相关类不被混淆
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
