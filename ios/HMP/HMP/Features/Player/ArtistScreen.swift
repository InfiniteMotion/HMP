import SwiftUI
import shared

/// 歌手详情页 — 对应 Android ArtistScreen.kt
struct ArtistScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(\.dismiss) private var dismiss

    let artistName: String
    @State private var viewModel = PlaylistViewModel()

    private var controller: MusicPlayerController { MusicPlayerController.shared }

    var body: some View {
        SubScreen(title: artistName) {
            switch viewModel.selectedArtistMusicState {
            case .idle, .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .empty:
                Text("暂无歌曲")
                    .foregroundColor(theme.text.opacity(0.4))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .success(let musicList):
                VStack(spacing: 0) {
                    playButtons(musicList)
                    musicListView(musicList)
                }
            case .error(let msg):
                Text(msg)
                    .foregroundColor(theme.text.opacity(0.4))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .onAppear { viewModel.loadArtistMusicList(artistName: artistName) }
    }

    private func playButtons(_ list: [MusicInfo_]) -> some View {
        HStack(spacing: 12) {
            Button {
                HapticManager.shared.click()
                controller.addAllToPlaylistByShuffle(list)
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "shuffle").font(.system(size: 14))
                    Text("随机播放").font(TypographyTokens.bodySmall)
                }
                .foregroundColor(theme.primary)
                .frame(maxWidth: .infinity).padding(.vertical, 10)
                .background(theme.primary.opacity(0.1), in: RoundedRectangle(cornerRadius: 10))
            }
            Button {
                HapticManager.shared.click()
                controller.addAllToPlaylistInOrder(list)
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "play.fill").font(.system(size: 14))
                    Text("顺序播放").font(TypographyTokens.bodySmall)
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity).padding(.vertical, 10)
                .background(theme.primary, in: RoundedRectangle(cornerRadius: 10))
            }
        }
        .padding(.horizontal, 16).padding(.vertical, 8)
    }

    private func musicListView(_ list: [MusicInfo_]) -> some View {
        let currentId = controller.currentPlayingMusic?.music.id
        let playingIndex = currentId.flatMap { id in list.firstIndex { $0.music.id == id } }
        let cb = MusicListCallbacks()
        cb.onItemClick = { info, _ in
            HapticManager.shared.click()
            controller.addAllToPlaylistInOrder(list)
            controller.playWith(info)
        }
        cb.onMenuClick = { _ in HapticManager.shared.click() }
        return FixedMusicList(
            musicInfoList: list,
            config: MusicListConfig(
                header: .none,
                item: ItemConfig.full(showRemove: false, showMenu: true),
                currentPlayingIndex: playingIndex,
                callbacks: cb
            )
        )
    }
}
