import SwiftUI
import shared

/// 搜索页 - 对应 Android SearchScreen.kt
struct SearchScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss

    @State private var viewModel = SearchViewModel()
    @State private var searchText = ""
    @State private var debounceTask: Task<Void, Never>?

    var body: some View {
        NavigationStack {
            List {
                switch viewModel.searchState {
                case .idle:
                    Section("搜索音乐") {
                        Text("输入关键词搜索歌曲、歌手或专辑")
                            .font(TypographyTokens.bodyMedium)
                            .foregroundColor(theme.text.opacity(0.4))
                    }
                case .loading:
                    Section { ProgressView() }
                case .empty:
                    Section {
                        VStack(spacing: 12) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 40))
                                .foregroundColor(theme.text.opacity(0.4))
                            Text("未找到 \"\(searchText)\"")
                                .font(TypographyTokens.bodyLarge)
                                .foregroundColor(theme.text.opacity(0.4))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 20)
                    }
                case .success(let results):
                    Section("搜索结果 (\(results.count))") {
                        ForEach(results, id: \.music.id) { info in
                            MusicInfoRow(info: info, theme: theme)
                                .onTapGesture {
                                    MusicPlayerController.shared.playWith(info)
                                }
                        }
                    }
                case .error(let message):
                    Section {
                        Text(message)
                            .foregroundColor(.red)
                    }
                }
            }
            .listStyle(.plain)
            .navigationTitle("搜索")
            .navigationBarTitleDisplayMode(.inline)
            .searchable(text: $searchText, prompt: "搜索音乐、歌手、专辑")
            .onChange(of: searchText) { _, newValue in
                debounceTask?.cancel()
                if newValue.isEmpty {
                    viewModel.searchState = .idle
                } else {
                    debounceTask = Task {
                        try? await Task.sleep(nanoseconds: 300_000_000)
                        guard !Task.isCancelled else { return }
                        viewModel.searchMusic(query: newValue)
                    }
                }
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
            }
        }
    }
}

private struct MusicInfoRow: View {
    let info: MusicInfo_
    let theme: HMPTheme

    var body: some View {
        HStack(spacing: 12) {
            AlbumCover(uri: info.music.albumArtUri, musicPath: info.music.path, size: 48, cornerRadius: 6)

            VStack(alignment: .leading, spacing: 2) {
                Text(info.music.title).font(TypographyTokens.bodyMedium).foregroundColor(theme.text).lineLimit(1)
                Text("\(info.music.artist) · \(info.music.album)").font(TypographyTokens.bodySmall).foregroundColor(theme.text.opacity(0.6)).lineLimit(1)
            }
            Spacer()
            Text(formatDuration(info.music.duration)).font(TypographyTokens.bodySmall).foregroundColor(theme.text.opacity(0.4))
        }
        .padding(.vertical, 4)
    }

    private func formatDuration(_ ms: Int64) -> String {
        let seconds = ms / 1000
        return String(format: "%d:%02d", seconds / 60, seconds % 60)
    }
}
