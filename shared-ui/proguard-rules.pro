# ProGuard rules for feature-ui module
# 库自身的 consumer rules 已处理 Compose/Coil/Palette/Navigation 的保留规则

# ViewModels（Koin 构造函数引用）
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keep class com.hearablemusic.player.ui.viewmodel.** { *; }
