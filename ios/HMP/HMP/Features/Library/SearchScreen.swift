import SwiftUI

/// 搜索页 - 对应 Android SearchScreen.kt
/// iOS 使用原生 .searchable modifier，大幅简化
struct SearchScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss

    @State private var searchText = ""
    @State private var searchResults: [MusicItem] = []
    @State private var isSearching = false

    // TODO: 连接 SearchViewModel (P6 完成后)
    // @StateObject private var searchVM: SearchViewModel

    var body: some View {
        NavigationStack {
            List {
                if searchText.isEmpty {
                    // 默认空状态或历史记录
                    Section("搜索音乐") {
                        Text("输入关键词搜索歌曲、歌手或专辑")
                            .font(TypographyTokens.bodyMedium)
                            .foregroundColor(theme.secondaryText)
                    }
                } else if isSearching {
                    Section {
                        ProgressView()
                    }
                } else if searchResults.isEmpty {
                    Section {
                        VStack(spacing: 12) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 40))
                                .foregroundColor(theme.secondaryText)
                            Text("未找到 \"\(searchText)\"")
                                .font(TypographyTokens.bodyLarge)
                                .foregroundColor(theme.secondaryText)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 20)
                    }
                } else {
                    Section("搜索结果 (\(searchResults.count))") {
                        ForEach(searchResults) { music in
                            MusicRow(
                                index: nil,
                                music: music,
                                showMenu: true
                            ) {
                                // onTap: play or navigate
                            } onMenu: {
                                // onMenu: show detail dialog
                            }
                        }
                    }
                }
            }
            .listStyle(.plain)
            .navigationTitle("搜索")
            .navigationBarTitleDisplayMode(.inline)
            .searchable(text: $searchText, prompt: "搜索音乐、歌手、专辑")
            .onChange(of: searchText) { _, newValue in
                // TODO: debounce 搜索
                if newValue.isEmpty {
                    searchResults = []
                } else {
                    performSearch(query: newValue)
                }
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
            }
        }
    }

    private func performSearch(query: String) {
        // TODO: searchVM.search(query)
    }
}
