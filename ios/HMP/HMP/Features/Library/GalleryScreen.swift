import SwiftUI
import shared

/// 浏览页 - 对应 Android GalleryScreen.kt
/// 全曲库 + 排序过滤 + 完整 MusicList
struct GalleryScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var viewModel = LibraryViewModel()
    @State private var sortBy: SortOption = .title
    @State private var sortOrder: SortOrder = .ascending
    @State private var showLibrarySettings = false

    enum SortOption: String, CaseIterable {
        case title = "标题"
        case artist = "艺术家"
        case album = "专辑"

        var androidKey: String {
            switch self {
            case .title: return "title"
            case .artist: return "artist"
            case .album: return "album"
            }
        }
    }

    enum SortOrder: String, CaseIterable {
        case ascending = "升序"
        case descending = "降序"

        var androidKey: String {
            switch self {
            case .ascending: return "ASC"
            case .descending: return "DESC"
            }
        }
    }

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    private var sortedMusicList: [MusicInfo_] {
        let list = viewModel.musicList
        return list.sorted { a, b in
            let result: Bool
            switch sortBy {
            case .title:
                result = a.music.title < b.music.title
            case .artist:
                result = a.music.artist < b.music.artist
            case .album:
                result = a.music.album < b.music.album
            }
            return sortOrder == .ascending ? result : !result
        }
    }

    var body: some View {
        TabScreen(
            title: "浏览",
            hasSearchButton: true
        ) {
            if viewModel.musicList.isEmpty {
                // Empty state with link to library settings
                VStack(spacing: 20) {
                    Image(systemName: "music.note.list")
                        .font(.system(size: 60))
                        .foregroundColor(theme.text.opacity(0.3))

                    Text("暂无音乐")
                        .font(TypographyTokens.titleLarge)
                        .foregroundColor(theme.text.opacity(0.4))

                    Text("请导入音乐文件或前往音乐库设置扫描")
                        .font(TypographyTokens.bodyMedium)
                        .foregroundColor(theme.text.opacity(0.3))
                        .multilineTextAlignment(.center)

                    Button {
                        showLibrarySettings = true
                    } label: {
                        HStack(spacing: 8) {
                            Image(systemName: "arrow.clockwise")
                            Text("前往音乐库设置")
                        }
                        .font(TypographyTokens.bodyMedium)
                        .foregroundColor(.white)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 12)
                        .background(
                            RoundedRectangle(cornerRadius: 24)
                                .fill(ColorTokens.hdRed)
                        )
                    }
                    .padding(.top, 8)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .sheet(isPresented: $showLibrarySettings) {
                    NavigationStack {
                        LibrarySettingsScreen()
                    }
                }
            } else {
                VStack(spacing: 0) {
                    // Sort header
                    HStack(spacing: 8) {
                        Picker("排序", selection: $sortBy) {
                            ForEach(SortOption.allCases, id: \.self) { option in
                                Text(option.rawValue).tag(option)
                            }
                        }
                        .pickerStyle(.segmented)

                        Picker("顺序", selection: $sortOrder) {
                            ForEach(SortOrder.allCases, id: \.self) { option in
                                Text(option.rawValue).tag(option)
                            }
                        }
                        .pickerStyle(.segmented)
                        .frame(width: 80)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)

                    // Music list
                    MusicList(
                        musicInfoList: sortedMusicList,
                        config: {
                            let cb = MusicListCallbacks()
                            cb.onItemClick = { info, _ in
                                controller.playWith(info)
                            }
                            return MusicListConfig(
                                header: .none,
                                item: .gallery(showMenu: true, extraMenuItems: [("加入播放列表", { (_: MusicInfo_) in })]),
                                currentPlayingIndex: nil,
                                callbacks: cb
                            )
                        }()
                    )
                }
            }
        }
    }
}
