import SwiftUI
import shared

/// 歌曲详情页 — 对应 Android SongDetailScreen.kt
struct SongDetailScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss

    let musicId: Int64
    @State private var vm = SongDetailViewModel()
    @State private var selectedTab = "intro"

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    var body: some View {
        SubScreen(title: titleText) {
            switch vm.state {
            case .idle, .loading:
                loadingView
            case .success(let data):
                ScrollView {
                    VStack(spacing: 0) {
                        posterSection(data)
                        tabPicker
                        tabContent(data)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                }
            case .error(let msg):
                VStack(spacing: 16) {
                    Text(msg).foregroundColor(.red)
                    Button("重试") { vm.retry() }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .empty:
                Text("未找到歌曲").foregroundColor(theme.text.opacity(0.4))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .onAppear { vm.load(musicId: musicId) }
    }

    private var titleText: String {
        if case .success(let data) = vm.state {
            return data.musicInfo.music.title
        }
        return "歌曲详情"
    }

    // MARK: - Poster

    private func posterSection(_ data: SongDetailData_) -> some View {
        VStack(spacing: 16) {
            AlbumCover(
                uri: data.musicInfo.music.albumArtUri,
                musicPath: data.musicInfo.music.path,
                size: 280,
                cornerRadius: 25
            )
            .onTapGesture { controller.playWith(data.musicInfo) }

            VStack(spacing: 4) {
                Text(data.musicInfo.music.artist)
                    .font(TypographyTokens.titleMedium)
                    .foregroundColor(theme.text.opacity(0.86))
                    .lineLimit(1)
                Text(data.musicInfo.music.album)
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.text.opacity(0.6))
                    .lineLimit(1)
            }

            if let extra = data.musicInfo.extra {
                technicalInfo(extra)
            }
        }
        .padding(18)
    }

    private func technicalInfo(_ extra: MusicExtra_) -> some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
            if let br = extra.bitRate { infoCell("比特率", "\(br) kbps") }
            if let sr = extra.sampleRate { infoCell("采样率", "\(sr) Hz") }
            if let fs = extra.fileSize, fs.int64Value > 0 { infoCell("文件大小", formatFileSize(fs.int64Value)) }
            if let fmt = extra.format, !fmt.isEmpty { infoCell("格式", fmt) }
            if let lang = extra.language, !lang.isEmpty { infoCell("语言", lang) }
            if let date = extra.date, date.int64Value > 0 { infoCell("日期", formatTimestamp(date.int64Value)) }
        }
    }

    private func infoCell(_ label: String, _ value: String) -> some View {
        VStack(spacing: 4) {
            Text(label).font(.caption2).foregroundColor(theme.text.opacity(0.5))
            Text(value).font(TypographyTokens.titleMedium).fontWeight(.black).foregroundColor(theme.text)
        }
        .frame(maxWidth: .infinity)
        .padding(8)
        .background(theme.surface.opacity(0.25), in: RoundedRectangle(cornerRadius: 12))
    }

    // MARK: - Tab Picker

    private var tabPicker: some View {
        HStack(spacing: 0) {
            ForEach(["intro", "lyrics", "user"], id: \.self) { tab in
                Button {
                    HapticManager.shared.click()
                    selectedTab = tab
                } label: {
                    Text(tabLabel(tab))
                        .font(TypographyTokens.bodySmall)
                        .fontWeight(selectedTab == tab ? .bold : .regular)
                        .foregroundColor(selectedTab == tab ? theme.primary : theme.text.opacity(0.5))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                }
            }
        }
        .background(theme.surface.opacity(0.3), in: RoundedRectangle(cornerRadius: 10))
        .padding(.top, 14)
    }

    private func tabLabel(_ tab: String) -> String {
        switch tab {
        case "intro": return "介绍"
        case "lyrics": return "歌词"
        default: return "用户"
        }
    }

    // MARK: - Tab Content

    @ViewBuilder
    private func tabContent(_ data: SongDetailData_) -> some View {
        switch selectedTab {
        case "intro": introTab(data)
        case "lyrics": lyricsTab(data)
        default: userTab(data)
        }
    }

    // MARK: - Intro Tab

    private func introTab(_ data: SongDetailData_) -> some View {
        guard let info = data.dailyMusicInfo else { return AnyView(EmptyView()) }
        if info.errorInfo != "None" && !info.errorInfo.isEmpty {
            return AnyView(
                Text(info.errorInfo)
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(.red)
                    .padding()
                    .background(Color.red.opacity(0.1), in: RoundedRectangle(cornerRadius: 12))
                    .padding(.top, 14)
            )
        }
        return AnyView(
            VStack(alignment: .leading, spacing: 16) {
                introBlock("创作背景", info.backgroundIntroduce)
                introBlock("歌曲描述", info.description_)
                introBlock("歌手介绍", info.singerIntroduce)
                introBlock("获奖成就", info.rewards)
                introBlock("相关推荐", info.relevantMusic)
            }
            .padding(.top, 14)
        )
    }

    @ViewBuilder
    private func introBlock(_ title: String, _ content: String) -> some View {
        if !content.isEmpty && content != "None" {
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(TypographyTokens.titleSmall).fontWeight(.bold)
                    .foregroundColor(theme.text)
                Text(content)
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.text.opacity(0.6))
                    .lineSpacing(4)
            }
        }
    }

    // MARK: - Lyrics Tab

    private func lyricsTab(_ data: SongDetailData_) -> some View {
        guard let info = data.dailyMusicInfo else { return AnyView(EmptyView()) }
        let fullLyrics = (data.musicInfo.extra?.lyrics ?? "")
            .replacingOccurrences(of: "\\[.*?\\]", with: "", options: .regularExpression)
            .trimmingCharacters(in: .whitespacesAndNewlines)

        return AnyView(
            VStack(alignment: .leading, spacing: 16) {
                if !info.lyric.isEmpty && info.lyric != "None" {
                    introBlock("热门歌词", info.lyric)
                }
                if !fullLyrics.isEmpty && fullLyrics != "None Full Lyrics" {
                    introBlock("完整歌词", fullLyrics)
                }
            }
            .padding(.top, 14)
        )
    }

    // MARK: - User Tab

    private func userTab(_ data: SongDetailData_) -> some View {
        let ui = data.musicInfo.userInfo
        return AnyView(
            VStack(alignment: .leading, spacing: 16) {
                // Stats grid
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                    statItem("播放次数", "\(ui?.playCount ?? 0)")
                    statItem("跳过次数", "\(ui?.skippedCount ?? 0)")
                    statItem("歌单数", "\(ui?.inCustomPlaylistCount ?? 0)")
                    statItem("评分", "\(ui?.userRating ?? 0)")
                    statItem("最近播放", formatLastPlayed(ui?.lastPlayed))
                    statItem("喜欢", (ui?.liked ?? false) ? "是" : "否")
                }

                // Playback history
                VStack(alignment: .leading, spacing: 4) {
                    Text("最近播放")
                        .font(TypographyTokens.titleSmall).fontWeight(.bold)
                    if data.playbackHistory.isEmpty {
                        Text("暂无播放记录")
                            .font(TypographyTokens.bodyMedium)
                            .foregroundColor(theme.text.opacity(0.4))
                    } else {
                        ForEach(data.playbackHistory.indices, id: \.self) { idx in
                            let h = data.playbackHistory[idx]
                            historyRow(h)
                        }
                    }
                }
            }
            .padding(.top, 14)
        )
    }

    private func statItem(_ label: String, _ value: String) -> some View {
        VStack(spacing: 4) {
            Text(label).font(.caption2).foregroundColor(theme.text.opacity(0.5))
            Text(value).font(TypographyTokens.titleMedium).fontWeight(.black)
        }
        .frame(maxWidth: .infinity)
        .aspectRatio(1, contentMode: .fit)
        .padding(8)
        .background(theme.surface.opacity(0.25), in: RoundedRectangle(cornerRadius: 20))
    }

    private func historyRow(_ h: PlaybackHistory_) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text("时长: \(formatDuration(h.playDuration))")
                    .font(TypographyTokens.bodySmall).foregroundColor(theme.text.opacity(0.6))
                Text(formatTimestamp(h.playedAt))
                    .font(TypographyTokens.bodySmall).foregroundColor(theme.text.opacity(0.5))
            }
            Spacer()
            if let src = h.source { Text(src).font(.caption2).foregroundColor(theme.text.opacity(0.4)) }
            Text(h.isCompleted ? "完成" : "未完成")
                .font(.caption2).fontWeight(.bold)
                .foregroundColor(h.isCompleted ? theme.primary : .red)
                .padding(.horizontal, 8).padding(.vertical, 2)
                .background((h.isCompleted ? theme.primary : Color.red).opacity(0.1), in: Capsule())
        }
        .padding(10)
        .background(theme.surface.opacity(0.2), in: RoundedRectangle(cornerRadius: 12))
    }

    // MARK: - Loading

    private var loadingView: some View {
        RoundedRectangle(cornerRadius: 24)
            .fill(theme.surface.opacity(0.38))
            .frame(height: 240)
            .overlay {
                HStack(spacing: 12) {
                    ProgressView().scaleEffect(0.8)
                    Text("加载中...").foregroundColor(theme.text.opacity(0.5))
                }
            }
            .padding(20)
    }

    // MARK: - Formatters

    private func formatDuration(_ ms: Int64) -> String {
        let sec = ms / 1000
        return String(format: "%02d:%02d", sec / 60, sec % 60)
    }

    private func formatFileSize(_ bytes: Int64) -> String {
        if bytes < 1024 { return "\(bytes) B" }
        if bytes < 1048576 { return String(format: "%.1f KB", Double(bytes) / 1024) }
        return String(format: "%.1f MB", Double(bytes) / 1048576)
    }

    private func formatTimestamp(_ ts: Int64) -> String {
        if ts <= 0 { return "" }
        let date = Date(timeIntervalSince1970: Double(ts) / 1000.0)
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd HH:mm"
        return f.string(from: date)
    }

    private func formatLastPlayed(_ ts: KotlinLong?) -> String {
        guard let ts = ts, ts.int64Value > 0 else { return "从未" }
        let diff = Int64(Date().timeIntervalSince1970 * 1000) - ts.int64Value
        if diff < 60_000 { return "刚刚" }
        if diff < 3_600_000 { return "\(diff / 60_000)分钟前" }
        if diff < 86_400_000 { return "\(diff / 3_600_000)小时前" }
        if diff < 604_800_000 { return "\(diff / 86_400_000)天前" }
        let d = Date(timeIntervalSince1970: Double(ts.int64Value) / 1000.0)
        let f = DateFormatter(); f.dateFormat = "MM-dd"
        return f.string(from: d)
    }
}
