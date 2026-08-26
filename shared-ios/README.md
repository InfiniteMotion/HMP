# shared-ios — iOS 聚合框架（方向 A Phase 1 / A1）

把 `:shared`（业务域+数据）与 `:shared-ui`（Compose 共享 UI）链接为**单一 static framework**
（baseName `sharedIos`），经 CocoaPods 以 `pod 'shared_ios'` 导入 iOS 工程，Swift 统一
`import sharedIos`。

## 为什么需要聚合

iOS 上直接双框架（shared + sharedUi 各一个 pod）存在两个结构性死结：

1. **双静态框架 duplicate symbol**：两个静态框架各内联一份 Kotlin/Native 运行时与传递库
   （stdlib / okio / kotlinx 等），app 链接期报 duplicate symbol。
2. **动态框架的 Koin 全局分裂**：sharedUi 改动态后其 dylib 会自带一份 shared 的代码副本，
   Koin 全局/类身份分裂（`KoinApplication has not been started`、`as!` 跨界强转语义混乱）。

聚合框架解决：所有代码（shared + shared-ui + 传递依赖）只链接一次，`startKoin` 与
playback 状态均在单一运行时内。

## 关键细节

- **export**：`framework { export(project(":shared")); export(project(":shared-ui")) }`
  并把两者声明为 `api` 依赖——K/N 默认只导出本编译单元的 API，依赖 klib 需显式 export
  才能进入 ObjC 头（Swift 侧类型才可见）。
- **锚点文件**：`src/iosMain/.../Anchor.kt`——空源码模块的 linkPod 任务会 NO-SOURCE 跳过，
  需要一份源码触发框架链接。
- **Kotlin 2.3 产物布局**：`linkPod{Debug|Release}FrameworkIos{...}` 输出在
  `build/bin/<target>/podDebugFramework/`，而 podspec（`vendored_frameworks`）指向旧布局
  `build/cocoapods/framework/`；Podfile 脚本阶段负责同步（详见 `ios/Podfile` post_install）。
- **Compose 资源**：shared-ui 的 composeResources 由 Podfile 脚本阶段按
  `<bundle>/compose-resources/composeResources/<Res包>/` 布局聚合打进 app
  （CMP iOS 加载器要求的 `com.hearablemusic.player.ui.generated.resources/...` 结构）。
- **iosX64**：未启用（`org.jetbrains.androidx.navigation3:navigation3-ui` 无 ios_x64 构件）。

## 构建命令

```bash
./gradlew :shared-ios:generateDummyFramework :shared-ios:podspec
cd ios && pod install
xcodebuild -workspace ios/HMP.xcworkspace -scheme HMP -configuration Debug \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' \
  ARCHS=arm64 ONLY_ACTIVE_ARCH=YES build
```