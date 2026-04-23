import SwiftUI

/// 播放列表管理页 - 对应 Android PlaylistManageScreen.kt
/// 管理用户歌单：排序、隐藏文件夹、批量操作
struct PlaylistManageScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(HMPTheme.self) private var theme

    @State private var isEditing = false
    @State private var playlists: [PlaylistItem] = []  // 占位

    var body: some View {
        NavigationStack {
            List {
                // 隐藏文件夹 Section
                Section("隐藏文件夹") {
                    Text("无隐藏文件夹")
                        .font(TypographyTokens.bodyMedium)
                        .foregroundColor(theme.secondaryText)
                }

                // 歌单列表
                Section("我的歌单 (\(playlists.count))") {
                    ForEach(playlists) { playlist in
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(playlist.name)
                                    .font(TypographyTokens.titleMedium)
                                Text("\(playlist.songCount) 首")
                                    .font(TypographyTokens.bodySmall)
                                    .foregroundColor(theme.secondaryText)
                            }

                            Spacer()

                            if playlist.isPinned {
                                Image(systemName: "pin.fill")
                                    .font(.system(size: 12))
                                    .foregroundColor(theme.primary)
                            }
                        }
                        .swipeActions(edge: .trailing) {
                            Button(role: .destructive) {
                                // delete playlist
                            } label: {
                                Label("删除", systemImage: "trash")
                            }
                        }
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("歌单管理")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("关闭") { dismiss() }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button(isEditing ? "完成" : "编辑") {
                        HapticManager.shared.click()
                        isEditing.toggle()
                    }
                }
            }
        }
    }
}
