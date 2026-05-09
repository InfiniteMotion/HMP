# Hearable Music Player — 版本命名与发布规范

本文档为项目**版本号格式、发版流程与分支策略**的正式约定。**自 v5.6.1 起施行**；此前已发布版本与历史记录保留不改。

---

## 1. 版本号格式

采用 **三位版本号**：`MAJOR.MINOR.PATCH`（如 `5.6.0`、`6.0.0`）。

- 文档与对外表述可带前缀 `v`，如 **v6.0.0**。
- 构建产物使用纯数字：`versionName = "6.0.0"`，与 ROADMAP 中的版本一致。

## 2. 何时升级哪一位

| 类型 | 何时递增 | 示例 |
|------|----------|------|
| **MAJOR** | 不兼容的架构或产品方向大变更、重大破坏性改动 | 单体 → 模块化（v4 → v5） |
| **MINOR** | 新功能或明显体验/能力提升，保持向后兼容 | 新页面、新能力、较大重构 |
| **PATCH** | 仅 bug 修复、文案/样式小调整、文档/配置更新，无新功能 | 修崩溃、改文案、更新依赖说明 |

**原则**：

- 每次正式发布**只递增一位**：能 PATCH 就 PATCH，否则 MINOR，再否则 MAJOR。
- 避免跳号：从 `5.10.0` 下次应为 `5.10.1` 或 `6.0.0`，不直接出现未发布过的版本号。
- 发布后立即在 **ROADMAP.md** 中写入该版本、日期及变更，并更新「当前版本」。

## 3. 与构建系统的对应关系

版本号集中维护在 `gradle.properties`：

```properties
hmp.versionCode=51000
hmp.versionName=5.10.0
```

- **versionName**：与三位版本号一致，如 `"6.0.0"`。`android/app/build.gradle.kts` 通过 `project.findProperty("hmp.versionName")` 引用。
- **versionCode**：每次发布**严格递增**的整数。`android/app/build.gradle.kts` 通过 `project.findProperty("hmp.versionCode")` 引用。建议按 `MAJOR*10000 + MINOR*1000 + PATCH` 换算（保证只增不减）。

**发布前检查**（每次发版必做）：

1. 确定 MAJOR / MINOR / PATCH 及新版本号。
2. 在 `gradle.properties` 中更新 `hmp.versionCode` 和 `hmp.versionName`。
3. 在 **ROADMAP.md** 的「项目历史与版本演进」中新增该版本条目，并更新「当前版本」。
4. 若 README 等文档有「当前版本」，一并更新。

## 4. 与 ROADMAP 的同步

- **ROADMAP.md** 是版本历史与变更日志的**单一事实来源**。
- 版本号、发布日期、变更说明以 ROADMAP 为准；`versionName` 与 ROADMAP 中的版本号保持一致。

## 5. 分支与发版

### 5.1 分支结构

```
master ─────────────────────────────── 已发布版本（保护分支）
  │
  ├── develop-android ──────────────── Android 开发
  ├── develop-ios ──────────────────── iOS 开发
  ├── develop-desktop ──────────────── 桌面端开发（未来）
  └── develop-shared ───────────────── shared 模块开发
```

- **master**：已发布版本。仅通过「从 release/X.Y 合并」或「在 master 上直接提交（PATCH）」更新。
- **develop-android / develop-ios / develop-desktop**：各平台独立开发分支，互不阻塞。
- **develop-shared**：跨平台共享模块开发。shared 改动先合入此分支，各平台分支定期从 `develop-shared` merge 同步。
- **feature/\***：功能分支，从对应 develop 拉出，完成后 PR 合回。
- **fix/\***：修复分支，从 develop 或 master 拉出。

### 5.2 日常开发

1. 从对应平台 develop 分支拉出 `feature/xxx` 分支。
2. 开发完成后提 PR 合回对应 develop 分支。
3. 如果改动涉及 shared 模块，PR 合入 `develop-shared` 后，各平台 develop 分支 merge `develop-shared` 同步。
4. 小改动（修 bug、改配置）可直接在 develop 分支上提交。

### 5.3 MINOR / MAJOR 发版

统一版本号，所有平台一起发。流程：

1. 各平台在各自 develop 分支上完成改动。
2. 创建 `release/X.Y` 分支（从 master 拉出），将各 develop 分支合入：
   ```
   git checkout -b release/6.0 master
   git merge develop-shared
   git merge develop-android
   git merge develop-ios
   ```
3. 按「发布前检查」更新版本号与 ROADMAP，本地执行 `./gradlew release` 构建所有平台产物并自测。
4. 将 `release/X.Y` **PR 到 master**，CI 自动构建并创建 GitHub Release + 部署 Storybook。
5. 发布后，各 develop 分支从 master merge 同步版本号和 tag。

### 5.4 PATCH 发版

1. **不切 release 分支**。在 **master** 上直接修改（或从 master 拉短命分支如 `fix/xxx`，修完合并回 master）。
2. 在 master 上升 PATCH 版本号（`gradle.properties`）、更新 ROADMAP、构建、自测通过后打 tag（如 `v5.10.1`）。
3. 若需 develop 分支同步此次修复，执行一次 **master → 各 develop** 合并。

### 5.5 CI/CD 自动发布

项目配置了 GitHub Actions 自动发布工作流 (`.github/workflows/release.yml`)：

- **触发条件**：`release/*` 分支的 PR 合并到 `master` 时自动触发
- **release job**：构建 Android APK + AAB，基于上一个 git tag 自动生成 changelog，创建版本 tag 并发布 GitHub Release
- **storybook job** + **deploy-pages job**：构建并部署 Storybook 到 GitHub Pages

PATCH 发版（直接在 master 上操作）不触发此工作流，需手动打 tag 和创建 Release。

---

**适用范围**：本规范自 **v5.6.1** 起施行。v5.6.1 之前的版本号与历史记录不做追溯修改。分支策略自 **v6.0** 起调整为按平台拆分的 develop 分支模式。

---

© 2026 Hearable Music Player | Developed by WLYB
