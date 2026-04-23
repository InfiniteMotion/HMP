import SwiftUI

/// 列表页 - 对应 Android ListScreen.kt
/// 按标签/文件夹/隐藏文件夹分类浏览
struct ListScreen: View {
    @Environment(HMPTheme.self) private var theme
    @Environment(HMPTheme.self) private var theme2

    @State private var selectedSegment: Int = 0
    private let segments = ["歌曲", "歌手", "专辑", "文件夹", "标签"]

    var body: some View {
        TabScreen(title: "列表") {
            VStack(spacing: 0) {
                // 分段选择器
                Picker("", selection: $selectedSegment) {
                    ForEach(0..<segments.count, id: \.self) { i in
                        Text(segments[i]).tag(i)
                    }
                }
                .pickerStyle(.segmented)
                .padding(.horizontal, 16)
                .padding(.bottom, 12)

                // 内容区域
                switch selectedSegment {
                case 0: SongListView()
                case 1: ArtistListView()
                case 2: AlbumListView()
                case 3: FolderListView()
                case 4: LabelListView()
                default: EmptyView()
                }
            }
        }
    }
}

// MARK: - 占位子视图

struct SongListView: View {
    @Environment(HMPTheme.self) private var theme
    var body: some View {
        Text("歌曲列表 (待实现)")
            .font(TypographyTokens.bodyLarge)
            .foregroundColor(theme.secondaryText)
            .padding(.top, 40)
    }
}

struct ArtistListView: View {
    @Environment(HMPTheme.self) private var theme
    var body: some View {
        Text("歌手列表 (待实现)")
            .font(TypographyTokens.bodyLarge)
            .foregroundColor(theme.secondaryText)
            .padding(.top, 40)
    }
}

struct AlbumListView: View {
    @Environment(HMPTheme.self) private var theme
    var body: some View {
        Text("专辑列表 (待实现)")
            .font(TypographyTokens.bodyLarge)
            .foregroundColor(theme.secondaryText)
            .padding(.top, 40)
    }
}

struct FolderListView: View {
    @Environment(HMPTheme.self) private var theme
    var body: some View {
        Text("文件夹列表 (待实现)")
            .font(TypographyTokens.bodyLarge)
            .foregroundColor(theme.secondaryText)
            .padding(.top, 40)
    }
}

struct LabelListView: View {
    @Environment(HMPTheme.self) private var theme
    var body: some View {
        Text("标签列表 (待实现)")
            .font(TypographyTokens.bodyLarge)
            .foregroundColor(theme.secondaryText)
            .padding(.top, 40)
    }
}
