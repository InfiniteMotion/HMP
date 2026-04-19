# Consumer ProGuard rules for feature-ui module

# Keep ViewModel classes visible to app module
-keep public class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Keep Composable functions visible
-keep @androidx.compose.runtime.Composable public class ** { 
    public *;
}
