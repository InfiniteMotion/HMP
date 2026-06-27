# HMP Android Google Play 上架指导手册

**版本**: 1.0  
**最后更新**: 2026年6月24日  
**适用范围**: HMP (Hearable Music Player) Android版本 Google Play上架

---

## 目录

1. [项目现状分析](#一项目现状分析)
2. [上架前准备工作](#二上架前准备工作)
3. [开发者账号注册](#三开发者账号注册)
4. [应用配置修改](#四应用配置修改)
5. [打包与签名](#五打包与签名)
6. [商店素材准备](#六商店素材准备)
7. [Google Play Console配置](#七google-play-console配置)
8. [测试轨道发布](#八测试轨道发布)
9. [生产发布流程](#九生产发布流程)
10. [审核与拒审处理](#十审核与拒审处理)
11. [发布后维护](#十一发布后维护)
12. [检查清单](#十二检查清单)

---

## 一、项目现状分析

### 1.1 当前配置概览

| 配置项 | 当前值 | 状态 | 建议 |
|--------|--------|------|------|
| applicationId | `com.example.hearablemusicplayer` | ⚠️ 需要修改 | 改为正式包名，如 `com.hmp.musicplayer` |
| compileSdk | 36 (Android 16) | ✅ 符合要求 | - |
| targetSdk | 36 (Android 16) | ✅ 符合要求 | 2026年要求，建议保持 |
| minSdk | 33 (Android 13) | ⚠️ 较高 | 考虑降至26+以覆盖更多用户 |
| versionCode | 51000 | ✅ 正常 | 按规范递增 |
| versionName | 5.10.0 | ✅ 正常 | - |
| 签名配置 | 已配置unified签名 | ⚠️ 需升级 | 使用生产密钥，不要用示例密码 |
| 构建格式 | 支持AAB输出 | ✅ 符合要求 | 已有`releaseAndroid`任务 |

### 1.2 权限配置现状

已在 `AndroidManifest.xml` 中声明的权限：

| 权限 | 用途 | 合规性 |
|------|------|--------|
| `READ_MEDIA_AUDIO` | 读取音频文件 | ✅ 正确（Android 13+推荐） |
| `INTERNET` | 网络访问 | ✅ 需要（封面/歌词下载等） |
| `FOREGROUND_SERVICE` | 前台服务 | ✅ 需要 |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | 媒体播放前台服务 | ✅ 正确（Android 14+必需） |
| `POST_NOTIFICATIONS` | 发送通知 | ✅ 正确（Android 13+必需） |
| `WAKE_LOCK` | 保持CPU运行 | ✅ 需要 |
| `BLUETOOTH` | 蓝牙连接 | ⚠️ 建议添加`BLUETOOTH_CONNECT` |
| `VIBRATE` | 震动反馈 | ✅ 可选 |
| `MODIFY_AUDIO_SETTINGS` | 调整音频设置 | ✅ 需要 |

### 1.3 需要补充的权限兼容配置

当前缺少Android 12及以下版本的存储权限兼容配置，需要添加。

---

## 二、上架前准备工作

### 2.1 时间规划（建议预留4-6周）

| 阶段 | 时间 | 说明 |
|------|------|------|
| 开发者账号注册 | 1-2周 | 含邓白氏编码申请（如注册公司账号） |
| 应用配置修改与测试 | 1周 | 包名、密钥、权限适配等 |
| 商店素材准备 | 1周 | 图标、截图、描述、隐私政策等 |
| 内部测试 | 3-5天 | 团队内部验证 |
| 封闭测试 | 14天 | 个人账号强制要求（12人+14天） |
| 审核发布 | 3-7天 | 首次审核可能更长 |

### 2.2 必备材料清单

#### 账号注册材料

**个人账号**：
- [ ] Google账号（Gmail，建议养号1-3个月）
- [ ] 身份证/护照/驾驶证（高清扫描件）
- [ ] Visa/MasterCard双币信用卡（支持境外支付）
- [ ] 手机号（推荐香港/海外号码，国内号码也可）
- [ ] 稳定的网络环境（纯净IP）

**公司账号（推荐）**：
- [ ] 上述个人账号所有材料
- [ ] 公司营业执照
- [ ] 邓白氏编码（D-U-N-S Number）
- [ ] 公司官网
- [ ] 企业邮箱
- [ ] 授权代表人身份证件

#### 应用上架材料

**图形资源**：
- [ ] 应用图标（512x512 PNG，32位带alpha）
- [ ] 功能图形（1024x500 PNG/JPG）
- [ ] 手机截图（至少2张，推荐8张，1080x1920）
- [ ] 平板截图（推荐4张）
- [ ] 宣传视频（YouTube链接，可选但推荐）

**文本内容**：
- [ ] 应用名称（≤50字符）
- [ ] 简短描述（≤80字符）
- [ ] 完整描述（≤4000字符）
- [ ] 发行说明
- [ ] 隐私政策URL（HTTPS）
- [ ] 开发者联系邮箱
- [ ] 官方网站（推荐）

**技术文件**：
- [ ] 签名密钥库（.jks文件）
- [ ] Release AAB文件
- [ ] 测试账号（如应用需要登录）

---

## 三、开发者账号注册

### 3.1 账号类型选择建议

| 对比项 | 个人账号 | 公司账号 |
|--------|----------|----------|
| 费用 | $25一次性 | $25一次性 |
| 稳定性 | 一般 | 较高 |
| 品牌展示 | 个人姓名 | 公司名称 |
| 封测要求 | 12人+14天 | 通常豁免 |
| 所需材料 | 身份证 | 营业执照+邓白氏编码 |
| 推荐场景 | 个人开发者、测试 | 商业化项目、长期运营 |

**建议**：如果是正式产品，优先注册公司账号。

### 3.2 注册流程详解

#### Step 1：准备Google账号
1. 创建或使用已有Gmail账号
2. 绑定手机号和备用邮箱
3. 建议提前1-3个月养号（正常使用Gmail、搜索等）

#### Step 2：邓白氏编码申请（公司账号）
1. 访问邓白氏中国官网申请
2. 免费通道：5-10个工作日
3. 加急通道：付费，约1个工作日
4. 准备材料：公司中英文名称、注册地址、营业执照、联系方式等

#### Step 3：进入Google Play Console
1. 访问：https://play.google.com/console
2. 使用Google账号登录
3. 选择账号类型（个人/组织）
4. **注意**：国家/地区选择"中国"，可使用大陆身份证验证

#### Step 4：填写开发者信息
- **个人账号**：真实姓名、地址、电话、邮箱
- **公司账号**：公司全称（与邓白氏一致）、邓白氏编码、地址、官网、电话、企业邮箱

#### Step 5：支付$25注册费
- 使用Visa/MasterCard信用卡支付
- 确保信用卡已开通境外网上支付
- 账单地址必须与注册地址一致
- 支付成功后商户名显示"GOOGLE*Dev Reg"

#### Step 6：身份验证
- 个人账号：上传身份证件照片
- 公司账号：上传营业执照+授权人ID
- 审核通常48小时内，最长7天

### 3.3 中国大陆开发者注意事项

1. **网络环境**：使用稳定、纯净的IP，避免公共VPN
2. **一机一号**：每个账号使用独立设备/浏览器环境
3. **支付问题**：国内双币信用卡通常可用，确保开通境外无卡支付
4. **信息一致**：证件、信用卡、注册信息的姓名/地址必须一致
5. **验证次数**：验证失败次数有限，确保材料清晰准确
6. **防关联**：不要在同一设备/IP下操作多个开发者账号

### 3.4 申诉渠道

如遇验证失败：
- 身份验证申诉：https://support.google.com/googleplay/android-developer/contact/idv_form
- 通用支持：https://support.google.com/googleplay/android-developer/gethelp
- 邮件：play-developer-support@google.com
- 申诉需用英文填写

---

## 四、应用配置修改

### 4.1 修改包名（Application ID）

当前包名 `com.example.hearablemusicplayer` 是示例包名，必须修改。

**建议包名格式**：`com.[公司/品牌名].musicplayer` 或 `com.hmp.player`

**修改位置**：`android/app/build.gradle.kts`

```kotlin
android {
    namespace = "com.yourcompany.hmp"  // 修改这里
    defaultConfig {
        applicationId = "com.yourcompany.hmp"  // 修改这里
        // ...
    }
}
```

**注意**：包名一旦发布不可更改，请慎重选择。

### 4.2 完善权限配置

修改 `android/app/src/main/AndroidManifest.xml`，添加版本兼容：

```xml
<!-- 基础媒体权限（Android 13+） -->
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" /> <!-- 如需读取专辑封面 -->

<!-- Android 12及以下兼容 -->
<uses-permission
    android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<!-- 蓝牙连接权限（Android 12+） -->
<uses-permission
    android:name="android.permission.BLUETOOTH_CONNECT"
    android:usesPermissionFlags="neverForLocation" />

<!-- 网络权限 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 前台服务 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROCESSING" /> <!-- 如需要媒体处理 -->

<!-- 通知 -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- 其他 -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.BROADCAST_STICKY" />
```

### 4.3 确保前台服务正确配置

检查你的MusicService配置，确保指定了正确的foregroundServiceType：

```xml
<service
    android:name=".service.MusicService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="false">
    <intent-filter>
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>
```

### 4.4 版本号管理规范

建议使用语义化版本编码：

```kotlin
android {
    defaultConfig {
        val versionMajor = 1
        val versionMinor = 0
        val versionPatch = 0
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
    }
}
```

**规则**：
- versionCode：每次上传必须单调递增
- versionName：用户可见的版本号，如 "1.0.0"
- 重大更新递增major，功能更新递增minor，bug修复递增patch

### 4.5 网络安全配置

确保使用HTTPS，如需要明文HTTP（仅用于本地测试），添加网络安全配置：

创建 `res/xml/network_security_config.xml`：
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- 生产环境禁用明文流量 -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    
    <!-- 仅调试版本允许本地明文 -->
    <debug-overrides>
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

在AndroidManifest.xml的application标签中添加：
```xml
android:networkSecurityConfig="@xml/network_security_config"
```

### 4.6 备份规则配置（已存在，确认正确）

检查 `res/xml/backup_rules.xml` 和 `res/xml/data_extraction_rules.xml` 已正确配置。

---

## 五、打包与签名

### 5.1 生成生产密钥

**重要**：不要使用当前配置中的示例密码和密钥！

#### 使用keytool生成（Windows PowerShell）：

```powershell
# 创建密钥存储目录
New-Item -ItemType Directory -Path "D:\keys" -Force

# 生成密钥
keytool -genkey -v -keystore "D:\keys\hmp-release-key.jks" -keyalg RSA -keysize 4096 -validity 9125 -alias hmp_release
```

参数说明：
- `-keysize 4096`: 4096位密钥（更安全）
- `-validity 9125`: 有效期25年（必须超过2033年10月22日）
- 按提示输入：密钥库密码、姓名、组织、城市、省份、国家代码（CN）

**记录以下信息并妥善保管**：
- 密钥库文件路径
- 密钥库密码
- 密钥别名
- 密钥密码

#### 密钥安全管理：
1. 备份密钥库文件到多个安全位置（加密U盘、私有云）
2. 使用密码管理器存储密码
3. 不要将密钥文件提交到版本控制
4. 导出PEM格式证书备份：
```powershell
keytool -export -rfc -keystore "D:\keys\hmp-release-key.jks" -alias hmp_release -file "D:\keys\hmp_upload_cert.pem"
```

### 5.2 配置Gradle签名

#### 步骤1：创建keystore.properties

在项目根目录创建 `keystore.properties`（不要提交到Git！）：

```properties
storePassword=your_strong_keystore_password
keyPassword=your_key_password
keyAlias=hmp_release
storeFile=D:\\keys\\hmp-release-key.jks
```

#### 步骤2：修改build.gradle.kts

修改 `android/app/build.gradle.kts`：

```kotlin
import java.util.Properties
import java.io.FileInputStream

// 加载签名配置
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.yourcompany.hmp"
    compileSdk {
        version = release(36)
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "com.yourcompany.hmp"
        minSdk = 33  // 考虑降至26+覆盖更多用户
        targetSdk = 36
        // 版本号配置...
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    
    // ... 其他配置
}
```

#### 步骤3：确保.gitignore包含密钥文件

在 `.gitignore` 中添加：
```
keystore.properties
*.jks
*.keystore
```

### 5.3 构建Release AAB

#### 使用命令行构建（Windows PowerShell）：

```powershell
# 进入项目目录
cd d:\MyFile\HMP

# 清理并构建Release AAB
.\gradlew.bat clean :android:app:bundleRelease
```

或者使用项目已有的release任务：
```powershell
.\gradlew.bat releaseAndroid
```

#### 构建输出位置：
```
android/app/build/outputs/bundle/release/app-release.aab
releases/android/HMP-v{version}-release.aab
```

### 5.4 本地测试AAB

使用bundletool测试AAB文件：

1. 下载bundletool：https://github.com/google/bundletool/releases
2. 生成APK并安装到连接的设备：

```powershell
java -jar bundletool.jar install-apks --bundle="releases\android\HMP-v5.10.0-release.aab" --apks=hmp-test.apks
```

3. 验证签名：
```powershell
# 先生成universal APK
java -jar bundletool.jar build-apks --bundle="releases\android\HMP-v5.10.0-release.aab" --output=hmp-universal.apks --mode=universal --ks="D:\keys\hmp-release-key.jks" --ks-key-alias=hmp_release --ks-pass=pass:your_password --key-pass=pass:your_password

# 解压后验证
Expand-Archive hmp-universal.apks -DestinationPath apks\
apksigner verify --verbose apks\universal.apk
```

### 5.5 AAB分包优化（可选）

在 `android/app/build.gradle.kts` 中配置：

```kotlin
android {
    bundle {
        language {
            enableSplit = true  // 按语言分包，减小下载体积
        }
        density {
            enableSplit = true  // 按屏幕密度分包
        }
        abi {
            enableSplit = true  // 按CPU架构分包
        }
    }
}
```

---

## 六、商店素材准备

### 6.1 应用图标（App Icon）

**规格**：
- 尺寸：512 x 512 像素
- 格式：32位 PNG（必须包含alpha透明层）
- 色彩空间：sRGB
- 文件大小：最大1024KB
- 形状：正方形（**不要**自行添加圆角/阴影，Google会自动处理）

**设计要求**：
- 简洁、易识别，符合音乐播放器主题
- 不要使用"最佳""免费""排名第一"等文字
- 不要添加价格、排名、类别标记
- 保持与应用内图标风格一致
- 项目中已有 `ic_launcher-playstore.png`，需确认尺寸和格式是否符合要求

### 6.2 功能图形（Feature Graphic）

**规格**：
- 尺寸：1024 x 500 像素
- 格式：JPEG 或 24位 PNG（无alpha透明层）
- 用途：推荐位展示、视频封面

**设计建议**：
- 关键元素放在中心区域，避免边缘被裁剪
- 使用鲜艳色彩，避免纯白/深灰
- 突出应用特色（音乐、播放控制等）
- 不重复图标品牌信息
- 不使用设备图片

### 6.3 屏幕截图（Screenshots）

**手机截图（必填）**：
- 数量：最少2张，最多8张（建议8张）
- 尺寸：最小边320px，最大边3840px
- 推荐尺寸：1080 x 1920 像素（竖屏）
- 宽高比：9:16（竖）或16:9（横），不超过2:1或1:2
- 格式：JPEG或24位PNG（无alpha）
- 单张最大：8MB

**建议截图内容**：
1. 首页/音乐库
2. 播放界面（核心）
3. 播放列表
4. 均衡器/音效设置
5. 主题/个性化设置
6. 歌词显示
7. 蓝牙/车载连接
8. 其他特色功能

**平板截图（推荐）**：
- 数量：至少4张
- 尺寸：1080px ~ 7680px
- 展示大屏适配效果

**截图要求**：
- 展示真实UI，不要过度美化
- 通知栏显示满格信号/电池，不显示运营商名称或通知
- 文字不超过图片面积20%
- 不包含"立即下载""免费"等CTA文字
- 为每张图片添加不超过140字符的替代文本（无障碍）

### 6.4 宣传视频（可选但强烈推荐）

**要求**：
- 上传到YouTube，设为"公开"或"不公开列出"
- 不能设为私有，不能有年龄限制
- 视频内不能包含广告
- URL不要添加时间码参数
- 时长建议30秒-2分钟
- 展示核心功能和使用场景
- 功能图形会作为视频封面

### 6.5 文本内容

#### 应用名称（Title）
- 长度：≤50字符
- 示例："HMP - Hearable Music Player"
- 简洁明了，包含核心关键词

#### 简短描述（Short Description）
- 长度：≤80字符
- 示例："一款简洁优雅的本地音乐播放器，支持高品质音频、歌词显示、蓝牙播放"
- 概括核心价值，突出独特卖点
- 不要使用表情符号、特殊符号、重复标点
- 不要使用全大写（除缩写外）
- 不要包含CTA用语

#### 完整描述（Full Description）
- 长度：≤4000字符
- 结构建议：
  1. 开头2-3句：核心价值主张（最重要，预览区域显示）
  2. 主要功能列表：分点列出核心功能
  3. 特色亮点：独特功能详述
  4. 支持的格式/设备
  5. 联系方式/反馈渠道
- 可使用简单HTML：`<b>`, `<i>`, `<br>`, `<ul>`, `<li>`
- 自然融入关键词，避免堆砌

**完整描述示例（框架）**：
```
HMP是一款专为音乐爱好者打造的高品质本地音乐播放器，采用现代Material Design设计，为您带来纯净的音乐聆听体验。

<b>🎵 核心功能</b>
• 支持多种音频格式：MP3, FLAC, WAV, AAC, OGG等
• 高保真音频播放，支持无损音质
• 自动歌词同步显示
• 智能播放列表管理
• 10段均衡器与多种预设音效
• 蓝牙耳机/车载音响完美适配
• 桌面小组件与通知栏控制
• 多种精美主题随心切换

<b>✨ 特色亮点</b>
- 纯净无广告，专注音乐体验
- 无需联网，保护您的隐私
- 智能分类：按艺术家、专辑、流派、文件夹浏览
- 睡眠定时器，伴您入眠
- 线控支持，轻松切歌

我们重视您的隐私，HMP不会收集任何个人数据，所有音乐文件仅存储在您的设备本地。

如有问题或建议，欢迎联系我们：support@hmp.app
```

---

## 七、Google Play Console配置

### 7.1 创建应用

1. 登录Google Play Console
2. 点击"Create app"
3. 填写：
   - App name：应用名称
   - Default language：默认语言（建议English (United States)，后续可添加中文）
   - App or game：选择App
   - Free or paid：选择Free（免费）
4. 勾选声明，点击"Create app"

### 7.2 应用内容（App content）配置

进入应用后，先完成"Policy > App content"中的所有必填项：

#### 7.2.1 隐私政策
- 提供公开可访问的HTTPS隐私政策URL
- 隐私政策必须包含：
  - 开发者名称和联系方式
  - 收集的数据类型（如：不收集个人数据、仅本地存储播放列表等）
  - 数据使用目的
  - 第三方SDK清单（如使用）
  - 用户数据删除方式
  - 安全保护措施

**隐私政策模板要点**（针对本地音乐播放器）：
- 明确说明应用仅访问设备上的音频文件
- 不收集、不上传用户的音乐文件或个人数据
- 播放历史、收藏列表等仅存储在本地
- 如使用崩溃统计/分析SDK，需明确说明
- 提供联系方式用于隐私问题咨询

#### 7.2.2 广告声明
- 如实声明应用是否包含广告
- 如无广告，选择"No，my app does not contain ads"

#### 7.2.3 内容分级
- 点击"Start questionnaire"开始IARC分级问卷
- 如实回答关于暴力、成人内容、赌博、药物等问题
- 音乐播放器通常为"Everyone"（3+）或"Teen"（13+）
- 完成后获得分级证书

#### 7.2.4 目标受众
- 选择目标年龄段
- 如不专门面向儿童，选择"18 and over"或"13 and over"
- 如面向儿童需遵守家庭政策额外要求

#### 7.2.5 新闻应用声明
- HMP不是新闻应用，选择"No"

#### 7.2.6 数据安全表单（重要！常见拒审原因）
点击"Data safety"开始填写：

**数据收集与共享声明**：
- 如果应用完全本地运行、不上传任何数据：
  - 对所有数据类型选择"No"
  - 在"Data collected"中确认没有收集任何数据
- 如果使用崩溃报告（如Firebase Crashlytics）：
  - 声明收集崩溃数据、设备信息
  - 说明用途为改进应用稳定性
  - 声明数据加密传输
  - 提供用户数据删除途径

**安全实践**：
- 传输中加密：选择"Yes"（如使用HTTPS）
- 用户可请求删除数据：根据实际情况选择

#### 7.2.7 权限声明
- 检查列出的权限是否都有合理用途
- 对于敏感权限，需提供使用说明
- HMP的权限都是媒体播放必需的，通常无需额外说明

### 7.3 商店列表（Store listing）配置

进入"Grow > Store presence > Main store listing"：

#### 7.3.1 图形资源上传
- 上传应用图标（512x512）
- 上传功能图形（1024x500）
- 上传手机截图
- 上传平板截图（推荐）
- 添加宣传视频链接（可选）

#### 7.3.2 文本内容填写
- 填写应用名称
- 填写简短描述
- 填写完整描述

#### 7.3.3 应用分类
- Application type：Applications
- Category：Music & Audio
- Tags：添加相关标签（music player, audio, local music, mp3 player等，最多5个）

#### 7.3.4 联系方式
- Email：开发者支持邮箱
- Phone：可选
- Website：可选但推荐

### 7.4 定价与分发

进入"Monetize > Products"：
- 确认应用为免费
- 在"Countries/regions"选择分发国家/地区（建议先选择部分国家测试，再逐步开放）
- 可选择所有国家，或针对性选择

---

## 八、测试轨道发布

### 8.1 测试轨道说明

Google Play提供四种发布轨道：

| 轨道 | 用户规模 | 用途 | 审核速度 |
|------|----------|------|----------|
| Internal testing | 最多100人 | 团队内部快速测试 | 几分钟-几小时 |
| Closed testing | 指定用户 | 个人账号必须：12人+14天 | 1-2天 |
| Open testing | 任何人 | 大规模Beta测试 | 1-3天 |
| Production | 所有用户 | 正式发布 | 3-7天 |

### 8.2 内部测试（Internal Testing）

1. 进入"Release > Testing > Internal testing"
2. 点击"Create new release"
3. 上传签名的AAB文件
4. 填写Release name（如"1.0.0-internal"）和Release notes
5. 点击"Save" → "Review release" → "Start rollout to Internal testing"
6. 在"Testers"标签页添加测试人员邮箱（最多100人）
7. 复制Opt-in链接分发给团队成员测试
8. 测试核心功能：播放、通知控制、蓝牙、权限申请等

### 8.3 封闭测试（Closed Testing）- 个人账号关键！

**新个人开发者账号必须完成：至少12名测试用户连续测试14天**

#### 创建封闭测试轨道：
1. 进入"Release > Testing > Closed testing"
2. 点击"Create track"，命名为"Pre-release"
3. 点击"Create new release"
4. 上传AAB，填写版本信息
5. 保存并提交审核

#### 添加测试人员：
1. 点击"Testers"标签
2. 创建测试组，选择"Email list"
3. 添加至少12个测试人员邮箱（建议15-20个，预留冗余）
4. 测试账号要求：
   - 长期正常使用的Gmail账号
   - 有Google Play使用历史
   - 避免刚注册的新账号
5. 保存设置

#### 开始测试：
1. 审核通过后（1-2天），复制Opt-in URL
2. 将链接发送给测试人员
3. 测试人员点击链接，加入测试计划并安装应用

#### 14天测试期间注意事项：
- 每个测试人员使用独立真实设备
- 避免同一IP下多个测试账号
- 测试行为要求：
  - **第1-2天**：安装、注册/授权、体验核心功能，停留1-3分钟
  - **第3-10天**：每隔1-2天打开应用，使用播放、列表等功能，模拟真实用户
  - **第11-14天**：再次使用，形成留存数据
- 3-5天后让部分测试人员提交测试反馈（在Play商店提交）
- 期间可发布小版本更新修复bug

### 8.4 申请生产发布权限

满足12人+14天要求后：
1. Play Console会出现"Production access"按钮
2. 填写测试问卷：
   - 测试了哪些功能
   - 发现了什么问题，如何修复
   - 测试人员数量和持续时间
   - 应用稳定性数据
3. 提交申请，等待Google审核（通常1-3天）

---

## 九、生产发布流程

### 9.1 发布前最终检查清单

- [ ] 所有App content表单已填写完整
- [ ] 商店列表资源完整（图标、截图、描述）
- [ ] 隐私政策URL可正常访问
- [ ] 数据安全表单填写准确
- [ ] Target SDK为35+（当前为36，符合）
- [ ] 应用已在多设备上测试通过
- [ ] 封闭测试要求已满足（个人账号）
- [ ] versionCode已正确递增
- [ ] 签名正确，使用生产密钥
- [ ] 测试账号已提供（如需要登录）
- [ ] 无侵权内容、无违规广告
- [ ] 内购配置完成（如适用）

### 9.2 创建生产发布

1. 进入"Release > Production"
2. 点击"Create new release"
3. 上传签名的Release AAB文件
4. 确认签名证书指纹正确
5. 填写：
   - Release name：版本号（如"1.0.0"）
   - Release notes：多语言更新说明
6. 点击"Save" → "Review release"
7. 检查所有警告信息，确认无误

### 9.3 分阶段发布（强烈推荐）

为降低风险，使用分阶段发布：
1. 选择"Staged rollout"
2. 初始比例建议：10%
3. 发布计划：
   - Day 1：10%用户
   - Day 2-3：监控崩溃率和反馈，如无问题提升至20%
   - Day 4-5：提升至50%
   - Day 6-7：全量发布100%
4. 如发现严重问题，可立即暂停发布或回滚

### 9.4 提交审核

1. 确认所有信息正确
2. 在"Review summary"页面检查
3. 点击"Start rollout to Production"
4. 等待审核

### 9.5 审核时间

- 首次生产发布：3-7天（新账号可能更长）
- 应用更新：1-3天（平均2天）
- 可在Play Console查看审核状态
- 如超过7天无消息，可联系支持催审

---

## 十、审核与拒审处理

### 10.1 音乐播放器应用常见拒审原因

| 拒审原因 | 解决方案 |
|----------|----------|
| 隐私政策缺失/无法访问 | 提供有效的HTTPS隐私政策链接 |
| 数据安全表单不准确 | 如实填写数据收集情况，确保与隐私政策一致 |
| 权限使用不当 | 只申请必需权限，移除不需要的权限 |
| MANAGE_EXTERNAL_STORAGE权限 | 不要申请，使用READ_MEDIA_AUDIO |
| 前台服务类型未声明 | 确保指定foregroundServiceType="mediaPlayback" |
| 功能缺陷/崩溃 | 充分测试，使用Firebase Test Lab |
| 版权问题 | 明确声明仅播放本地音乐，不提供在线资源 |
| 元数据违规 | 不使用绝对化用语、不堆砌关键词、截图真实 |
| Target SDK过低 | 设置targetSdk为35+ |
| 测试账号未提供 | 如需要登录，在审核备注中提供测试账号 |

### 10.2 审核加速技巧

1. **渐进式发布**：先内部测试，再封闭测试，最后生产
2. **首个版本简化**：元数据简洁真实，功能完整
3. **审核备注**：在"Notes to reviewer"中提供：
   - 测试账号（如需登录）
   - 关键功能测试步骤
   - 特殊说明（如应用仅播放本地音乐）
   - 可上传演示视频展示功能
4. **利用预检查**：Play Console会自动检测常见问题
5. **避免节假日提交**：审核可能延迟

### 10.3 卡审处理策略（等、催、撤）

1. **等（Wait）**：正常审核7天内，等待期间每天检查状态
2. **催（Escalate）**：超过7天且无审核活动时，通过以下方式催审：
   - 在线聊天支持（Play Console内）
   - 支持表单：https://support.google.com/googleplay/android-developer/gethelp
   - 电话支持（如有）
3. **撤（Resubmit）**：明确发现问题时，撤销后修复重提（慎用，会重新排队）

### 10.4 拒审申诉流程

1. 仔细阅读拒绝邮件，定位具体违规原因
2. 根据拒审原因逐条修复
3. 回复申诉（建议用英文）：
   - 引用拒绝原因的具体条款
   - 逐条说明已做的修改
   - 提供测试流程和账号
   - 附上相关证明材料
4. 模板示例：
```
Dear Google Play Review Team,

Thank you for your review. We have addressed the issues mentioned in the rejection notice:

1. Regarding [Issue 1]: We have [specific changes made]. Please see [details].
2. Regarding [Issue 2]: We updated [changes] to comply with the policy.

Testing instructions:
- Test account: xxx@xxx.com
- Password: xxx
- Steps to test: 1... 2... 3...

We have thoroughly reviewed our app to ensure full compliance with all Google Play policies. Please let us know if you need any additional information.

Best regards,
[Developer Name]
```

### 10.5 账号封禁处理

如账号被封：
1. 不要立即注册新账号（会被关联）
2. 认真阅读封禁原因邮件
3. 准备申诉材料：
   - 详细的整改说明PDF
   - 代码截图和功能说明
   - 合规证明材料
4. 通过官方申诉渠道提交，态度诚恳
5. 如因环境异常，提供IP归属证明

---

## 十一、发布后维护

### 11.1 监控关键指标

发布后重点关注：
- **Android Vitals**：
  - 崩溃率（Crash rate）：建议<1%
  - ANR率（应用无响应）：建议<0.5%
  - 唤醒次数过多
  - 唤醒锁超时
- **用户评价**：及时回复用户评论，处理差评
- **安装数据**：安装量、卸载量、活跃用户
- **政策中心**：定期检查是否有政策违规警告

### 11.2 版本更新流程

1. 递增versionCode和versionName
2. 修复bug、添加新功能
3. 充分测试
4. 构建新的Release AAB
5. 在Play Console创建新版本
6. 填写Release notes
7. 建议使用分阶段发布
8. 提交审核（通常1-3天）

### 11.3 定期合规检查

- 每季度检查一次：
  - 权限是否最小化
  - 第三方SDK是否更新到合规版本
  - 隐私政策是否需要更新
  - Target SDK是否符合最新要求
  - 是否有新的政策公告
- 关注Google Play政策更新邮件
- 参加Google Play Academy免费培训

### 11.4 收款设置（如适用）

如需收费应用或内购：
1. 在"Payments profile"中设置收款账户
2. 中国大陆账号可使用：
   - 第三方收款工具（连连国际、Payoneer等）
   - 国内银行美元电汇
3. 填写税务表单W-8BEN（非美国纳税人）
4. 内购商品在"Monetize > Products > In-app products"中配置

---

## 十二、检查清单

### 12.1 账号准备
- [ ] Google账号准备就绪（已养号）
- [ ] 开发者账号类型确定（个人/公司）
- [ ] 邓白氏编码申请（公司账号）
- [ ] 信用卡准备（Visa/MasterCard，开通境外支付）
- [ ] 身份证件准备清晰扫描件
- [ ] $25注册费支付完成
- [ ] 身份验证通过

### 12.2 应用配置
- [ ] applicationId修改为正式包名
- [ ] 权限配置完善（含版本兼容）
- [ ] targetSdk设置为35+（当前36符合）
- [ ] 前台服务正确声明mediaPlayback类型
- [ ] 网络安全配置（禁用明文流量）
- [ ] 版本号管理规范配置
- [ ] 生产密钥生成并安全备份
- [ ] Gradle签名配置完成（keystore.properties不提交Git）
- [ ] ProGuard规则正确配置
- [ ] 应用内无测试代码、无日志泄露

### 12.3 构建测试
- [ ] Release AAB构建成功
- [ ] 本地安装测试通过
- [ ] 核心功能测试：音频播放、通知控制、蓝牙
- [ ] 权限申请流程测试
- [ ] 多设备兼容性测试
- [ ] 崩溃率、ANR率达标
- [ ] 签名验证正确

### 12.4 商店素材
- [ ] 应用图标（512x512 PNG，符合规范）
- [ ] 功能图形（1024x500）
- [ ] 手机截图（至少2张，推荐8张）
- [ ] 平板截图（推荐）
- [ ] 宣传视频（可选）
- [ ] 应用名称（≤50字符）
- [ ] 简短描述（≤80字符）
- [ ] 完整描述（≤4000字符）
- [ ] 应用分类选择（Music & Audio）
- [ ] 标签添加（最多5个）
- [ ] 联系邮箱
- [ ] 官方网站（推荐）

### 12.5 合规配置
- [ ] 隐私政策URL（HTTPS，可访问）
- [ ] 数据安全表单填写准确
- [ ] 内容分级问卷完成
- [ ] 目标受众声明
- [ ] 广告声明
- [ ] 新闻应用声明（否）
- [ ] 第三方SDK清单整理
- [ ] 无侵权内容（版权、商标）
- [ ] 无违规广告（如适用）

### 12.6 发布流程
- [ ] Google Play Console应用创建完成
- [ ] 所有App content表单填写完成
- [ ] 商店列表配置完成
- [ ] 定价与分发国家设置
- [ ] 内部测试完成，团队验证通过
- [ ] 封闭测试创建完成
- [ ] 12+测试人员添加
- [ ] 封闭测试进行14天
- [ ] 生产发布权限申请通过
- [ ] Release AAB上传到生产轨道
- [ ] 分阶段发布设置（10%起）
- [ ] 审核备注填写（测试账号、步骤说明）
- [ ] 提交审核
- [ ] 审核期间监控状态
- [ ] 发布后监控Android Vitals
- [ ] 回复用户评价

---

## 附录：有用资源链接

### 官方资源
- Google Play Console: https://play.google.com/console
- Google Play政策中心: https://support.google.com/googleplay/android-developer/topic/9858052
- 目标API级别要求: https://developer.android.com/google/play/requirements/target-sdk
- Android 15/16适配指南: https://developer.android.com/about/versions
- Play App Signing文档: https://developer.android.com/studio/publish/app-signing
- bundletool下载: https://github.com/google/bundletool/releases
- Google Play Academy: https://g.co/playacademy

### 支持渠道
- 开发者支持入口: https://support.google.com/googleplay/android-developer/gethelp
- 身份验证申诉: https://support.google.com/googleplay/android-developer/contact/idv_form

---

**文档结束**

*本手册基于2025-2026年Google Play最新政策编写，政策可能随时调整，建议上架前查阅官方最新文档。*
