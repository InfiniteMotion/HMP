# ============================================================
# R8 Optimization Rules — HMP Android App
# ============================================================

# --- R8 优化标志 ---
# 启用类重新打包（合并包，减小体积）
-repackageclasses ''
# 允许 R8 修改访问修饰符以优化代码
-allowaccessmodification

# --- 调试堆栈跟踪 ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlin 元数据（Koin/序列化等依赖） ---
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*, Signature, InnerClasses, Exceptions

# --- 枚举 ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- 应用入口（Manifest 引用） ---
-keep class com.hearablemusic.player.MusicApplication { *; }
-keep class com.hearablemusic.player.MainActivity { *; }

# --- Kotlinx Serialization ---
# 保留 @Serializable 类的 Companion 和 serializer 方法
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
-keepclasseswithmembers @kotlinx.serialization.Serializable class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# --- Room ---
# Entity 字段被生成的 SQL 引用，必须保留
-keep class com.hmp.data.database.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# --- Media3 Service（Manifest 引用） ---
-keep class com.hearablemusic.player.player.service.** { *; }

# --- Jaudiotagger（音乐标签解析/写入） ---
# Jaudiotagger 在桌面端使用 javax.imageio，Android 无此包（仅编译期引用，不影响运行）
-dontwarn javax.imageio.ImageIO
-dontwarn javax.imageio.stream.ImageInputStream

# --- ViewModel（Koin 构造函数引用） ---
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }

# --- 抑制桌面端警告 ---
-dontwarn java.awt.**
-dontwarn javax.swing.**
