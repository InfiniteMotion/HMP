import SwiftUI
import shared

/// 列表页 - 对应 Android ListScreen.kt
struct ListScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var playlistVM = PlaylistViewModel()
    @State private var currentPlaylistId: Int64? = nil
    @State private var likedPlaylistId: Int64? = nil
    @State private var recentPlaylistId: Int64? = nil
    @State private var showCreateDialog = false

    var body: some View {
        TabScreen(title: "我的歌单", hasSearchButton: false) {
            AnyView(
                Button {
                    HapticManager.shared.click()
                    showCreateDialog = true
                } label: {
                    Image(systemName: "plus.circle.fill")
                        .font(.system(size: 20))
                        .foregroundColor(theme.primary)
                }
                .buttonStyle(.plain)
            )
        } content: {
            ScrollView {
                VStack(spacing: 24) {
                    // 用户歌单 - 横向滚动卡片
                    userPlaylistsSection

                    // 常用列表 - 3 个横幅卡片
                    commonPlaylistsSection

                    // 适用场景 - 沉浸推荐
                    if !playlistVM.scenarioPlaylistName.isEmpty {
                        LabelListGroup(
                            title: "适用场景",
                            subtitle: "沉浸推荐",
                            themeColor: Color(red: 0.2, green: 0.8, blue: 0.3)
                        ) {
                            scenarioHorizontalScroll
                        }
                    }

                    // 风格流派 - 横向画廊
                    if !playlistVM.genrePlaylistName.isEmpty {
                        LabelListGroup(
                            title: "风格流派",
                            subtitle: "音乐探索",
                            themeColor: theme.primary
                        ) {
                            genreHorizontalScroll
                        }
                    }

                    // 音乐情绪 - 网格探索
                    if !playlistVM.moodPlaylistName.isEmpty {
                        LabelListGroup(
                            title: "音乐情绪",
                            subtitle: "心情匹配",
                            themeColor: .orange
                        ) {
                            moodGridSection
                        }
                    }

                    // 探索更多 - 语言 & 年代 标签云
                    let exploreItems = playlistVM.languagePlaylistName + playlistVM.eraPlaylistName
                    if !exploreItems.isEmpty {
                        LabelListGroup(
                            title: "探索更多",
                            subtitle: "",
                            themeColor: Color(red: 0.5, green: 0.3, blue: 0.8)
                        ) {
                            exploreFlowRow(items: exploreItems)
                        }
                    }

                    Spacer().frame(height: 72)
                }
                .padding(.horizontal, 0)
            }
        }
        .onAppear {
            playlistVM.loadLabels()
            playlistVM.loadUserCustomPlaylists()
            Task {
                currentPlaylistId = await playlistVM.getCurrentPlaylistId()
                likedPlaylistId = await playlistVM.getLikedPlaylistId()
                recentPlaylistId = await playlistVM.getRecentPlaylistId()
            }
        }
        .sheet(isPresented: $showCreateDialog) {
            CreatePlaylistDialog { name, _ in
                playlistVM.createPlaylist(name: name)
            }
        }
    }

    // MARK: - User Playlists Section
    private var userPlaylistsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            ListSectionHeader(
                title: "我的歌单",
                subtitle: "每日推荐",
                themeColor: Color(red: 0.5, green: 0.3, blue: 0.8)
            ) {
                NavigationLink {
                    PlaylistManageScreen()
                } label: {
                    Text("管理").font(TypographyTokens.bodySmall).foregroundColor(theme.primary)
                }
            }

            if playlistVM.userCustomPlaylists.isEmpty {
                Text("暂无歌单")
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.text.opacity(0.4))
                    .frame(maxWidth: .infinity)
                    .padding(.top, 20)
            } else {
                let sortedPlaylists = sortedUserPlaylists
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 16) {
                        ForEach(sortedPlaylists, id: \.id) { playlist in
                            NavigationLink {
                                PlaylistScreen(playlistId: playlist.id)
                            } label: {
                                UserPlaylistCard(playlist: playlist, firstSongMusicPath: playlistVM.userPlaylistFirstSongPaths[playlist.id])
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 20)
                }
            }
        }
    }

    private var sortedUserPlaylists: [Playlist_] {
        playlistVM.userCustomPlaylists.sorted { a, b in
            if a.isPinned != b.isPinned { return a.isPinned }
            let aLastPlayed = a.lastPlayedAt?.int64Value ?? 0
            let bLastPlayed = b.lastPlayedAt?.int64Value ?? 0
            if aLastPlayed != bLastPlayed { return aLastPlayed > bLastPlayed }
            return a.updatedAt > b.updatedAt
        }
    }

    // MARK: - Common Playlists Section
    private var commonPlaylistsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            ListSectionHeader(
                title: "常用",
                subtitle: "快速访问",
                themeColor: .red
            )

            HStack(spacing: 0) {
                commonBannerItem(id: currentPlaylistId, title: "默认", icon: "music.note.list", accent: theme.primary)
                Spacer()
                commonBannerItem(id: likedPlaylistId, title: "红心", icon: "heart.fill", accent: .red)
                Spacer()
                commonBannerItem(id: recentPlaylistId, title: "最近", icon: "clock.fill", accent: .orange)
            }
            .padding(.horizontal, 20)
        }
    }

    @ViewBuilder
    private func commonBannerItem(id: Int64?, title: String, icon: String, accent: Color) -> some View {
        if let playlistId = id {
            NavigationLink {
                PlaylistScreen(playlistId: playlistId)
            } label: {
                CommonBannerView(title: title, icon: icon, accent: accent)
            }
            .buttonStyle(.plain)
        } else {
            CommonBannerView(title: title, icon: icon, accent: accent)
        }
    }

    // MARK: - Scenario Horizontal Scroll
    private var scenarioHorizontalScroll: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 16) {
                ForEach(playlistVM.scenarioPlaylistName, id: \.self) { label in
                    NavigationLink {
                        PlaylistScreen(playlistName: label)
                    } label: {
                        ScenarioCardView(label: label)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 20)
        }
    }

    // MARK: - Genre Horizontal Scroll
    private var genreHorizontalScroll: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 16) {
                ForEach(playlistVM.genrePlaylistName, id: \.self) { label in
                    NavigationLink {
                        PlaylistScreen(playlistName: label)
                    } label: {
                        GenreCardView(label: label)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 20)
        }
    }

    // MARK: - Mood Grid Section (横向滚动单行)
    private var moodGridSection: some View {
        let displayItems = Array(playlistVM.moodPlaylistName.prefix(4))
        return ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 16) {
                ForEach(displayItems, id: \.self) { label in
                    NavigationLink {
                        PlaylistScreen(playlistName: label)
                    } label: {
                        MoodCardView(label: label)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 20)
        }
    }

    // MARK: - Explore Flow Row (模拟 FlowRow 标签云)
    @ViewBuilder
    private func exploreFlowRow(items: [String]) -> some View {
        FlowRowView(items: items, color: theme.primary)
    }
}

// MARK: - List Section Header (对应 Android ListGroupName)
struct ListSectionHeader<Trailing: View>: View {
    @Environment(HMPTheme.self) private var theme
    let title: String
    let subtitle: String
    let themeColor: Color
    @ViewBuilder let trailing: () -> Trailing

    init(
        title: String,
        subtitle: String,
        themeColor: Color,
        @ViewBuilder trailing: @escaping () -> Trailing = { EmptyView() }
    ) {
        self.title = title
        self.subtitle = subtitle
        self.themeColor = themeColor
        self.trailing = trailing
    }

    var body: some View {
        HStack(alignment: .center, spacing: 0) {
            Text(title)
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.text)
            
            Circle()
                .fill(themeColor)
                .frame(width: 8, height: 8)
                .padding(.horizontal, 8)
            
            Text(subtitle)
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.text)
            
            Spacer()
            
            trailing()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 8)
    }
}

// MARK: - Label List Group
struct LabelListGroup<Content: View>: View {
    @Environment(HMPTheme.self) private var theme
    let title: String
    let subtitle: String
    let themeColor: Color
    @ViewBuilder let content: () -> Content

    init(
        title: String,
        subtitle: String,
        themeColor: Color,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.title = title
        self.subtitle = subtitle
        self.themeColor = themeColor
        self.content = content
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            ListSectionHeader(
                title: title,
                subtitle: subtitle,
                themeColor: themeColor
            )
            content()
        }
    }
}

// MARK: - User Playlist Card
private let USER_PLAYLIST_CARD_WIDTH: CGFloat = 280
private let USER_PLAYLIST_CARD_HEIGHT: CGFloat = 360

struct UserPlaylistCard: View {
    @Environment(HMPTheme.self) private var theme
    let playlist: Playlist_
    let firstSongMusicPath: String?

    @State private var fallbackImage: UIImage? = nil

    var body: some View {
        VStack(spacing: 0) {
            ZStack(alignment: .bottomLeading) {
                if let image = fallbackImage {
                    Image(uiImage: image)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: USER_PLAYLIST_CARD_WIDTH, height: USER_PLAYLIST_CARD_HEIGHT - 50)
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                } else if let coverUri = playlist.coverUri, !coverUri.isEmpty, let image = CoverCache.shared.get(path: coverUri) {
                    Image(uiImage: image)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: USER_PLAYLIST_CARD_WIDTH, height: USER_PLAYLIST_CARD_HEIGHT - 50)
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                } else {
                    RoundedRectangle(cornerRadius: 20)
                        .fill(LinearGradient(
                            colors: [theme.primary.opacity(0.2), theme.primary.opacity(0.1)],
                            startPoint: .top,
                            endPoint: .bottom
                        ))
                        .frame(width: USER_PLAYLIST_CARD_WIDTH, height: USER_PLAYLIST_CARD_HEIGHT - 50)
                        .overlay {
                            Image(systemName: "music.note.list")
                                .font(.system(size: 72))
                                .foregroundColor(theme.primary.opacity(0.6))
                        }
                }

                LinearGradient(
                    stops: [
                        .init(color: .clear, location: 0),
                        .init(color: .clear, location: 0.5),
                        .init(color: .black.opacity(0.8), location: 1)
                    ],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .frame(width: USER_PLAYLIST_CARD_WIDTH, height: USER_PLAYLIST_CARD_HEIGHT - 50)
                .clipShape(RoundedRectangle(cornerRadius: 20))

                VStack(alignment: .leading, spacing: 4) {
                    Text(playlist.name)
                        .font(TypographyTokens.titleMedium.bold())
                        .foregroundColor(.white)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)

                    if playlist.songCount > 0 || playlist.totalDurationMs > 0 {
                        let statsText = buildStatsText()
                        if !statsText.isEmpty {
                            Text(statsText)
                                .font(TypographyTokens.bodySmall)
                                .foregroundColor(.white.opacity(0.8))
                                .lineLimit(1)
                        }
                    }
                }
                .padding(.bottom, 16)
                .padding(.leading, 16)
                .padding(.trailing, 16)
            }
        }
        .frame(width: USER_PLAYLIST_CARD_WIDTH, height: USER_PLAYLIST_CARD_HEIGHT)
        .shadow(color: .black.opacity(0.15), radius: 10)
        .task(id: playlist.coverUri ?? "") { resolveCover() }
    }

    private func resolveCover() {
        if let coverUri = playlist.coverUri, !coverUri.isEmpty, CoverCache.shared.get(path: coverUri) != nil {
            return
        }
        guard let musicPath = firstSongMusicPath, !musicPath.isEmpty else { return }
        if let image = CoverCache.shared.getOrExtractSync(musicPath: musicPath) {
            fallbackImage = image
            return
        }
        let path = musicPath
        Task.detached(priority: .utility) {
            let extractor = ArtworkExtractor()
            guard let coverPath = extractor.extractAndSave(filePath: path) else { return }
            if let image = UIImage(contentsOfFile: coverPath) {
                CoverCache.shared.put(path: coverPath, image: image)
                await MainActor.run { fallbackImage = image }
            }
        }
    }

    private func buildStatsText() -> String {
        var parts: [String] = []
        if playlist.songCount > 0 {
            parts.append("\(playlist.songCount) 首")
        }
        if playlist.totalDurationMs > 0 {
            let minutes = playlist.totalDurationMs / 60000
            if minutes > 0 {
                parts.append("\(minutes) 分钟")
            }
        }
        if playlist.playCount > 0 && parts.count < 2 {
            parts.append("播放 \(playlist.playCount) 次")
        }
        return parts.prefix(2).joined(separator: " · ")
    }
}

// MARK: - Common Banner View
struct CommonBannerView: View {
    @Environment(HMPTheme.self) private var theme
    let title: String
    let icon: String
    let accent: Color

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 48))
                .foregroundColor(accent)
                .frame(width: 100, height: 100)
                .background(accent.opacity(0.12))
                .clipShape(RoundedRectangle(cornerRadius: 15))
                .shadow(color: .black.opacity(0.1), radius: 5)
            Text(title)
                .font(TypographyTokens.bodySmall)
                .foregroundColor(theme.text)
                .lineLimit(1)
        }
        .frame(width: 110)
    }
}

// MARK: - Scenario Card (对应 Android ScenarioCard)
struct ScenarioCardView: View {
    @Environment(HMPTheme.self) private var theme
    let label: String

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            SharedLabelIcon(iconName: label, size: 280)
                .scaledToFill()
                .frame(width: 280, height: 160)
                .clipped()

            LinearGradient(
                colors: [.clear, .black.opacity(0.4), .black.opacity(0.8)],
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(width: 280, height: 160)

            VStack(alignment: .leading, spacing: 4) {
                Text(label)
                    .font(TypographyTokens.headlineSmall.bold())
                    .foregroundColor(.white)
                Text("适合现在听")
                    .font(TypographyTokens.bodySmall)
                    .foregroundColor(.white.opacity(0.8))
            }
            .padding(.bottom, 16)
            .padding(.leading, 16)
        }
        .frame(width: 280, height: 160)
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }
}

// MARK: - Genre Card (对应 Android GenreCard)
struct GenreCardView: View {
    @Environment(HMPTheme.self) private var theme
    let label: String

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            SharedLabelIcon(iconName: label, size: 160)
                .scaledToFill()
                .frame(width: 160, height: 100)
                .clipped()

            LinearGradient(
                colors: [.clear, .black.opacity(0.7)],
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(width: 160, height: 100)

            Text(label)
                .font(TypographyTokens.headlineSmall.bold())
                .foregroundColor(.white)
                .padding(.bottom, 12)
                .padding(.leading, 12)
        }
        .frame(width: 160, height: 100)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// MARK: - Mood Card (对应 Android MoodCard)
struct MoodCardView: View {
    @Environment(HMPTheme.self) private var theme
    let label: String

    var body: some View {
        ZStack {
            SharedLabelIcon(iconName: label, size: 100)
                .scaledToFill()
                .frame(width: 100, height: 100)
                .clipped()
        }
        .frame(width: 100, height: 100)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

// MARK: - Flow Row View (流式标签云，每行3个自动换行)
struct FlowRowView: View {
    @Environment(HMPTheme.self) private var theme
    let items: [String]
    let color: Color

    var body: some View {
        let rows: [[String]] = {
            var result: [[String]] = []
            for i in stride(from: 0, to: items.count, by: 3) {
                result.append(Array(items[i..<min(i + 3, items.count)]))
            }
            return result
        }()

        VStack(spacing: 8) {
            ForEach(rows, id: \.self) { rowItems in
                HStack(spacing: 12) {
                    ForEach(rowItems, id: \.self) { label in
                        NavigationLink {
                            PlaylistScreen(playlistName: label)
                        } label: {
                            CapsuleTag(label, color: color)
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 20)
            }
        }
    }
}
