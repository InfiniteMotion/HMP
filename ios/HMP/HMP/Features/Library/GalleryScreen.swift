import SwiftUI
import shared

struct GalleryScreen: View {
    @Environment(HMPTheme.self) private var theme
    @StateObject private var viewModel = LibraryViewModel()

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    private var sortBy: SortOption {
        SortOption.allCases.first { $0.orderByKey == viewModel.orderBy } ?? .title
    }

    private var currentPlayingIndex: Int? {
        guard let currentId = controller.currentPlayingMusic?.music.id else { return nil }
        return viewModel.musicList.firstIndex { $0.music.id == currentId }
    }

    var body: some View {
        TabScreen(title: "浏览", hasSearchButton: true) {
            if viewModel.musicList.isEmpty {
                emptyState
            } else {
                VStack(spacing: 0) {
                    sortHeader
                    ZStack(alignment: .trailing) {
                        MusicList(
                            musicInfoList: viewModel.musicList,
                            config: {
                                let cb = MusicListCallbacks()
                                cb.onItemClick = { info, _ in controller.playWith(info) }
                                return MusicListConfig(
                                    header: .none,
                                    item: .gallery(showMenu: true, extraMenuItems: [("加入播放列表", { info in controller.addToNextPlay(info) })]),
                                    currentPlayingIndex: currentPlayingIndex,
                                    callbacks: cb
                                )
                            }()
                        )
                        IndexStrip(
                            musicList: viewModel.musicList,
                            isLetterMode: sortBy.isLetterMode,
                            isReversed: viewModel.orderType == "DESC",
                            orderBy: sortBy.orderByKey
                        )
                    }
                }
            }
        }
    }

    private var sortHeader: some View {
        HStack(spacing: 8) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(SortOption.allCases, id: \.self) { option in
                        Button {
                            HapticManager.shared.click()
                            viewModel.selectSortOption(option.orderByKey)
                        } label: {
                            Text(option.rawValue)
                                .font(TypographyTokens.bodySmall)
                                .foregroundColor(sortBy == option ? .white : theme.text)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(Capsule().fill(sortBy == option ? theme.primary : theme.surface))
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            Button {
                HapticManager.shared.click()
                viewModel.toggleSortOrder()
            } label: {
                Image(systemName: viewModel.orderType == "ASC" ? "arrow.up" : "arrow.down")
                    .font(.system(size: 14))
                    .foregroundColor(theme.text)
                    .frame(width: 32, height: 32)
                    .background(theme.surface, in: Circle())
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }

    private var emptyState: some View {
        VStack(spacing: 20) {
            Image(systemName: "music.note.list")
                .font(.system(size: 60))
                .foregroundColor(theme.text.opacity(0.3))
            Text("暂无音乐")
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.text.opacity(0.4))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    enum SortOption: String, CaseIterable {
        case title = "标题"
        case artist = "艺术家"
        case album = "专辑"
        case duration = "时长"
        case dateAdded = "添加时间"
        case playCount = "播放次数"

        var orderByKey: String {
            switch self {
            case .title: return "title"
            case .artist: return "artist"
            case .album: return "album"
            case .duration: return "duration"
            case .dateAdded: return "id"
            case .playCount: return "playCount"
            }
        }

        var isLetterMode: Bool {
            switch self {
            case .title, .artist, .album: return true
            default: return false
            }
        }
    }
}
