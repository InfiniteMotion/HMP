import SwiftUI
import shared

/// 搜索页 - 对应 Android SearchScreen.kt
struct SearchScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss

    @State private var viewModel = SearchViewModel()
    @State private var searchText = ""
    @State private var debounceTask: Task<Void, Never>?
    @State private var selectedMusicId: Int64? = nil

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    private var searchResults: [MusicInfo_] {
        switch viewModel.searchState {
        case .success(let results): return results
        default: return []
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Search bar
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(theme.text.opacity(0.4))
                TextField("搜索音乐、歌手、专辑", text: $searchText)
                    .font(TypographyTokens.bodyMedium)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                if !searchText.isEmpty {
                    Button {
                        searchText = ""
                        viewModel.searchState = .idle
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(theme.text.opacity(0.4))
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(theme.surface)
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .padding(.horizontal, 16)
            .padding(.top, 8)

            switch viewModel.searchState {
            case .idle:
                VStack(spacing: 12) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 40))
                        .foregroundColor(theme.text.opacity(0.4))
                    Text("输入关键词搜索歌曲、歌手或专辑")
                        .font(TypographyTokens.bodyMedium)
                        .foregroundColor(theme.text.opacity(0.4))
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .empty:
                VStack(spacing: 12) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 40))
                        .foregroundColor(theme.text.opacity(0.4))
                    Text("未找到 \"\(searchText)\"")
                        .font(TypographyTokens.bodyLarge)
                        .foregroundColor(theme.text.opacity(0.4))
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .success:
                MusicList(
                    musicInfoList: searchResults,
                    config: {
                        let cb = MusicListCallbacks()
                        cb.onItemClick = { info, _ in
                            controller.playWith(info)
                        }
                        cb.onMenuClick = { info in
                            selectedMusicId = info.music.id
                        }
                        return MusicListConfig(
                            header: .none,
                            item: .full(showMenu: true),
                            currentPlayingIndex: nil,
                            callbacks: cb
                        )
                    }()
                )
            case .error(let message):
                VStack(spacing: 12) {
                    Text("搜索失败")
                        .font(TypographyTokens.bodyMedium)
                        .foregroundColor(theme.text.opacity(0.4))
                    Text(message)
                        .font(TypographyTokens.bodySmall)
                        .foregroundColor(.red)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationTitle("搜索")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("取消") { dismiss() }
            }
        }
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
        .background {
            if let musicId = selectedMusicId {
                NavigationLink(value: HMPRoute.songDetail(musicId: musicId)) {
                    EmptyView()
                }
                .hidden()
            }
        }
    }
}
