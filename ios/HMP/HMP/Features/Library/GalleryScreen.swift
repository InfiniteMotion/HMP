import SwiftUI

/// 画廊页 - 对应 Android GalleryScreen.kt
/// 专辑封面网格浏览
struct GalleryScreen: View {
    @Environment(HMPTheme.self) private var theme
    @State private var selectedSegment = 0
    private let segments = ["专辑", "歌手"]

    var body: some View {
        TabScreen(title: "画廊") {
            VStack(spacing: 0) {
                Picker("", selection: $selectedSegment) {
                    ForEach(0..<segments.count, id: \.self) { i in
                        Text(segments[i]).tag(i)
                    }
                }
                .pickerStyle(.segmented)
                .padding(.horizontal, 16)
                .padding(.bottom, 12)

                if selectedSegment == 0 {
                    AlbumGridView()
                } else {
                    ArtistGridView()
                }
            }
        }
    }
}

// MARK: - AlbumGridView
struct AlbumGridView: View {
    @Environment(HMPTheme.self) private var theme

    // 占位数据
    private let placeholderAlbums: [AlbumItem] = []

    var body: some View {
        ScrollView {
            LazyVGrid(
                columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: 3),
                spacing: 12
            ) {
                if placeholderAlbums.isEmpty {
                    Text("暂无专辑")
                        .font(TypographyTokens.bodyMedium)
                        .foregroundColor(theme.secondaryText)
                        .frame(maxWidth: .infinity)
                        .padding(.top, 40)
                }
                // TODO: ForEach 遍历 albums
            }
            .padding(.horizontal, 16)
        }
    }
}

struct ArtistGridView: View {
    @Environment(HMPTheme.self) private var theme

    var body: some View {
        ScrollView {
            LazyVGrid(
                columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: 3),
                spacing: 12
            ) {
                Text("暂无歌手")
                    .font(TypographyTokens.bodyMedium)
                    .foregroundColor(theme.secondaryText)
                    .frame(maxWidth: .infinity)
                    .padding(.top, 40)
            }
            .padding(.horizontal, 16)
        }
    }
}

struct AlbumItem: Identifiable {
    let id = UUID()
    let name: String
    let coverUri: String?
    let songCount: Int
}
