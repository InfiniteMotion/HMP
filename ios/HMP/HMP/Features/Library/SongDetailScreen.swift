import SwiftUI
import shared

struct SongDetailScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss
    
    let musicId: Int64
    @State private var uiState: UiState<SongDetailData>? = nil
    @State private var selectedSection: String = "user"
    
    private let userSection = "user"
    private let introSection = "intro"
    private let lyricsSection = "lyrics"
    
    var body: some View {
        Group {
            switch uiState {
            case .idle, .loading, .none:
                loadingView
            case .success(let data):
                successView(data: data)
            case .empty:
                emptyView
            case .error(let message):
                errorView(message: message)
            }
        }
        .navigationTitle(uiState?.data?.musicInfo.music.title ?? "歌曲详情")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button {
                    HapticManager.shared.click()
                    dismiss()
                } label: {
                    Image(systemName: "chevron.left")
                        .foregroundColor(theme.text)
                }
            }
        }
        .task {
            await loadSongDetail()
        }
    }
    
    private func loadSongDetail() async {
        uiState = .loading
        do {
            // 这里需要调用 shared 模块的方法来加载歌曲详情
            // 由于目前没有专门的 UseCase，我们先做简单实现
            let allMusic = try await KoinHelperKt.getGetAllMusicUseCase().invoke(orderBy: "title", orderType: "ASC")
            if let music = allMusic.first(where: { $0.music.id == musicId }) {
                let data = SongDetailData(
                    musicInfo: music,
                    dailyMusicInfo: nil,
                    labels: [],
                    playbackHistory: []
                )
                uiState = .success(data)
            } else {
                uiState = .empty
            }
        } catch {
            uiState = .error(error.localizedDescription)
        }
    }
    
    private var loadingView: some View {
        VStack(spacing: 20) {
            ProgressView()
            Text("加载中...")
                .foregroundColor(theme.text.opacity(0.6))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private var emptyView: some View {
        VStack(spacing: 20) {
            Image(systemName: "music.note")
                .font(.system(size: 60))
                .foregroundColor(theme.text.opacity(0.3))
            Text("未找到歌曲")
                .foregroundColor(theme.text.opacity(0.6))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private func errorView(message: String) -> some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 60))
                .foregroundColor(theme.error)
            Text(message)
                .foregroundColor(theme.error)
            Button("重试") {
                Task { await loadSongDetail() }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private func successView(data: SongDetailData) -> some View {
        ScrollView {
            VStack(spacing: 0) {
                SongDetailPoster(
                    musicInfo: data.musicInfo,
                    onOpenPlayer: {
                        // 打开播放器
                    }
                )
                .padding(.bottom, 24)
                
                SongDetailInfo(
                    musicInfo: data.musicInfo,
                    selectedSection: $selectedSection
                )
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
    }
}

struct SongDetailPoster: View {
    @Environment(HMPTheme.self) private var theme
    
    let musicInfo: MusicInfo_
    let onOpenPlayer: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            // 专辑封面
            Button {
                HapticManager.shared.click()
                onOpenPlayer()
            } label: {
                RoundedRectangle(cornerRadius: 25)
                    .fill(theme.surface)
                    .frame(width: 280, height: 280)
                    .overlay {
                        let albumArtUri = musicInfo.music.albumArtUri
                        if !albumArtUri.isEmpty {
                            // 这里可以使用 AsyncImage 加载
                            Image(systemName: "music.note")
                                .font(.system(size: 80))
                                .foregroundColor(theme.text.opacity(0.3))
                        } else {
                            Image(systemName: "music.note")
                                .font(.system(size: 80))
                                .foregroundColor(theme.text.opacity(0.3))
                        }
                    }
                    .shadow(color: Color.black.opacity(0.15), radius: 15, x: 0, y: 8)
            }
            .buttonStyle(.plain)
            
            // 艺术家和专辑信息
            VStack(spacing: 4) {
                Text(musicInfo.music.artist)
                    .font(.title3)
                    .fontWeight(.medium)
                    .foregroundColor(theme.text.opacity(0.86))
                    .multilineTextAlignment(.center)
                    .lineLimit(1)
                
                Text(musicInfo.music.album)
                    .font(.subheadline)
                    .foregroundColor(theme.text.opacity(0.6))
                    .multilineTextAlignment(.center)
                    .lineLimit(1)
            }
            
            // 技术信息卡片
            if let extra = musicInfo.extra {
                TechnicalInfoCard(music: musicInfo.music, extra: extra)
            }
        }
    }
}

struct TechnicalInfoCard: View {
    @Environment(HMPTheme.self) private var theme
    
    let music: Music_
    let extra: MusicExtra_
    
    var body: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                StatItem(
                    icon: "clock",
                    label: "时长",
                    value: formatDuration(music.duration)
                )
                StatItem(
                    icon: "waveform",
                    label: "比特率",
                    value: (Int(extra.bitRate?.intValue ?? 0)) > 0 ? "\(extra.bitRate!.intValue)kbps" : "-"
                )
            }
            HStack(spacing: 12) {
                StatItem(
                    icon: "speaker.wave.2",
                    label: "采样率",
                    value: (Int(extra.sampleRate?.intValue ?? 0)) > 0 ? "\(extra.sampleRate!.intValue)Hz" : "-"
                )
                StatItem(
                    icon: "doc",
                    label: "格式",
                    value: (extra.format ?? "").isEmpty ? "-" : (extra.format ?? "-")
                )
            }
        }
        .padding(16)
        .background(theme.surfaceVariant.opacity(0.25))
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(theme.outline.opacity(0.1), lineWidth: 1)
        )
    }
    
    private func formatDuration(_ ms: Int64) -> String {
        let totalSeconds = ms / 1000
        let minutes = totalSeconds / 60
        let seconds = totalSeconds % 60
        return String(format: "%02d:%02d", minutes, seconds)
    }
}

struct StatItem: View {
    @Environment(HMPTheme.self) private var theme
    
    let icon: String
    let label: String
    let value: String
    
    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(theme.primary)
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(theme.text.opacity(0.6))
                Text(value)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(theme.text)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(theme.surface)
        .cornerRadius(12)
    }
}

struct SongDetailInfo: View {
    @Environment(HMPTheme.self) private var theme
    
    let musicInfo: MusicInfo_
    @Binding var selectedSection: String
    
    private let userSection = "user"
    private let introSection = "intro"
    private let lyricsSection = "lyrics"
    
    var body: some View {
        VStack(spacing: 0) {
            // 分段控制器
            Picker("详情", selection: $selectedSection) {
                Text("统计").tag(userSection)
                Text("介绍").tag(introSection)
                Text("歌词").tag(lyricsSection)
            }
            .pickerStyle(.segmented)
            .padding(.bottom, 14)
            
            // 内容区域
            switch selectedSection {
            case introSection:
                introContent
            case lyricsSection:
                lyricsContent
            default:
                userStatsContent
            }
        }
    }
    
    private var introContent: some View {
        VStack(spacing: 16) {
            // 这里放置介绍内容，由于没有 DailyMusicInfo，先显示占位
            EmptyContentView(
                icon: "info.circle",
                message: "暂无介绍信息"
            )
        }
    }
    
    private var lyricsContent: some View {
        VStack(spacing: 16) {
            if let lyrics = musicInfo.extra?.lyrics, !lyrics.isEmpty, lyrics != "None" {
                let processedLyrics = lyrics
                    .replacingOccurrences(of: "\\[.*?\\]", with: "", options: .regularExpression)
                    .components(separatedBy: .newlines)
                    .filter { !$0.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
                    .joined(separator: "\n")
                    .trimmingCharacters(in: .whitespacesAndNewlines)
                
                if !processedLyrics.isEmpty {
                    SectionTitleView(title: "歌词") {
                        Text(processedLyrics)
                            .font(.body)
                            .foregroundColor(theme.text.opacity(0.8))
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .lineSpacing(6)
                    }
                } else {
                    EmptyContentView(
                        icon: "text.quote",
                        message: "暂无歌词"
                    )
                }
            } else {
                EmptyContentView(
                    icon: "text.quote",
                    message: "暂无歌词"
                )
            }
        }
    }
    
    private var userStatsContent: some View {
        VStack(spacing: 16) {
            SectionTitleView(title: "个人统计") {
                let userInfo = musicInfo.userInfo
                
                LazyVGrid(columns: [
                    GridItem(.flexible()),
                    GridItem(.flexible()),
                    GridItem(.flexible())
                ], spacing: 8) {
                    GridStatItem(
                        label: "播放次数",
                        value: "\(userInfo?.playCount ?? 0)"
                    )
                    GridStatItem(
                        label: "跳过次数",
                        value: "\(userInfo?.skippedCount ?? 0)"
                    )
                    GridStatItem(
                        label: "播放列表",
                        value: "\(userInfo?.inCustomPlaylistCount ?? 0)"
                    )
                    GridStatItem(
                        label: "评分",
                        value: "\(userInfo?.userRating ?? 0)"
                    )
                    GridStatItem(
                        label: "最后播放",
                        value: formatLastPlayed(userInfo?.lastPlayed)
                    )
                    GridStatItem(
                        label: "喜欢",
                        value: (userInfo?.liked ?? false) ? "是" : "否"
                    )
                }
            }
        }
    }
    
    private func formatLastPlayed(_ timestamp: KotlinLong?) -> String {
        guard let timestamp = timestamp?.int64Value, timestamp > 0 else {
            return "从未"
        }
        let now = Date().timeIntervalSince1970 * 1000
        let diff = now - Double(timestamp)
        
        if diff < 60000 {
            return "刚刚"
        } else if diff < 3600000 {
            return "\(Int(diff / 60000))分钟前"
        } else if diff < 86400000 {
            return "\(Int(diff / 3600000))小时前"
        } else if diff < 604800000 {
            return "\(Int(diff / 86400000))天前"
        } else {
            let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
            let formatter = DateFormatter()
            formatter.dateFormat = "MM-dd"
            return formatter.string(from: date)
        }
    }
}

struct SectionTitleView<Content: View>: View {
    @Environment(HMPTheme.self) private var theme
    
    let title: String
    @ViewBuilder let content: () -> Content
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
            
            content()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct GridStatItem: View {
    @Environment(HMPTheme.self) private var theme
    
    let label: String
    let value: String
    
    var body: some View {
        VStack(spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundColor(theme.text.opacity(0.6))
                .lineLimit(1)
            
            Spacer()
            
            Text(value)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(theme.text)
                .lineLimit(1)
        }
        .padding(8)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .aspectRatio(1, contentMode: .fill)
        .background(theme.surfaceVariant.opacity(0.25))
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(theme.outline.opacity(0.1), lineWidth: 1)
        )
    }
}

struct EmptyContentView: View {
    @Environment(HMPTheme.self) private var theme
    
    let icon: String
    let message: String
    
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 40))
                .foregroundColor(theme.text.opacity(0.3))
            Text(message)
                .font(.body)
                .foregroundColor(theme.text.opacity(0.5))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
    }
}

// 临时数据模型
struct SongDetailData {
    let musicInfo: MusicInfo_
    let dailyMusicInfo: Any?
    let labels: [Any?]
    let playbackHistory: [Any]
}

#Preview {
    NavigationStack {
        SongDetailScreen(musicId: 0)
    }
}
