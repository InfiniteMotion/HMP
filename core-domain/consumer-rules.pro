# Consumer ProGuard rules for core-domain module

# Keep Use Case classes visible to feature-ui module
-keep public class com.example.hearablemusicplayer.domain.usecase.** {
    public *;
}
