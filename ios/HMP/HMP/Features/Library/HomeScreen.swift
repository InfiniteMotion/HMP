import SwiftUI
import shared

/// 主页 - 与 Android HomeScreen.kt 对齐
struct HomeScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var recommendationVM = RecommendationViewModel()
    @State private var selectedMusicId: Int64? = nil

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    var body: some View {
        TabScreen(
            title: "首页",
            hasSearchButton: false,
            trailing: {
                AnyView(
                    Button {
                        HapticManager.shared.click()
                        recommendationVM.refreshDailyMusicInfo()
                    } label: {
                        Image(systemName: "arrow.clockwise.circle")
                            .font(.system(size: 18))
                            .foregroundColor(theme.text)
                    }
                )
            }
        ) {
            if recommendationVM.dailyMusic == nil {
                emptyState
            } else {
                ScrollView {
                    VStack(spacing: 0) {
                        todayRecommendationHeader
                        dailyHeroCard
                        Spacer(minLength: 24)
                        heartbeatSection
                    }
                    .padding(.bottom, 16)
                }
            }
        }
        .onAppear {
            recommendationVM.getDailyMusicInfo()
            recommendationVM.loadHeartbeatList()
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

    // MARK: - Section header

    private var todayRecommendationHeader: some View {
        Text("今日推荐")
            .font(TypographyTokens.titleLarge)
            .fontWeight(.bold)
            .foregroundColor(theme.text)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
    }

    // MARK: - Daily hero card

    private var dailyHeroCard: some View {
        let daily = recommendationVM.dailyMusic!
        return DailyHeroCard(
            title: daily.music.title,
            artist: daily.music.artist,
            albumArtUri: daily.music.albumArtUri,
            musicPath: daily.music.path,
            onTap: { selectedMusicId = daily.music.id },
            onPlay: {
                HapticManager.shared.click()
                controller.playWith(daily)
            }
        )
    }

    // MARK: - Heartbeat section

    private var heartbeatSection: some View {
        VStack(spacing: 0) {
            // Header row
            HStack {
                Text("心动歌单")
                    .font(TypographyTokens.titleLarge)
                    .fontWeight(.bold)
                    .foregroundColor(theme.text)
                Spacer()
                if !recommendationVM.heartbeatList.isEmpty {
                    Button {
                        HapticManager.shared.click()
                        controller.addAllToPlaylistInOrder(recommendationVM.heartbeatList)
                    } label: {
                        Image(systemName: "music.note.list")
                            .font(.system(size: 16))
                            .foregroundColor(.white)
                            .frame(width: 24, height: 24)
                            .background(theme.primary, in: Circle())
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            // Content
            if recommendationVM.heartbeatList.isEmpty {
                Text("正在生成心动歌单...")
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.text.opacity(0.4))
                    .frame(maxWidth: .infinity, minHeight: 100)
            } else {
                let currentId = controller.currentPlayingMusic?.music.id
                let playingIndex = currentId.flatMap { id in
                    recommendationVM.heartbeatList.firstIndex { $0.music.id == id }
                }
                FixedMusicList(
                    musicInfoList: recommendationVM.heartbeatList,
                    config: MusicListConfig(
                        header: .none,
                        item: ItemConfig.full(showRemove: false, showMenu: true),
                        currentPlayingIndex: playingIndex,
                        callbacks: heartbeatCallbacks
                    )
                )
                .padding(.horizontal, 16)
            }
        }
    }

    private var heartbeatCallbacks: MusicListCallbacks {
        let cb = MusicListCallbacks()
        cb.onItemClick = { info, index in
            HapticManager.shared.click()
            controller.addAllToPlaylistInOrder(recommendationVM.heartbeatList)
            controller.playAt(Int(index))
        }
        cb.onMenuClick = { info in
            HapticManager.shared.click()
            selectedMusicId = info.music.id
        }
        return cb
    }

    // MARK: - Empty state

    private var emptyState: some View {
        VStack(spacing: 16) {
            Text("暂无数据")
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.text.opacity(0.4))
            Text("启动app后自动刷新，或前往AI设置进行配置")
                .font(TypographyTokens.titleLarge)
                .foregroundColor(theme.text.opacity(0.4))
            NavigationLink(value: HMPRoute.ai) {
                Text("前往 AI 设置")
            }
            .buttonStyle(.borderedProminent)
            .tint(theme.primary)
            Spacer().frame(height: 32)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 200)
    }
}

// MARK: - DailyHeroCard

struct DailyHeroCard: View {
    @Environment(HMPTheme.self) private var theme

    let title: String
    let artist: String
    let albumArtUri: String?
    let musicPath: String?
    let onTap: () -> Void
    let onPlay: () -> Void

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            if let image = resolveImage() {
                Image(uiImage: image)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } else {
                RoundedRectangle(cornerRadius: 28)
                    .fill(theme.primary.opacity(0.15))
                    .overlay {
                        Image(systemName: "music.note")
                            .font(.system(size: 60))
                            .foregroundColor(theme.primary)
                    }
            }

            // Gradient: matches Android colorStops 0→Transparent, 0.5→Transparent, 1.0→Black(0.8)
            Rectangle()
                .fill(
                    LinearGradient(
                        stops: [
                            .init(color: .clear, location: 0),
                            .init(color: .clear, location: 0.5),
                            .init(color: .black.opacity(0.8), location: 1),
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )

            // Text + Play button
            VStack {
                Spacer()
                HStack(alignment: .bottom) {
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
                    Spacer()
                    Button {
                        HapticManager.shared.click()
                        onPlay()
                    } label: {
                        Image(systemName: "play.fill")
                            .font(.system(size: 28))
                            .foregroundColor(.white)
                            .frame(width: 48, height: 48)
                            .background(theme.primary, in: Circle())
                    }
                }
                .padding(24)
            }
        }
        .aspectRatio(1, contentMode: .fit)
        .clipShape(RoundedRectangle(cornerRadius: 28))
        .shadow(color: .black.opacity(0.2), radius: 8)
        .padding(.horizontal, 32)
        .padding(.vertical, 16)
        .onTapGesture {
            HapticManager.shared.click()
            onTap()
        }
    }

    private func resolveImage() -> UIImage? {
        if let uri = albumArtUri, !uri.isEmpty {
            if let image = CoverCache.shared.get(path: uri) { return image }
        }
        if let path = musicPath, !path.isEmpty {
            return CoverCache.shared.getOrExtract(musicPath: path)
        }
        return nil
    }
}
