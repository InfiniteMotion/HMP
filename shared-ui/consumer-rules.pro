# Consumer ProGuard rules for feature-ui module
# 传递给消费此库的模块（如 app）

# ViewModels（Koin 构造函数引用）
-keep public class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
