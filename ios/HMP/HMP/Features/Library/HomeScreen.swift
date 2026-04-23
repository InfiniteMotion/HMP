import SwiftUI

/// 主页 - 对应 Android HomeScreen.kt
/// 展示每日推荐 + 心动歌单
struct HomeScreen: View {
    @Environment(HMPTheme.self) private var theme

    // TODO: 连接 ViewModel (P6 完成后)
    // @StateObject private var libraryVM: LibraryViewModel
    // @StateObject private var playbackVM: PlaybackViewModel

    /// 占位数据
    private let placeholderMusicList: [MusicItem] = []

    var body: some View {
        TabScreen(
            title: "音乐库",
            hasSearchButton: true,
            trailing: {
                AnyView(
                    Button {
                        HapticManager.shared.click()
                        // refreshDailyMusicInfo()
                    } label: {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 18))
                            .foregroundColor(theme.text)
                    }
                )
            }
        ) {
            ScrollView {
                VStack(spacing: 0) {
                    // Section 1: 今日推荐
                    TitleWidget(title: "今日推荐")

                    if placeholderMusicList.isEmpty {
                        // 空状态 - 提示配置 AI
                        VStack(spacing: 16) {
                            Text("暂无数据")
                                .font(TypographyTokens.titleLarge)
                                .foregroundColor(theme.secondaryText)
                            Text("前往配置 AI 推荐")
                                .font(TypographyTokens.titleLarge)
                                .foregroundColor(theme.secondaryText)
                            Button("前往 AI 设置") {
                                // navigate to AI
                            }
                            .buttonStyle(.bordered)
                            .padding(.top, 8)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.top, 40)
                    } else {
                        // 每日推荐卡片
                        DailyHeroCard(
                            title: placeholderMusicList[0].title,
                            artist: placeholderMusicList[0].artist,
                            albumArtUri: placeholderMusicList[0].albumArtUri
                        ) {
                            // onPlay
                        }
                    }

                    Spacer(minLength: 24)

                    // Section 2: 心动歌单
                    HStack {
                        TitleWidget(title: "心动歌单")
                        // Play all button would go here
                    }

                    if placeholderMusicList.isEmpty {
                        Text("正在生成心动歌单...")
                            .font(TypographyTokens.bodyMedium)
                            .foregroundColor(theme.secondaryText)
                            .frame(maxWidth: .infinity)
                            .padding(.top, 20)
                    } else {
                        MusicList(musicList: placeholderMusicList)
                    }
                }
            }
        }
    }
}

// MARK: - DailyHeroCard (对应 Android HomeScreen.kt:266-349)
struct DailyHeroCard: View {
    @Environment(HMPTheme.self) private var theme

    let title: String
    let artist: String
    let albumArtUri: String?
    let onPlay: () -> Void

    var body: some View {
        Button {
            HapticManager.shared.click()
            onPlay()
        } label: {
            GeometryReader { geo in
                let size = min(geo.size.width, geo.size.height)
                ZStack(alignment: .bottomLeading) {
                    AlbumCover(uri: albumArtUri, size: size)

                    // 渐变遮罩
                    Rectangle()
                        .fill(
                            LinearGradient(
                                colors: [.clear, .black.opacity(0.8)],
                                startPoint: .top,
                                endPoint: .bottom
                            )
                        )

                    VStack(alignment: .leading, spacing: 8) {
                        Text(title)
                            .font(TypographyTokens.headlineLarge)
                            .fontWeight(.black)
                            .foregroundColor(.white)
                            .lineLimit(2)

                        Text(artist)
                            .font(TypographyTokens.headlineMedium)
                            .foregroundColor(.white.opacity(0.8))
                            .lineLimit(1)
                    }
                    .padding(24)
                }
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: UIScreen.main.bounds.width * 0.75)
        .padding(.horizontal, 16)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(theme.cardBackground)
        )
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }
}

// MARK: - MusicItem 模型 (Swift 端数据封装)
struct MusicItem: Identifiable, Hashable {
    let id: Int64
    let title: String
    let artist: String
    let album: String
    let durationMs: Int64
    let albumArtUri: String?
    let path: String

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }

    static func == (lhs: MusicItem, rhs: MusicItem) -> Bool {
        lhs.id == rhs.id
    }
}

// MARK: - MusicList (歌曲列表组件)
struct MusicList: View {
    @Environment(HMPTheme.self) private var theme

    let musicList: [MusicItem]
    let showIndex: Bool
    let showMenu: Bool
    let onItemClick: ((MusicItem, Int) -> Void)?
    let onMenuClick: ((MusicItem) -> Void)?

    init(
        musicList: [MusicItem],
        showIndex: Bool = false,
        showMenu: Bool = true,
        onItemClick: ((MusicItem, Int) -> Void)? = nil,
        onMenuClick: ((MusicItem) -> Void)? = nil
    ) {
        self.musicList = musicList
        self.showIndex = showIndex
        self.showMenu = showMenu
        self.onItemClick = onItemClick
        self.onMenuClick = onMenuClick
    }

    var body: some View {
        LazyVStack(spacing: 0) {
            ForEach(Array(musicList.enumerated()), id: \.element.id) { index, music in
                MusicRow(
                    index: showIndex ? index + 1 : nil,
                    music: music,
                    showMenu: showMenu
                ) {
                    HapticManager.shared.lightClick()
                    onItemClick?(music, index)
                } onMenu: {
                    HapticManager.shared.click()
                    onMenuClick?(music)
                }
                Divider()
                        .padding(.leading, 60)
            }
        }
    }
}

struct MusicRow: View {
    @Environment(HMPTheme.self) private var theme

    let index: Int?
    let music: MusicItem
    let showMenu: Bool
    let onTap: () -> Void
    let onMenu: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            if let index = index {
                Text("\(index)")
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.secondaryText)
                    .frame(width: 24)
            }

            AlbumCover(uri: music.albumArtUri, size: 48, cornerRadius: 8)

            VStack(alignment: .leading, spacing: 2) {
                Text(music.title)
                    .font(TypographyTokens.titleMedium)
                    .foregroundColor(theme.text)
                    .lineLimit(1)

                Text(music.artist)
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(theme.secondaryText)
                    .lineLimit(1)
            }

            Spacer()

            if showMenu {
                Button {
                    onMenu()
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 16))
                        .foregroundColor(theme.secondaryText)
                }
            }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            onTap()
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 16)
    }
}
