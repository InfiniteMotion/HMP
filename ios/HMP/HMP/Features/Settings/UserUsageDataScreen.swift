import SwiftUI
import shared

/// 用户使用数据 — 对应 Android UserUsageDataScreen.kt
struct UserUsageDataScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss
    @State private var vm = UserUsageDataViewModel()

    var body: some View {
        SubScreen(title: "使用数据") {
            switch vm.state {
            case .idle, .loading:
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            case .error(let msg):
                VStack(spacing: 16) {
                    Text(msg).foregroundColor(.red)
                    Button("重试") { vm.load() }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .empty:
                Text("暂无数据").foregroundColor(theme.text.opacity(0.4))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .success(let data):
                ScrollView {
                    VStack(spacing: 20) {
                        overviewSection(data)
                        topListsSection(data)
                        recentHistorySection(data)
                    }
                    .padding(16)
                }
            }
        }
        .onAppear { vm.load() }
    }

    // MARK: - Overview

    private func overviewSection(_ data: UserUsageAnalytics) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("概览").font(TypographyTokens.titleLarge).fontWeight(.bold)

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                statCard("播放次数", "\(data.totalPlayCount)")
                statCard("跳过次数", "\(data.totalSkipCount)")
                statCard("喜欢数", "\(data.likedCount)")
                statCard("总听歌", "\(data.totalListeningMinutes)分钟")
                statCard("平均时长", String(format: "%.1f分", data.averageSessionMinutes))
                statCard("完成率", String(format: "%.0f%%", data.completionRate * 100))
            }

            HStack {
                statRow("本周", "\(data.thisWeekMinutes)分钟")
                statRow("上周", "\(data.lastWeekMinutes)分钟")
            }
        }
    }

    private func statCard(_ label: String, _ value: String) -> some View {
        VStack(spacing: 4) {
            Text(label).font(.caption2).foregroundColor(theme.text.opacity(0.5))
            Text(value).font(TypographyTokens.titleMedium).fontWeight(.black)
        }
        .frame(maxWidth: .infinity).aspectRatio(1, contentMode: .fit).padding(8)
        .background(theme.surface.opacity(0.25), in: RoundedRectangle(cornerRadius: 20))
    }

    private func statRow(_ label: String, _ value: String) -> some View {
        HStack {
            Text(label).font(TypographyTokens.bodySmall).foregroundColor(theme.text.opacity(0.5))
            Text(value).font(TypographyTokens.titleSmall).fontWeight(.bold)
            Spacer()
        }
        .padding(12)
        .background(theme.surface.opacity(0.25), in: RoundedRectangle(cornerRadius: 12))
    }

    // MARK: - Top Lists

    private func topListsSection(_ data: UserUsageAnalytics) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("排行").font(TypographyTokens.titleLarge).fontWeight(.bold)

            if !data.topGenres.isEmpty {
                tagRow("热门流派", data.topGenres.map { "\($0.labelDisplayName)(\($0.count))" })
            }
            if !data.topMoods.isEmpty {
                tagRow("热门心情", data.topMoods.map { "\($0.labelDisplayName)(\($0.count))" })
            }
            if !data.topScenarios.isEmpty {
                tagRow("热门场景", data.topScenarios.map { "\($0.labelDisplayName)(\($0.count))" })
            }
            if !data.topArtists.isEmpty {
                VStack(alignment: .leading, spacing: 4) {
                    Text("热门歌手").font(TypographyTokens.titleSmall).fontWeight(.bold)
                    ForEach(data.topArtists.indices, id: \.self) { idx in
                        let a = data.topArtists[idx]
                        HStack {
                            Text(a.artistName).font(TypographyTokens.bodyMedium)
                            Spacer()
                            Text("\(a.playCount)次").font(.caption).foregroundColor(theme.text.opacity(0.4))
                        }
                        .padding(.vertical, 2)
                    }
                }
                .padding(12)
                .background(theme.surface.opacity(0.25), in: RoundedRectangle(cornerRadius: 12))
            }
        }
    }

    private func tagRow(_ title: String, _ tags: [String]) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title).font(TypographyTokens.titleSmall).fontWeight(.bold)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(tags, id: \.self) { tag in
                        Text(tag)
                            .font(.caption).padding(.horizontal, 10).padding(.vertical, 4)
                            .background(theme.primary.opacity(0.1), in: Capsule())
                    }
                }
            }
        }
        .padding(12)
        .background(theme.surface.opacity(0.25), in: RoundedRectangle(cornerRadius: 12))
    }

    // MARK: - Recent History

    private func recentHistorySection(_ data: UserUsageAnalytics) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            if !data.topPlayedSongs.isEmpty {
                Text("热门歌曲").font(TypographyTokens.titleLarge).fontWeight(.bold)
                ForEach(data.topPlayedSongs.indices, id: \.self) { idx in
                    let s = data.topPlayedSongs[idx]
                    HStack {
                        Text("\(idx + 1).").font(.caption).foregroundColor(theme.text.opacity(0.4)).frame(width: 20)
                        VStack(alignment: .leading) {
                            Text(s.title).font(TypographyTokens.bodyMedium).lineLimit(1)
                            Text(s.artist).font(.caption).foregroundColor(theme.text.opacity(0.4))
                        }
                        Spacer()
                        Text("\(s.playCount)次").font(.caption).foregroundColor(theme.text.opacity(0.4))
                    }
                    .padding(.vertical, 4)
                }
            }

            if !data.recentPlaybackWithTitle.isEmpty {
                Text("最近播放").font(TypographyTokens.titleLarge).fontWeight(.bold).padding(.top, 8)
                ForEach(data.recentPlaybackWithTitle.indices, id: \.self) { idx in
                    let r = data.recentPlaybackWithTitle[idx]
                    HStack {
                        VStack(alignment: .leading) {
                            Text(r.title).font(TypographyTokens.bodyMedium).lineLimit(1)
                            Text(r.artist).font(.caption).foregroundColor(theme.text.opacity(0.4))
                        }
                        Spacer()
                        Text(r.isCompleted ? "完成" : "未完成")
                            .font(.caption2)
                            .foregroundColor(r.isCompleted ? theme.primary : .red)
                    }
                    .padding(.vertical, 2)
                }
            }
        }
    }
}

// MARK: - ViewModel

@Observable
class UserUsageDataViewModel {
    var state: UiState<UserUsageAnalytics> = .idle

    func load() {
        state = .loading
        Task {
            do {
                let data = try await KoinHelperKt.getUserUsageAnalytics()
                await MainActor.run { self.state = .success(data) }
            } catch {
                await MainActor.run { self.state = .error(error.localizedDescription) }
            }
        }
    }
}
