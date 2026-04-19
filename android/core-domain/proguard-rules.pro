# Add project specific ProGuard rules here.

# Keep all Use Cases
-keep class com.example.hearablemusicplayer.domain.usecase.** { *; }

# Keep Use Case constructors for Hilt injection
-keepclassmembers class * extends com.example.hearablemusicplayer.domain.usecase.** {
    public <init>(...);
}
