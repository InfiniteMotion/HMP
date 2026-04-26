import SwiftUI
import shared

/// 主页 - 对应 Android HomeScreen.kt
/// 展示每日推荐 + 心动歌单
struct HomeScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var recommendationVM = RecommendationViewModel()

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
            ScrollView {
                VStack(spacing: 0) {
                    if let daily = recommendationVM.dailyMusic {
                        // Section 1: 今日推荐
                        TitleWidget(title: "今日推荐")

                        DailyHeroCard(
                            title: daily.music.title,
                            artist: daily.music.artist,
                            albumArtUri: daily.music.albumArtUri,
                            musicPath: daily.music.path
                        ) {
                            controller.playWith(daily)
                        }

                        Spacer(minLength: 24)

                        // Section 2: 心动歌单
                        HStack {
                            TitleWidget(title: "心动歌单")
                            Spacer()
                            if !recommendationVM.heartbeatList.isEmpty {
                                Button {
                                    controller.addAllToPlaylistInOrder(recommendationVM.heartbeatList)
                                } label: {
                                    HStack(spacing: 4) {
                                        Image(systemName: "play.fill").font(.system(size: 10))
                                        Text("播放全部").font(TypographyTokens.bodySmall)
                                    }
                                    .foregroundColor(theme.primary)
                                }
                                .padding(.trailing, 16)
                            }
                        }

                        if recommendationVM.heartbeatList.isEmpty {
                            Text("正在生成心动歌单...")
                                .font(TypographyTokens.bodyMedium)
                                .foregroundColor(theme.text.opacity(0.4))
                                .frame(maxWidth: .infinity)
                                .padding(.top, 20)
                        } else {
                            FixedMusicList(
                                musicInfoList: recommendationVM.heartbeatList,
                                config: .galleryPreset(
                                    callbacks: {
                                        let cb = MusicListCallbacks()
                                        cb.onItemClick = { info, _ in
                                            controller.playWith(info)
                                        }
                                        return cb
                                    }()
                                )
                            )
                            .padding(.horizontal, 16)
                        }
                    } else {
                        // 无数据状态：只显示提示和跳转按钮
                        VStack(spacing: 16) {
                            Text("暂无数据")
                                .font(TypographyTokens.titleLarge)
                                .foregroundColor(theme.text.opacity(0.4))
                            Button("前往 AI 设置") {
                                // TODO: navigate to AI
                            }
                            .buttonStyle(.bordered)
                            .padding(.top, 8)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .padding(.top, 200)
                    }
                }
            }
        }
        .onAppear {
            recommendationVM.getDailyMusicInfo()
            recommendationVM.loadHeartbeatList()
        }
    }
}

// MARK: - DailyHeroCard
struct DailyHeroCard: View {
    @Environment(HMPTheme.self) private var theme

    let title: String
    let artist: String
    let albumArtUri: String?
    let musicPath: String?
    let onPlay: () -> Void

    var body: some View {
        Button {
            HapticManager.shared.click()
            onPlay()
        } label: {
            GeometryReader { geo in
                let size = min(geo.size.width, geo.size.height)
                ZStack(alignment: .bottomLeading) {
                    if let image = resolveImage() {
                        Image(uiImage: image)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } else {
                        RoundedRectangle(cornerRadius: 20)
                            .fill(theme.primary.opacity(0.15))
                            .overlay { Image(systemName: "music.note").font(.system(size: 60)).foregroundColor(theme.primary) }
                    }

                    Rectangle()
                        .fill(LinearGradient(colors: [.clear, .black.opacity(0.8)], startPoint: .top, endPoint: .bottom))

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
        .clipShape(RoundedRectangle(cornerRadius: 20))
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
