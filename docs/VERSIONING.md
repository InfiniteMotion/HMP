# Hearable Music Player — 版本命名与发布规范

本文档为项目**版本号格式、发版流程与分支策略**的正式约定。**自 v5.6.1 起施行**；此前已发布版本与历史记录保留不改。

---

## 1. 版本号格式

采用 **三位版本号**：`MAJOR.MINOR.PATCH`（如 `5.6.0`、`5.7.1`）。

- 文档与对外表述可带前缀 `v`，如 **v5.6.0**。
- 构建产物使用纯数字：`versionName = "5.6.0"`，与 ROADMAP 中的版本一致。

## 2. 何时升级哪一位

| 类型 | 何时递增 | 示例 |
|------|----------|------|
| **MAJOR** | 不兼容的架构或产品方向大变更、重大破坏性改动 | 单体 → 模块化（v4 → v5） |
| **MINOR** | 新功能或明显体验/能力提升，保持向后兼容 | 新页面、新能力、较大重构 |
| **PATCH** | 仅 bug 修复、文案/样式小调整、文档/配置更新，无新功能 | 修崩溃、改文案、更新依赖说明 |

**原则**：

- 每次正式发布**只递增一位**：能 PATCH 就 PATCH，否则 MINOR，再否则 MAJOR。
- 避免跳号：从 `5.6.0` 下次应为 `5.6.1` 或 `5.7.0`，不直接出现未发布过的 5.8.0 等。
- 发布后立即在 **ROADMAP.md** 中写入该版本、日期及变更，并更新「当前版本」。

## 3. 与 Android 构建的对应关系

- **versionName**：与三位版本号一致，如 `"5.6.0"`。维护点：`app/build.gradle.kts` → `defaultConfig.versionName`。
- **versionCode**：每次发布**严格递增**的整数。维护点：`app/build.gradle.kts` → `defaultConfig.versionCode`。建议每发一版自增 1，或按 `MAJOR*10000 + MINOR*100 + PATCH` 换算（保证只增不减）。

**发布前检查**（每次发版必做）：

1. 确定 MAJOR / MINOR / PATCH 及新版本号。
2. 在 `app/build.gradle.kts` 中更新 `versionCode` 和 `versionName`。
3. 在 **ROADMAP.md** 的「项目历史与版本演进」中新增该版本条目，并更新「当前版本」。
4. 若 README 等文档有「当前版本」，一并更新。

## 4. 与 ROADMAP 的同步

- **ROADMAP.md** 是版本历史与变更日志的**单一事实来源**。
- 版本号、发布日期、变更说明以 ROADMAP 为准；`versionName` 与 ROADMAP 中的版本号保持一致。

## 5. 分支与发版（main + develop）

采用 **main** 与 **develop** 双分支；PATCH 不单独切发版分支，直接在 main 上完成。

- **main**：已发布版本，可随时构建对外分发包。仅通过「从 develop 合并」或「在 main 上直接提交（PATCH）」更新。
- **develop**：下一版本的集成分支，MINOR/MAJOR 功能开发在此进行。

### 5.1 MINOR / MAJOR 发版

1. 在 **develop** 上完成改动，按「发布前检查」更新版本号与 ROADMAP，构建并自测通过。
2. 将 develop **合并到 main**，在 main 上打 tag（如 `v5.7.0`）。

### 5.2 PATCH 发版

1. **不切专门发版分支**。在 **main** 上直接修改（或从 main 拉短命分支如 `fix/xxx`，修完合并回 main）。
2. 在 main 上升 PATCH 版本号、更新 ROADMAP、构建、自测通过后打 tag（如 `v5.6.1`）。
3. 若需 develop 同步此次修复，执行一次 **main → develop** 合并。

个人或小团队通常不需 `release/*` 分支；若日后需同时维护多条发布线，再考虑从 main 拉出 `release/x.y`。

---

**适用范围**：本规范自 **v5.6.1** 起施行。v5.6.1 之前的版本号与历史记录不做追溯修改。

---

© 2026 Hearable Music Player | Developed by WLYB