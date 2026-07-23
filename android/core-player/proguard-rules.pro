# ProGuard rules for core-player module
# 库自身的 consumer rules 已处理 Media3/Coil/Koin/Coroutines 的保留规则

# Media3 Service（Manifest 通过类名访问）
-keep class com.hearablemusic.player.player.service.** { *; }
