# Consumer ProGuard rules for core-player module
# 传递给消费此库的模块（如 app）

# Media3 Service（Manifest 通过类名访问）
-keep class com.hearablemusic.player.player.service.** { *; }
