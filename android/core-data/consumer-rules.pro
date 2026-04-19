# Consumer ProGuard rules for core-data module
# These rules will be automatically applied to consumers of this library

# 确保Room生成的代码不被混淆
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
